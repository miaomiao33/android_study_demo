//
// Created by Machenike on 2026/10/14.
//

#ifndef ANDROID_STUDY_DEMO_PROJECT_DZMEDIA4_H
#define ANDROID_STUDY_DEMO_PROJECT_DZMEDIA4_H

#include "DZJNICall4.h"
#include "DZPacketQueue4.h"
#include "DZPlayerStatus4.h"

extern "C"{
#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"
};

class DZMedia4 {
public:
    int streamIndex = -1;
    AVCodecContext *pCodecContext = NULL;
    DZJNICall4 *pJniCall = NULL;
    DZPacketQueue4 *pPacketQueue = NULL;
    DZPlayerStatus4 *pPlayerStatus = NULL;
    AVCodecParameters *pCodecParameters;
    /***
     * 整个视频的时长
     */
    long duration = 0;
    /***
     * 当前时间
     */
    double currentTime = 0;
    /***
     * 上次更新时间（回调到 Java层）
     */
    double lastUpdateTime = 0;

    /***
     * 时间基
     */
    AVRational timeBase;
public:
    DZMedia4(int streamIndex, DZJNICall4 *pJniCall, DZPlayerStatus4 *pPlayerStatus);
    ~DZMedia4();

public:
    //纯虚函数，必须要继承
    virtual void play() = 0;
    virtual void release();

    void analysisStream(ThreadMode threadMode, AVFormatContext *pFormatContext);

    virtual void privateAnalysisStream(ThreadMode threadMode, AVFormatContext *pFormatContext) = 0;
    void callPlayerJniError(ThreadMode threadMode, int code, char *msg);
private:
    void publicAnalysisStream(ThreadMode threadMode, AVFormatContext *pFormatContext);

};


#endif //ANDROID_STUDY_DEMO_PROJECT_DZMEDIA4_H
