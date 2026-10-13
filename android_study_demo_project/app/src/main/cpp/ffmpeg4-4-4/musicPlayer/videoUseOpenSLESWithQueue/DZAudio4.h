//
// Created by Machenike on 2026/10/10.
//

#ifndef ANDROID_STUDY_DEMO_PROJECT_DZAUDIO4_H
#define ANDROID_STUDY_DEMO_PROJECT_DZAUDIO4_H

#include <pthread.h>
#include "DZJNICall4.h"
#include "DZPacketQueue4.h"
#include "DZPlayerStatus4.h"

extern "C"{
#include "libavformat/avformat.h"
#include "libswresample/swresample.h"
//OpenSLES 的包
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>
};

class DZAudio4 {
public:
    AVFormatContext *pFormatContext = NULL;
    AVCodecContext *pCodecContext = NULL;
    SwrContext *pSwrContext = NULL;
    uint8_t *resampleOutBuffer = NULL;
    char *url = NULL;
    DZJNICall4 *pJniCall = NULL;
    int audioStreamIndex = -1;
    DZPacketQueue4 *pPacketQueue = NULL;
    DZPlayerStatus4 *pPlayerStatus = NULL;

public:
    DZAudio4(int audioStreamIndex, DZJNICall4 *pJniCall, AVFormatContext *pFormatContext);
    ~DZAudio4();

    void play();

    void initCreateOpenSLES4();

    int resampleAudio();

    void analysisStream(ThreadMode threadMode, AVStream **streams);

    void callPlayerJniError(ThreadMode threadMode, int code, char *msg);

    void release();
};


#endif //ANDROID_STUDY_DEMO_PROJECT_DZAUDIO4_H
