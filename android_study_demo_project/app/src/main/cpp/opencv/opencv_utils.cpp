//
// Created by Machenike on 2026/6/4.
//
// This file is part of OpenCV project.
// It is subject to the license terms in the LICENSE file found in the top-level directory
// of this distribution and at http://opencv.org/license.html

#include "opencv2/core.hpp"
#include "opencv2/imgproc.hpp"

#ifdef __ANDROID__

#include <android/bitmap.h>

#include "../headerFile/LogUtils.h"

using namespace cv;

extern "C" {

/*
 * 实现 Native 端 Bitmap 转 Mat
 * Method:    void nBitmapToMat2(Bitmap b, long m_addr, boolean unPremultiplyAlpha)
 */
//声明方法
JNIEXPORT void JNICALL Java_org_opencv_android_Utils_nBitmapToMat2
        (JNIEnv *env, jclass, jobject bitmap, jlong m_addr, jboolean needUnPremultiplyAlpha);
//实现方法
JNIEXPORT void JNICALL Java_org_opencv_android_Utils_nBitmapToMat2
        (JNIEnv *env, jclass, jobject bitmap, jlong m_addr, jboolean needUnPremultiplyAlpha) {
    AndroidBitmapInfo info;
    //Bitmap原生像素缓存地址
    void *pixelsAddress = 0;//void* 可以指向任意类型的数据
    Mat &dst = *((Mat *) m_addr);//Java层通过getNativeObjAddr()传来的Mat对象地址，Mat &dst表示该地址的Mat对象引用

    try {
        LOGD("nBitmapToMat");
        //CV_Assert判断后面的条件是否成立，不成立就抛出异常
        //AndroidBitmap_getInfo()获取Bitmap信息，宽、高、格式等
        CV_Assert(AndroidBitmap_getInfo(env, bitmap, &info) >= 0);
        CV_Assert(info.format == ANDROID_BITMAP_FORMAT_RGBA_8888 ||
                  info.format == ANDROID_BITMAP_FORMAT_RGB_565);
        //AndroidBitmap_lockPixels()锁定Bitmap原生像素缓存并获取Bitmap原生像素缓存地址，lock期间Bitmap原生像素缓存不会被改变
        //pixels 返回Bitmap原生像素缓存地址
        CV_Assert(AndroidBitmap_lockPixels(env, bitmap, &pixelsAddress) >= 0);
        CV_Assert(pixelsAddress);
        //创建Mat对象
        dst.create(info.height, info.width, CV_8UC4);
        if (info.format == ANDROID_BITMAP_FORMAT_RGBA_8888) {
            //当前是RGBA_8888格式
            LOGD("nBitmapToMat: RGBA_8888 -> CV_8UC4");
            //根据地址创建Mat
            Mat tmp(info.height, info.width, CV_8UC4, pixelsAddress);
            //是否需要预乘alpha
            if (needUnPremultiplyAlpha)
                cvtColor(tmp, dst, COLOR_mRGBA2RGBA);
            else
                tmp.copyTo(dst);//tmp复制到dst中
        } else {
            // info.format == ANDROID_BITMAP_FORMAT_RGB_565
            LOGD("nBitmapToMat: RGB_565 -> CV_8UC4");
            Mat tmp(info.height, info.width, CV_8UC2, pixelsAddress);
            cvtColor(tmp, dst, COLOR_BGR5652RGBA);
        }
        //与AndroidBitmap_lockPixels配对使用，锁定之后调用AndroidBitmap_unlockPixels解除锁定
        AndroidBitmap_unlockPixels(env, bitmap);
        return;
    } catch (const cv::Exception &e) {
        AndroidBitmap_unlockPixels(env, bitmap);
        LOGE("nBitmapToMat caught cv::Exception: %s", e.what());
        jclass je = env->FindClass("org/opencv/core/CvException");
        if (!je) je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, e.what());
        return;
    } catch (...) {
        //放...，表示不限型态种类 的任何例外
        AndroidBitmap_unlockPixels(env, bitmap);
        LOGE("nBitmapToMat caught unknown exception (...)");
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, "Unknown exception in JNI code {nBitmapToMat}");
        return;
    }
}

// old signature is left for binary compatibility with 2.4.0 & 2.4.1, to removed in 2.5
JNIEXPORT void JNICALL Java_org_opencv_android_Utils_nBitmapToMat
        (JNIEnv *env, jclass, jobject bitmap, jlong m_addr);

