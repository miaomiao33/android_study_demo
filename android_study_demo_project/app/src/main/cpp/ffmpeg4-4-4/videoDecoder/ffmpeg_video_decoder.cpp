//
// Created by Machenike on 2026/8/13.
//

extern "C"{
#include <jni.h>
#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"
#include "libswscale/swscale.h"
#include <android/native_window.h>
#include <android/native_window_jni.h>
#include "com_example_android_study_demo_project_opencv_ffmpegUsage_videoDecoder_FFmpegVideoDecoderNativePlayer.h"
#include "../../headerFile/LogUtils.h"
#include "libavutil/imgutils.h"
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_android_1study_1demo_1project_opencv_ffmpegUsage_videoDecoder_FFmpegVideoDecoderNativePlayer_playVideo(
        JNIEnv *env, jclass clazz, jstring url, jobject surface) {
    //视频URL
    const char *file_name = env->GetStringUTFChars(url, JNI_FALSE);
    LOGD("start play video... url=%s",file_name)
    av_register_all();

    //分配 AVFormatContext 上下文对象
    AVFormatContext *pFormatCtx = avformat_alloc_context();

    //打开视频
    int ret = avformat_open_input(&pFormatCtx,file_name,NULL,NULL);
    if(ret != 0)
    {
        //获取错误编码和信息
        char err_buf[AV_ERROR_MAX_STRING_SIZE] = {0};
        av_strerror(ret, err_buf, sizeof(err_buf));
        LOGE("Couldn't open file: %s , ret=%d , err=%s", file_name, ret, err_buf);
        return -1;
    }

    //检索流信息
    if(avformat_find_stream_info(pFormatCtx, NULL) < 0)
    {
        LOGE("Couldn't find stream information.")
        return -1;
    }

    //找到第一个视频流
    int videoStream = -1;
    for(int i = 0;i < pFormatCtx->nb_streams;i++)
    {
        if(pFormatCtx->streams[i]->codec->codec_type == AVMEDIA_TYPE_VIDEO
           && videoStream < 0)
        {
            videoStream = i;
        }
    }
    if(videoStream == -1)
    {
        LOGE("Didn't find a video stream.")
        return -1;
    }

    //得到视频流的编解码器上下文环境的指针
    AVCodecContext *pCodecCtx = pFormatCtx->streams[videoStream]->codec;

    //找到视频流对应的解码器
    AVCodec *pCodec = avcodec_find_decoder(pCodecCtx->codec_id);
    if(pCodec == NULL)
    {
        LOGE("Codec not find.")
        return -1;
    }

    //打开解码器
    if(avcodec_open2(pCodecCtx, pCodec, NULL) < 0)
    {
        LOGE("Could not open codec.")
        return -1;
    }

    //获取NativeWindow，用于渲染视频
    ANativeWindow *nativeWindow = ANativeWindow_fromSurface(env,surface);

    //获取视频宽高值
    int videoWidth = pCodecCtx->width;
    int videoHeight = pCodecCtx->height;

    //设置NativeWindow的Buffer大小，可自动拉伸
    ANativeWindow_setBuffersGeometry(nativeWindow,
                                     videoWidth, videoHeight, WINDOW_FORMAT_RGBA_8888);
    ANativeWindow_Buffer windowBuffer;
//    //打开解码器
//    if(avcodec_open2(pCodecCtx, pCodec, NULL) < 0)
//    {
//        LOGE("Could not open codec.")
//        return -1;
//    }

    //分配视频帧空间内存
    AVFrame *pFrame = av_frame_alloc();
    //用于渲染
    AVFrame *pFrameRGBA = av_frame_alloc();
    if(pFrameRGBA == NULL || pFrame == NULL)
    {
        LOGE("Could not allocate video frame.")
        return -1;
    }
    //确定所需缓冲区大小并分配缓冲区内存空间
    //Buffer 中的数据就是用于渲染的，且格式为RGBA
    int numBytes = av_image_get_buffer_size(AV_PIX_FMT_RGBA,pCodecCtx->width,
                                            pCodecCtx->height, 1);
    uint8_t *buffer = (uint8_t *)av_malloc(numBytes * sizeof(uint8_t));
    av_image_fill_arrays(pFrameRGBA->data, pFrameRGBA->linesize,
                         buffer, AV_PIX_FMT_RGBA,
                         pCodecCtx->width, pCodecCtx->height, 1);

    //由于解码出来的帧格式不是 RGBA 的，故在渲染之前需要进行格式转换
    struct SwsContext *sws_ctx = sws_getContext(pCodecCtx->width,
            pCodecCtx->height,
            pCodecCtx->pix_fmt,
            pCodecCtx->width,
            pCodecCtx->height,
            AV_PIX_FMT_RGBA,
            SWS_BILINEAR,
            NULL,
            NULL,
            NULL);

    int frameFinished;
    AVPacket packet;
    while(av_read_frame(pFormatCtx, &packet) >= 0)
    {
        //判断packet（音视频压缩数据）是否是视频流
        if(packet.stream_index == videoStream)
        {
            //解码视频帧
            avcodec_decode_video2(pCodecCtx, pFrame,
                                  &frameFinished, &packet);

            //并不是解码一次就可以解码出 1 帧
            if(frameFinished)
            {
                //锁住NativeWindow的缓冲区
                ANativeWindow_lock(nativeWindow, &windowBuffer, 0);

                //格式转换
                sws_scale(sws_ctx, (uint8_t const * const *)pFrame->data,
                          pFrame->linesize, 0, pCodecCtx->height,
                          pFrameRGBA->data, pFrameRGBA->linesize);
                //获取stride
                uint8_t *dst = static_cast<uint8_t *>(windowBuffer.bits);
                int dstStride = windowBuffer.stride * 4;
                uint8_t  *src = (uint8_t *)(pFrameRGBA->data[0]);
                int srcStride = pFrameRGBA->linesize[0];
                //由于窗口的stride和帧的stride不同，因此需要逐行复制
                int h;
                for (h = 0; h < videoHeight; h++) {
                    memcpy(dst + h * dstStride, src + h * srcStride, srcStride);
                }

                ANativeWindow_unlockAndPost(nativeWindow);

            }
        }

        av_packet_unref(&packet);
    }

    av_free(buffer);
    av_free(pFrameRGBA);
    //释放YUV图像帧
    av_free(pFrame);
    //关闭解码器
    avcodec_close(pCodecCtx);
    //关闭视频文件
    avformat_close_input(&pFormatCtx);

    env->ReleaseStringUTFChars(url,file_name);

    return 0;
}