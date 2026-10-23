//
// Created by Machenike on 2026/10/10.
//

#ifndef ANDROID_STUDY_DEMO_PROJECT_DZAUDIO6_H
#define ANDROID_STUDY_DEMO_PROJECT_DZAUDIO6_H

#include <pthread.h>
#include "DZJNICall6.h"
#include "DZPacketQueue6.h"
#include "DZPlayerStatus6.h"

extern "C"{
#include "libavformat/avformat.h"
#include "libswresample/swresample.h"
//OpenSLES 的包
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>
};

class DZAudio6 {
public:
    AVFormatContext *pFormatContext = NULL;
    AVCodecContext *pCodecContext = NULL;
    SwrContext *pSwrContext = NULL;
    uint8_t *resampleOutBuffer = NULL;
    char *url = NULL;
    DZJNICall6 *pJniCall = NULL;
    int audioStreamIndex = -1;
    DZPacketQueue6 *pPacketQueue = NULL;
    DZPlayerStatus6 *pPlayerStatus = NULL;
    AVFrame *pFrame = NULL;  // 新增成员，只分配一次
    SLPlayItf pPlayItf = NULL;
    pthread_t readPacketThreadT;

public:
    DZAudio6(int audioStreamIndex, DZJNICall6 *pJniCall, AVFormatContext *pFormatContext);
    ~DZAudio6();

    void play();


    void initCreateOpenSLES6();

    int resampleAudio6();

    void analysisStream(ThreadMode threadMode, AVStream **streams);

    void callPlayerJniError(ThreadMode threadMode, int code, char *msg);

    void release();

    void stop();
};


#endif //ANDROID_STUDY_DEMO_PROJECT_DZAUDIO6_H
