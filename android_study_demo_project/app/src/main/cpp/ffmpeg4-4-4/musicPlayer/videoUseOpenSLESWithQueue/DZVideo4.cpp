//
// Created by Machenike on 2026/10/12.
//

#include "DZVideo4.h"

int resampleVideo(DZVideo4 *pVideo);

DZVideo4::DZVideo4(int videoStreamIndex, DZJNICall4 *pJniCall, DZPlayerStatus4 *pPlayerStatus,DZAudio4 *pAudio)
        : DZMedia4(videoStreamIndex, pJniCall, pPlayerStatus) {
    this->pAudio = pAudio;
}

DZVideo4::~DZVideo4() {
    release();
}

//一个线程去播放
void *threadVideoPlay(void* context)
{
    //当前是子线程
    DZVideo4 *pVideo = (DZVideo4 *)context;
    //去队列获取并解码一帧
    resampleVideo(pVideo);
    return 0;
}

void DZVideo4::play() {
    //一个线程去解码播放
    //创建一个线程去播放，多线程边解码边播放
    pthread_t playThreadT;
    //this：传给 threadPlay 的参数，也就是当前 DZFFmpeg4 对象指针。在线程内部可以强转回来
    pthread_create(&playThreadT,NULL,threadVideoPlay,this);
    //把线程设置为 分离态 ,线程退出后自动释放线程资源，不需要 pthread_join 等待回收
    pthread_detach(playThreadT);
}

