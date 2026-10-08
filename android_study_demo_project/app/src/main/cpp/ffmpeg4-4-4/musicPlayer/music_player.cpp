//
// Created by Machenike on 2026/10/3.
//

#include "music_player.h"

#include "DZJNICall.h"
#include "DZFFmpeg.h"

DZJNICall *pJniCall;
DZFFmpeg *pFFmpeg;

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
Java_com_example_android_1study_1demo_1project_opencv_ffmpegUsage_MusicPlayer_DarrenPlayer_nPlay(
        JNIEnv *env, jobject instance, jstring url_) {
    pJniCall = new DZJNICall(pJavaVM,env,instance);
    const char *url = (*env).GetStringUTFChars(url_,JNI_FALSE);
    pFFmpeg = new DZFFmpeg(pJniCall,url);
    pFFmpeg->play();

    //有多线程就不要在主线程去删除，会导致子线程资源被释放
//    delete pJniCall;
//    delete pFFmpeg;//虽然delete后会去调用析构函数，但是也不一定会delete
    env->ReleaseStringUTFChars(url_,url);

    //注意本方法可能在子线程执行完，所以 instance 和 url_ 都有可能在子线程使用前销毁
}