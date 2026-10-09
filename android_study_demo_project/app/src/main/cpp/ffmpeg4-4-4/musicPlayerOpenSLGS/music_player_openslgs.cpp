//
// Created by Machenike on 2026/10/9.
//

#include "music_player_openslgs.h"
#include <jni.h>
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>
#include <malloc.h>

void initCreateOpenSLES();
void playerCallback(SLAndroidSimpleBufferQueueItf caller,void *pContext);

FILE *pcmFile = NULL;
void *pcmBuffer = NULL;

extern "C"
JNIEXPORT void JNICALL
Java_com_example_android_1study_1demo_1project_opencv_ffmpegUsage_MusicPlayerOpenSLES_MusicPlayerOpenSLESMainActivity_playPCM(
        JNIEnv *env, jobject thiz, jstring absolutePath_) {
    const char *absolutePath = env->GetStringUTFChars(absolutePath_,JNI_FALSE);
    //打开音频文件
    pcmFile = fopen(absolutePath,"r");
    pcmBuffer = malloc(44100 * 2 * 2);
    initCreateOpenSLES();

    env->ReleaseStringUTFChars(absolutePath_,absolutePath);
}

//初始化OpenSLES，并进行播放
void initCreateOpenSLES() {
    //3.1创建引擎接口对象(设置参数，实现，获取接口)
    SLObjectItf engineObject = NULL;
    SLEngineItf engineEngine;
    slCreateEngine(&engineObject,0,NULL,0,
                   NULL,NULL);
    //realize the engine
    (*engineObject)->Realize(engineObject, SL_BOOLEAN_FALSE);
    //get the engine interface,which is needed in order to create other object
    (*engineObject)->GetInterface(engineObject,SL_IID_ENGINE,&engineEngine);
    //3.2设置混音器
    static SLObjectItf outputMixObject = NULL;
    const SLInterfaceID ids[1] = {SL_IID_ENVIRONMENTALREVERB};
    const SLboolean req[1] = {SL_BOOLEAN_FALSE};
    (*engineEngine)->CreateOutputMix(engineEngine, &outputMixObject,1,ids,req);//1表示一个接口

    (*outputMixObject)->Realize(outputMixObject, SL_BOOLEAN_FALSE);
    SLEnvironmentalReverbItf outputMixEnvironmentalReverb = NULL;
    (*outputMixObject)->GetInterface(outputMixObject,SL_IID_ENVIRONMENTALREVERB,&outputMixEnvironmentalReverb);
    SLEnvironmentalReverbSettings reverbSettings = SL_I3DL2_ENVIRONMENT_PRESET_STONECORRIDOR;
    (*outputMixEnvironmentalReverb)->SetEnvironmentalReverbProperties(outputMixEnvironmentalReverb,
                                                                      &reverbSettings);
    //3.3创建播放器
    SLObjectItf pPlayer = NULL;

    SLDataLocator_AndroidSimpleBufferQueue simpleBufferQueue = {
            SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE,2};
    SLDataFormat_PCM formatPcm = {
            SL_DATAFORMAT_PCM,
            2,//2通道
            SL_SAMPLINGRATE_44_1,//44100
            SL_PCMSAMPLEFORMAT_FIXED_16,
            SL_PCMSAMPLEFORMAT_FIXED_16,
            SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT,
            SL_BYTEORDER_LITTLEENDIAN};
    SLDataSource audioSrc = {&simpleBufferQueue,&formatPcm};

    SLDataLocator_OutputMix outputMix = {SL_DATALOCATOR_OUTPUTMIX,outputMixObject};
    SLDataSink audioSnk = {&outputMix,NULL};

    const SLInterfaceID interfaceIds[3] = {SL_IID_BUFFERQUEUE,SL_IID_VOLUME,SL_IID_PLAYBACKRATE};
    const SLboolean interfaceRequired[3] = {SL_BOOLEAN_TRUE,SL_BOOLEAN_TRUE,SL_BOOLEAN_TRUE};

    (*engineEngine)->CreateAudioPlayer(engineEngine,&pPlayer,&audioSrc,&audioSnk,3,
                                       interfaceIds,interfaceRequired);

    SLPlayItf pPlayItf  = NULL;
    (*pPlayer)->Realize(pPlayer,SL_BOOLEAN_FALSE);
    (*pPlayer)->GetInterface(pPlayer,SL_IID_PLAY,&pPlayItf);
    //3.4设置缓存队列和回调函数
    SLAndroidSimpleBufferQueueItf playerBufferQueue;
    (*pPlayer)->GetInterface(pPlayer,SL_IID_BUFFERQUEUE,&playerBufferQueue);
    (*playerBufferQueue)->RegisterCallback(playerBufferQueue,playerCallback,NULL);
    //3.5设置播放状态
    (*pPlayItf)->SetPlayState(pPlayItf, SL_PLAYSTATE_PLAYING);
    //3.6调用回调函数
    playerCallback(playerBufferQueue,NULL);
}

void playerCallback(SLAndroidSimpleBufferQueueItf caller,void *pContext){
    //回调函数，循环
    if(!feof(pcmFile))
    {
        //没有到结尾
        fread(pcmBuffer,1,44100 * 2 * 2, pcmFile);
        //将获取到的数据压入queue中进行播放
        (*caller)->Enqueue(caller, pcmBuffer, 44100 * 2 * 2);
    } else{
        fclose(pcmFile);
        free(pcmFile);
    }
}
