//
// Created by Machenike on 2026/10/3.
//

#ifndef ANDROID_STUDY_DEMO_PROJECT_MUSIC_PLAYER_H
#define ANDROID_STUDY_DEMO_PROJECT_MUSIC_PLAYER_H

extern "C"{
#include "libavformat/avformat.h"
#include "../../headerFile/LogUtils.h"
#include <jni.h>
//    重采样的包
#include "libswresample/swresample.h"
};
#define AUDIO_SIMPLE_RATE 44100

//------- 播放错误码 start -------
#define FIND_STREAM_ERROR_CODE -0x10
#define CODEC_FIND_DECODER_ERROR_CODE -0x11
#define CODEC_ALLOC_CONTEXT_ERROR_CODE -0x12
#define SWR_ALLOC_SET_OPTS_ERROR_CODE -0x13
#define SWR_CONTEXT_INIT_ERROR_CODE -0x14

//------- 播放错误码 end -------

class music_player {

public:
};


#endif //ANDROID_STUDY_DEMO_PROJECT_MUSIC_PLAYER_H
