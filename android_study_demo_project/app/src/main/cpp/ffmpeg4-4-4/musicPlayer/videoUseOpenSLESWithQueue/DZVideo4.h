//
// Created by Machenike on 2026/10/12.
//

#ifndef ANDROID_STUDY_DEMO_PROJECT_DZVIDEO4_H
#define ANDROID_STUDY_DEMO_PROJECT_DZVIDEO4_H


#include "DZMedia4.h"
#include "DZAudio4.h"
#include <android/native_window.h>
#include <android/native_window_jni.h>

extern "C"{
#include "../../../headerFile/LogUtils.h"
#include "libswscale/swscale.h"
#include "libavutil/imgutils.h"
#include "libavutil/time.h"
};

class DZVideo4 : public DZMedia4{
public:
    SwsContext *pSwsContext = NULL;
    uint8_t *pFrameBuffer = NULL;
    int frameSize = -1;
    AVFrame *pRgbaFrame = NULL;
    jobject surface = NULL;
    DZAudio4 *pAudio = NULL;

    /**
     * 视频的延迟时间
     */
     double delayTime = 0;
     /**
      * 默认情况下，最合适的一个延迟时间（一帧的间隔），动态获取
      */
      double defaultDelayTime = 0.04;

public:
    DZVideo4(int streamIndex, DZJNICall4 *pJniCall, DZPlayerStatus4 *pPlayerStatus,DZAudio4 *pAudio);
    ~DZVideo4();

public:
    void play() override;
    void privateAnalysisStream(ThreadMode threadMode, AVFormatContext *pFormatContexts) override;

    void release() override;
    void setSurface(jobject surface);

    /***
     * 视频同步音频，计算获取休眠的时间
     * @param pFrame 当前视频帧
     * @return 休眠时间（s）
     */
    double getFrameSleepTime(AVFrame *pFrame);
};


#endif //ANDROID_STUDY_DEMO_PROJECT_DZVIDEO4_H
