//
// Created by Machenike on 2026/5/18.
//

/***
 * Java 传递一个 Bitmap 给 JNI
 */
#include <stdint.h>
#include <jni.h>
#include "headerFile/LogUtils.h"
#include <android/bitmap.h>

/***
 * bitmap.h的一些信息和方法
 */
////包含 Bitmap 常用信息的结构体
///** Bitmap info, see AndroidBitmap_getInfo(). */
//typedef struct {
//    /** The bitmap width in pixels. */
//    uint32_t width;
//    /** The bitmap height in pixels. */
//    uint32_t height;
//    /** The number of byte per row. */
//    uint32_t stride;
//    /** The bitmap pixel format. See {@link AndroidBitmapFormat} */
//    int32_t format;
//    /** Unused. */
//    uint32_t flags;      // 0 for now
//} AndroidBitmapInfo;
//
////Bitmap 的图片格式枚举
///** Bitmap pixel format. */
//enum AndroidBitmapFormat {
//    /** No format. */
//    ANDROID_BITMAP_FORMAT_NONE = 0,
//    /** Red: 8 bits, Green: 8 bits, Blue: 8 bits, Alpha: 8 bits. **/
//    ANDROID_BITMAP_FORMAT_RGBA_8888 = 1,
//    /** Red: 5 bits, Green: 6 bits, Blue: 5 bits. **/
//    ANDROID_BITMAP_FORMAT_RGB_565 = 4,
//    /** Deprecated in API level 13. Because of the poor quality of this configuration, it is advised to use ARGB_8888 instead. **/
//    ANDROID_BITMAP_FORMAT_RGBA_4444 = 7,
//    /** Alpha: 8 bits. */
//    ANDROID_BITMAP_FORMAT_A_8 = 8,
//};
//
////接口返回码
///** AndroidBitmap functions result code. */
//enum {
//    /** Operation was successful. */
//    ANDROID_BITMAP_RESULT_SUCCESS = 0,
//    /** Bad parameter. */
//    ANDROID_BITMAP_RESULT_BAD_PARAMETER = -1,
//    /** JNI exception occured. */
//    ANDROID_BITMAP_RESULT_JNI_EXCEPTION = -2,
//    /** Allocation failed. */
//    ANDROID_BITMAP_RESULT_ALLOCATION_FAILED = -3,
//};

///**给定一个 java 的 bitmap 对象，获取到对应的 AndroidBitmapInfo 结构体*/
//int AndroidBitmap_getInfo(JNIEnv *env, jobject jBitmap,
//                          AndroidBitmapInfo *info);
//
///** 将 addrPtr 指向图片的像素地址。这个方法会锁定图片的所有像素不能改变，直到调用AndroidBitmap_unlockPixels 方法*/
//int AndroidBitmap_lockPixels(JNIEnv *env, jobject jBitmap, void **addrPtr);
//
///**解除对图片像素的锁定*/
//int AndroidBitmap_unlockPixels(JNIEnv *env, jobject jBitmap);

extern "C"
JNIEXPORT void JNICALL
Java_com_example_android_1study_1demo_1project_JNI_JNIJavaCallC_passBitmap(
        JNIEnv *env, jobject jniJavaCallCThis, jobject bitmap) {
    if (bitmap == NULL) {
        LOGE("bitmap is null!");
        return;
    }
    AndroidBitmapInfo info; // create a AndroidBitmapInfo
    int resultCode;//结果编码
// 获取图片信息
    resultCode = AndroidBitmap_getInfo(env, bitmap, &info);
    if (resultCode != ANDROID_BITMAP_RESULT_SUCCESS) {
        LOGE("AndroidBitmap_getInfo failed, result: %d", resultCode);
        return;
    }
    LOGD("bitmap width: %d, height: %d, format: %d, stride: %d", info.width, info.height,
         info.format, info.stride);
// 获取像素信息
    unsigned char *addrPtr;
    resultCode = AndroidBitmap_lockPixels(env, bitmap, reinterpret_cast<void **>(&addrPtr));
    if (resultCode != ANDROID_BITMAP_RESULT_SUCCESS) {
        LOGE("AndroidBitmap_lockPixels failed, result: %d", resultCode);
        return;
    }
// 执行图片操作的逻辑
    int length = info.stride * info.height;
    for (int i = 0; i < length; ++i) {
        LOGD("value: %x", addrPtr[i]);
    }
// 像素信息不再使用后需要解除锁定
    resultCode = AndroidBitmap_unlockPixels(env, bitmap);
    if (resultCode != ANDROID_BITMAP_RESULT_SUCCESS) {
        LOGE("AndroidBitmap_unlockPixels failed, result: %d", resultCode);
    }
}
