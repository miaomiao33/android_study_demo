//
// Created by Machenike on 2026/10/3.
//

#include "video_player_common4.h"
#include "DZJNICall4.h"
#include "DZFFmpeg4.h"
#include <android/native_window.h>
#include <android/native_window_jni.h>
#include <jni.h>

extern "C"{
#include "libavformat/avformat.h"
#include "../../../headerFile/LogUtils.h"
//    重采样的包
#include "libswresample/swresample.h"
#include "libswscale/swscale.h"
#include "libavutil/imgutils.h"
};


DZJNICall4 *pJniCall;
DZFFmpeg4 *pFFmpeg;

JavaVM *pJavaVM = NULL;

//重写 so 被加载时会调用的一个方法
//作业：了解一下什么叫动态注册
extern "C"
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *javaVm, void *reserved)
{
    LOGE("JNI_OnLoad -->")
    pJavaVM = javaVm;
    JNIEnv *env;
    if(javaVm->GetEnv((void **)(&env), JNI_VERSION_1_4) != JNI_OK)
    {
        return -1;
    }

    return JNI_VERSION_1_4;
}

//静态注册
//音频播放
extern "C"
JNIEXPORT void JNICALL
Java_com_example_android_1study_1demo_1project_opencv_ffmpegUsage_MusicAndVideoPlayer_DarrenPlayer4_nPlay4(
        JNIEnv *env, jobject thiz, jstring url) {
    if(pFFmpeg != NULL)
    {
        pFFmpeg->play();
    }
}
//音频准备
extern "C"
JNIEXPORT void JNICALL
Java_com_example_android_1study_1demo_1project_opencv_ffmpegUsage_MusicAndVideoPlayer_DarrenPlayer4_nPrepare4(
        JNIEnv *env, jobject instance, jstring url_) {
    const char *url = (*env).GetStringUTFChars(url_,JNI_FALSE);
    if(pFFmpeg == NULL)
    {
        pJniCall = new DZJNICall4(pJavaVM, env, instance);
        pFFmpeg = new DZFFmpeg4(pJniCall, url);
        pFFmpeg->prepareAsync();
    }
    env->ReleaseStringUTFChars(url_,url);
}

