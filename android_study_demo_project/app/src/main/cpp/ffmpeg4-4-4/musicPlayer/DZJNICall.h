//
// Created by Machenike on 2026/10/7.
//

#ifndef ANDROID_STUDY_DEMO_PROJECT_DZJNICALL_H
#define ANDROID_STUDY_DEMO_PROJECT_DZJNICALL_H

extern "C"{
#include <jni.h>
};

enum ThreadMode{
    THREAD_MAIN,    // JNI Java主线程
    THREAD_CHILD  // 子线程
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

    void callPlayerError(ThreadMode threadMode,int code, char *msg);
};


#endif //ANDROID_STUDY_DEMO_PROJECT_DZJNICALL_H
