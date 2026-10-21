//
// Created by Machenike on 2026/10/11.
//

#include "DZPacketQueue5.h"

void DZPacketQueue5::clear() {
    //需要清除队列，还需要清除每个 AVPacket* 的内存数据
    pthread_mutex_lock(&packetMutex);

    while (!packetQueue->empty())
    {
        RTMPPacket *pPacket = packetQueue->front();
        packetQueue->pop();
        RTMPPacket_Free(pPacket);
        free(pPacket);
    }

    pthread_mutex_unlock(&packetMutex);
}

DZPacketQueue5::DZPacketQueue5() {
    packetQueue = new std::queue<RTMPPacket *>();
    pthread_mutex_init(&packetMutex,NULL);
    pthread_cond_init(&packetCond,NULL);
}

DZPacketQueue5::~DZPacketQueue5() {
    if(packetQueue != NULL)
    {
        clear();
        delete(packetQueue);
        packetQueue = NULL;
    }
    pthread_mutex_destroy(&packetMutex);
    pthread_cond_destroy(&packetCond);
}

void DZPacketQueue5::push(RTMPPacket *pPacket) {
    pthread_mutex_lock(&packetMutex);

    packetQueue->push(pPacket);
    pthread_cond_signal(&packetCond);
    pthread_mutex_unlock(&packetMutex);

}

RTMPPacket *DZPacketQueue5::pop() {
    RTMPPacket *pPacket = NULL;
    pthread_mutex_lock(&packetMutex);
    //队列为空就一直等待
    while (packetQueue->empty())
    {
        pthread_cond_wait(&packetCond,&packetMutex);
    }
    //有数据了，则获取最顶端
    pPacket = packetQueue->front();
    packetQueue->pop();
    pthread_mutex_unlock(&packetMutex);
    return nullptr;
}


//增加超时版本，队列为空返回 nullptr，不卡死音频线程
RTMPPacket* DZPacketQueue5::pop_timeout(int ms)
{
    pthread_mutex_lock(&packetMutex);

    struct timespec ts;
    clock_gettime(CLOCK_REALTIME, &ts);
    ts.tv_sec += ms / 1000;
    ts.tv_nsec += (ms % 1000) * 1000000;
    if (ts.tv_nsec >= 1000000000)
    {
        ts.tv_sec++;
        ts.tv_nsec -= 1000000000;
    }

    int ret = 0;
    while (packetQueue->empty() && ret == 0)
    {
        ret = pthread_cond_timedwait(&packetCond, &packetMutex, &ts);
    }

    RTMPPacket *pkt = nullptr;
    if (!packetQueue->empty())
    {
        pkt = packetQueue->front();
        packetQueue->pop();
    }

    pthread_mutex_unlock(&packetMutex);
    return pkt;
}