//视频解码并播放
extern "C"
JNIEXPORT void JNICALL
Java_com_example_android_1study_1demo_1project_opencv_ffmpegUsage_MusicAndVideoPlayer_MusicAndVideoPlayerMainActivity_decodeVideo(
        JNIEnv *env, jobject thiz, jstring uri_,jobject surface) {
    const char *uri = (*env).GetStringUTFChars(uri_,JNI_FALSE);

    AVFormatContext *pFormatContext = NULL;
    AVCodecContext *pCodecContext = NULL;
    SwrContext *swrContext = NULL;
    char *url = NULL;
    DZAudio4 *pAudio = NULL;

    //解码视频，解码音频类似，解码的流程类似
    av_register_all();
    avformat_network_init();

    //C++命名是format_open_input_ret,android命名格式：formatOpenInputRes
    int formatOpenInputRes = 0;
    int formatFindStreamInfoRes = 0;
    int videoStreamIndex = -1;
    AVCodecParameters *pCodecParameters;
    AVCodec *pCodec;

    int codecParametersToContextRes = -1;
    int codecOpenRes = -1;
    AVPacket *pPacket = NULL;
    AVFrame *pFrame = NULL;
    int index = 0;

    formatOpenInputRes = avformat_open_input(&pFormatContext,uri,NULL,NULL);
    if(formatOpenInputRes != 0)
    {
        //失败了
        //第一件事，需要回调给 Java 层
        //第二件事，需要释放资源
        //return;
        LOGE("format open input error:%s",av_err2str(formatOpenInputRes))
//        goto _av_resource_destroy;
        return;
    }
    formatFindStreamInfoRes = avformat_find_stream_info(pFormatContext,NULL);
    if(formatFindStreamInfoRes < 0 )
    {
        LOGE("format find stream info error:%s",av_err2str(formatFindStreamInfoRes))
        //不推荐这样写，但是的确很方便
//        goto _av_resource_destroy;
        return;
    }

    //查找视频流的packet
    videoStreamIndex = av_find_best_stream(pFormatContext,AVMEDIA_TYPE_VIDEO,
                                           -1,-1,NULL,0);
    if(videoStreamIndex < 0)
    {
        LOGE("format video stream error:%s",av_err2str(videoStreamIndex))
//        goto _av_resource_destroy;
        return;
    }

    //查找解码
    pCodecParameters = pFormatContext->streams[videoStreamIndex]->codecpar;//声道，频率等参数
    pCodec = avcodec_find_decoder(pCodecParameters->codec_id);
    if(pCodec == NULL )
    {
        LOGE("codec find decoder error")
        //不推荐这样写，但是的确很方便
//        goto _av_resource_destroy;
        return;
    }

    //打开解码器
    pCodecContext = avcodec_alloc_context3(pCodec);//初始化一个AVCodecContext
    if(pCodecContext == NULL )
    {
        LOGE("avcodec alloc context3 error")
        return;
    }
    //将编码器参数放入pCodecContext中
    codecParametersToContextRes = avcodec_parameters_to_context(pCodecContext,pCodecParameters);
    if(codecParametersToContextRes < 0 )
    {
        LOGE("avcodec parameters to context error:%s",av_err2str(codecParametersToContextRes))
//        goto _av_resource_destroy;
        return;
    }
    codecOpenRes = avcodec_open2(pCodecContext,pCodec,NULL);
    if(codecOpenRes != 0 )
    {
        LOGE("codec audio open error:%s",av_err2str(codecOpenRes))
//        goto _av_resource_destroy;
        return;
    }

    ///绑定 Surface 的缓冲区
    //1、获取窗体
    ANativeWindow *pNativeWindow= ANativeWindow_fromSurface(env,surface);
    //2、设置缓冲区的数据
    ANativeWindow_setBuffersGeometry(pNativeWindow, pCodecContext->width,pCodecContext->height,
                                     WINDOW_FORMAT_RGBA_8888);
    //3、初始化转换上下文
    SwsContext *pSwsContext = sws_getContext(pCodecContext->width,pCodecContext->height,
                                             pCodecContext->pix_fmt,pCodecContext->width,
                                             pCodecContext->height,AV_PIX_FMT_RGBA,SWS_BILINEAR,
                                             NULL,NULL,NULL);
    //Windows 缓冲区的 Buffer
    ANativeWindow_Buffer outBuffer;

    //分配AVFrame结构体
    AVFrame *pRgbaFrame = av_frame_alloc();
    //计算RGBA图片需要多少字节
    int frameSize = av_image_get_buffer_size(AV_PIX_FMT_RGBA,pCodecContext->width,
                                             pCodecContext->height,1);
    //手动申请像素缓冲区，存放RGBA原始像素
    uint8_t *frameBuffer = (uint8_t *)malloc(frameSize * sizeof(uint8_t));
    //将frameBuffer内存地址绑定到AVFrame的data[]、linesize[]
    av_image_fill_arrays(pRgbaFrame->data,pRgbaFrame->linesize,frameBuffer,
                         AV_PIX_FMT_RGBA,pCodecContext->width,pCodecContext->height,1);
    ///


    pPacket = av_packet_alloc();
    pFrame = av_frame_alloc();
    //不断读取压缩数据并解码成 PCM 数据
    while(av_read_frame(pFormatContext,pPacket) >= 0)
    {
        if(pPacket->stream_index == videoStreamIndex)
        {
            //视频
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
                    //渲染，显示，OpenGLES（高效，硬件支持），SurfaceView

                    //这个 pFrame->data,一般是 yuv420P 的，RGBA8888,因此需要转换
                    //假设拿到转换后的 RGBA 的 data 数据，如何渲染？把数据推到缓冲区
                    //转换
                    sws_scale(pSwsContext,(const uint8_t *const *)pFrame->data,//输入帧的各个平面数据指针
                              pFrame->linesize,//输入每个平面一行的字节长度
                              0,pCodecContext->height,//slice的0行到pCodecContext->height行
                              pRgbaFrame->data,//输出帧（RGBA）的数据指针数组
                              pRgbaFrame->linesize);//输出帧每行字节数
                    //把数据推送到缓冲区
                    ANativeWindow_lock(pNativeWindow,&outBuffer,NULL);
                    memcpy(outBuffer.bits,frameBuffer,frameSize);//frameSize其实就是宽高*2*2
                    ANativeWindow_unlockAndPost(pNativeWindow);

                }
            }
        } else{
            //音频
        }

        //解引用(让这一块结构体不指向 data 内存区域，这样可以重复利用 pPacket 等结构体)
        av_packet_unref(pPacket);
        av_frame_unref(pFrame);
    }


    //1、解引用数据 data，释放 pPacket 结构体内存，3、pPacket = NULL
    av_packet_free(&pPacket);
    av_frame_free(&pFrame);

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

    env->ReleaseStringUTFChars(uri_,uri);
}
