//
// Created by Machenike on 2026/10/11.
//

#ifndef ANDROID_STUDY_DEMO_PROJECT_DZPACKETQUEUE_H
#define ANDROID_STUDY_DEMO_PROJECT_DZPACKETQUEUE_H

extern "C"{

#include "libavcodec/avcodec.h"
};

#include <queue>
#include <pthread.h>

class DZPacketQueue {
public:
    std::queue<AVPacket *> *packetQueue;
    //互斥锁
    pthread_mutex_t packetMutex;
    //条件变量
    pthread_cond_t packetCond;
public:
    DZPacketQueue();
    ~DZPacketQueue();

public:
    void push(AVPacket *pPacket);
    AVPacket* pop();
    /***
     * 清理整个队列
     */
    void clear();

};


#endif //ANDROID_STUDY_DEMO_PROJECT_DZPACKETQUEUE_H
