//
// Created by Machenike on 2026/10/7.
//

#include "DZJNICall2.h"

DZJNICall2::DZJNICall2(JavaVM *javaVm, JNIEnv *jniEnv, jobject jPlayerObj) {
    this->javaVm = javaVm;
    this->jniEnv = jniEnv;
    //new一个全局的object，因为外面的 instance 会随着运行被释放
    this->jPlayerObj = jniEnv->NewGlobalRef(jPlayerObj);

    //当前初始化的调用的地方是主线程，所以不用区别子线程
    jclass jPlayerClass = jniEnv->FindClass("com/example/android_study_demo_project/opencv/ffmpegUsage/MusicPlayer/DarrenPlayer");
    jPlayerErrorMid = jniEnv->GetMethodID(jPlayerClass,"onError", "(ILjava/lang/String;)V");

    jPlayerPreparedMid = jniEnv->GetMethodID(jPlayerClass,"onPrepared", "()V");
}

DZJNICall2::~DZJNICall2() {
    jniEnv->DeleteGlobalRef(jPlayerObj);
}


void DZJNICall2::callPlayerError2(ThreadMode threadMode, int code, char *msg) {
    /** 当前线程是子线程，故使用 jniEnv 前需要注意 **/
    //子线程用不了主线程 jniEnv（native 线程）
    //子线程是不共享 jniEnv，它们有自己所独立的
    if(threadMode == THREAD_MAIN)
    {
        jstring jMsg = jniEnv->NewStringUTF(msg);
        jniEnv->CallVoidMethod(jPlayerObj,jPlayerErrorMid,code,jMsg);
        jniEnv->DeleteLocalRef(jMsg);
    } else if(threadMode == THREAD_CHILD){
        //获取当前线程的 JNIEnv，通过 JavaVM 的 AttachCurrentThread 创建自己的 JNIEnv
        JNIEnv *env = nullptr;
        jint ret = javaVm->GetEnv((void**)&env, JNI_VERSION_1_6);
        bool needDetach = false;

        if(ret == JNI_EDETACHED)
        {
            //线程未挂载，attach
            if(javaVm->AttachCurrentThread(&env,nullptr) != JNI_OK)
            {
                LOGE("AttachCurrentThread failed");
                return;
            }
            needDetach = true;
        }
        else if(ret != JNI_OK)
        {
            LOGE("GetEnv error, ret = %d", ret);
            return;
        }

        // 这里一定是 env，不是成员变量 jniEnv！
        jstring jMsg = env->NewStringUTF(msg);
        env->CallVoidMethod(jPlayerObj,jPlayerErrorMid,code,jMsg);
        env->DeleteLocalRef(jMsg);

        if(needDetach)
        {
            //如果挂载成功
            javaVm->DetachCurrentThread();
        }
    }
    /** 当前线程是子线程，故使用 jniEnv 前需要注意 end **/
}

/***
 * 回调到Java层，告诉准备好了
 * @param threadMode
 */

void DZJNICall2::callPlayerPrepared2(ThreadMode threadMode) {
//子线程用不了主线程 jniEnv（native 线程）
    //子线程是不共享 jniEnv，它们有自己所独立的
    if(threadMode == THREAD_MAIN)
    {
        jniEnv->CallVoidMethod(jPlayerObj,jPlayerPreparedMid);
    } else if(threadMode == THREAD_CHILD){
        //获取当前线程的 JNIEnv，通过 JavaVM 的 AttachCurrentThread 创建自己的 JNIEnv
        JNIEnv *env = nullptr;
        jint ret = javaVm->GetEnv((void**)&env, JNI_VERSION_1_6);
        bool needDetach = false;
        if(ret == JNI_EDETACHED)
        {
            //线程未挂载，attach
            if(javaVm->AttachCurrentThread(&env,nullptr) != JNI_OK)
            {
                LOGE("AttachCurrentThread failed");
                return;
            }
            needDetach = true;

        } else if(ret != JNI_OK)
        {
            LOGE("GetEnv error, ret = %d", ret);
            return;
        }

        env->CallVoidMethod(jPlayerObj,jPlayerPreparedMid);

        if(needDetach)
        {
            //如果挂载成功
            javaVm->DetachCurrentThread();
        }
    }

}
