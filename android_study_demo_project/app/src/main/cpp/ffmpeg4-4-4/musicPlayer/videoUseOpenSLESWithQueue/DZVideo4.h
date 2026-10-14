//
// Created by Machenike on 2026/10/12.
//

#ifndef ANDROID_STUDY_DEMO_PROJECT_DZVIDEO4_H
#define ANDROID_STUDY_DEMO_PROJECT_DZVIDEO4_H


#include "DZMedia4.h"
#include <android/native_window.h>
#include <android/native_window_jni.h>

extern "C"{
#include "../../../headerFile/LogUtils.h"
#include "libswscale/swscale.h"
#include "libavutil/imgutils.h"
};

class DZVideo4 : public DZMedia4{
public:
    SwsContext *pSwsContext = NULL;
    uint8_t *pFrameBuffer = NULL;
    int frameSize = -1;
    AVFrame *pRgbaFrame = NULL;
    jobject surface = NULL;
public:
    DZVideo4(int streamIndex, DZJNICall4 *pJniCall, DZPlayerStatus4 *pPlayerStatus);
    ~DZVideo4();

public:
    void play() override;
    void privateAnalysisStream(ThreadMode threadMode, AVFormatContext *context) override;

    void release() override;
    void setSurface(jobject surface);
};


#endif //ANDROID_STUDY_DEMO_PROJECT_DZVIDEO4_H
