//
// Created by Machenike on 2026/10/11.
//

#ifndef ANDROID_STUDY_DEMO_PROJECT_DZPACKETQUEUE4_H
#define ANDROID_STUDY_DEMO_PROJECT_DZPACKETQUEUE4_H

extern "C"{

#include "libavcodec/avcodec.h"
};

#include <queue>
#include <pthread.h>

class DZPacketQueue4 {
public:
    std::queue<AVPacket *> *packetQueue;
    //互斥锁
    pthread_mutex_t packetMutex;
    //条件变量
    pthread_cond_t packetCond;
public:
    DZPacketQueue4();
    ~DZPacketQueue4();

public:
    void push(AVPacket *pPacket);
    AVPacket* pop();
    /***
     * 清理整个队列
     */
    void clear();

};


#endif //ANDROID_STUDY_DEMO_PROJECT_DZPACKETQUEUE4_H
