//
// Created by Machenike on 2026/10/10.
//

#ifndef ANDROID_STUDY_DEMO_PROJECT_DZAUDIO4_H
#define ANDROID_STUDY_DEMO_PROJECT_DZAUDIO4_H

#include <pthread.h>
#include "DZJNICall4.h"
#include "DZPacketQueue4.h"
#include "DZPlayerStatus4.h"
#include "DZMedia4.h"
#include "video_player_common4.h"

extern "C"{
#include "libavformat/avformat.h"
#include "libswresample/swresample.h"
//OpenSLES 的包
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>
};

class DZAudio4 : public DZMedia4{
public:
    AVFormatContext *pFormatContext = NULL;
    SwrContext *pSwrContext = NULL;
    uint8_t *resampleOutBuffer = NULL;
    char *url = NULL;

public:
    DZAudio4(int audioStreamIndex, DZJNICall4 *pJniCall, DZPlayerStatus4 *pPlayerStatus);
    ~DZAudio4();

    void play() override;

    void initCreateOpenSLES4();

    int resampleAudio();
    void privateAnalysisStream(ThreadMode threadMode, AVFormatContext *context) override;

    void release() override;
};


#endif //ANDROID_STUDY_DEMO_PROJECT_DZAUDIO4_H
