//
// Created by Machenike on 2026/10/11.
//

#include "DZPacketQueue6.h"

void DZPacketQueue6::clear() {
    //需要清除队列，还需要清除每个 AVPacket* 的内存数据

}

DZPacketQueue6::DZPacketQueue6() {
    packetQueue = new std::queue<AVPacket *>();
    pthread_mutex_init(&packetMutex,NULL);
    pthread_cond_init(&packetCond,NULL);
}

DZPacketQueue6::~DZPacketQueue6() {
    if(packetQueue != NULL)
    {
        clear();
        delete(packetQueue);
        packetQueue = NULL;
    }
    pthread_mutex_destroy(&packetMutex);
    pthread_cond_destroy(&packetCond);
}

void DZPacketQueue6::push(AVPacket *pPacket) {
    pthread_mutex_lock(&packetMutex);

    packetQueue->push(pPacket);
    pthread_cond_signal(&packetCond);
    pthread_mutex_unlock(&packetMutex);

}

AVPacket *DZPacketQueue6::pop() {
    AVPacket *pPacket = NULL;
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
AVPacket* DZPacketQueue6::pop_timeout(int ms)
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

    AVPacket *pkt = nullptr;
    if (!packetQueue->empty())
    {
        pkt = packetQueue->front();
        packetQueue->pop();
    }

    pthread_mutex_unlock(&packetMutex);
    return pkt;
}