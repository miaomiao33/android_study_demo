//
// Created by Machenike on 2026/10/8.
//

#ifndef ANDROID_STUDY_DEMO_PROJECT_DZFFMPEG4_H
#define ANDROID_STUDY_DEMO_PROJECT_DZFFMPEG4_H

#include <pthread.h>
//包含有 DZJNICall4，故把线程的枚举类型放在 DZJNICall4 中
#include "DZJNICall4.h"
#include "video_player4.h"
#include "DZAudio4.h"
#include "DZPlayerStatus4.h"

extern "C"{
#include "libavformat/avformat.h"
#include "libswresample/swresample.h"
#include "../../../headerFile/LogUtils.h"
};


class DZFFmpeg4 {
public:
    AVFormatContext *pFormatContext = NULL;
    char *url = NULL;
    DZJNICall4 *pJniCall = NULL;
    DZAudio4 *pAudio = NULL;
    DZPlayerStatus4 *pPlayerStatus;
public:
    DZFFmpeg4(DZJNICall4 *pJniCall, const char *url);
    ~DZFFmpeg4();

public:
    void play();

    void prepare();

    void prepareAsync();

    void prepare(ThreadMode threadMode);

    void callPlayerJniError(ThreadMode threadMode,int code,char* msg);

    void release();
private:
};


#endif //ANDROID_STUDY_DEMO_PROJECT_DZFFMPEG4_H
