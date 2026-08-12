//
// Created by Machenike on 2026/8/5.
//
#include "OpenSL_ES_Core.h"

#ifdef __cplusplus
extern "C" {
#endif

//引擎接口
SLObjectItf engineObject = NULL;
SLEngineItf engineEngine;

//混合输出接口
SLObjectItf outputMixObject = NULL;
SLEnvironmentalReverbItf outputMixEnvironmentalReverb = NULL;

//播放器缓冲队列接口
SLObjectItf bqPlayerObject = NULL;
SLPlayItf bqPlayerPlay;
SLAndroidSimpleBufferQueueItf bqPlayerBufferQueue;
SLEffectSendItf bqPlayerEffectSend;
SLMuteSoloItf bqPlayerMuteSolo;
SLVolumeItf bqPlayerVolume;

//混合输出时的辅助效果，用于播放器缓冲队列
static const SLEnvironmentalReverbSettings reverbSettings =
        SL_I3DL2_ENVIRONMENT_PRESET_STONECORRIDOR;

void *buffer;
size_t bufferSize;

//每当缓冲区结束播放时，调用bqPlayerCallback函数
void bqPlayerCallback(SLAndroidSimpleBufferQueueItf bq, void *context) {
    LOGD(">> buffere queue callback")
    assert(bq == bqPlayerBufferQueue);
    bufferSize = 0;
//    assert(context == NULL);
    //调用getPCM()获取下一块 PCM 数据
    getPCM(&buffer, &bufferSize);
    //寻找并且填充下一个缓冲区
    if (buffer != NULL && bufferSize != 0) {
        SLresult result;
        //入队另一个缓冲区
        result = (*bq)->Enqueue(bq,
                                                 buffer, bufferSize);
        assert(SL_RESULT_SUCCESS == result);
        (void) result;
    }else{
        // 没有更多PCM数据，停止播放器
        LOGD("pcm buffer empty, stop player");
        if(bqPlayerPlay != nullptr){
            (*bqPlayerPlay)->SetPlayState(bqPlayerPlay, SL_PLAYSTATE_STOPPED);
        }
    }
}

void initOpenSLES() {
    LOGD(">> initOpenSLES")
    SLresult result;
    //1.创建引擎
    result = slCreateEngine(&engineObject, 0, NULL,
                            0, NULL, NULL);
    LOGD(">> initOpenSLES... step 1, result = %d", result)
    //2.实现引擎（SL_BOOLEAN_FALSE：同步阻塞调用）
    result = (*engineObject)->Realize(engineObject, SL_BOOLEAN_FALSE);
    LOGD(">> initOpenSLES... step 2, result = %d", result)
    //3.得到引擎接口，用于创建其他对象(SL_IID_ENGINE:要获取的接口 ID)
    result = (*engineObject)->GetInterface(engineObject, SL_IID_ENGINE, &engineEngine);
    LOGD(">> initOpenSLES... step 3, result = %d", result)
    //4.创建混合输出，将环境混响指定为非必需接口
    const SLInterfaceID ids[1] = {SL_IID_ENVIRONMENTALREVERB};//接口id
    const SLboolean req[1] = {SL_BOOLEAN_FALSE};//SL_BOOLEAN_FALSE：非必需接口；设备不支持混响，对象依然可以正常创建成功
    result = (*engineEngine)->CreateOutputMix(engineEngine, &outputMixObject, 1, ids, req);
    LOGD(">> initOpenSLES... step 4, result = %d", result)
    //5.实现混合输出
    result = (*outputMixObject)->Realize(outputMixObject, SL_BOOLEAN_FALSE);//真正实例化资源
    LOGD(">> initOpenSLES... step 5, result = %d", result)
    //6.获取环境混响接口
    //如果环境混响效果不可用，获取接口操作可能会失败，或者因为该特性不存在，CPU负载过大，
    //或者因为 MODIFY_AUDIO_SETTINGS 权限没有被授予，都可能会导致获取接口失败
    result = (*outputMixObject)->GetInterface(outputMixObject,
                                              SL_IID_ENVIRONMENTALREVERB,
                                              &outputMixEnvironmentalReverb);
    if (SL_RESULT_SUCCESS == result) {
        result = (*outputMixEnvironmentalReverb)->SetEnvironmentalReverbProperties(
                outputMixEnvironmentalReverb, &reverbSettings
        );
        LOGD(">> initOpenSLES... step 6, result = %d", result)
    }
}

//初始化缓冲队列
void initBufferQueue(int rate, int channel, int bitsPerSample) {
    LOGD(">> initBufferQueue")
    SLresult result;
    /**构建音频源**/
    //固定写 SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE：使用 Android 简单缓冲区队列模式，PCM 内存缓冲播放模式。
    //numBuffers = 2：内部缓冲区数量，双缓冲。
    SLDataLocator_AndroidSimpleBufferQueue loc_bufq = {
            SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE, 2};
    SLDataFormat_PCM format_pcm;
    format_pcm.formatType = SL_DATAFORMAT_PCM;
    format_pcm.numChannels = channel;//声道数，1 = 单声道，2 = 立体声
    format_pcm.samplesPerSec = rate * 1000;//单位 mHz（毫赫兹）44100Hz → 44100000
    format_pcm.bitsPerSample = bitsPerSample;
    format_pcm.containerSize = 16;//存储位宽，S16 就填16
    if (channel == 2) {
        //立体声，同时启用左 + 右两个声道
        format_pcm.channelMask = SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT;
    } else {
        //单声道，开启中置声道
        format_pcm.channelMask = SL_SPEAKER_FRONT_CENTER;
    }
    //字节序（大小端）, SL_BYTEORDER_LITTLEENDIAN 小端序,低字节放前面：0x34 0x12 → Android、x86
    format_pcm.endianness = SL_BYTEORDER_LITTLEENDIAN;
    SLDataSource audioSrc = {&loc_bufq, &format_pcm};
    /**构建音频后端**/
    //指定音频输出到混合器  SL_DATALOCATOR_OUTPUTMIX:目标是 OutputMix 混音对象
    SLDataLocator_OutputMix loc_outmix = {SL_DATALOCATOR_OUTPUTMIX,
                                          outputMixObject};
    //参数1：输出定位器，告诉播放器音频往哪里送,
    // 参数2：输出端不需要填格式，格式只在数据源 SLDataSource设置
    SLDataSink audioSnk = {&loc_outmix, NULL};
    /**构建音频播放器**/
    //待获取的功能接口 ID
    // SL_IID_BUFFERQUEUE(必选):核心接口，用来把 PCM 缓冲区入队、注册播放回调
    // SL_IID_VOLUME(必选)：音量控制接口，用来调节播放器音量、静音
    // SL_IID_EFFECTSEND:音效发送接口，可以给音频增加音效（均衡器、混响等）
    const SLInterfaceID ids[3] = {SL_IID_BUFFERQUEUE, SL_IID_EFFECTSEND,
            /*SL_IID_MUTESOLO,*/ SL_IID_VOLUME};
    //对应每一个接口
    const SLboolean req[3] = {SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE,
            /*SL_BOOLEAN_TRUE,*/ SL_BOOLEAN_TRUE};
    // engineEngine：OpenSL 引擎接口
    // &bqPlayerObject：输出，播放器对象句柄
    // &audioSrc：数据源（AndroidSimpleBufferQueue + PCM 格式）
    // &audioSnk：输出槽（指向 OutputMix 混音器）
    //3:有3个接口
    result = (*engineEngine)->CreateAudioPlayer(engineEngine, &bqPlayerObject,
                                                &audioSrc, &audioSnk, 3, ids, req);
    assert(SL_RESULT_SUCCESS == result);
    //消除编译器警告：变量result赋值之后只给 assert 使用，Release 下 assert 被移除，编译器报unused variable 'result'。
    //(void)result; 显式 “使用” 一次变量，屏蔽警告
    (void) result;
    /**实现音频播放器**/
    result = (*bqPlayerObject)->Realize(bqPlayerObject, SL_BOOLEAN_FALSE);
    assert(SL_RESULT_SUCCESS == result);
    (void) result;
    /**获取播放接口**/
    result = (*bqPlayerObject)->GetInterface(bqPlayerObject, SL_IID_PLAY, &bqPlayerPlay);
    assert(SL_RESULT_SUCCESS == result);
    (void) result;
    /**获取缓冲队列接口**/
    result = (*bqPlayerObject)->GetInterface(bqPlayerObject, SL_IID_BUFFERQUEUE,
                                             &bqPlayerBufferQueue);
    assert(SL_RESULT_SUCCESS == result);
    (void) result;
    /**注册缓冲队列的回调函数**/
    //OpenSL‑ES 每消费完一块 PCM 缓冲区，就会调用 bqPlayerCallback
    //在回调里面：读取 FFmpeg 解码重采样后的 PCM 数据，调用 Enqueue 把新 PCM 送入播放队列
    result = (*bqPlayerBufferQueue)->RegisterCallback(bqPlayerBufferQueue, bqPlayerCallback, NULL);
    assert(SL_RESULT_SUCCESS == result);
    (void) result;
    /**获取音效发送接口**/
    result = (*bqPlayerObject)->GetInterface(bqPlayerObject, SL_IID_EFFECTSEND,
                                             &bqPlayerEffectSend);
    assert(SL_RESULT_SUCCESS == result);
    (void) result;
    /**获取音量接口**/
    result = (*bqPlayerObject)->GetInterface(bqPlayerObject, SL_IID_VOLUME, &bqPlayerVolume);
    assert(SL_RESULT_SUCCESS == result);
    (void) result;
    /**设置播放器播放时的状态**/
    //SL_PLAYSTATE_PLAYING  // 播放
    //SL_PLAYSTATE_PAUSED   // 暂停
    //SL_PLAYSTATE_STOPPED  // 停止，清空队列中剩余buffer
    result = (*bqPlayerPlay)->SetPlayState(bqPlayerPlay, SL_PLAYSTATE_PLAYING);
    assert(SL_RESULT_SUCCESS == result);
    (void) result;
}

//停止Native音频系统
void stop() {
    //销毁缓冲队列音频播放器实例，置空所有关联接口
    if (bqPlayerObject != NULL) {
        (*bqPlayerObject)->Destroy(bqPlayerObject);
        bqPlayerObject = NULL;
        bqPlayerPlay = NULL;
        bqPlayerBufferQueue = NULL;
        bqPlayerEffectSend = NULL;
        bqPlayerMuteSolo = NULL;
        bqPlayerVolume = NULL;
    }
    //销毁混合输出实例，置空所有关联接口
    if (outputMixObject != NULL) {
        (*outputMixObject)->Destroy(outputMixObject);
        outputMixObject = NULL;
        outputMixEnvironmentalReverb = NULL;
    }
    //销毁引擎实例，置空所有关联接口
    if (engineObject != NULL) {
        (*engineObject)->Destroy(engineObject);
        engineObject = NULL;
        engineEngine = NULL;
    }
    //释放FFmpeg解码器
    releaseFFmpeg();
}

void play(char *url) {
    int rate, channel;
    int ret;
    LOGD("...get url=%s", url)
    //1.初始化FFmpeg解码器
    ret = initFFmpeg(&rate, &channel, url);
    if(ret == -1)
    {
        return;
    }
    //2.初始化OpenSLES
    initOpenSLES();
    //3.初始化BufferQueue
    initBufferQueue(rate, channel, SL_PCMSAMPLEFORMAT_FIXED_16);
    //4.启动音频播放
    //手动触发bqPlayerCallback：只有队列里面已经空了，才会自动触发一次回调，请求新数据。但是队列里已经有数据，不会触发回调。
    //手动直接调用一次回调函数，主动执行一次 Enqueue，把第一块 PCM 送进去，把播放启动起来
    bqPlayerCallback(bqPlayerBufferQueue, NULL);
}

#ifdef __cplusplus
}
#endif