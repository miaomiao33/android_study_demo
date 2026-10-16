//
// Created by Machenike on 2026/10/3.
//

#include "music_player3.h"

#include "DZJNICall3.h"
#include "DZFFmpeg3.h"

DZJNICall3 *pJniCall3;
DZFFmpeg3 *pFFmpeg3;

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
Java_com_example_android_1study_1demo_1project_opencv_ffmpegUsage_MusicPlayer_DarrenPlayer_nPlayOpenSLESWithQueue(
        JNIEnv *env, jobject instance, jstring url_) {
    if(pFFmpeg3 != NULL)
    {
        pFFmpeg3->play();
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_android_1study_1demo_1project_opencv_ffmpegUsage_MusicPlayer_DarrenPlayer_nPrepareWithQueue(
        JNIEnv *env, jobject instance, jstring url_) {
    const char *url = (*env).GetStringUTFChars(url_,JNI_FALSE);
    if(pFFmpeg3 == NULL)
    {
        pJniCall3 = new DZJNICall3(pJavaVM, env, instance);
        pFFmpeg3 = new DZFFmpeg3(pJniCall3, url);
        pFFmpeg3->prepare();
    }
    env->ReleaseStringUTFChars(url_,url);

    //注意本方法可能在子线程执行完，所以 instance 和 url_ 都有可能在子线程使用前销毁
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_android_1study_1demo_1project_opencv_ffmpegUsage_MusicPlayer_DarrenPlayer_nPrepareAsyncWithQueue(
        JNIEnv *env, jobject instance, jstring url_) {
    const char *url = (*env).GetStringUTFChars(url_,JNI_FALSE);
    if(pFFmpeg3 == NULL)
    {
        pJniCall3 = new DZJNICall3(pJavaVM, env, instance);
        pFFmpeg3 = new DZFFmpeg3(pJniCall3, url);
        pFFmpeg3->prepareAsync();
    }
    env->ReleaseStringUTFChars(url_,url);
}