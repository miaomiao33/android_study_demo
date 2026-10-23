//
// Created by Machenike on 2026/10/3.
//

#include "music_player_for_record_video.h"

#include "DZJNICall6.h"
#include "DZFFmpeg6.h"

DZJNICall6 *pJniCall6;
DZFFmpeg6 *pFFmpeg6;

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
Java_com_example_android_1study_1demo_1project_opencv_ffmpegUsage_livePush_CameraView_RecordVideoDarrenPlayer_nPlayOpenSLESWithQueueRecordVideo(
        JNIEnv *env, jobject instance, jstring url_) {
    if(pFFmpeg6 != NULL)
    {
        pFFmpeg6->play();
    }
}
//
//extern "C"
//JNIEXPORT void JNICALL
//Java_com_example_android_1study_1demo_1project_opencv_ffmpegUsage_livePush_CameraView_DarrenPlayer_nPrepareWithQueue(
//        JNIEnv *env, jobject instance, jstring url_) {
//    const char *url = (*env).GetStringUTFChars(url_,JNI_FALSE);
//    if(pFFmpeg3 == NULL)
//    {
//        pJniCall3 = new DZJNICall3(pJavaVM, env, instance);
//        pFFmpeg3 = new DZFFmpeg3(pJniCall3, url);
//        pFFmpeg3->prepare();
//    }
//    env->ReleaseStringUTFChars(url_,url);
//
//    //注意本方法可能在子线程执行完，所以 instance 和 url_ 都有可能在子线程使用前销毁
//}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_android_1study_1demo_1project_opencv_ffmpegUsage_livePush_CameraView_RecordVideoDarrenPlayer_nPrepareAsyncWithQueueRecordVideo(
        JNIEnv *env, jobject instance, jstring url_) {
    const char *url = (*env).GetStringUTFChars(url_,JNI_FALSE);
    if(pFFmpeg6 == NULL)
    {
        pJniCall6 = new DZJNICall6(pJavaVM, env, instance);
        pFFmpeg6 = new DZFFmpeg6(pJniCall6, url);
        pFFmpeg6->prepareAsync();
    }
    env->ReleaseStringUTFChars(url_,url);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_android_1study_1demo_1project_opencv_ffmpegUsage_livePush_CameraView_RecordVideoDarrenPlayer_nStop(
        JNIEnv *env, jobject thiz) {
    if(pFFmpeg6 != NULL){
        pFFmpeg6->stop();
        delete pFFmpeg6;
        pFFmpeg6 = NULL;
    }

    if(pJniCall6 != NULL)
    {
        delete pJniCall6;
        pJniCall6 = NULL;
    }
}