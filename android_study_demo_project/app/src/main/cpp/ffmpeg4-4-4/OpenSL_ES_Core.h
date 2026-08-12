//
// Created by Machenike on 2026/8/6.
//

#ifndef ANDROID_STUDY_DEMO_PROJECT_OPENSL_ES_CORE_H
#define ANDROID_STUDY_DEMO_PROJECT_OPENSL_ES_CORE_H

#ifdef __cplusplus
extern "C" {
#endif

#include "FFmpegCore.h"
#include <assert.h>
#include <jni.h>
#include <string.h>

#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>

//调用native的AssetManager类
#include <sys/types.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include "../headerFile/LogUtils.h"

#include <cassert>
#include <jni.h>
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include "OpenSL_ES_Core.h"

// ========== 外部全局变量声明（源文件里定义，头文件extern声明） ==========
extern SLObjectItf engineObject;
extern SLEngineItf engineEngine;

extern SLObjectItf outputMixObject;
extern SLEnvironmentalReverbItf outputMixEnvironmentalReverb;

extern SLObjectItf bqPlayerObject;
extern SLPlayItf bqPlayerPlay;
extern SLAndroidSimpleBufferQueueItf bqPlayerBufferQueue;
extern SLEffectSendItf bqPlayerEffectSend;
extern SLMuteSoloItf bqPlayerMuteSolo;
extern SLVolumeItf bqPlayerVolume;

extern void *buffer;
extern size_t bufferSize;

// ========== 回调函数声明 ==========
void bqPlayerCallback(SLAndroidSimpleBufferQueueItf bq, void *context);

// ==========对外暴露接口声明，供别的cpp include后调用 ==========
void initOpenSLES();
void initBufferQueue(int rate, int channel, int bitsPerSample);
void stop();
void play(char *url);

#ifdef __cplusplus
}
#endif

#endif //ANDROID_STUDY_DEMO_PROJECT_OPENSL_ES_CORE_H
