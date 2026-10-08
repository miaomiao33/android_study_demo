//
// Created by Machenike on 2026/10/8.
//

#ifndef ANDROID_STUDY_DEMO_PROJECT_DZFFMPEG_H
#define ANDROID_STUDY_DEMO_PROJECT_DZFFMPEG_H

extern "C"{
#include "libavformat/avformat.h"
#include "libswresample/swresample.h"
#include "DZJNICall.cpp"
};

class DZFFmpeg {
public:
    AVFormatContext *pFormatContext = NULL;
    AVCodecContext *pCodecContext = NULL;
    SwrContext *swrContext = NULL;
    uint8_t *resampleOutBuffer = NULL;
    const char *url = NULL;
    DZJNICall *pJniCall = NULL;
public:
    DZFFmpeg(DZJNICall *pJniCall, const char *url);
    ~DZFFmpeg();

public:
    void play();
    void callPlayerJniError(int code,char* msg);
private:
};


#endif //ANDROID_STUDY_DEMO_PROJECT_DZFFMPEG_H
