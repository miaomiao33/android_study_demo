//
// Created by Machenike on 2026/6/25.
//

#ifndef ANDROID_STUDY_DEMO_PROJECT_PACKT_H
#define ANDROID_STUDY_DEMO_PROJECT_PACKT_H

#include <cstdlib>
#include "../librtmp/rtmp.h"

typedef struct {
    int16_t sps_len;
    int16_t pps_len;
    int8_t *sps;
    int8_t *pps;
    RTMP *rtmp;
} Live;

//把sps和pps封包
RTMPPacket *createVideoPackage(Live *live)
{
    int body_size = 13 + live->sps_len + 3 + live->pps_len;
    RTMPPacket *packet = (RTMPPacket *)malloc(sizeof(RTMPPacket));
    RTMPPacket_Alloc(packet, body_size);
    int i = 0;
    //AVC sequence header 与IDR一样
    packet->m_body[i++] = 0x17;
    //AVC sequence header 设置为0x00
    packet->m_body[i++] = 0x00;
    //CompositionTime
    packet->m_body[i++] = 0x00;
    packet->m_body[i++] = 0x00;
    packet->m_body[i++] = 0x00;
    //AVC sequence header
    packet->m_body[i++] = 0x01;//configurationVersion 版本号 1
    packet->m_body[i++] = live->sps[1];//profile 如baseline、main、high

    packet->m_body[i++] = live->sps[2];//profile_compatibility 兼容性
    packet->m_body[i++] = live->sps[3];//profile level
    packet->m_body[i++] = 0xFF;//reserved(1111 11) + lengthSizeMinusOne (2 位 nal 长度) 总是0xFF
    //sps
    packet->m_body[i++] = 0xE1;//reserved(111) + lengthSizeMinusOne (5 位 sps 个数) 总是0xE1
    //sps length 2字节
    packet->m_body[i++] = (live->sps_len >> 8) & 0xff;//第0个字节
    packet->m_body[i++] = live->sps_len & 0xff;       //第1个字节
    memcpy(&packet->m_body[i], live->sps, live->sps_len);
    i += live->sps_len;

    /*pps*/
    packet->m_body[i++] = 0x01;//pps number
    //pps length
    packet->m_body[i++] = (live->pps_len >> 8) & 0xff;//第0个字节
    packet->m_body[i++] = live->pps_len & 0xff;       //第1个字节
    memcpy(&packet->m_body[i], live->pps, live->pps_len);

    //视频类型
    packet->m_packetType = RTMP_PACKET_TYPE_VIDEO;
    packet->m_nBodySize = body_size;
    packet->m_nChannel = 0x04;
    packet->m_nTimeStamp = 0;
    packet->m_hasAbsTimestamp = 0;
    packet->m_headerType = RTMP_PACKET_SIZE_LARGE;
    packet->m_nInfoField2 = live->rtmp->m_stream_id;

    return packet;
}

//video I帧和其他帧的封包
RTMPPacket *createVideoPackage(int8_t *buf,int len, const long tms,Live *live)
{
    buf += 4;
    len -= 4;
    int body_size = len + 9;
    RTMPPacket *packet = (RTMPPacket *)malloc(sizeof(RTMPPacket));
    RTMPPacket_Alloc(packet,len + 9);

    packet->m_body[0] = 0x27;//非关键帧
    //关键帧
    if((buf[0] & 0x1F) == 5)
    {
        packet->m_body[0] = 0x17;
        LOGI("发送关键帧数据 data");
    }

    packet->m_body[1] = 0x01;
    packet->m_body[2] = 0x00;
    packet->m_body[3] = 0x00;
    packet->m_body[4] = 0x00;

    //长度
    //拿出int数据的4个字节数据
    packet->m_body[5] = (len >> 24) & 0xFF;
    packet->m_body[6] = (len >> 16) & 0xFF;
    packet->m_body[7] = (len >> 8) & 0xFF;
    packet->m_body[8] = (len ) & 0xFF;

    //数据
    memcpy(&packet->m_body[9], buf, len);


    packet->m_packetType = RTMP_PACKET_TYPE_VIDEO;
    packet->m_nBodySize = body_size;
    packet->m_nChannel = 0x04;
    packet->m_nTimeStamp = tms;
    packet->m_hasAbsTimestamp = 0;
    packet->m_headerType = RTMP_PACKET_SIZE_LARGE;
    packet->m_nInfoField2 = live->rtmp->m_stream_id;
    return packet;
}

//音频封包
RTMPPacket *createAudioPackage(int8_t *buf,const int len, const int type,const long tms,
                               Live *live)
{
    int body_size = len + 2;
    RTMPPacket *packet = (RTMPPacket *)malloc(sizeof(RTMPPacket));
    RTMPPacket_Alloc(packet,body_size);

    packet->m_body[0] = 0xAF;//m_body每个元素为1byte，8位
    //type == 1：声音的解码信息
    if(type == 1)
    {
        packet->m_body[1] = 0x00;
    } else
    {
        //type == 2：声音数据
        packet->m_body[1] = 0x01;
    }
    memcpy(&packet->m_body[2], buf, len);

    packet->m_packetType = RTMP_PACKET_TYPE_AUDIO;
    packet->m_nBodySize = body_size;
    packet->m_nChannel = 0x05;
    packet->m_nTimeStamp = tms;
    packet->m_hasAbsTimestamp = 0;
    packet->m_headerType = RTMP_PACKET_SIZE_LARGE;
    packet->m_nInfoField2 = live->rtmp->m_stream_id;
    return packet;
}

//把data中的sps和pps分离出来并存储在live中
void prepareVideo(int8_t *data,int len,Live *live)
{
    for (int i = 0; i < len; i++) {
        //0x00 0x00 0x00 0x01
        if(i + 4 < len)
        {
            if(data[i] == 0x00
               && data[i+1] == 0x00
               && data[i+2] == 0x00
               && data[i+3] == 0x01)
            {
                //0x00 0x00 0x00 0x01 7 sps 0x00 0x00 0x00 0x01 8 sps
                //将sps pps分开
                //找到pps
//                if(data[i+4] == 0x68)
                if((data[i+4] & 0x1F) == 8)
                {
                    //去掉界定符
                    //sps解析
                    live->sps_len = i - 4;
                    live->sps = static_cast<int8_t *>(malloc(live->sps_len));
                    memcpy(live->sps, data + 4,live->sps_len);

                    //pps解析
                    live->pps_len = i - 4;
                    live->pps = static_cast<int8_t *>(malloc(live->pps_len));
                    memcpy(live->pps, data + 4 + live->sps_len + 4,live->sps_len);
                    LOGI("onActivityResult sps:%d pps: %d",live->sps_len,live->pps_len);
                    break;
                }
            }
        }
    }
}


#endif //ANDROID_STUDY_DEMO_PROJECT_PACKT_H
