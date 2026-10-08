//
// Created by Machenike on 2026/10/7.
//

#ifndef ANDROID_STUDY_DEMO_PROJECT_DZJNICALL_H
#define ANDROID_STUDY_DEMO_PROJECT_DZJNICALL_H

#include <jni.h>
#include "music_player.h"

class DZJNICall {
public:
    jobject jAudioTrackObj;
    jmethodID jAudioTrackWriteMid;
    JavaVM *javaVm;
    JNIEnv *jniEnv;
public:
    DZJNICall(JavaVM *javaVm, JNIEnv *jniEnv);
    ~DZJNICall();

private:
    void initCreateAudioTrack();

public:
    void callAudioTrackWrite(jbyteArray audioData, int offsetInBytes, int sizeInBytes);
};


#endif //ANDROID_STUDY_DEMO_PROJECT_DZJNICALL_H
