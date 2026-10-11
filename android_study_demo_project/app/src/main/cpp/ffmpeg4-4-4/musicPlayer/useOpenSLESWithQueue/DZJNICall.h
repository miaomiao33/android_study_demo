//
// Created by Machenike on 2026/10/7.
//

#ifndef ANDROID_STUDY_DEMO_PROJECT_DZJNICALL_H
#define ANDROID_STUDY_DEMO_PROJECT_DZJNICALL_H

extern "C"{
#include <jni.h>

#include "music_player.h"
};

enum ThreadMode{
    THREAD_MAIN,    // JNI Java主线程
    THREAD_CHILD  // 子线程
};

class DZJNICall {
public:
    JavaVM *javaVm;
    JNIEnv *jniEnv;
    jmethodID jPlayerErrorMid;
    jmethodID jPlayerPreparedMid;
    jobject jPlayerObj;
public:
    DZJNICall(JavaVM *javaVm, JNIEnv *jniEnv,jobject jPlayerObj);
    ~DZJNICall();

public:

    void callPlayerError(ThreadMode threadMode,int code, char *msg);

    void callPlayerPrepared(ThreadMode mode);
};


#endif //ANDROID_STUDY_DEMO_PROJECT_DZJNICALL_H
