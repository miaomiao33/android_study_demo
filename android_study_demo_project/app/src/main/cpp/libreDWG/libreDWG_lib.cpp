#include <jni.h>
#include <string>
#include "../headerFile/LogUtils.h"
#include "dwg.h"

//
// Created by Machenike on 2026/7/19.
//
using namespace std;

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_android_1study_1demo_1project_opencv_buildShell_BuildShellMainActivity_testLibreDWG(
        JNIEnv *env, jobject thiz,jstring path_jstr) {
    string hello = "Hello from C++";
    const char* path = env->GetStringUTFChars(path_jstr, nullptr);

    static Dwg_Data g_dwg;
    memset(&g_dwg, 0, sizeof(Dwg_Data));

    int error = dwg_read_file(path, &g_dwg);
    LOGI("dwg 打开文件：%d",error);
    if(error < DWG_ERR_CRITICAL)
    {
        LOGI("dwg 打开文件成功");
    } else{
        LOGI("dwg 打开文件失败");
    }

    // 释放字符串资源，必须写，否则内存泄漏
    env->ReleaseStringUTFChars(path_jstr, path);
    return env->NewStringUTF(hello.c_str());
}