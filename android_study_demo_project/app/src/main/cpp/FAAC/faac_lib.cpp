#include <string>
#include <jni.h>
#include "../headerFile/LogUtils.h"
#include <faac.h>
//
// Created by Machenike on 2026/7/21.
//
using namespace std;
extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_android_1study_1demo_1project_opencv_buildShell_BuildShellMainActivity_testFAAC(
        JNIEnv *env, jobject thiz) {
    string hello = "Hello from C++";
    LOGI("FAAC 版本字符串：%d", FAAC_CFG_VERSION);
    char *versionStr = nullptr;
    char *copyrightStr = nullptr;
    int ret = faacEncGetVersion(&versionStr,&copyrightStr);
    LOGI("faacEncGetVersion ret = %d", ret);
    LOGI("FAAC版本号：%s", versionStr);
    LOGI("FAAC版权信息：%s", copyrightStr);

    return env->NewStringUTF(hello.c_str());
}