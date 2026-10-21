//
// Created by Machenike on 2026/10/15.
//

#include "DZLivePush.h"

DZLivePush::DZLivePush(const char *liveUrl, DZJNICall5 *pJniCall) {
    this->pJniCall5 = pJniCall;
    this->liveUrl = (char *)malloc(strlen(liveUrl) + 1);//申请内存
    strcpy(this->liveUrl,liveUrl);//拷贝数据
    pPacketQueue5 = new DZPacketQueue5();
}

DZLivePush::~DZLivePush() {
    if(pRtmp != NULL)
    {
        RTMP_Close(pRtmp);
        free(pRtmp);
        pRtmp = NULL;
    }

    if(liveUrl != NULL)
    {
        free(liveUrl);
        liveUrl = NULL;
    }

    if(pPacketQueue5 != NULL)
    {
        delete(pPacketQueue5);
        pPacketQueue5 = NULL;
    }
}

void *initConnectFun(void *context){
    DZLivePush *pLivePush = (DZLivePush *)context;
    //1、创建Rtmp
    pLivePush->pRtmp = RTMP_Alloc();
    //2、初始化
    RTMP_Init(pLivePush->pRtmp);
    //3、设置参数，连接的超时时间等
    pLivePush->pRtmp->Link.timeout = 10;
    pLivePush->pRtmp->Link.lFlags |= RTMP_LF_LIVE;
    RTMP_SetupURL(pLivePush->pRtmp,pLivePush->liveUrl);
    RTMP_EnableWrite(pLivePush->pRtmp);
    //开始连接
    if(!RTMP_Connect(pLivePush->pRtmp,NULL))
    {
        //回调到Java层
        LOGE("rtmp connect error")
        pLivePush->pJniCall5->callConnectError(THREAD_CHILD,
                                               INIT_RTMP_CONNECT_ERROR_CODE,
                                               "rtmp connect error");
        return (void *)INIT_RTMP_CONNECT_ERROR_CODE;
    }
    if(!RTMP_ConnectStream(pLivePush->pRtmp,NULL))
    {
        //回调到Java层
        LOGE("rtmp connect stream error")
        pLivePush->pJniCall5->callConnectError(THREAD_CHILD,
                                               INIT_RTMP_CONNECT_STREAM_ERROR_CODE,
                                               "rtmp connect stream error");
        return (void *)INIT_RTMP_CONNECT_STREAM_ERROR_CODE;
    }
    LOGE("rtmp connect success")
    pLivePush->pJniCall5->callConnectSuccess(THREAD_CHILD);
}

void DZLivePush::initConnect() {
    pthread_t initConnectTid;
    pthread_create(&initConnectTid, NULL, initConnectFun, this);
    pthread_detach(initConnectTid);
}
