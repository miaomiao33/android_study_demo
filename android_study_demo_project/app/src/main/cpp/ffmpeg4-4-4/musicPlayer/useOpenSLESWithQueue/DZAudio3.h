//
// Created by Machenike on 2026/10/10.
//

#ifndef ANDROID_STUDY_DEMO_PROJECT_DZAUDIO3_H
#define ANDROID_STUDY_DEMO_PROJECT_DZAUDIO3_H

#include <pthread.h>
#include "DZJNICall3.h"
#include "DZPacketQueue3.h"
#include "DZPlayerStatus3.h"

extern "C"{
#include "libavformat/avformat.h"
#include "libswresample/swresample.h"
//OpenSLES 的包
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>
};

class DZAudio3 {
public:
    AVFormatContext *pFormatContext = NULL;
    AVCodecContext *pCodecContext = NULL;
    SwrContext *pSwrContext = NULL;
    uint8_t *resampleOutBuffer = NULL;
    char *url = NULL;
    DZJNICall3 *pJniCall = NULL;
    int audioStreamIndex = -1;
    DZPacketQueue3 *pPacketQueue = NULL;
    DZPlayerStatus3 *pPlayerStatus = NULL;
    AVFrame *pFrame = NULL;  // 新增成员，只分配一次

public:
    DZAudio3(int audioStreamIndex, DZJNICall3 *pJniCall, AVFormatContext *pFormatContext);
    ~DZAudio3();

    void play();


    void initCreateOpenSLES3();

    int resampleAudio3();

    void analysisStream(ThreadMode threadMode, AVStream **streams);

    void callPlayerJniError(ThreadMode threadMode, int code, char *msg);

    void release();
};


#endif //ANDROID_STUDY_DEMO_PROJECT_DZAUDIO3_H
