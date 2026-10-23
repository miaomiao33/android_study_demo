//
// Created by Machenike on 2026/10/7.
//

#include "DZJNICall6.h"

DZJNICall6::DZJNICall6(JavaVM *javaVm, JNIEnv *jniEnv, jobject jPlayerObj) {
    this->javaVm = javaVm;
    this->jniEnv = jniEnv;
    //new一个全局的object，因为外面的 instance 会随着运行被释放
    this->jPlayerObj = jniEnv->NewGlobalRef(jPlayerObj);

    jclass jPlayerClass = jniEnv->FindClass("com/example/android_study_demo_project/opencv/ffmpegUsage/livePush/CameraView/RecordVideoDarrenPlayer");
    jPlayerErrorMid = jniEnv->GetMethodID(jPlayerClass,"onError", "(ILjava/lang/String;)V");

    jPlayerPreparedMid = jniEnv->GetMethodID(jPlayerClass,"onPrepared", "()V");
    jMusicInfoMid = jniEnv->GetMethodID(jPlayerClass,"musicInfo", "(II)V");
    jCallbackPcmMid = jniEnv->GetMethodID(jPlayerClass,"callbackPcm", "([BI)V");
}

DZJNICall6::~DZJNICall6() {
    jniEnv->DeleteGlobalRef(jPlayerObj);
}


void DZJNICall6::callPlayerError6(ThreadMode threadMode, int code, char *msg) {
    //子线程用不了主线程 jniEnv（native 线程）
    //子线程是不共享 jniEnv，它们有自己所独立的
    if(threadMode == THREAD_MAIN)
    {
        jstring jMsg = jniEnv->NewStringUTF(msg);
        jniEnv->CallVoidMethod(jPlayerObj,jPlayerErrorMid,code,jMsg);
        jniEnv->DeleteGlobalRef(jMsg);
    } else if(threadMode == THREAD_CHILD){
        //获取当前线程的 JNIEnv，通过 JavaVM 的 AttachCurrentThread 创建自己的 JNIEnv
        JNIEnv *env = nullptr;
        int ret = javaVm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6);
        bool needDetach = false;
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
        env->CallVoidMethod(jPlayerObj,jPlayerErrorMid,code,jMsg);
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

void DZJNICall6::callPlayerPrepared6(ThreadMode threadMode) {
//子线程用不了主线程 jniEnv（native 线程）
    //子线程是不共享 jniEnv，它们有自己所独立的
    if(threadMode == THREAD_MAIN)
    {
        jniEnv->CallVoidMethod(jPlayerObj,jPlayerPreparedMid);
    } else if(threadMode == THREAD_CHILD){
        //获取当前线程的 JNIEnv，通过 JavaVM 的 AttachCurrentThread 创建自己的 JNIEnv
        JNIEnv *env = nullptr;
        int ret = javaVm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6);
        bool needDetach = false;
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

        env->CallVoidMethod(jPlayerObj,jPlayerPreparedMid);

        if(needDetach)
        {
            javaVm->DetachCurrentThread();
        }
    }

}

void DZJNICall6::callMusicInfo(ThreadMode threadMode, int sampleRate, int channels) {
//子线程是不共享 jniEnv，它们有自己所独立的
    if(threadMode == THREAD_MAIN)
    {
        jniEnv->CallVoidMethod(jPlayerObj,jMusicInfoMid,sampleRate,channels);
    } else if(threadMode == THREAD_CHILD){
        //获取当前线程的 JNIEnv，通过 JavaVM 的 AttachCurrentThread 创建自己的 JNIEnv
        JNIEnv *env = nullptr;
        int ret = javaVm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6);
        bool needDetach = false;
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

        env->CallVoidMethod(jPlayerObj,jMusicInfoMid,sampleRate,channels);

        if(needDetach)
        {
            javaVm->DetachCurrentThread();
        }
    }
}

void DZJNICall6::callCallbackPcm(ThreadMode threadMode, void *pcmBuffer, int size) {
//子线程是不共享 jniEnv，它们有自己所独立的
    JNIEnv* env = nullptr;
    bool needDetach = false;

    if (threadMode == THREAD_MAIN)
    {
        env = jniEnv; // 主线程直接用传入env
    }
    else
    {
        // 子线程：attach 获取env
        jint ret = javaVm->AttachCurrentThread(&env, nullptr);
        if (ret != JNI_OK || env == nullptr)
        {
            return;
        }
        needDetach = true;
    }

    if (env == nullptr || jPlayerObj == nullptr || jCallbackPcmMid == nullptr)
    {
        if (needDetach)
        {
            javaVm->DetachCurrentThread();
        }
        return;
    }

    // ========== 重点：创建jbyteArray，拷贝PCM数据 ==========
    jbyteArray jPcmArr = env->NewByteArray(size);
    if (jPcmArr == nullptr)
    {
        if (needDetach)
        {
            javaVm->DetachCurrentThread();
        }
        return;
    }
    // 把 pcmBuffer 内存拷贝到 jbyteArray
    env->SetByteArrayRegion(jPcmArr, 0, size, (const jbyte*)pcmBuffer);

    // 调用Java方法：参数是 jbyteArray, int
    env->CallVoidMethod(jPlayerObj, jCallbackPcmMid, jPcmArr, size);

    // 【重要】释放局部引用，防止JNI局部引用表溢出（高频PCM回调必写）
    env->DeleteLocalRef(jPcmArr);

    if (needDetach)
    {
        javaVm->DetachCurrentThread();
    }
}

