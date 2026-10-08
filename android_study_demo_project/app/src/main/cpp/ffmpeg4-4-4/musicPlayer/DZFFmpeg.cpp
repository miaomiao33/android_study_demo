//
// Created by Machenike on 2026/10/8.
//

#include "DZFFmpeg.h"
#include "music_player.h"

DZFFmpeg::DZFFmpeg(DZJNICall *pJniCall, const char *url) {
    this->pJniCall = pJniCall;
    //复制一份 url，防止外面方法结束销毁了传入的url
    this->url = (char *)malloc(strlen(url) + 1);
    memcpy(this->url,url,strlen(url) + 1);
}

DZFFmpeg::~DZFFmpeg() {
    release();
}

void *threadPlay(void* context)
{
    DZFFmpeg *pFFmpeg = (DZFFmpeg *)context;
    pFFmpeg->prepare(THREAD_CHILD);

    return 0;
}

void DZFFmpeg::play() {
    //创建一个线程去播放，多线程边解码边播放
    pthread_t playThreadT;
    //this：传给 threadPlay 的参数，也就是当前 DZFFmpeg 对象指针。在线程内部可以强转回来
    pthread_create(&playThreadT,NULL,threadPlay,this);
    //把线程设置为 分离态 ,线程退出后自动释放线程资源，不需要 pthread_join 等待回收
    pthread_detach(playThreadT);

}

void DZFFmpeg::callPlayerJniError(ThreadMode threadMode,int code, char *msg) {
    //失败了释放资源
    release();
    //回调给 Java 层调用
    pJniCall->callPlayerError(threadMode,code,msg);
}

void DZFFmpeg::release() {

    //失败后释放
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

    if(swrContext != NULL)
    {
        swr_free(&swrContext);
        free(swrContext);
        swrContext = NULL;
    }

    if(resampleOutBuffer != NULL)
    {
        free(resampleOutBuffer);
        resampleOutBuffer = NULL;
    }
    avformat_network_deinit();

    if(url != NULL)
    {
        free(url);
        url = NULL;
    }
}

void DZFFmpeg::prepare() {

}

void DZFFmpeg::prepareAsync() {

}

