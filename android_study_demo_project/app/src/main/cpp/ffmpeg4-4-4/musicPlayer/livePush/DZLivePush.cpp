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
        RTMP_Free(pRtmp);
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
        LOGE("TAG rtmp connect error")
        pLivePush->pJniCall5->callConnectError(THREAD_CHILD,
                                               INIT_RTMP_CONNECT_ERROR_CODE,
                                               "rtmp connect error");
        return (void *)INIT_RTMP_CONNECT_ERROR_CODE;
    }
    if(!RTMP_ConnectStream(pLivePush->pRtmp,0))
    {
        //回调到Java层
        LOGE("TAG rtmp connect stream error")
        pLivePush->pJniCall5->callConnectError(THREAD_CHILD,
                                               INIT_RTMP_CONNECT_STREAM_ERROR_CODE,
                                               "rtmp connect stream error");
        return (void *)INIT_RTMP_CONNECT_STREAM_ERROR_CODE;
    }
    pLivePush->mStreamId = pLivePush->pRtmp->m_stream_id;
    //RTMP 连接成功，回调 LivePush.java 的方法
    LOGE("TAG rtmp connect success")
    pLivePush->pJniCall5->callConnectSuccess(THREAD_CHILD);
    pLivePush->startTime = RTMP_GetTime();

    while(pLivePush->isPushing){
        //不断往流媒体服务器上推
        RTMPPacket *pPacket = pLivePush->pPacketQueue5->pop_timeout(20);
        if(pPacket == nullptr){
            LOGI("TAG  RTMPPacket nullptr")
            continue;
        }
        int res = RTMP_SendPacket(pLivePush->pRtmp,pPacket,1);
        LOGI("TAG res = %d",res)

        RTMPPacket_Free(pPacket);
        free(pPacket);
    }
    LOGI("停止了")

    return 0;
}

void DZLivePush::initConnect() {
    pthread_create(&initConnectTid, NULL, initConnectFun, this);
//    pthread_detach(initConnectTid);
}

void DZLivePush::pushSpsPps(jbyte *spsData, jint spsLen, jbyte *ppsData, jint ppsLen) {
    //frame type : 1 关键帧，2 非关键帧（4bit）
    //CodecID: 7 表示 AVC（4bit），与 frame type 组合起来刚好是 1 个字节  0x17
    //fixed : 0x00 0x00 0x00 0x00 (4byte)
    //configurationVersion: (1byte) 0x01版本
    //AVCProfileIndication : (1byte) sps[1] profile
    //profile_compatibility : (1byte) sps[2] profile compatibility
    //AVCLevelIndication : (1byte) sps[3] Profile level
    //lengthSizeMinusOne : (1byte) Oxff 包长数据所使用的字节数

    //sps + pps 的数据
    //sps number          (1byte) 0xe1 sps 个数
    //sps data length     (2byte) sps 长度
    //sps data                    sps 的内容
    //pps number          (1byte) 0x01 pps 个数
    //pps data length     (2byte) pps 长度
    //pps data                    pps 的内容

    //数据的长度（大小）= sps 大小 + pps 大小 + 16字节（头信息）
    int bodySize = spsLen + ppsLen + 16;
    //构建 RTMPPacket
    RTMPPacket *pPacket = (RTMPPacket *)malloc(sizeof(RTMPPacket));
    RTMPPacket_Alloc(pPacket,bodySize);
    RTMPPacket_Reset(pPacket);

    //构建 body ，按照上面赋值
    char *body = pPacket->m_body;
    int index = 0;
    //CodecID: 7 表示 AVC（4bit），与 frame type 组合起来刚好是 1 个字节  0x17
    body[index++] = 0x17;
    //fixed : 0x00 0x00 0x00 0x00 (4byte)
    body[index++] = 0x00;
    body[index++] = 0x00;
    body[index++] = 0x00;
    body[index++] = 0x00;
    //configurationVersion: (1byte) 0x01版本
    body[index++] = 0x01;
    //AVCProfileIndication : (1byte) sps[1] profile
    body[index++] = spsData[1];
    //profile_compatibility : (1byte) sps[2] profile compatibility
    body[index++] = spsData[2];
    //AVCLevelIndication : (1byte) sps[3] Profile level
    body[index++] = spsData[3];
    //lengthSizeMinusOne : (1byte) Oxff 包长数据所使用的字节数
    body[index++] = 0xFF;


    //sps + pps 的数据
    //sps number          (1byte) 0xe1 sps 个数
    body[index++] = 0xe1;
    //sps data length     (2byte) sps 长度(两字节分别存储高八位和低八位)
    body[index++] = (spsLen >> 8) & 0xFF;//高八位
    body[index++] = spsLen & 0xFF;//低八位
    //sps data                    sps 的内容
    memcpy(&body[index],spsData,spsLen);
    index += spsLen;
    //pps number          (1byte) 0x01 pps 个数
    body[index++] = 0x01;
    //pps data length     (2byte) pps 长度
    body[index++] = (ppsLen >> 8) & 0xFF;//高八位
    body[index++] = ppsLen & 0xFF;//低八位
    //pps data                    pps 的内容
    memcpy(&body[index],ppsData,ppsLen);

    //设置一些信息
    pPacket->m_headerType = RTMP_PACKET_SIZE_MEDIUM;
    pPacket->m_packetType = RTMP_PACKET_TYPE_VIDEO;
    pPacket->m_hasAbsTimestamp = 0;
    pPacket->m_nTimeStamp = 0;//SPS 和 PPS 的 m_nTimeStamp 固定为0
    pPacket->m_nBodySize = bodySize;
    pPacket->m_nChannel = 0x04;
    pPacket->m_nInfoField2 = mStreamId;

    pPacketQueue5->push(pPacket);
}

