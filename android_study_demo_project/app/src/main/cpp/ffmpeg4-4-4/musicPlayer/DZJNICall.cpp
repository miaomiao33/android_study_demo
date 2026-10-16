//
// Created by Machenike on 2026/10/7.
//

#include "DZJNICall.h"
#include "music_player.h"

DZJNICall::DZJNICall(JavaVM *javaVm, JNIEnv *jniEnv,jobject jPlayerObj) {
    this->javaVm = javaVm;
    this->jniEnv = jniEnv;
    //new一个全局的object，因为外面的 instance 会随着运行被释放
    this->jPlayerObj = jniEnv->NewGlobalRef(jPlayerObj);
    initCreateAudioTrack();

    jclass jPlayerClass = jniEnv->FindClass("com/example/android_study_demo_project/opencv/ffmpegUsage/MusicPlayer/DarrenPlayer");
    jPlayerErrorMid = jniEnv->GetMethodID(jPlayerClass,"onError", "(ILjava/lang/String;)V");

}

DZJNICall::~DZJNICall() {
    if(jAudioTrackObj != nullptr)
    {
        jniEnv->DeleteGlobalRef(jAudioTrackObj);
        jAudioTrackObj = nullptr;
    }
    if(jPlayerObj != nullptr)
    {
        jniEnv->DeleteGlobalRef(jPlayerObj);
        jPlayerObj = nullptr;
    }
}

//初始化并创建一个AudioTrack的object
void DZJNICall::initCreateAudioTrack() {
    //public AudioTrack(int streamType, int sampleRateInHz, int channelConfig,
    // int audioFormat, int bufferSizeInBytes, int mode)
    //public static final int STREAM_MUSIC = 3
    int streamType = 3;//正常应该是Java去拿然后传过来
    int sampleRateInHz = AUDIO_SIMPLE_RATE;
    //public static final int CHANNEL_OUT_STEREO = (CHANNEL_OUT_FRONT_LEFT | CHANNEL_OUT_FRONT_RIGHT);
    int channelConfig = (0x4 | 0x8);
    //public static final int ENCODING_PCM_16BIT = 2;
    int audioFormat = 2;//16位，就是2字节
    //public static final int MODE_STREAM = 1;
    int mode = 1;//播放音频总时间只有几秒钟就用0

    jclass jAudioTrackClass = jniEnv->FindClass("android/media/AudioTrack");
    jmethodID jAudioTrackMid = jniEnv->GetMethodID(jAudioTrackClass,"<init>","(IIIIII)V");

    //public static int getMinBufferSize(int sampleRateInHz, int channelConfig, int audioFormat)
    jmethodID getMinBufferSizeMid = jniEnv->GetStaticMethodID(jAudioTrackClass,
                                                           "getMinBufferSize","(III)I");
    int bufferSizeInBytes = jniEnv->CallStaticIntMethod(jAudioTrackClass,getMinBufferSizeMid,
                                                     sampleRateInHz,channelConfig,audioFormat);

//    jAudioTrackObj = jniEnv->NewObject(jAudioTrackClass,jAudioTrackMid,
//                                            streamType, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes, mode);
     /** 当前线程是子线程，故使用 jniEnv 前需要注意 **/
     //NewObject 返回的是 局部引用 LocalRef ，创建它的是 构造函数里主线程的 JNIEnv 。
    //当 DZJNICall 构造函数执行结束，这个函数栈退出后，LocalRef 会失效，jAudioTrackObj 变成野引用。
    // 1. 先拿到局部引用
    jobject localAudioTrack = jniEnv->NewObject(jAudioTrackClass,jAudioTrackMid,
                                                streamType, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes, mode);
    // 2. 转为全局引用保存到成员变量
    jAudioTrackObj = (jobject)jniEnv->NewGlobalRef(localAudioTrack);
    // 3. 删除局部引用
    jniEnv->DeleteLocalRef(localAudioTrack);
    /** 当前线程是子线程，故使用 jniEnv 前需要注意 end **/

    //start AudioTruck play方法
    // void play()
    jmethodID playMid = jniEnv->GetMethodID(jAudioTrackClass,"play","()V");
    jniEnv->CallVoidMethod(jAudioTrackObj,playMid);

    //write method
    jAudioTrackWriteMid = jniEnv->GetMethodID(jAudioTrackClass,"write","([BII)I");
}

void DZJNICall::callAudioTrackWrite(jbyteArray audioData, int offsetInBytes, int sizeInBytes) {
//    //调用java的write方法
//    jniEnv->CallIntMethod(jAudioTrackObj,jAudioTrackWriteMid,audioData,offsetInBytes,
//                          sizeInBytes);
    /** 当前线程是子线程，故使用 jniEnv 前需要注意 **/
    JNIEnv *env = nullptr;
    jint ret = javaVm->GetEnv((void**)&env, JNI_VERSION_1_6);
    if(ret != JNI_OK || env == nullptr)
    {
        LOGE("callAudioTrackWrite GetEnv fail");
        return;
    }
    // 使用当前线程的env调用方法
    env->CallIntMethod(jAudioTrackObj, jAudioTrackWriteMid, audioData, offsetInBytes, sizeInBytes);
    /** 当前线程是子线程，故使用 jniEnv 前需要注意 end **/
}

void DZJNICall::callPlayerError(ThreadMode threadMode,int code, char *msg) {
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
