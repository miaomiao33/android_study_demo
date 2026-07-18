#include <jni.h>
#include <string>
#include <x264.h>
#include "../headerFile/LogUtils.h"

//
// Created by Machenike on 2026/7/17.
//
using namespace std;
extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_android_1study_1demo_1project_opencv_buildShell_BuildShellMainActivity_getString(
        JNIEnv *env, jobject thiz) {
    string hello = "Hello from C++";

    x264_param_t param;
    x264_param_default(&param);
    LOGI("当前x264库安装成功");

    return env->NewStringUTF(hello.c_str());
}