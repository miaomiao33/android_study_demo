//
// Created by Machenike on 2026/10/7.
//

#include "DZJNICall2.h"

DZJNICall2::DZJNICall2(JavaVM *javaVm, JNIEnv *jniEnv, jobject jPlayerObj) {
    this->javaVm = javaVm;
    this->jniEnv = jniEnv;
    //new一个全局的object，因为外面的 instance 会随着运行被释放
    this->jPlayerObj = jniEnv->NewGlobalRef(jPlayerObj);

    jclass jPlayerClass = jniEnv->FindClass("com/example/android_study_demo_project/opencv/ffmpegUsage/MusicPlayer$DarrenPlayer");
    jPlayerErrorMid = jniEnv->GetMethodID(jPlayerClass,"onError", "(ILjava/lang/String;)V");

    jPlayerPreparedMid = jniEnv->GetMethodID(jPlayerClass,"onPrepared", "()V");
}

DZJNICall2::~DZJNICall2() {
    jniEnv->DeleteGlobalRef(jPlayerObj);
}


void DZJNICall2::callPlayerError(ThreadMode threadMode, int code, char *msg) {
    //子线程用不了主线程 jniEnv（native 线程）
    //子线程是不共享 jniEnv，它们有自己所独立的
    if(threadMode == THREAD_MAIN)
    {
        jstring jMsg = jniEnv->NewStringUTF(msg);
        jniEnv->CallVoidMethod(jPlayerObj,jPlayerErrorMid,code,jMsg);
        jniEnv->DeleteGlobalRef(jMsg);
    } else if(threadMode == THREAD_CHILD){
        //获取当前线程的 JNIEnv，通过 JavaVM 的 AttachCurrentThread 创建自己的 JNIEnv
        JNIEnv *env;
        if(javaVm->AttachCurrentThread(&env,0) != JNI_OK)
        {
            LOGE("get child thread jniEnv error!");
            return;
        }

        jstring jMsg = jniEnv->NewStringUTF(msg);
        jniEnv->CallVoidMethod(jPlayerObj,jPlayerErrorMid,code,jMsg);
        jniEnv->DeleteGlobalRef(jMsg);

        javaVm->DetachCurrentThread();
    }

}

/***
 * 回调到Java层，告诉准备好了
 * @param threadMode
 */

void DZJNICall2::callPlayerPrepared(ThreadMode threadMode) {
//子线程用不了主线程 jniEnv（native 线程）
    //子线程是不共享 jniEnv，它们有自己所独立的
    if(threadMode == THREAD_MAIN)
    {
        jniEnv->CallVoidMethod(jPlayerObj,jPlayerPreparedMid);
    } else if(threadMode == THREAD_CHILD){
        //获取当前线程的 JNIEnv，通过 JavaVM 的 AttachCurrentThread 创建自己的 JNIEnv
        JNIEnv *env;
        if(javaVm->AttachCurrentThread(&env,0) != JNI_OK)
        {
            LOGE("get child thread jniEnv error!");
            return;
        }

        env->CallVoidMethod(jPlayerObj,jPlayerPreparedMid);

        javaVm->DetachCurrentThread();
    }

}