//去队列获取并解码一帧
int resampleVideo(DZVideo4 *pVideo)
{
    //当前为子线程，但是只有获取窗口一处地方使用了env，故 AttachCurrentThread 不放外面
    ///绑定 Surface 的缓冲区
    //获取当前线程的 JNIEnv，通过 JavaVM 的 AttachCurrentThread 创建自己的 JNIEnv
    JavaVM* javaVm = pVideo->pJniCall->javaVm;
    JNIEnv *env = nullptr;
    bool needDetach = false;
    jint ret = javaVm->GetEnv((void**)&env, JNI_VERSION_1_6);
    if(ret == JNI_EDETACHED)
    {
        if(pVideo->pJniCall->javaVm->AttachCurrentThread(&env,0) != JNI_OK)
        {
            LOGE("get child thread jniEnv error!");
            return 0;
        }
        needDetach = true;
    }

    //1、获取窗体
    ANativeWindow *pNativeWindow= ANativeWindow_fromSurface(env,pVideo->surface);

    // 线程退出，解绑一次
    if(needDetach)
    {
        pVideo->pJniCall->javaVm->DetachCurrentThread();
    }

    //2、设置缓冲区的数据
    ANativeWindow_setBuffersGeometry(pNativeWindow, pVideo->pCodecContext->width,
                                     pVideo->pCodecContext->height,WINDOW_FORMAT_RGBA_8888);
    ///

    //Windows 缓冲区的 Buffer
    ANativeWindow_Buffer outBuffer;

    AVPacket *pPacket = NULL;
    AVFrame *pFrame = av_frame_alloc();
    int dataSize = 0;

    //不断读取压缩数据并解码成 PCM 数据
    while(pVideo->pPlayerStatus != NULL && !pVideo->pPlayerStatus->isExit)
    {
        pPacket = pVideo->pPacketQueue->pop();
        //音频
        //Packet 包，压缩的数据，解码成原始数据
        int codecSendPacketRes = avcodec_send_packet(pVideo->pCodecContext,pPacket);
        if(codecSendPacketRes == 0)
        {
            int codecReceiveFrameRes = avcodec_receive_frame(pVideo->pCodecContext,pFrame);
            if(codecReceiveFrameRes == 0)
            {
                // AVPacket -> AVFrame
                LOGI("解码视频帧");

                //渲染，显示，OpenGLES（高效，硬件支持），SurfaceView

                //这个 pFrame->data,一般是 yuv420P 的，RGBA8888,因此需要转换
                //假设拿到转换后的 RGBA 的 data 数据，如何渲染？把数据推到缓冲区
                //转换
                sws_scale(pVideo->pSwsContext,(const uint8_t *const *)pFrame->data,//输入帧的各个平面数据指针
                          pFrame->linesize,//输入每个平面一行的字节长度
                          0,pVideo->pCodecContext->height,//slice的0行到pCodecContext->height行
                          pVideo->pRgbaFrame->data,//输出帧（RGBA）的数据指针数组
                          pVideo->pRgbaFrame->linesize);//输出帧每行字节数
                //在播放之前判断一下需要休眠多久
                double frameSleepTime = pVideo->getFrameSleepTime(pFrame);
                LOGI("frameSleepTime-> %f",frameSleepTime)
                av_usleep(frameSleepTime * 1000000);

                //把数据推送到缓冲区
                ANativeWindow_lock(pNativeWindow,&outBuffer,NULL);
                memcpy(outBuffer.bits,pVideo->pFrameBuffer,pVideo->frameSize);//frameSize其实就是宽高*2*2
                ANativeWindow_unlockAndPost(pNativeWindow);

            }
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

/***
 * 视频同步音频，计算获取休眠的时间
 * @param pFrame
 * @return
 */
double DZVideo4::getFrameSleepTime(AVFrame *pFrame)
{
    double times = static_cast<double>(pFrame->best_effort_timestamp) * av_q2d(timeBase);
    if(times > currentTime)
    {
        currentTime = times;
    }

    //相差多少秒（音频和视频）
    double diffTime = pAudio->currentTime - currentTime;

    //视频快了就慢一点，视频慢了就快一点
    //但是尽量把时间控制在视频的帧率时间范围左右 1/24s 大约0.04s 1/30 0.033

    //小偏差，开始动态调整 delay
    //第一次控制 0.016s 到 -0.016s (0.033的一半)
    if(diffTime > 0.016 || diffTime < -0.016)
    {
        if(diffTime > 0.016)
        {
            //diffTime > 0.016：音频时间 > 视频时间 → 音频超前，视频慢了 → 减少视频等待时间
            // `delayTime = delayTime * 2/3`，视频加快追赶
            delayTime = delayTime * 2 / 3;
        } else if(diffTime < -0.016)
        {
            delayTime = delayTime * 3 / 2;
        }


        //第二次控制 defaultDelayTime * 2 / 3 到 defaultDelayTime * 3 / 2
        // (defaultDelayTime 初始默认值为0.04)
        if(delayTime < defaultDelayTime / 2.0)
        {
            delayTime = defaultDelayTime * 2.0 / 3.0;
        } else if(delayTime > defaultDelayTime * 2)
        {
            delayTime = defaultDelayTime * 3 / 2;
        }

//        //第二种调整
//        if(delayTime < defaultDelayTime * 2.0 / 3.0)
//        {
//            delayTime = defaultDelayTime * 2.0 / 3.0;
//        } else if(delayTime > defaultDelayTime * 3 / 2)
//        {
//            delayTime = defaultDelayTime * 3 / 2;
//        }
    }

    // 大偏差，做极端矫正
    //第三次控制，基本是异常情况
    if(diffTime >= 0.25)
    {
        // 音频大幅领先，视频尽快渲染，不等待
        delayTime = 0;
    } else if(diffTime <= -0.25)
    {
        // 视频大幅领先音频
        delayTime = defaultDelayTime * 2;

        // =========【推荐替换方案B】=========
        // 视频超前太多，直接丢当前帧，不渲染，取消sleep，更快对齐音频
        // av_frame_free(&pFrame);
        // continue;
    }

    return delayTime;
}

void DZVideo4::privateAnalysisStream(ThreadMode threadMode, AVFormatContext *pFormatContexts) {
    //3、初始化转换上下文
    pSwsContext = sws_getContext(pCodecContext->width,pCodecContext->height,
                                             pCodecContext->pix_fmt,pCodecContext->width,
                                             pCodecContext->height,AV_PIX_FMT_RGBA,SWS_BILINEAR,
                                             NULL,NULL,NULL);

    //分配AVFrame结构体
    pRgbaFrame = av_frame_alloc();
    //计算RGBA图片需要多少字节
    frameSize = av_image_get_buffer_size(AV_PIX_FMT_RGBA,pCodecContext->width,
                                             pCodecContext->height,1);
    //手动申请像素缓冲区，存放RGBA原始像素
    pFrameBuffer = (uint8_t *)malloc(frameSize * sizeof(uint8_t));
    //将frameBuffer内存地址绑定到AVFrame的data[]、linesize[]
    av_image_fill_arrays(pRgbaFrame->data,pRgbaFrame->linesize,pFrameBuffer,
                         AV_PIX_FMT_RGBA,pCodecContext->width,pCodecContext->height,1);

    int num = pFormatContexts->streams[streamIndex]->avg_frame_rate.num;
    int den = pFormatContexts->streams[streamIndex]->avg_frame_rate.den;
    if(den != 0 && num != 0)
    {
        //1s/25帧
//        int fps = num / den;
//        defaultDelayTime = 1.0f / fps;
        defaultDelayTime = 1.0f * den / num;
    }
}

void DZVideo4::release() {
    DZMedia4::release();
    if(pSwsContext != NULL)
    {
        sws_freeContext(pSwsContext);
        free(pSwsContext);
        pSwsContext = NULL;
    }

    if(pFrameBuffer != NULL)
    {
        free(pFrameBuffer);
        pFrameBuffer = NULL;
    }

    if(pRgbaFrame != NULL)
    {
        //里面已经进行了free，不需要再次调用free()
        av_frame_free(&pRgbaFrame);
        pRgbaFrame = NULL;
    }

    //注意 ：pJniCall 需要在 DZFFmpeg 之后销毁
    if(pJniCall != NULL)
    {
        pJniCall->jniEnv->DeleteGlobalRef(surface);
    }
}

void DZVideo4::setSurface(jobject surface) {
    this->surface = pJniCall->jniEnv->NewGlobalRef(surface);
}
