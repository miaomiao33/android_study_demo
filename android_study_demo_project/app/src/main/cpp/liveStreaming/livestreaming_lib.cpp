//
// Created by Machenike on 2026/6/23.
//
#include <jni.h>
#include <stdlib.h>
#include "../librtmp/rtmp.h"
#include "../headerFile/LogUtils.h"
#include "packt.h"

using namespace std;

Live *live = 0;//NULL即0

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_example_android_1study_1demo_1project_opencv_liveStreamingPush_ScreenLive_connect(
        JNIEnv *env, jobject thiz, jstring url_) {
    const char *url = env->GetStringUTFChars(url_,0);
    int ret = 0;
    do{
        live = (Live *)(malloc(sizeof(Live)));
        memset(live,0, sizeof(Live));

        //申请内存
        live->rtmp = RTMP_Alloc();
        //初始化
        RTMP_Init(live->rtmp);
        live->rtmp->Link.timeout = 10;
        //设置地址, rtmp://xxx
        ret = RTMP_SetupURL(live->rtmp,(char *)url);
        if(!ret)
        {
            LOGI("setup URL error:%s",url);
            break;
        }
        //开启输出模式，没有这一行就是拉流模式
        RTMP_EnableWrite(live->rtmp);
        //连接服务器
        ret = RTMP_Connect(live->rtmp,0);
        if(!ret)
        {
            LOGI("connect URL error:%s",url);
            break;
        }
        //连接流
        ret = RTMP_ConnectStream(live->rtmp,0);
        if(!ret)
        {
            LOGI("RTMP_ConnectStream URL error:%s",url);
            break;
        }
        LOGI("connect success");
    }while(0);//do while(0)可以快速跳出，并释放url

    env->ReleaseStringUTFChars(url_,url);
    return ret;
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_android_1study_1demo_1project_opencv_liveStreamingPush_ScreenLive_disConnect(
        JNIEnv *env, jobject thiz) {
    if(live)
    {
        if(live->rtmp)
        {
            RTMP_Close(live->rtmp);
            RTMP_Free(live->rtmp);
        }
        if(live->sps)
        {
            free(live->sps);
        }
        if(live->pps)
        {
            free(live->pps);
        }
        free(live);
        live = nullptr;
    }
}

int sendVideo(int8_t *buf,int len, long tms);
int sendAudio(int8_t *buf, int len, int type, long tms);
int sendPackage(RTMPPacket *packet);

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_example_android_1study_1demo_1project_opencv_liveStreamingPush_ScreenLive_sendData(
        JNIEnv *env, jobject thiz, jbyteArray data_, jint len, jint type, jlong tms) {
    jbyte *data = env->GetByteArrayElements(data_, NULL);
    int ret;
    switch (type)
    {
        case 0:
            ret = sendVideo(data,len,tms);
            break;
        default:
            ret = sendAudio(data,len,type,tms);
            break;
    }
    env->ReleaseByteArrayElements(data_,data,0);
    return ret;
}

//发送视频数据
int sendVideo(int8_t *buf,int len, long tms)
{
    //H264格式：起始码（32位）+NALU单元
    //NALU单元：NAL header（8位，低5位表示NALU类型） + EBSP
    int ret = 0;
    do{
        //buf的0、1、2、3都是起始码
        //H264中NAL Header 低五位表示类型，所以从buf[4]开始
        //取后五位：0x1F：0001 1111
        //7表示序列参数集，sps、pps
        //5：关键帧
        LOGI("onActivityResult buf[4]:%d",(buf[4] & 0x1F));
        if((buf[4] & 0x1F) == 7)
        {
            LOGI("onActivityResult prepareVideo");
            //sps+pps
            //在每次发送关键帧之前把sps和pps发出去，把sps和pps记录下来
            //sps和pps任一没有值
            if(live && (!live->sps || !live->pps))
            {
                //记录
                prepareVideo(buf,len,live);
            }
            //获取到sps和pps帧也是成功的
            ret = 1;
        } else{
            LOGI("onActivityResult != 7");
            if((buf[4] & 0x1F) == 5)
            {
                //关键帧
                //先把sps和pps打包发出去，再把关键帧打包发出去
                if(live && (live->sps && live->pps))
                {
                    LOGI("onActivityResult 关键帧，");
                    RTMPPacket *packet = createVideoPackage(live);
                    ret = sendPackage(packet);
                    LOGI("onActivityResult 关键帧sps ret，%d",ret);
                    //发送失败
                    if(!ret)
                    {
                        break;
                    }
                } else
                {
                    LOGI("SPS和pps缺失");
                }
            }
            //关键帧发完sps和pps就发送本身
            //非关键帧：直接打包发出去，不需要再发一次sps和pps
            RTMPPacket *packet = createVideoPackage(buf,len,tms,live);

            ret = sendPackage(packet);
            LOGI("onActivityResult native ret:%d",ret);
        }
    }while(0);

    return ret;
}

//发送音频数据
int sendAudio(int8_t *buf, int len, int type, long tms) {
    RTMPPacket *packet = createAudioPackage(buf,len,type,tms,live);
    return sendPackage(packet);
}

//使用rtmp发送封装好的包
int sendPackage(RTMPPacket *packet)
{
    int r = RTMP_SendPacket(live->rtmp, packet, 1);
    RTMPPacket_Free(packet);
    free(packet);
    return r;
}