//
// Created by Machenike on 2026/10/8.
//

#include "DZFFmpeg4.h"

DZFFmpeg4::DZFFmpeg4(DZJNICall4 *pJniCall, const char *url) {
    this->pJniCall = pJniCall;
    //复制一份 url，防止外面方法结束销毁了传入的url
    this->url = (char *)malloc(strlen(url) + 1);
    memcpy(this->url,url,strlen(url) + 1);
}

DZFFmpeg4::~DZFFmpeg4() {
    release();
}


void DZFFmpeg4::play() {
    if(pAudio != NULL)
    {
        //调用 Audio 进行音频播放
        pAudio->play();
    }

}


void DZFFmpeg4::release() {

    //失败后释放

    if(pFormatContext != NULL)
    {
        //关闭一些流之类的
        avformat_close_input(&pFormatContext);
        //释放内存
        avformat_free_context(pFormatContext);
        pFormatContext = NULL;
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
    DZFFmpeg4 *pFFmpeg = (DZFFmpeg4 *)context;
    pFFmpeg->prepare(THREAD_CHILD);

    return 0;
}

void DZFFmpeg4::prepare() {
    prepare(THREAD_MAIN);
}

void DZFFmpeg4::prepareAsync() {
    //创建一个线程去播放，多线程边解码边播放
    pthread_t prepareThreadT;
    //this：传给 threadPrepare 的参数，也就是当前 DZFFmpeg4 对象指针。在线程内部可以强转回来
    pthread_create(&prepareThreadT,NULL,threadPrepare,this);
    //把线程设置为 分离态 ,线程退出后自动释放线程资源，不需要 pthread_join 等待回收
    pthread_detach(prepareThreadT);
}

void DZFFmpeg4::prepare(ThreadMode threadMode) {
    av_register_all();
    avformat_network_init();

    //C++命名是format_open_input_ret,android命名格式：formatOpenInputRes
    int formatOpenInputRes = 0;
    int formatFindStreamInfoRes = 0;

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

    //不是我的事我不干，但是不要想的过于复杂
    pAudio = new DZAudio4(audioStreamIndex, pJniCall, pFormatContext);
    //初始化Audio
    pAudio->analysisStream(threadMode, pFormatContext->streams);
    //------- 重采样 end -------

    //回调到 Java 层，通知准备好了
    pJniCall->callPlayerPrepared(threadMode);
}
