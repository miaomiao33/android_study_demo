//
// Created by Machenike on 2026/10/7.
//

#ifndef ANDROID_STUDY_DEMO_PROJECT_DZJNICALL2_H
#define ANDROID_STUDY_DEMO_PROJECT_DZJNICALL2_H

extern "C"{
#include <jni.h>

#include "music_player2.h"
};

enum ThreadMode{
    THREAD_MAIN,    // JNI Java主线程
    THREAD_CHILD  // 子线程
};

class DZJNICall2 {
public:
    JavaVM *javaVm;
    JNIEnv *jniEnv;
    jmethodID jPlayerErrorMid;
    jmethodID jPlayerPreparedMid;
    jobject jPlayerObj;
public:
    DZJNICall2(JavaVM *javaVm, JNIEnv *jniEnv, jobject jPlayerObj);
    ~DZJNICall2();

public:

    void callPlayerError(ThreadMode threadMode,int code, char *msg);

    void callPlayerPrepared(ThreadMode mode);
};


#endif //ANDROID_STUDY_DEMO_PROJECT_DZJNICALL2_H
