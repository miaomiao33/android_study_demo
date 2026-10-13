//
// Created by Machenike on 2026/10/3.
//

#include "music_player2.h"

#include "DZJNICall2.h"
#include "DZFFmpeg2.h"

DZJNICall2 *pJniCall;
DZFFmpeg2 *pFFmpeg;

JavaVM *pJavaVM = NULL;

//重写 so 被加载时会调用的一个方法
//作业：了解一下什么叫动态注册
extern "C"
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *javaVm, void *reserved)
{
    LOGE("JNI_OnLoad -->")
    pJavaVM = javaVm;
    JNIEnv *env;
    if(javaVm->GetEnv((void **)(&env), JNI_VERSION_1_4) != JNI_OK)
    {
        return -1;
    }

    return JNI_VERSION_1_4;
}

//静态注册
extern "C"
JNIEXPORT void JNICALL
Java_com_example_android_1study_1demo_1project_opencv_ffmpegUsage_MusicPlayer_DarrenPlayer_nPlayOpenSLES(
        JNIEnv *env, jobject instance, jstring url_) {
    if(pFFmpeg != NULL)
    {
        pFFmpeg->play();
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_android_1study_1demo_1project_opencv_ffmpegUsage_MusicPlayer_DarrenPlayer_nPrepare(
        JNIEnv *env, jobject instance, jstring url_) {
    const char *url = (*env).GetStringUTFChars(url_,JNI_FALSE);
    if(pFFmpeg == NULL)
    {
        pJniCall = new DZJNICall2(pJavaVM, env, instance);
        pFFmpeg = new DZFFmpeg2(pJniCall, url);
        pFFmpeg->prepare();
    }
    env->ReleaseStringUTFChars(url_,url);

    //注意本方法可能在子线程执行完，所以 instance 和 url_ 都有可能在子线程使用前销毁
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_android_1study_1demo_1project_opencv_ffmpegUsage_MusicPlayer_DarrenPlayer_nPrepareAsync(
        JNIEnv *env, jobject instance, jstring url_) {
    const char *url = (*env).GetStringUTFChars(url_,JNI_FALSE);
    if(pFFmpeg == NULL)
    {
        pJniCall = new DZJNICall2(pJavaVM, env, instance);
        pFFmpeg = new DZFFmpeg2(pJniCall, url);
        pFFmpeg->prepareAsync();
    }
    env->ReleaseStringUTFChars(url_,url);
}