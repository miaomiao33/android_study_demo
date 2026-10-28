//
// Created by Machenike on 2026/10/15.
//

#include <jni.h>
#include "DZJNICall5.h"
#include "DZLivePush.h"

DZLivePush *pLivePush = NULL;
DZJNICall5 *pJniCall5 = NULL;

extern JavaVM *pJavaVM;//使用music_player的pJavaVM
//JavaVM *pJavaVM = NULL;

////重写 so 被加载时会调用的一个方法
////本方法别的地方在使用时，记得注释掉
////作业：了解一下什么叫动态注册
//extern "C"
//JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *javaVm, void *reserved)
//{
//    LOGE("JNI_OnLoad -->")
//    pJavaVM = javaVm;
//    JNIEnv *env;
//    if(javaVm->GetEnv((void **)(&env), JNI_VERSION_1_6) != JNI_OK)
//    {
//        return -1;
//    }
//
//    return JNI_VERSION_1_6;
//}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_android_1study_1demo_1project_opencv_ffmpegUsage_livePush_livePushMain_LivePush_nInitConnect(
        JNIEnv *env, jobject instance,jstring liveUrl_) {
    const char* liveUrl = env->GetStringUTFChars(liveUrl_,JNI_FALSE);

    pJniCall5 = new DZJNICall5(pJavaVM,env,instance);
    pLivePush = new DZLivePush(liveUrl,pJniCall5);
    pLivePush->initConnect();

    env->ReleaseStringUTFChars(liveUrl_,liveUrl);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_android_1study_1demo_1project_opencv_ffmpegUsage_livePush_livePushMain_LivePush_nStop(
        JNIEnv *env, jobject thiz) {
    if(pLivePush != NULL)
    {
        pLivePush->stop();
        delete(pLivePush);
        pLivePush = NULL;
    }

    if(pJniCall5 != NULL)
    {
        delete(pJniCall5);
        pJniCall5 = NULL;
    }
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_android_1study_1demo_1project_opencv_ffmpegUsage_livePush_livePushMain_LivePush_pushSpsPps(
        JNIEnv *env, jobject thiz, jbyteArray sps_data, jint sps_len, jbyteArray pps_data,
        jint pps_len) {
    // TODO: implement pushSPSPps()
    jbyte * spsData = env->GetByteArrayElements(sps_data,JNI_FALSE);
    jbyte * ppsData = env->GetByteArrayElements(pps_data,JNI_FALSE);

    if(pLivePush != NULL)
    {
        pLivePush->pushSpsPps(spsData,sps_len,ppsData,pps_len);
    }

    env->ReleaseByteArrayElements(sps_data,spsData,0);
    env->ReleaseByteArrayElements(pps_data,ppsData,0);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_android_1study_1demo_1project_opencv_ffmpegUsage_livePush_livePushMain_LivePush_pushVideo(
        JNIEnv *env, jobject thiz, jbyteArray video_data, jint data_len, jboolean key_frame) {
    jbyte * videoData = env->GetByteArrayElements(video_data,JNI_FALSE);

    if(pLivePush != NULL)
    {
        pLivePush->pushVideo(videoData,data_len,key_frame);
    }

    env->ReleaseByteArrayElements(video_data,videoData,0);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_android_1study_1demo_1project_opencv_ffmpegUsage_livePush_livePushMain_LivePush_pushAudio(
        JNIEnv *env, jobject thiz, jbyteArray audio_data, jint data_len) {
    jbyte * audioData = env->GetByteArrayElements(audio_data,JNI_FALSE);

    if(pLivePush != NULL)
    {
        pLivePush->pushAudio(audioData,data_len);
    }

    env->ReleaseByteArrayElements(audio_data,audioData,0);
}