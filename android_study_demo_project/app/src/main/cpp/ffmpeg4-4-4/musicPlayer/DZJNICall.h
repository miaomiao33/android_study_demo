//
// Created by Machenike on 2026/10/7.
//

#ifndef ANDROID_STUDY_DEMO_PROJECT_DZJNICALL_H
#define ANDROID_STUDY_DEMO_PROJECT_DZJNICALL_H

extern "C"{
#include <jni.h>
};

class DZJNICall {
public:
    jobject jAudioTrackObj;
    jmethodID jAudioTrackWriteMid;
    JavaVM *javaVm;
    JNIEnv *jniEnv;
    jmethodID jPlayerErrorMid;
    jobject jPlayerObj;
public:
    DZJNICall(JavaVM *javaVm, JNIEnv *jniEnv,jobject jPlayerObj);
    ~DZJNICall();

private:
    void initCreateAudioTrack();

public:
    void callAudioTrackWrite(jbyteArray audioData, int offsetInBytes, int sizeInBytes);

    void callPlayerError(int code, char *msg);
};


#endif //ANDROID_STUDY_DEMO_PROJECT_DZJNICALL_H
