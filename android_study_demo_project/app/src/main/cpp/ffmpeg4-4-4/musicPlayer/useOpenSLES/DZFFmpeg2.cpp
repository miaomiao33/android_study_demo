//
// Created by Machenike on 2026/10/8.
//

#include "DZFFmpeg2.h"

DZFFmpeg2::DZFFmpeg2(DZJNICall2 *pJniCall, const char *url) {
    this->pJniCall = pJniCall;
    //复制一份 url，防止外面方法结束销毁了传入的url
    this->url = (char *)malloc(strlen(url) + 1);
    memcpy(this->url,url,strlen(url) + 1);
}

DZFFmpeg2::~DZFFmpeg2() {
    release();
}


void DZFFmpeg2::play() {
    if(pAudio != NULL)
    {
        //调用 Audio 进行音频播放
        pAudio->play();
    }

}

void DZFFmpeg2::callPlayerJniError(ThreadMode threadMode, int code, char *msg) {
    //失败了释放资源
    release();
    //回调给 Java 层调用
    pJniCall->callPlayerError(threadMode,code,msg);
}

void DZFFmpeg2::release() {

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

    avformat_network_deinit();

    if(url != NULL)
    {
        free(url);
        url = NULL;
    }
}

void *threadPrepare(void* context)
{
    DZFFmpeg2 *pFFmpeg = (DZFFmpeg2 *)context;
    pFFmpeg->prepare(THREAD_CHILD);

    return 0;
}

void DZFFmpeg2::prepare() {
    prepare(THREAD_MAIN);
}

void DZFFmpeg2::prepareAsync() {
    //创建一个线程去播放，多线程边解码边播放
    pthread_t prepareThreadT;
    //this：传给 threadPrepare 的参数，也就是当前 DZFFmpeg2 对象指针。在线程内部可以强转回来
    pthread_create(&prepareThreadT,NULL,threadPrepare,this);
    //把线程设置为 分离态 ,线程退出后自动释放线程资源，不需要 pthread_join 等待回收
    pthread_detach(prepareThreadT);
}

void DZFFmpeg2::prepare(ThreadMode threadMode) {
    av_register_all();
    avformat_network_init();

    //C++命名是format_open_input_ret,android命名格式：formatOpenInputRes
    int formatOpenInputRes = 0;
    int formatFindStreamInfoRes = 0;
    AVCodecParameters *pCodecParameters;
    AVCodec *pCodec;

    int codecParametersToContextRes = -1;
    int codecOpenRes = -1;

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
        callPlayerJniError(threadMode,formatFindStreamInfoRes,av_err2str(formatFindStreamInfoRes));
        return;
    }

    //查找音频流的packet
    int audioStreamIndex = av_find_best_stream(pFormatContext,AVMEDIA_TYPE_AUDIO,
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
    //初始化Audio
    pAudio = new DZAudio2(audioStreamIndex, pJniCall, pCodecContext, pFormatContext, swrContext);
    //------- 重采样 end -------
    //回调到 Java 层，通知准备好了
    pJniCall->callPlayerPrepared(threadMode);
}
