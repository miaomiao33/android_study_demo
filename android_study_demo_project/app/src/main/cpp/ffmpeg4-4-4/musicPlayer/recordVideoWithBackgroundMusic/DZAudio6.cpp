//
// Created by Machenike on 2026/10/10.
//

#include "DZAudio6.h"

void playerCallback6(SLAndroidSimpleBufferQueueItf caller,void *pContext);

DZAudio6::DZAudio6(int audioStreamIndex, DZJNICall6 *pJniCall, AVFormatContext *pFormatContext) {
    this->audioStreamIndex = audioStreamIndex;
    this->pJniCall = pJniCall;
    this->pFormatContext = pFormatContext;
    pPacketQueue = new DZPacketQueue6();
    pPlayerStatus = new DZPlayerStatus6();
    pFrame = av_frame_alloc(); // 只分配一次！
}

DZAudio6::~DZAudio6() {
    release();
}
//一个线程去播放
void *threadPlay6(void* context)
{
    DZAudio6 *pAudio = (DZAudio6 *)context;
    //初始化 OpenSLES
    pAudio->initCreateOpenSLES6();

    return 0;
}

//一个线程去解码
void *threadReadPacket6(void* context)
{
    DZAudio6 *pAudio = (DZAudio6 *)context;

    while (pAudio->pPlayerStatus != NULL && !pAudio->pPlayerStatus->isExit)
    {
        AVPacket *pPacket = av_packet_alloc();

        if(av_read_frame(pAudio->pFormatContext,pPacket) >= 0)
        {
            if(pPacket->stream_index == pAudio->audioStreamIndex)
            {
                //音频
                pAudio->pPacketQueue->push(pPacket);
            } else{
                //视频
                //1、解引用数据 data，释放 pPacket 结构体内存，3、pPacket = NULL
                av_packet_free(&pPacket);
            }
        } else{
            // 文件读取结束
            //1、解引用数据 data，释放 pPacket 结构体内存，3、pPacket = NULL
            av_packet_free(&pPacket);
            //解码完了，睡眠一下，尽量不去消耗CPU的资源，也可以退出销毁这个线程
            break;
        }
    }

    return 0;
}

void DZAudio6::play() {
    //一个线程去读 Packet
    readPacketThreadT;
    pthread_create(&readPacketThreadT,NULL,threadReadPacket6,this);
//    pthread_detach(readPacketThreadT);//自己手动释放的

    //一个线程去解码播放
    //创建一个线程去播放，多线程边解码边播放
    pthread_t playThreadT;
    //this：传给 threadPlay 的参数，也就是当前 DZFFmpeg6 对象指针。在线程内部可以强转回来
    pthread_create(&playThreadT,NULL,threadPlay6,this);
    //把线程设置为 分离态 ,线程退出后自动释放线程资源，不需要 pthread_join 等待回收
    pthread_detach(playThreadT);
}


