#include <jni.h>
#include "opencv2/opencv.hpp"
using namespace std;
using namespace cv;

//
// Created by Machenike on 2026/5/28.
//
//定义身份证默认宽高和size
#define DEFAULT_ID_CARD_WIDTH 640
#define DEFAULT_ID_CARD_HEIGHT 480
#define FIX_ID_CARD_SIZE Size(DEFAULT_ID_CARD_WIDTH,DEFAULT_ID_CARD_HEIGHT)
#define FIX_TEMPLATE_SIZE Size(153,28)

/***
 * 外部扩展函数
 */
//bitmap转Mat、Mat转bitmap工具方法
extern "C"
{
extern JNIEXPORT void JNICALL Java_org_opencv_android_Utils_nBitmapToMat2
        (JNIEnv *env, jclass, jobject bitmap, jlong m_addr, jboolean needUnPremultiplyAlpha);
extern JNIEXPORT void JNICALL Java_org_opencv_android_Utils_nMatToBitmap2
        (JNIEnv * env, jclass, jlong m_addr, jobject bitmap, jboolean needPremultiplyAlpha);
}

/***
 * 自定义函数
 */
 //根据srcData创建bitmap
jobject createBitmap(JNIEnv *env,  jclass clazz, Mat srcData, jobject config);


/**
 * 返回opencv的版本
 */
extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_android_1study_1demo_1project_opencv_OpenCVJNIJavaCallC_getOpenCVInfoFromJNI(
        JNIEnv *env, jobject thiz) {
    string opencv_info = CV_VERSION;
    Mat a;
    return env->NewStringUTF(opencv_info.c_str());
}

/***
 * 从身份证全图中获取身份证号码截图
 */
extern "C"
JNIEXPORT jobject JNICALL
Java_com_example_android_1study_1demo_1project_opencv_OpenCVJNIJavaCallC_getIDCardImage(
        JNIEnv *env, jclass OpenCVJNIJavaCallCClazz,
        jobject src, jobject config) {
    
    Mat src_img ;//源图
    Mat dst_img;
    Mat dst;//灰度化、二值化图片
    //将传过来的源bitmap转为Mat
    Java_org_opencv_android_Utils_nBitmapToMat2(env,OpenCVJNIJavaCallCClazz,
                                                src,
                                                (jlong)&src_img,
                                                false);

    /**无损压缩，640*480*/
    resize(src_img, src_img,FIX_ID_CARD_SIZE);

    /**灰度化*/
    cvtColor(src_img, dst, COLOR_BGR2GRAY);

    /**二值化*/
    double thresh = 100; // 阈值
    double maxval = 255; // 最大值
    //超过阈值返回最大值
    threshold(dst, dst, thresh, maxval, THRESH_BINARY);

    /**膨胀和腐蚀*/
    //膨胀，便于后续轮廓检测，像素点存在则放大为（20，10）
    //卷积核
    Mat erodeElement = getStructuringElement(MORPH_RECT, Size(20,10));
    //腐蚀，将白色区域“蚕食”变小，黑色区域扩大
    erode(dst, dst, erodeElement);

    /** 轮廓检测 */
    vector< vector<Point>> contours;//存储图片找到的所有矩形轮廓
    vector<Rect> rects;//Rect ：矩形框，用向量表示，左上点到右下点

    //检测图像中轮廓
    findContours(dst, contours, RETR_TREE, CHAIN_APPROX_SIMPLE, Point(0, 0));

    for(vector<int>::size_type i = 0; i < contours.size(); i++)
    {
        //boundingRect 计算并返回一个点集的最小包围边界矩形
        Rect rect = boundingRect(contours.at(i));
        //在dst膨胀腐蚀后的图上画上矩形
        rectangle(dst, rect, Scalar(0, 0, 255));
        //对符合条件的图片筛选收集,宽高比大于9:1的
        if(rect.width > rect.height * 9)
        {
            rects.push_back(rect);
            //在dst图片上显示 rect 矩形(便于查看)
            rectangle(dst, rect, Scalar(0, 0, 255));
            //提取src_img的rect区域，身份证号区域
            dst_img = src_img(rect);
        }
    }

    //如果只找到一个矩形，那么这个就是目标图片
    if(rects.size() == 1)
    {
        Rect rect = rects.at(0);
        dst_img = src_img(rect);
    }else
    {
        //不止一个矩形
        int lowPoint = 0;
        Rect finalRect;
        Rect tempRect;
        Point p;
        //身份证号在最右下的边框
        //遍历所有轮廓，选择纵坐标最低的
        for(vector<int>::size_type i = 0; i < rects.size(); i++)
        {
            tempRect = rects.at(i);
            p = tempRect.tl();
            if(tempRect.tl().y > lowPoint)
            {
                lowPoint = tempRect.tl().y;
                finalRect = tempRect;
            }
        }
        //在图像上绘制矩形,Scalar是矩形颜色或亮度
        rectangle(dst, finalRect, Scalar(255, 255, 0));
        dst_img = src_img(finalRect);
    }

    jobject bitmap = createBitmap(env, OpenCVJNIJavaCallCClazz,dst_img,config);

    src_img.release();
    dst_img.release();
    dst.release();

    return bitmap;
}

jobject createBitmap(JNIEnv *env,  jclass clazz, Mat srcData, jobject config)
{
    int imgWidth = srcData.cols;
    int imgHeight = srcData.rows;
    int numPix = imgWidth * imgHeight;
    //创建bitmap对象
    jclass bitmapClazz = env->FindClass("android/graphics/Bitmap");
    jmethodID createBitmapID = env->GetStaticMethodID(bitmapClazz,"createBitmap",
                                                      "(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;");
    jobject jBitmapObj = env->CallStaticObjectMethod(bitmapClazz,
                                                     createBitmapID,
                                                     imgWidth,imgHeight,config);
    //Mat转bitmap
    Java_org_opencv_android_Utils_nMatToBitmap2(env,clazz,(jlong)&srcData,
                                                jBitmapObj, false);
    return jBitmapObj;
}