JNIEXPORT void JNICALL Java_org_opencv_android_Utils_nBitmapToMat
        (JNIEnv *env, jclass, jobject bitmap, jlong m_addr) {
    //旧的Bitmap转Mat调用新实现的方法
    Java_org_opencv_android_Utils_nBitmapToMat2(env, 0, bitmap, m_addr, false);
}

/*
 * 实现 Mat 转 Bitmap
 * Method:    void nMatToBitmap2(long m_addr, Bitmap b, boolean premultiplyAlpha)
 */

JNIEXPORT void JNICALL Java_org_opencv_android_Utils_nMatToBitmap2
        (JNIEnv *env, jclass, jlong m_addr, jobject bitmap, jboolean needPremultiplyAlpha);

JNIEXPORT void JNICALL Java_org_opencv_android_Utils_nMatToBitmap2
        (JNIEnv *env, jclass, jlong m_addr, jobject bitmap, jboolean needPremultiplyAlpha) {
    AndroidBitmapInfo info;
    void *pixelsAddress = 0;
    Mat &src = *((Mat *) m_addr);

    try {
        LOGD("nMatToBitmap");
        CV_Assert(AndroidBitmap_getInfo(env, bitmap, &info) >= 0);
        CV_Assert(info.format == ANDROID_BITMAP_FORMAT_RGBA_8888 ||
                  info.format == ANDROID_BITMAP_FORMAT_RGB_565);
        CV_Assert(src.dims == 2 && info.height == (uint32_t) src.rows &&
                  info.width == (uint32_t) src.cols);
        CV_Assert(src.type() == CV_8UC1 || src.type() == CV_8UC3 || src.type() == CV_8UC4);
        CV_Assert(AndroidBitmap_lockPixels(env, bitmap, &pixelsAddress) >= 0);
        CV_Assert(pixelsAddress);
        if (info.format == ANDROID_BITMAP_FORMAT_RGBA_8888) {
            Mat tmp(info.height, info.width, CV_8UC4, pixelsAddress);
            if (src.type() == CV_8UC1) {
                LOGD("nMatToBitmap: CV_8UC1 -> RGBA_8888");
                cvtColor(src, tmp, COLOR_GRAY2RGBA);
            } else if (src.type() == CV_8UC3) {
                LOGD("nMatToBitmap: CV_8UC3 -> RGBA_8888");
                cvtColor(src, tmp, COLOR_RGB2RGBA);
            } else if (src.type() == CV_8UC4) {
                LOGD("nMatToBitmap: CV_8UC4 -> RGBA_8888");
                if (needPremultiplyAlpha) cvtColor(src, tmp, COLOR_RGBA2mRGBA);
                else src.copyTo(tmp);
            }
        } else {
            // info.format == ANDROID_BITMAP_FORMAT_RGB_565
            Mat tmp(info.height, info.width, CV_8UC2, pixelsAddress);
            if (src.type() == CV_8UC1) {
                LOGD("nMatToBitmap: CV_8UC1 -> RGB_565");
                cvtColor(src, tmp, COLOR_GRAY2BGR565);
            } else if (src.type() == CV_8UC3) {
                LOGD("nMatToBitmap: CV_8UC3 -> RGB_565");
                cvtColor(src, tmp, COLOR_RGB2BGR565);
            } else if (src.type() == CV_8UC4) {
                LOGD("nMatToBitmap: CV_8UC4 -> RGB_565");
                cvtColor(src, tmp, COLOR_RGBA2BGR565);
            }
        }
        AndroidBitmap_unlockPixels(env, bitmap);
        return;
    } catch (const cv::Exception &e) {
        AndroidBitmap_unlockPixels(env, bitmap);
        LOGE("nMatToBitmap caught cv::Exception: %s", e.what());
        jclass je = env->FindClass("org/opencv/core/CvException");
        if (!je) je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, e.what());
        return;
    } catch (...) {
        AndroidBitmap_unlockPixels(env, bitmap);
        LOGE("nMatToBitmap caught unknown exception (...)");
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, "Unknown exception in JNI code {nMatToBitmap}");
        return;
    }
}

// old signature is left for binary compatibility with 2.4.0 & 2.4.1, to removed in 2.5
JNIEXPORT void JNICALL Java_org_opencv_android_Utils_nMatToBitmap
        (JNIEnv *env, jclass, jlong m_addr, jobject bitmap);

JNIEXPORT void JNICALL Java_org_opencv_android_Utils_nMatToBitmap
        (JNIEnv *env, jclass, jlong m_addr, jobject bitmap) {
    Java_org_opencv_android_Utils_nMatToBitmap2(env, 0, m_addr, bitmap, false);
}

} // extern "C"

#endif //__ANDROID__