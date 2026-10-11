//
// Created by Machenike on 2026/10/8.
//

#ifndef ANDROID_STUDY_DEMO_PROJECT_DZFFMPEG_H
#define ANDROID_STUDY_DEMO_PROJECT_DZFFMPEG_H

#include <pthread.h>
//包含有 DZJNICall，故把线程的枚举类型放在 DZJNICall 中
#include "DZJNICall.h"
#include "music_player.h"
#include "DZAudio.h"

extern "C"{
#include "libavformat/avformat.h"
#include "libswresample/swresample.h"
#include "../../../headerFile/LogUtils.h"
};


class DZFFmpeg {
public:
    AVFormatContext *pFormatContext = NULL;
    char *url = NULL;
    DZJNICall *pJniCall = NULL;
    DZAudio *pAudio = NULL;
public:
    DZFFmpeg(DZJNICall *pJniCall, const char *url);
    ~DZFFmpeg();

public:
    void play();

    void prepare();

    void prepareAsync();

    void prepare(ThreadMode threadMode);

    void callPlayerJniError(ThreadMode threadMode,int code,char* msg);

    void release();

private:
};


#endif //ANDROID_STUDY_DEMO_PROJECT_DZFFMPEG_H
