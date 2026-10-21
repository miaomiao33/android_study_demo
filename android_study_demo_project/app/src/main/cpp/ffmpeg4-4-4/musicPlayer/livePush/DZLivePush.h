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

public:
    DZLivePush(const char *liveUrl, DZJNICall5 *pJniCall);
    ~DZLivePush();

    void initConnect();
};


#endif //ANDROID_STUDY_DEMO_PROJECT_DZLIVEPUSH_H
