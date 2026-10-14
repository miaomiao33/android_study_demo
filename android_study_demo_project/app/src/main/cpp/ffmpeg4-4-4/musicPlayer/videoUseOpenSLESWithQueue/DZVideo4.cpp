//
// Created by Machenike on 2026/10/12.
//

#include "DZVideo4.h"
int resampleVideo(DZVideo4 *pVideo);

DZVideo4::DZVideo4(int videoStreamIndex, DZJNICall4 *pJniCall, DZPlayerStatus4 *pPlayerStatus)
        : DZMedia4(videoStreamIndex, pJniCall, pPlayerStatus) {

}

DZVideo4::~DZVideo4() {
    release();
}

//一个线程去播放
void *threadVideoPlay(void* context)
{
    DZVideo4 *pVideo = (DZVideo4 *)context;
    //去解码一帧
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

//去解码一帧
int resampleVideo(DZVideo4 *pVideo)
{

    ///绑定 Surface 的缓冲区
    //获取当前线程的 JNIEnv，通过 JavaVM 的 AttachCurrentThread 创建自己的 JNIEnv
    JNIEnv *env;
    if(pVideo->pJniCall->javaVm->AttachCurrentThread(&env,0) != JNI_OK)
    {
        LOGE("get child thread jniEnv error!");
        return 0;
    }
    //1、获取窗体
    ANativeWindow *pNativeWindow= ANativeWindow_fromSurface(env,pVideo->surface);
    pVideo->pJniCall->javaVm->DetachCurrentThread();

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
        //Packet 包，压缩的数据，解码成 PCM 数据
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

void DZVideo4::privateAnalysisStream(ThreadMode threadMode, AVFormatContext *context) {
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
