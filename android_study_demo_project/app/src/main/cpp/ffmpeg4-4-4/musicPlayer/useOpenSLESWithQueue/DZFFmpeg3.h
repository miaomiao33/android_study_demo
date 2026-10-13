//
// Created by Machenike on 2026/10/8.
//

#ifndef ANDROID_STUDY_DEMO_PROJECT_DZFFMPEG3_H
#define ANDROID_STUDY_DEMO_PROJECT_DZFFMPEG3_H

#include <pthread.h>
//包含有 DZJNICall3，故把线程的枚举类型放在 DZJNICall3 中
#include "DZJNICall3.h"
#include "music_player3.h"
#include "DZAudio3.h"

extern "C"{
#include "libavformat/avformat.h"
#include "libswresample/swresample.h"
#include "../../../headerFile/LogUtils.h"
};


class DZFFmpeg3 {
public:
    AVFormatContext *pFormatContext = NULL;
    char *url = NULL;
    DZJNICall3 *pJniCall = NULL;
    DZAudio3 *pAudio = NULL;
public:
    DZFFmpeg3(DZJNICall3 *pJniCall, const char *url);
    ~DZFFmpeg3();

public:
    void play();

    void prepare();

    void prepareAsync();

    void prepare(ThreadMode threadMode);

    void callPlayerJniError(ThreadMode threadMode,int code,char* msg);

    void release();

private:
};


#endif //ANDROID_STUDY_DEMO_PROJECT_DZFFMPEG3_H
