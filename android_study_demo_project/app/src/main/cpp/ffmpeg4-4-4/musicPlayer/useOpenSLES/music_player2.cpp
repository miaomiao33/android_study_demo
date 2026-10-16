//
// Created by Machenike on 2026/10/3.
//

#include "music_player2.h"

#include "DZJNICall2.h"
#include "DZFFmpeg2.h"

DZJNICall2 *pJniCall2;
DZFFmpeg2 *pFFmpeg2;

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
//    if(javaVm->GetEnv((void **)(&env), JNI_VERSION_1_4) != JNI_OK)
//    {
//        return -1;
//    }
//
//    return JNI_VERSION_1_4;
//}

//静态注册
extern "C"
JNIEXPORT void JNICALL
Java_com_example_android_1study_1demo_1project_opencv_ffmpegUsage_MusicPlayer_DarrenPlayer_nPlayOpenSLES(
        JNIEnv *env, jobject instance, jstring url_) {
    if(pFFmpeg2 != NULL)
    {
        pFFmpeg2->play();
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_android_1study_1demo_1project_opencv_ffmpegUsage_MusicPlayer_DarrenPlayer_nPrepare(
        JNIEnv *env, jobject instance, jstring url_) {
    const char *url = (*env).GetStringUTFChars(url_,JNI_FALSE);
    if(pFFmpeg2 == NULL)
    {
        pJniCall2 = new DZJNICall2(pJavaVM, env, instance);
        pFFmpeg2 = new DZFFmpeg2(pJniCall2, url);
        pFFmpeg2->prepare();
    }
    env->ReleaseStringUTFChars(url_,url);

    //注意本方法可能在子线程执行完，所以 instance 和 url_ 都有可能在子线程使用前销毁
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_android_1study_1demo_1project_opencv_ffmpegUsage_MusicPlayer_DarrenPlayer_nPrepareAsync(
        JNIEnv *env, jobject instance, jstring url_) {
    const char *url = (*env).GetStringUTFChars(url_,JNI_FALSE);
    if(pFFmpeg2 == NULL)
    {
        pJniCall2 = new DZJNICall2(pJavaVM, env, instance);
        pFFmpeg2 = new DZFFmpeg2(pJniCall2, url);
        pFFmpeg2->prepareAsync();
    }
    env->ReleaseStringUTFChars(url_,url);
}