void DZLivePush::pushVideo(jbyte *videoData, jint dataLen, jboolean keyFrame) {
    //frame type : 1关键帧，2 非关键帧 (4bit)
    //CodecID ：7表示 AVC (4bit)，与 frame type 组合起来刚好是 1个字节 0x17
    //fixed: Ox01 0x00 Ox00 0x00 (4byte） Ox01 表示 NALU 单元
    //video data length   (4byte） video 长度
    //video data

    //数据的长度（大小）= dataLen + 9字节（头信息）
    int bodySize = dataLen + 9;
    //构建 RTMPPacket
    RTMPPacket *pPacket = (RTMPPacket *)malloc(sizeof(RTMPPacket));
    RTMPPacket_Alloc(pPacket,bodySize);
    RTMPPacket_Reset(pPacket);

    //构建 body ，按照上面赋值
    char *body = pPacket->m_body;
    int index = 0;
    //frame type : 1关键帧，2 非关键帧 (4bit)
    //CodecID: 7 表示 AVC（4bit），与 frame type 组合起来刚好是 1 个字节  0x17
    if(keyFrame)
    {
        body[index++] = 0x17;
    } else{
        //非关键帧
        body[index++] = 0x27;
    }
    //fixed: Ox01 0x00 Ox00 0x00 (4byte） Ox01 表示 NALU 单元
    body[index++] = 0x01;
    body[index++] = 0x00;
    body[index++] = 0x00;
    body[index++] = 0x00;
    //video data length   (4byte） video 长度
    body[index++] = (dataLen >> 24) & 0xFF;
    body[index++] = (dataLen >> 16) & 0xFF;
    body[index++] = (dataLen >> 8) & 0xFF;
    body[index++] = dataLen & 0xFF;
    //video data
    memcpy(&body[index],videoData,dataLen);

    //设置一些信息
    pPacket->m_headerType = RTMP_PACKET_SIZE_LARGE;
    pPacket->m_packetType = RTMP_PACKET_TYPE_VIDEO;
    pPacket->m_hasAbsTimestamp = 0;
    pPacket->m_nTimeStamp = RTMP_GetTime() - startTime;
    pPacket->m_nBodySize = bodySize;
    pPacket->m_nChannel = 0x04;
    pPacket->m_nInfoField2 = mStreamId;

    pPacketQueue5->push(pPacket);
}

void DZLivePush::pushAudio(jbyte *audioData, jint dataLen) {
    //2字节头信息
    //前四位表示音频数据格式 AAC 10 -> 1010 -> A
    //五六位表示采样率 0=5.5k 1=11k 2=22k 3(二进制11)=44k
    //七位表示采样的精度 0 = 8bits 1 = 16bits
    //八位表示音频类型 0 = mono 1 = stereo
    //我们这里算出来第7个字节是 OxAF -> 1010 11+1+1(上面的综合)
    //0x01 代表 aac 原𡜺数据

    //数据的长度（大小）= dataLen + 2字节（头信息）
    int bodySize = dataLen + 2;
    //构建 RTMPPacket
    RTMPPacket *pPacket = (RTMPPacket *)malloc(sizeof(RTMPPacket));
    RTMPPacket_Alloc(pPacket,bodySize);
    RTMPPacket_Reset(pPacket);

    //构建 body ，按照上面赋值
    char *body = pPacket->m_body;
    int index = 0;
    //我们这里算出来第7个字节是 OxAF -> 1010 1111(上面的综合)
    body[index++] = 0xAF;
    //0x01 代表 aac 原𡜺数据
    body[index++] = 0x01;

    //audio data
    memcpy(&body[index],audioData,dataLen);

    //设置一些信息
    pPacket->m_headerType = RTMP_PACKET_SIZE_LARGE;
    pPacket->m_packetType = RTMP_PACKET_TYPE_AUDIO;
    pPacket->m_hasAbsTimestamp = 0;
    pPacket->m_nTimeStamp = RTMP_GetTime() - startTime;
    pPacket->m_nBodySize = bodySize;
    pPacket->m_nChannel = 0x04;
    pPacket->m_nInfoField2 = mStreamId;

    pPacketQueue5->push(pPacket);

}

void DZLivePush::stop() {
    isPushing = false;
    // 唤醒阻塞在pop_timeout的线程！
    pPacketQueue5->signal_quit();
    pthread_join( initConnectTid,NULL);//等待线程安全退出
    LOGI("等待停止了")
}