//去队列解码一帧
int DZAudio6::resampleAudio6()
{
    int dataSize = 0;
    //不断读取压缩数据并解码成 PCM 数据
    while(pPlayerStatus != NULL && !pPlayerStatus->isExit)
    {
        //最多等待20ms，拿不到包直接返回nullptr，不永久卡住音频线程
        AVPacket *pPacket = pPacketQueue->pop_timeout(20);
        if(pPacket == nullptr)
        {
            LOGI("队列空，无音频包");
            // 队列空，没有数据，直接返回0，OpenSL 会填充静音,等待 OpenSL 下次循环
            av_frame_unref(pFrame);
            return 0;
        }
        LOGI("拿到音频packet");
        
        //音频
        //Packet 包，压缩的数据，解码成 PCM 数据
        int codecSendPacketRes = avcodec_send_packet(pCodecContext,pPacket);

        // packet使命完成！立刻在当前轮次内完整释放，不要跨轮持有！
        av_packet_unref(pPacket);   // 释放内部音频data
        av_packet_free(&pPacket);    // 释放AVPacket结构体本身
        pPacket = nullptr;

        // send失败（EAGAIN/错误），继续循环取下一个包
        if(codecSendPacketRes != 0)
        {
            av_frame_unref(pFrame);
            continue;
        } else{
            int codecReceiveFrameRes = avcodec_receive_frame(pCodecContext,pFrame);
            if(codecReceiveFrameRes != 0)
            {
                // EAGAIN：解码器内部还没攒够一帧，继续循环喂下一个packet
                av_frame_unref(pFrame);
                continue;
            } else
            {
                // AVPacket -> AVFrame
                LOGI("解码音频帧");

                //调用重采样方法(将原始数据重采样放入resampleOutBuffer中)，返回值是返回重采样的个数，也就是pFrame->nb_samples
                dataSize = swr_convert(pSwrContext,&resampleOutBuffer,pFrame->nb_samples,
                                       (const uint8_t**)pFrame->data,pFrame->nb_samples);
                LOGI("解码音频帧：%d，%d",dataSize,pFrame->nb_samples);
                //这里使用 dataSize 作为数组长，此时dataSize = pFrame->nb_samples，故还需要* 2 * 2
                dataSize = dataSize * 2 * 2;

                //解码之后开始播放
                //write 写到缓冲区 pFrame，data -> java byte
                //size 是多大，装 PCM 的数据
                //1s（1秒）：44100采样点 2通道 2字节，总的 44100 * 2 * 2
                //1帧不是一秒，pFrame->nb_samples点

                // 释放当前frame引用，退出循环，把PCM喂给OpenSL
                av_frame_unref(pFrame);
                break;
            }
        }
//        //解引用(让这一块结构体不指向 data 内存区域，这样可以重复利用 pPacket 等结构体)
//        av_packet_unref(pPacket);
//        av_frame_unref(pFrame);
//        pPacket = nullptr; //清空本地指针，防止重复操作
    }

//    // 不能在这里av_packet_free！packet结构体所有权属于PacketQueue
//    // 只负责 av_packet_unref释放packet里面的data缓冲区
//    //释放掉结构体内存
//    //1、解引用数据 data，释放 pPacket 结构体内存，3、pPacket = NULL
//    av_packet_free(&pPacket);
//    av_frame_free(&pFrame);
//    pPacket = NULL;
//    pFrame = NULL;

    return dataSize;
}

//初始化OpenSLES，并进行播放
void DZAudio6::initCreateOpenSLES6() {
    //3.1创建引擎接口对象(设置参数，实现，获取接口)
    SLObjectItf engineObject = NULL;
    SLEngineItf engineEngine;
    slCreateEngine(&engineObject,0,NULL,0,
                   NULL,NULL);
    //realize the engine
    (*engineObject)->Realize(engineObject, SL_BOOLEAN_FALSE);
    //get the engine interface,which is needed in order to create other object
    (*engineObject)->GetInterface(engineObject,SL_IID_ENGINE,&engineEngine);
    //3.2设置混音器
    static SLObjectItf outputMixObject = NULL;
    const SLInterfaceID ids[1] = {SL_IID_ENVIRONMENTALREVERB};
    const SLboolean req[1] = {SL_BOOLEAN_FALSE};
    (*engineEngine)->CreateOutputMix(engineEngine, &outputMixObject,1,ids,req);//1表示一个接口

    (*outputMixObject)->Realize(outputMixObject, SL_BOOLEAN_FALSE);
    SLEnvironmentalReverbItf outputMixEnvironmentalReverb = NULL;
    (*outputMixObject)->GetInterface(outputMixObject,SL_IID_ENVIRONMENTALREVERB,&outputMixEnvironmentalReverb);
    SLEnvironmentalReverbSettings reverbSettings = SL_I3DL2_ENVIRONMENT_PRESET_STONECORRIDOR;
    (*outputMixEnvironmentalReverb)->SetEnvironmentalReverbProperties(outputMixEnvironmentalReverb,
                                                                      &reverbSettings);
    //3.3创建播放器
    SLObjectItf pPlayer = NULL;

    SLDataLocator_AndroidSimpleBufferQueue simpleBufferQueue = {
            SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE,2};
    SLDataFormat_PCM formatPcm = {
            SL_DATAFORMAT_PCM,
            2,//2通道
            SL_SAMPLINGRATE_44_1,//44100
            SL_PCMSAMPLEFORMAT_FIXED_16,
            SL_PCMSAMPLEFORMAT_FIXED_16,
            SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT,
            SL_BYTEORDER_LITTLEENDIAN};
    SLDataSource audioSrc = {&simpleBufferQueue,&formatPcm};

    SLDataLocator_OutputMix outputMix = {SL_DATALOCATOR_OUTPUTMIX,outputMixObject};
    SLDataSink audioSnk = {&outputMix,NULL};

    const SLInterfaceID interfaceIds[3] = {SL_IID_BUFFERQUEUE,SL_IID_VOLUME,SL_IID_PLAYBACKRATE};
    const SLboolean interfaceRequired[3] = {SL_BOOLEAN_TRUE,SL_BOOLEAN_TRUE,SL_BOOLEAN_TRUE};

    (*engineEngine)->CreateAudioPlayer(engineEngine,&pPlayer,&audioSrc,&audioSnk,3,
                                       interfaceIds,interfaceRequired);

    pPlayItf  = NULL;
    (*pPlayer)->Realize(pPlayer,SL_BOOLEAN_FALSE);
    (*pPlayer)->GetInterface(pPlayer,SL_IID_PLAY,&pPlayItf);
    //3.4设置缓存队列和回调函数
    SLAndroidSimpleBufferQueueItf playerBufferQueue;
    (*pPlayer)->GetInterface(pPlayer,SL_IID_BUFFERQUEUE,&playerBufferQueue);
    //每次回调 this 会被带给 playerCallback6 里面的 context
    (*playerBufferQueue)->RegisterCallback(playerBufferQueue,playerCallback6,this);
    //3.5设置播放状态
    (*pPlayItf)->SetPlayState(pPlayItf, SL_PLAYSTATE_PLAYING);
    //3.6调用回调函数
    playerCallback6(playerBufferQueue,this);
}

