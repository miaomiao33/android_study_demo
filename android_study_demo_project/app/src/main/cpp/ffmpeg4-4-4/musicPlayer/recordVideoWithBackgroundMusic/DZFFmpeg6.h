//
// Created by Machenike on 2026/10/8.
//

#ifndef ANDROID_STUDY_DEMO_PROJECT_DZFFMPEG6_H
#define ANDROID_STUDY_DEMO_PROJECT_DZFFMPEG6_H

#include <pthread.h>
//包含有 DZJNICall6，故把线程的枚举类型放在 DZJNICall6 中
#include "DZJNICall6.h"
#include "music_player_for_record_video.h"
#include "DZAudio6.h"

extern "C"{
#include "libavformat/avformat.h"
#include "libswresample/swresample.h"
#include "../../../headerFile/LogUtils.h"
};


class DZFFmpeg6 {
public:
    AVFormatContext *pFormatContext = NULL;
    char *url = NULL;
    DZJNICall6 *pJniCall = NULL;
    DZAudio6 *pAudio = NULL;

    void stop();

public:
    DZFFmpeg6(DZJNICall6 *pJniCall, const char *url);
    ~DZFFmpeg6();

public:
    void play();

    void prepare();

    void prepareAsync();

    void prepare(ThreadMode threadMode);

    void callPlayerJniError(ThreadMode threadMode,int code,char* msg);

    void release();

};


#endif //ANDROID_STUDY_DEMO_PROJECT_DZFFMPEG6_H