void DZFFmpeg::prepare(ThreadMode threadMode) {

    av_register_all();
    avformat_network_init();

    //C++命名是format_open_input_ret,android命名格式：formatOpenInputRes
    int formatOpenInputRes = 0;
    int formatFindStreamInfoRes = 0;
    int audioStreamIndex = -1;
    AVCodecParameters *pCodecParameters;
    AVCodec *pCodec;

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
//        goto _av_resource_destroy;
        callPlayerJniError(threadMode,formatOpenInputRes,av_err2str(formatOpenInputRes));
        return;
    }
    formatFindStreamInfoRes = avformat_find_stream_info(pFormatContext,NULL);
    if(formatFindStreamInfoRes < 0 )
    {
        LOGE("format find stream info error:%s",av_err2str(formatFindStreamInfoRes))
        //不推荐这样写，但是的确很方便
//        goto _av_resource_destroy;
        callPlayerJniError(threadMode,formatFindStreamInfoRes,av_err2str(formatFindStreamInfoRes));
        return;
    }

    //查找音频流的packet
    audioStreamIndex = av_find_best_stream(pFormatContext,AVMEDIA_TYPE_AUDIO,
                                           -1,-1,NULL,0);
    if(audioStreamIndex < 0)
    {
        LOGE("format audio stream error:%s",av_err2str(audioStreamIndex))

        callPlayerJniError(threadMode,FIND_STREAM_ERROR_CODE,"format audio stream error");
        return;
    }

    //查找解码
    pCodecParameters = pFormatContext->streams[audioStreamIndex]->codecpar;//声道，频率等参数
    pCodec = avcodec_find_decoder(pCodecParameters->codec_id);
    if(pCodec == NULL )
    {
        LOGE("codec find decoder error")
        //不推荐这样写，但是的确很方便
//        goto _av_resource_destroy;
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
    codecParametersToContextRes = avcodec_parameters_to_context(pCodecContext,pCodecParameters);//将编码器参数放入pCodecContext中
    if(codecParametersToContextRes < 0 )
    {
        LOGE("avcodec parameters to context error:%s",av_err2str(codecParametersToContextRes))
        callPlayerJniError(threadMode,codecParametersToContextRes,av_err2str(codecParametersToContextRes));
        return;
    }
    codecOpenRes = avcodec_open2(pCodecContext,pCodec,NULL);
    if(codecOpenRes != 0 )
    {
        LOGE("codec audio open error:%s",av_err2str(codecOpenRes))
        callPlayerJniError(threadMode,codecOpenRes,av_err2str(codecOpenRes));
        return;
    }

    LOGI("sample_rate：%d,channels：%d",pCodecParameters->sample_rate,pCodecParameters->channels);


    pPacket = av_packet_alloc();
    pFrame = av_frame_alloc();

    //------- 重采样 start -------
    //struct SwrContext *swr_alloc_set_opts(struct SwrContext *s,
    //                                      int64_t out_ch_layout, enum AVSampleFormat out_sample_fmt, int out_sample_rate,
    //                                      int64_t  in_ch_layout, enum AVSampleFormat  in_sample_fmt, int  in_sample_rate,
    //                                      int log_offset, void *log_ctx);
    //输出
    int64_t out_ch_layout = AV_CH_LAYOUT_STEREO;
    enum AVSampleFormat out_sample_fmt = AVSampleFormat::AV_SAMPLE_FMT_S16;
    int out_sample_rate = AUDIO_SIMPLE_RATE;
    //输入
    int64_t  in_ch_layout = pCodecContext->channels;
    enum AVSampleFormat  in_sample_fmt = pCodecContext->sample_fmt;
    int  in_sample_rate = pCodecContext->sample_rate;
    //设置参数并创建SwrContext
    swrContext = swr_alloc_set_opts(NULL ,out_ch_layout, out_sample_fmt, out_sample_rate,
                                    in_ch_layout,  in_sample_fmt, in_sample_rate,0,NULL);
    if(swrContext == NULL){
        // 提示错误
        callPlayerJniError(threadMode,SWR_ALLOC_SET_OPTS_ERROR_CODE,"swr alloc set opts error");
        return;
    }
    int swrInitRes = swr_init(swrContext);
    if(swrInitRes < 0)
    {
        callPlayerJniError(threadMode,SWR_CONTEXT_INIT_ERROR_CODE,"swr context init error");
        return;
    }
    //size 是播放指定的大小，是最终输出的大小
    int outChannels = av_get_channel_layout_nb_channels(out_ch_layout);
    int dataSize = av_samples_get_buffer_size(NULL,outChannels,
                                              pCodecParameters->frame_size,
                                              out_sample_fmt,0);
    resampleOutBuffer = (uint8_t *)malloc(dataSize);
    //------- 重采样 end -------


    //pJniCall->jniEnv: 主线程的，如果是子线程需要 attach，因为当前的prepare()是开启的一个子线程操作的，下面是例子
//    if(threadMode == THREAD_MAIN)
//    {
//        jbyteArray jPcmByteArray = pJniCall->jniEnv->NewByteArray(dataSize);
//    } else if(threadMode == THREAD_CHILD){
//        //获取当前线程的 JNIEnv，通过 JavaVM 的 AttachCurrentThread 创建自己的 JNIEnv
//        JNIEnv *env;
//        if(pJniCall->javaVm->AttachCurrentThread(&env,0) != JNI_OK)
//        {
//            LOGE("get child thread jniEnv error!");
//            return;
//        }
//        jbyteArray jPcmByteArray = pJniCall->jniEnv->NewByteArray(dataSize);
//        pJniCall->javaVm->DetachCurrentThread();
//    }
    /// 防止内存使用过大写法(注意更换av_samples_get_buffer_size参数取值，pFrame没有数值)
    jbyteArray jPcmByteArray = pJniCall->jniEnv->NewByteArray(dataSize);
    //同步数据到Java中
    //native 创建 C 数组
    jbyte *jPcmData = pJniCall->jniEnv->GetByteArrayElements(jPcmByteArray,NULL);
    ///

    //不断读取压缩数据并解码成 PCM 数据
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

                    //调用重采样方法(将原始数据重采样放入resampleOutBuffer中)
                    swr_convert(swrContext,&resampleOutBuffer,pFrame->nb_samples,
                                (const uint8_t**)pFrame->data,pFrame->nb_samples);

                    //解码之后开始播放
                    //write 写到缓冲区 pFrame，data -> java byte
                    //size 是多大，装 PCM 的数据
                    //1s（1秒）：44100采样点 2通道 2字节，总的 44100 * 2 * 2
                    //1帧不是一秒，pFrame->nb_samples点

                    memcpy(jPcmData, resampleOutBuffer,dataSize);//拷贝数据

                    // 0: 把 C 的数组的数据同步到 jbyteArray，然后释放 native 数组; JNI_COMMIT不释放，继续使用
                    pJniCall->jniEnv->ReleaseByteArrayElements(jPcmByteArray,jPcmData,JNI_COMMIT);

                    //TODO
                    pJniCall->callAudioTrackWrite(jPcmByteArray,0,dataSize);
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

    ///节约内存写法
    // 0: 把 C 的数组的数据同步到 jbyteArray，然后释放 native 数组
    pJniCall->jniEnv->ReleaseByteArrayElements(jPcmByteArray,jPcmData,0);
    //解除 jPcmByteArray 的持有，让 JavaGC 回收
    pJniCall->jniEnv->DeleteLocalRef(jPcmByteArray);
    ///

}

