//
// Created by Machenike on 2026/10/14.
//

#include "DZMedia4.h"

DZMedia4::DZMedia4(int streamIndex, DZJNICall4 *pJniCall, DZPlayerStatus4 *pPlayerStatus) {
    this->streamIndex = streamIndex;
    this->pJniCall = pJniCall;
    this->pPlayerStatus = pPlayerStatus;
    pPacketQueue = new DZPacketQueue4();
}

DZMedia4::~DZMedia4() {
    release();
}

void DZMedia4::analysisStream(ThreadMode threadMode, AVFormatContext *pFormatContext) {
    publicAnalysisStream(threadMode,pFormatContext);
    privateAnalysisStream(threadMode,pFormatContext);
}

void DZMedia4::release() {
    if(pPacketQueue != NULL)
    {
        delete(pPacketQueue);
        pPacketQueue = NULL;
    }

    if(pCodecContext != NULL)
    {
        //这是主动alloc的AVCodecContext，所以需要释放
        //关闭一些流之类的
        avcodec_close(pCodecContext);
        //释放内存
        avcodec_free_context(&pCodecContext);
        pCodecContext = NULL;
    }
}

void DZMedia4::publicAnalysisStream(ThreadMode threadMode, AVFormatContext *pFormatContext) {

    //查找解码
    pCodecParameters = pFormatContext->streams[streamIndex]->codecpar;//声道，频率等参数
    AVCodec *pCodec = avcodec_find_decoder(pCodecParameters->codec_id);
    if(pCodec == NULL )
    {
        LOGE("codec find decoder error")
        callPlayerJniError(threadMode,CODEC_FIND_DECODER_ERROR_CODE,"codec find decoder error");
        return;
    }

    //打开解码器
    pCodecContext = avcodec_alloc_context3(pCodec);//初始化一个AVCodecContext
    if(pCodecContext == NULL )
    {
        LOGE("avcodec alloc context3 error")
        callPlayerJniError(threadMode,CODEC_ALLOC_CONTEXT_ERROR_CODE,"avcodec alloc context3 error");
        return;
    }
    //将编码器参数放入pCodecContext中
    int codecParametersToContextRes = avcodec_parameters_to_context(pCodecContext,pCodecParameters);
    if(codecParametersToContextRes < 0 )
    {
        LOGE("avcodec parameters to context error:%s",av_err2str(codecParametersToContextRes))
        callPlayerJniError(threadMode,codecParametersToContextRes,av_err2str(codecParametersToContextRes));
        return;
    }
    int codecOpenRes = avcodec_open2(pCodecContext,pCodec,NULL);
    if(codecOpenRes != 0 )
    {
        LOGE("codec audio open error:%s",av_err2str(codecOpenRes))
        callPlayerJniError(threadMode,codecOpenRes,av_err2str(codecOpenRes));
        return;
    }

    duration = pFormatContext->duration;//封装层总时长（容器整体时长）
    timeBase = pFormatContext->streams[streamIndex]->time_base;//单条流的时间基
}

void DZMedia4::callPlayerJniError(ThreadMode threadMode, int code, char *msg) {
    //失败了释放资源
    release();
    //回调给 Java 层调用
    pJniCall->callPlayerError(threadMode,code,msg);
}

