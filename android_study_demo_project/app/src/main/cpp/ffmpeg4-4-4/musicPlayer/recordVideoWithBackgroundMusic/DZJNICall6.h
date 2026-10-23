//
// Created by Machenike on 2026/10/7.
//

#ifndef ANDROID_STUDY_DEMO_PROJECT_DZJNICALL6_H
#define ANDROID_STUDY_DEMO_PROJECT_DZJNICALL6_H

extern "C"{
#include <jni.h>

#include "music_player_for_record_video.h"
};

enum ThreadMode{
    THREAD_MAIN,    // JNI Java主线程
    THREAD_CHILD  // 子线程
};

class DZJNICall6 {
public:
    JavaVM *javaVm;
    JNIEnv *jniEnv;
    jmethodID jPlayerErrorMid;
    jmethodID jPlayerPreparedMid;
    jmethodID jMusicInfoMid;
    jmethodID jCallbackPcmMid;
    jobject jPlayerObj;
public:
    DZJNICall6(JavaVM *javaVm, JNIEnv *jniEnv, jobject jPlayerObj);
    ~DZJNICall6();

public:

    void callPlayerError6(ThreadMode threadMode,int code, char *msg);

    void callPlayerPrepared6(ThreadMode mode);

    void callMusicInfo(ThreadMode mode,int sampleRate, int channels);

    void callCallbackPcm(ThreadMode mode, void *pcmBuffer, int size);
};


#endif //ANDROID_STUDY_DEMO_PROJECT_DZJNICALL6_H
