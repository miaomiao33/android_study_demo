#include <jni.h>
#include "opencv2/opencv.hpp"
using namespace std;
using namespace cv;

//
// Created by Machenike on 2026/5/28.
//

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