void DZAudio6::analysisStream(ThreadMode threadMode, AVStream **streams) {

    //查找解码
    AVCodecParameters *pCodecParameters = pFormatContext->streams[audioStreamIndex]->codecpar;//声道，频率等参数
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
    int codecParametersToContextRes = avcodec_parameters_to_context(pCodecContext,pCodecParameters);//将编码器参数放入pCodecContext中
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
     pSwrContext = swr_alloc_set_opts(NULL ,out_ch_layout, out_sample_fmt, out_sample_rate,
                                    in_ch_layout,  in_sample_fmt, in_sample_rate,0,NULL);
    if(pSwrContext == NULL){
        // 提示错误
        callPlayerJniError(threadMode,SWR_ALLOC_SET_OPTS_ERROR_CODE,"swr alloc set opts error");
        return;
    }
    int swrInitRes = swr_init(pSwrContext);
    if(swrInitRes < 0)
    {
        callPlayerJniError(threadMode,SWR_CONTEXT_INIT_ERROR_CODE,"swr context init error");
        return;
    }

    resampleOutBuffer = (uint8_t *)malloc(pCodecParameters->frame_size * 2 * 2);

    //回调给Java，音频信息
    pJniCall->callMusicInfo(threadMode, AUDIO_SIMPLE_RATE,2);
    //------- 重采样 end -------
}


void DZAudio6::callPlayerJniError(ThreadMode threadMode, int code, char *msg) {
    //失败了释放资源
    release();
    //回调给 Java 层调用
    pJniCall->callPlayerError6(threadMode,code,msg);
}

void DZAudio6::release() {
    if(pPacketQueue != NULL)
    {
        delete(pPacketQueue);
        pPacketQueue = NULL;
    }
    if(resampleOutBuffer != NULL)
    {
        free(resampleOutBuffer);
        resampleOutBuffer = NULL;
    }
    if(pPlayerStatus != NULL)
    {
        delete(pPlayerStatus);
        pPlayerStatus = NULL;
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

    if(pSwrContext != NULL)
    {
        swr_free(&pSwrContext);
       // free(pSwrContext); //swr_free已经释放内存
        pSwrContext = NULL;
    }

    //释放AVFrame
    if(pFrame != nullptr)
    {
        av_frame_free(&pFrame);
        pFrame = nullptr;
    }
}

//最好是 单独开一个解码线程预解码 PCM 放到 PCM 环形 buffer，OpenSL 回调只读取 PCM，完全不碰 packet 队列和解码器。
void playerCallback6(SLAndroidSimpleBufferQueueItf caller,void *pContext){
    DZAudio6 *pAudio = (DZAudio6 *)pContext;
    //去队列解码一帧
    int dataSize = pAudio->resampleAudio6();
    //把 pcm 的数据回调到 java 层，方便做一些其他的需求
    pAudio->pJniCall->callCallbackPcm(THREAD_CHILD,pAudio->resampleOutBuffer,dataSize);
    //放入缓冲区
    (*caller)->Enqueue(caller,pAudio->resampleOutBuffer,dataSize);
}

void DZAudio6::stop() {
    if(!pPlayerStatus->isExit)
    {
        pPlayerStatus->isExit = true;

        //设置播放状态为停止
        (*pPlayItf)->SetPlayState(pPlayItf, SL_PLAYSTATE_STOPPED);

        pthread_join(readPacketThreadT,NULL);
        LOGI("等待退出了")
    }
}
