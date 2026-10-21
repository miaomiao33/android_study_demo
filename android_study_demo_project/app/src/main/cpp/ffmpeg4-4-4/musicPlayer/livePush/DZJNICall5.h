//
// Created by Machenike on 2026/10/7.
//

#ifndef ANDROID_STUDY_DEMO_PROJECT_DZJNICALL5_H
#define ANDROID_STUDY_DEMO_PROJECT_DZJNICALL5_H

extern "C"{
#include <jni.h>

#include "../../../headerFile/LogUtils.h"
};

enum ThreadMode{
    THREAD_MAIN,    // JNI Java主线程
    THREAD_CHILD  // 子线程
};

class DZJNICall5 {
public:
    JavaVM *javaVm;
    JNIEnv *jniEnv;
    jmethodID jConnectErrorMid;
    jmethodID jConnectSuccessMid;
    jobject jLiveObj;
public:
    DZJNICall5(JavaVM *javaVm, JNIEnv *jniEnv, jobject jLiveObj);
    ~DZJNICall5();

public:

    void callConnectError(ThreadMode threadMode,int code, char *msg);

    void callConnectSuccess(ThreadMode mode);
};


#endif //ANDROID_STUDY_DEMO_PROJECT_DZJNICALL5_H
