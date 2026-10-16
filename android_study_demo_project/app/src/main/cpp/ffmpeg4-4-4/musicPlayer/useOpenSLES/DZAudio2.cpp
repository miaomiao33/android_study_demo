//
// Created by Machenike on 2026/10/10.
//

#include "DZAudio2.h"

void playerCallback2(SLAndroidSimpleBufferQueueItf caller,void *pContext);

DZAudio2::DZAudio2(int audioStreamIndex, DZJNICall2 *pJniCall, AVCodecContext *pCodecContext,
                   AVFormatContext *pFormatContext, SwrContext *pSwrContext) {
    this->audioStreamIndex = audioStreamIndex;
    this->pJniCall = pJniCall;
    this->pCodecContext = pCodecContext;
    this->pFormatContext = pFormatContext;
    this->pSwrContext = pSwrContext;
    resampleOutBuffer = (uint8_t *)malloc(pCodecContext->frame_size * 2 * 2);
}

void *threadPlay2(void* context)
{
    //当前是子线程内
    DZAudio2 *pDZAudio = (DZAudio2 *)context;
    //初始化 OpenSLES
    pDZAudio->initCreateOpenSLES2();

    return 0;
}

void DZAudio2::play() {
    //创建一个线程去播放，多线程边解码边播放
    pthread_t playThreadT;
    //this：传给 threadPlay 的参数，也就是当前 DZFFmpeg2 对象指针。在线程内部可以强转回来
    pthread_create(&playThreadT,NULL,threadPlay2,this);
    //把线程设置为 分离态 ,线程退出后自动释放线程资源，不需要 pthread_join 等待回收
    pthread_detach(playThreadT);
}


//去解码一帧
int DZAudio2::resampleAudio()
{
    AVPacket *pPacket = av_packet_alloc();
    AVFrame *pFrame = av_frame_alloc();
    int dataSize = 0;

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
                    break;
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

    return dataSize;
}

//初始化OpenSLES，并进行播放
void DZAudio2::initCreateOpenSLES2() {
    //当前在子线程，但是未使用env
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

    SLPlayItf pPlayItf  = NULL;
    (*pPlayer)->Realize(pPlayer,SL_BOOLEAN_FALSE);
    (*pPlayer)->GetInterface(pPlayer,SL_IID_PLAY,&pPlayItf);
    //3.4设置缓存队列和回调函数
    SLAndroidSimpleBufferQueueItf playerBufferQueue;
    (*pPlayer)->GetInterface(pPlayer,SL_IID_BUFFERQUEUE,&playerBufferQueue);
    //每次回调 this 会被带给 playerCallback2 里面的 context
    (*playerBufferQueue)->RegisterCallback(playerBufferQueue,playerCallback2,this);
    //3.5设置播放状态
    (*pPlayItf)->SetPlayState(pPlayItf, SL_PLAYSTATE_PLAYING);
    //3.6调用回调函数
    playerCallback2(playerBufferQueue,this);
}

void playerCallback2(SLAndroidSimpleBufferQueueItf caller,void *pContext){
    DZAudio2 *pDZAudio = (DZAudio2 *)pContext;
    //去解码一帧
    int dataSize = pDZAudio->resampleAudio();
    //放入 OpenSLES 的队列中进行播放
    (*caller)->Enqueue(caller,pDZAudio->resampleOutBuffer,dataSize);
}
