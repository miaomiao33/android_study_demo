//
// Created by Machenike on 2026/10/10.
//

#ifndef ANDROID_STUDY_DEMO_PROJECT_DZAUDIO2_H
#define ANDROID_STUDY_DEMO_PROJECT_DZAUDIO2_H

#include <pthread.h>
#include "DZJNICall2.h"

extern "C"{
#include "libavformat/avformat.h"
#include "libswresample/swresample.h"
//OpenSLES 的包
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>
};

class DZAudio2 {
public:
    AVFormatContext *pFormatContext = NULL;
    AVCodecContext *pCodecContext = NULL;
    SwrContext *pSwrContext = NULL;
    uint8_t *resampleOutBuffer = NULL;
    char *url = NULL;
    DZJNICall2 *pJniCall = NULL;
    int audioStreamIndex = -1;
public:
    DZAudio2(int audioStreamIndex, DZJNICall2 *pJniCall,
             AVCodecContext *pCodecContext, AVFormatContext *pFormatContext, SwrContext *pSwrContext);

    void play();


    void initCreateOpenSLES2();

    int resampleAudio();
};


#endif //ANDROID_STUDY_DEMO_PROJECT_DZAUDIO2_H
