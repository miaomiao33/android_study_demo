//
// Created by Machenike on 2026/10/7.
//

#include "DZJNICall5.h"

DZJNICall5::DZJNICall5(JavaVM *javaVm, JNIEnv *jniEnv, jobject jLiveObj) {
    this->javaVm = javaVm;
    this->jniEnv = jniEnv;
    //new一个全局的object，因为外面的 instance 会随着运行被释放
    this->jLiveObj = jniEnv->NewGlobalRef(jLiveObj);

    jclass jPlayerClass = jniEnv->GetObjectClass(this->jLiveObj);
    jConnectErrorMid = jniEnv->GetMethodID(jPlayerClass,"onConnectError", "(ILjava/lang/String;)V");

    jConnectSuccessMid = jniEnv->GetMethodID(jPlayerClass,"onConnectSuccess", "()V");
}

DZJNICall5::~DZJNICall5() {
    jniEnv->DeleteGlobalRef(jLiveObj);
}


void DZJNICall5::callConnectError(ThreadMode threadMode, int code, char *msg) {
    //子线程用不了主线程 jniEnv（native 线程）
    //子线程是不共享 jniEnv，它们有自己所独立的
    if(threadMode == THREAD_MAIN)
    {
        jstring jMsg = jniEnv->NewStringUTF(msg);
        jniEnv->CallVoidMethod(jLiveObj,jConnectErrorMid,code,jMsg);
        jniEnv->DeleteGlobalRef(jMsg);
    } else if(threadMode == THREAD_CHILD){
        //获取当前线程的 JNIEnv，通过 JavaVM 的 AttachCurrentThread 创建自己的 JNIEnv
        JNIEnv *env = nullptr;
        int ret = javaVm->GetEnv((void **)&jniEnv,JNI_VERSION_1_6);
        bool  needDetach = false;
        if(ret == JNI_EDETACHED)
        {
            //线程未挂载，attach
            if(javaVm->AttachCurrentThread(&env,0) != JNI_OK)
            {
                LOGE("get child thread jniEnv error!");
                return;
            }
            needDetach = true;
        } else if(ret != JNI_OK)
        {
            LOGE("GetEnv error, ret = %d", ret);
            return;
        }

        jstring jMsg = env->NewStringUTF(msg);
        env->CallVoidMethod(jLiveObj,jConnectErrorMid,code,jMsg);
        env->DeleteGlobalRef(jMsg);

        if(needDetach)
        {
            javaVm->DetachCurrentThread();
        }
    }

}

/***
 * 回调到Java层，告诉准备好了
 * @param threadMode
 */

void DZJNICall5::callConnectSuccess(ThreadMode threadMode) {
//子线程用不了主线程 jniEnv（native 线程）
    //子线程是不共享 jniEnv，它们有自己所独立的
    if(threadMode == THREAD_MAIN)
    {
        jniEnv->CallVoidMethod(jLiveObj,jConnectSuccessMid);
    } else if(threadMode == THREAD_CHILD){
        //获取当前线程的 JNIEnv，通过 JavaVM 的 AttachCurrentThread 创建自己的 JNIEnv
        JNIEnv *env = nullptr;
        int ret = javaVm->GetEnv((void **)&jniEnv,JNI_VERSION_1_6);
        bool  needDetach = false;
        if(ret == JNI_EDETACHED)
        {
            //线程未挂载，attach
            if(javaVm->AttachCurrentThread(&env,0) != JNI_OK)
            {
                LOGE("get child thread jniEnv error!");
                return;
            }
            needDetach = true;
        } else if(ret != JNI_OK)
        {
            LOGE("GetEnv error, ret = %d", ret);
            return;
        }

        env->CallVoidMethod(jLiveObj,jConnectSuccessMid);

        if(needDetach)
        {
            javaVm->DetachCurrentThread();
        }
    }

}
