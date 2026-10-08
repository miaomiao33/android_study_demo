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

    jclass jPlayerClass = jniEnv->FindClass("com/example/android_study_demo_project/opencv/ffmpegUsage/MusicPlayer$DarrenPlayer");
    jPlayerErrorMid = jniEnv->GetMethodID(jPlayerClass,"onError", "(ILjava/lang/String;)V");

}

DZJNICall::~DZJNICall() {
    jniEnv->DeleteLocalRef(jAudioTrackObj);
    jniEnv->DeleteGlobalRef(jPlayerObj);
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
    jAudioTrackObj = jniEnv->NewObject(jAudioTrackClass,jAudioTrackMid,
                                            streamType, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes, mode);

    //start play方法
    // void play()
    jmethodID playMid = jniEnv->GetMethodID(jAudioTrackClass,"play","()V");
    jniEnv->CallVoidMethod(jAudioTrackObj,playMid);

    //write method
    jAudioTrackWriteMid = jniEnv->GetMethodID(jAudioTrackClass,"write","([BII)I");
}

void DZJNICall::callAudioTrackWrite(jbyteArray audioData, int offsetInBytes, int sizeInBytes) {
    //调用java的write方法
    jniEnv->CallIntMethod(jAudioTrackObj,jAudioTrackWriteMid,audioData,offsetInBytes,
                          sizeInBytes);
}

void DZJNICall::callPlayerError(ThreadMode threadMode,int code, char *msg) {
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
