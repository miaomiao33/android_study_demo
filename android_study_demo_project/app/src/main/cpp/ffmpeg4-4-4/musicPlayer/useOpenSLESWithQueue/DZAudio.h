//
// Created by Machenike on 2026/10/10.
//

#ifndef ANDROID_STUDY_DEMO_PROJECT_DZAUDIO_H
#define ANDROID_STUDY_DEMO_PROJECT_DZAUDIO_H

#include <pthread.h>
#include "DZJNICall.h"
#include "DZPacketQueue.h"
#include "DZPlayerStatus.h"

extern "C"{
#include "libavformat/avformat.h"
#include "libswresample/swresample.h"
//OpenSLES 的包
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>
};

class DZAudio {
public:
    AVFormatContext *pFormatContext = NULL;
    AVCodecContext *pCodecContext = NULL;
    SwrContext *pSwrContext = NULL;
    uint8_t *resampleOutBuffer = NULL;
    char *url = NULL;
    DZJNICall *pJniCall = NULL;
    int audioStreamIndex = -1;
    DZPacketQueue *pPacketQueue = NULL;
    DZPlayerStatus *pPlayerStatus = NULL;

public:
    DZAudio(int audioStreamIndex,DZJNICall *pJniCall,AVFormatContext *pFormatContext);
    ~DZAudio();

    void play();


    void initCreateOpenSLES2();

    int resampleAudio();

    void analysisStream(ThreadMode threadMode, AVStream **streams);

    void callPlayerJniError(ThreadMode threadMode, int code, char *msg);

    void release();
};


#endif //ANDROID_STUDY_DEMO_PROJECT_DZAUDIO_H
