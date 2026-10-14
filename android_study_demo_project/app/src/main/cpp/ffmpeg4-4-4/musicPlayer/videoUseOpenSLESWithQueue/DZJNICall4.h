//
// Created by Machenike on 2026/10/7.
//

#ifndef ANDROID_STUDY_DEMO_PROJECT_DZJNICALL4_H
#define ANDROID_STUDY_DEMO_PROJECT_DZJNICALL4_H

extern "C"{
#include <jni.h>

#include "../../../headerFile/LogUtils.h"
};

enum ThreadMode{
    THREAD_MAIN,    // JNI Java主线程
    THREAD_CHILD  // 子线程
};

class DZJNICall4 {
public:
    JavaVM *javaVm;
    JNIEnv *jniEnv;
    jmethodID jPlayerErrorMid;
    jmethodID jPlayerPreparedMid;
    jobject jPlayerObj;
public:
    DZJNICall4(JavaVM *javaVm, JNIEnv *jniEnv, jobject jPlayerObj);
    ~DZJNICall4();

public:

    void callPlayerError(ThreadMode threadMode,int code, char *msg);

    void callPlayerPrepared(ThreadMode mode);
};


#endif //ANDROID_STUDY_DEMO_PROJECT_DZJNICALL4_H
