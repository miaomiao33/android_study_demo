//
// Created by Machenike on 2026/10/3.
//

#include "video_player_common4.h"
#include "DZJNICall4.h"
#include "DZFFmpeg4.h"
#include <android/native_window.h>
#include <android/native_window_jni.h>
#include <jni.h>

extern "C"{
#include "libavformat/avformat.h"
#include "../../../headerFile/LogUtils.h"
//    重采样的包
#include "libswresample/swresample.h"
#include "libswscale/swscale.h"
#include "libavutil/imgutils.h"
};


DZJNICall4 *pJniCall4;
DZFFmpeg4 *pFFmpeg4;

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
//音频播放
extern "C"
JNIEXPORT void JNICALL
Java_com_example_android_1study_1demo_1project_opencv_ffmpegUsage_MusicAndVideoPlayer_DarrenPlayer4_nPlay4(
        JNIEnv *env, jobject thiz) {
    if(pFFmpeg4 != NULL)
    {
        pFFmpeg4->play();
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_android_1study_1demo_1project_opencv_ffmpegUsage_MusicAndVideoPlayer_DarrenPlayer4_nPrepare4(
        JNIEnv *env, jobject instance, jstring url_) {
    const char *url = (*env).GetStringUTFChars(url_,JNI_FALSE);
    if(pFFmpeg4 == NULL)
    {
        pJniCall4 = new DZJNICall4(pJavaVM, env, instance);
        pFFmpeg4 = new DZFFmpeg4(pJniCall4, url);
        pFFmpeg4->prepare();
    }
    env->ReleaseStringUTFChars(url_,url);

    //注意本方法可能在子线程执行完，所以 instance 和 url_ 都有可能在子线程使用前销毁
}

//音频准备
extern "C"
JNIEXPORT void JNICALL
Java_com_example_android_1study_1demo_1project_opencv_ffmpegUsage_MusicAndVideoPlayer_DarrenPlayer4_nPrepareAsync4(
        JNIEnv *env, jobject instance, jstring url_) {
    const char *url = (*env).GetStringUTFChars(url_,JNI_FALSE);
    if(pFFmpeg4 == NULL)
    {
        pJniCall4 = new DZJNICall4(pJavaVM, env, instance);
        pFFmpeg4 = new DZFFmpeg4(pJniCall4, url);
        pFFmpeg4->prepareAsync();
    }
    env->ReleaseStringUTFChars(url_,url);
}


extern "C"
JNIEXPORT void JNICALL
Java_com_example_android_1study_1demo_1project_opencv_ffmpegUsage_MusicAndVideoPlayer_DarrenPlayer4_setSurface(
        JNIEnv *env, jobject thiz, jobject surface) {
    //当前函数是在准备之后播放之前调用的
    if(pFFmpeg4 != NULL){
        pFFmpeg4->setSurface(surface);
    }
}