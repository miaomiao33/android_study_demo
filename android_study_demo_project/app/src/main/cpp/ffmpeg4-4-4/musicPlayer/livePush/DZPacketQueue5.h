//
// Created by Machenike on 2026/10/11.
//

#ifndef ANDROID_STUDY_DEMO_PROJECT_DZPACKETQUEUE5_H
#define ANDROID_STUDY_DEMO_PROJECT_DZPACKETQUEUE5_H

extern "C"{
#include "libavcodec/avcodec.h"
#include "../../../librtmp/rtmp.h"
};

#include <queue>
#include <pthread.h>

class DZPacketQueue5 {
public:
    std::queue<RTMPPacket *> *packetQueue;
    //互斥锁
    pthread_mutex_t packetMutex;
    //条件变量
    pthread_cond_t packetCond;
public:
    DZPacketQueue5();
    ~DZPacketQueue5();

public:
    void push(RTMPPacket *pPacket);
    RTMPPacket* pop();
    /***
     * 清理整个队列
     */
    void clear();

    RTMPPacket *pop_timeout(int ms);
};


#endif //ANDROID_STUDY_DEMO_PROJECT_DZPACKETQUEUE5_H
