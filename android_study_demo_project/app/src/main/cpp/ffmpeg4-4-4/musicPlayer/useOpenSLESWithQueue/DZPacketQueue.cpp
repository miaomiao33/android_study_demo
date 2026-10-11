//
// Created by Machenike on 2026/10/11.
//

#include "DZPacketQueue.h"

void DZPacketQueue::clear() {
    //需要清楚队列，还需要清楚每个 AVPacket* 的内存数据


}

DZPacketQueue::DZPacketQueue() {
    packetQueue = new std::queue<AVPacket *>();
    pthread_mutex_init(&packetMutex,NULL);
    pthread_cond_init(&packetCond,NULL);
}

DZPacketQueue::~DZPacketQueue() {
    if(packetQueue != NULL)
    {
        clear();
        delete(packetQueue);
        packetQueue = NULL;
    }
    pthread_mutex_destroy(&packetMutex);
    pthread_cond_destroy(&packetCond);
}

void DZPacketQueue::push(AVPacket *pPacket) {
    pthread_mutex_lock(&packetMutex);

    packetQueue->push(pPacket);
    pthread_cond_signal(&packetCond);
    pthread_mutex_unlock(&packetMutex);

}

AVPacket *DZPacketQueue::pop() {
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
