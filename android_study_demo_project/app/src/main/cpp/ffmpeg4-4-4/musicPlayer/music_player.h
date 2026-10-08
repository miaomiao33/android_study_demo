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

class music_player {

public:
};


#endif //ANDROID_STUDY_DEMO_PROJECT_MUSIC_PLAYER_H
