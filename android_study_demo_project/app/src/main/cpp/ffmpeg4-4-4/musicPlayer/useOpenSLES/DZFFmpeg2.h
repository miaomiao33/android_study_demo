//
// Created by Machenike on 2026/10/8.
//

#ifndef ANDROID_STUDY_DEMO_PROJECT_DZFFMPEG2_H
#define ANDROID_STUDY_DEMO_PROJECT_DZFFMPEG2_H

#include <pthread.h>
//包含有 DZJNICall2，故把线程的枚举类型放在 DZJNICall2 中
#include "DZJNICall2.h"
#include "music_player2.h"
#include "DZAudio2.h"

extern "C"{
#include "libavformat/avformat.h"
#include "libswresample/swresample.h"
#include "../../../headerFile/LogUtils.h"
};


class DZFFmpeg2 {
public:
    AVFormatContext *pFormatContext = NULL;
    AVCodecContext *pCodecContext = NULL;
    SwrContext *swrContext = NULL;
    char *url = NULL;
    DZJNICall2 *pJniCall = NULL;
    DZAudio2 *pAudio = NULL;
public:
    DZFFmpeg2(DZJNICall2 *pJniCall, const char *url);
    ~DZFFmpeg2();

public:
    void play();

    void prepare();

    void prepareAsync();

    void prepare(ThreadMode threadMode);

    void callPlayerJniError(ThreadMode threadMode,int code,char* msg);

    void release();

private:
};


#endif //ANDROID_STUDY_DEMO_PROJECT_DZFFMPEG2_H
