//
// Created by Machenike on 2026/10/15.
//

#ifndef ANDROID_STUDY_DEMO_PROJECT_DZLIVEPUSH_H
#define ANDROID_STUDY_DEMO_PROJECT_DZLIVEPUSH_H

#include <malloc.h>
#include "DZJNICall5.h"
#include "DZPacketQueue5.h"
#include "video_player_common5.h"
extern "C"{

};

class DZLivePush {
public:
    DZJNICall5 *pJniCall5 = NULL;
    char *liveUrl = NULL;
    DZPacketQueue5 *pPacketQueue5 = NULL;
    RTMP *pRtmp = NULL;
    volatile bool isPushing = true;
    uint32_t startTime;
    pthread_t initConnectTid;
    int mStreamId;

public:
    DZLivePush(const char *liveUrl, DZJNICall5 *pJniCall);
    ~DZLivePush();

    void initConnect();

    void pushSpsPps(jbyte *spsData, jint spsLen, jbyte *ppsData, jint ppsLen);

    void pushVideo(jbyte *videoData, jint dataLen, jboolean keyFrame);

    void pushAudio(jbyte *audioData, jint audioLen);

    void stop();
};


#endif //ANDROID_STUDY_DEMO_PROJECT_DZLIVEPUSH_H
