//
// Created by Machenike on 2026/10/3.
//

#include "music_player.h"
#include <jni.h>

extern "C"
JNIEXPORT void JNICALL
Java_com_example_android_1study_1demo_1project_opencv_ffmpegUsage_MusicPlayer_DarrenPlayer_nPlay(
        JNIEnv *env, jobject thiz, jstring url_) {

    const char *url = (*env).GetStringUTFChars(url_,JNI_FALSE);

    av_register_all();

    avformat_network_init();

    AVFormatContext *pFormatContext = NULL;
    //C++命名是format_open_input_ret,android命名格式：formatOpenInputRes
    int formatOpenInputRes = 0;
    int formatFindStreamInfoRes = 0;
    int audioStreamIndex = -1;
    AVCodecParameters *pCodecParameters;
    AVCodec *pCodec;
    AVCodecContext *pCodecContext;
    int codecParametersToContextRes = -1;
    int codecOpenRes = -1;
    AVPacket *pPacket = NULL;
    AVFrame *pFrame = NULL;
    int index = 0;

    formatOpenInputRes = avformat_open_input(&pFormatContext,url,NULL,NULL);
    if(formatOpenInputRes != 0)
    {
        //失败了
        //第一件事，需要回调给 Java 层
        //第二件事，需要释放资源
        //return;
        LOGE("format open input error:%s",av_err2str(formatOpenInputRes))
        goto _av_resource_destroy;
    }
    formatFindStreamInfoRes = avformat_find_stream_info(pFormatContext,NULL);
    if(formatFindStreamInfoRes < 0 )
    {
        LOGE("format find stream info error:%s",av_err2str(formatFindStreamInfoRes))
        //不推荐这样写，但是的确很方便
        goto _av_resource_destroy;
    }

    //查找音频流的packet
    audioStreamIndex = av_find_best_stream(pFormatContext,AVMEDIA_TYPE_AUDIO,
                                           -1,-1,NULL,0);
    if(audioStreamIndex < 0)
    {
        LOGE("format audio stream error:%s",av_err2str(audioStreamIndex))
        //不推荐这样写，但是的确很方便
        goto _av_resource_destroy;
    }

    //查找解码
    pCodecParameters = pFormatContext->streams[audioStreamIndex]->codecpar;//声道，频率等参数
    pCodec = avcodec_find_decoder(pCodecParameters->codec_id);
    if(pCodec == NULL )
    {
        LOGE("codec find decoder error")
        //不推荐这样写，但是的确很方便
        goto _av_resource_destroy;
    }

    //打开解码器
    pCodecContext = avcodec_alloc_context3(pCodec);//初始化一个AVCodecContext
    if(pCodecContext == NULL )
    {
        LOGE("avcodec alloc context3 error")
        //不推荐这样写，但是的确很方便
        goto _av_resource_destroy;
    }
    codecParametersToContextRes = avcodec_parameters_to_context(pCodecContext,pCodecParameters);//将编码器参数放入pCodecContext中
    if(codecParametersToContextRes < 0 )
    {
        LOGE("avcodec parameters to context error:%s",av_err2str(codecParametersToContextRes))
        //不推荐这样写，但是的确很方便
        goto _av_resource_destroy;
    }
    codecOpenRes = avcodec_open2(pCodecContext,pCodec,NULL);
    if(codecOpenRes != 0 )
    {
        LOGE("codec audio open error:%s",av_err2str(codecOpenRes))
        //不推荐这样写，但是的确很方便
        goto _av_resource_destroy;
    }

    LOGI("sample_rate：%d,channels：%d",pCodecParameters->sample_rate,pCodecParameters->channels);
    pPacket = av_packet_alloc();
    pFrame = av_frame_alloc();
    while(av_read_frame(pFormatContext,pPacket) >= 0)
    {
        if(pPacket->stream_index == audioStreamIndex)
        {
            //音频
            //Packet 包，压缩的数据，解码成 PCM 数据
            int codecSendPacketRes = avcodec_send_packet(pCodecContext,pPacket);
            if(codecSendPacketRes == 0)
            {
                int codecReceiveFrameRes = avcodec_receive_frame(pCodecContext,pFrame);
                if(codecReceiveFrameRes == 0)
                {
                    // AVPacket -> AVFrame
                    index++;
                    LOGI("解码第 %d 帧", index);
                }
            }
        } else{
            //视频
        }

        //解引用(让这一块结构体不指向 data 内存区域，这样可以重复利用 pPacket 等结构体)
        av_packet_unref(pPacket);
        av_frame_unref(pFrame);
    }

    //释放掉结构体内存
    //1、解引用数据 data，释放 pPacket 结构体内存，3、pPacket = NULL
    av_packet_free(&pPacket);
    av_frame_free(&pFrame);

    pPacket = NULL;
    pFrame = NULL;


    //失败后释放
    _av_resource_destroy:
    if(pCodecContext != NULL)
    {
        //这是主动alloc的AVCodecContext，所以需要释放
        //关闭一些流之类的
        avcodec_close(pCodecContext);
        //释放内存
        avcodec_free_context(&pCodecContext);
        pCodecContext = NULL;
    }
    if(pFormatContext != NULL)
    {
        //关闭一些流之类的
        avformat_close_input(&pFormatContext);
        //释放内存
        avformat_free_context(pFormatContext);
        pFormatContext = NULL;
    }
    avformat_network_deinit();

    (*env).ReleaseStringUTFChars(url_,url);
}