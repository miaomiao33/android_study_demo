//
// Created by Machenike on 2026/8/5.
//
#include "FFmpegCore.h"

#ifdef __cplusplus
extern "C" {
#endif

uint8_t *outputBuffer;
size_t outputBufferSize;

AVPacket packet;
int audioStream;
AVFrame *aFrame;
SwrContext *swr;
AVFormatContext *aFormatCtx;
AVCodecContext *aCodecCtx;

int initFFmpeg(int *rate, int *channel, char *url) {
    av_register_all();
    // 初始化（全局一次）
    int net_ret = avformat_network_init();
    LOGD("avformat_network_init ret = %d", net_ret);
    aFormatCtx = avformat_alloc_context();
    LOGD("ffmpeg get url=:%s", url)
    //网络音频流
    char *file_name = url;

    //打开音频文件
    int ret = avformat_open_input(&aFormatCtx, file_name, NULL, NULL);
    if (ret != 0) {
        //获取错误编码和信息
        char err_buf[AV_ERROR_MAX_STRING_SIZE] = {0};
        av_strerror(ret, err_buf, sizeof(err_buf));
        //url不能使用也可能会报错：ret=-1330794744 , err=Protocol not found
        LOGE("Couldn't open file: %s , ret=%d , err=%s", file_name, ret, err_buf);
        return -1;
    }

    //检索流信息
    if (avformat_find_stream_info(aFormatCtx, NULL) < 0) {
        LOGE("Couldn't find stream information.")
        return -1;
    }

    //找到一条音频流
    audioStream = -1;
    for (int i = 0; i < aFormatCtx->nb_streams; i++) {
        if (aFormatCtx->streams[i]->codec->codec_type == AVMEDIA_TYPE_AUDIO
            && audioStream < 0) {
            audioStream = i;
        }
    }
    if (audioStream == -1) {
        LOGE("Couldn't find audio stream.")
        return -1;
    }

    //得到音频流的编解码器上下文环境指针
    aCodecCtx = aFormatCtx->streams[audioStream]->codec;

    //找到音频流的解码器
    AVCodec *aCodec = avcodec_find_decoder(aCodecCtx->codec_id);
    if (!aCodec) {
        LOGE("Unsupported codec.")
        return -1;
    }

    //打开编解码器
    if ((avcodec_open2(aCodecCtx, aCodec, NULL)) < 0) {
        LOGE("Could not open codec.")
        //返回-1，表示不能打开编解码器
        return -1;
    }

    //分配一个 AVFrame 结构体对象
    aFrame = av_frame_alloc();

    //设置格式转换（输入输出声道布局、采样率保持不变，解码器输出音频格式 → AV_SAMPLE_FMT_S16（16 位整形 PCM））
    swr = swr_alloc();
    av_opt_set_int(swr, "in_channel_layout", aCodecCtx->channel_layout, 0);
    av_opt_set_int(swr, "out_channel_layout", aCodecCtx->channel_layout, 0);
    av_opt_set_int(swr, "in_sample_rate", aCodecCtx->sample_rate, 0);
    av_opt_set_int(swr, "out_sample_rate", aCodecCtx->sample_rate, 0);
    av_opt_set_sample_fmt(swr, "in_sample_fmt", aCodecCtx->sample_fmt, 0);
    av_opt_set_sample_fmt(swr, "out_sample_fmt", AV_SAMPLE_FMT_S16, 0);
    swr_init(swr);

    //分配PCM数据缓存
    // 最大采样点数，建议跟解码器frame的nb_samples对齐，一般1024/2048
    int max_out_samples = 2048;
    int channels = aCodecCtx->channels;
    // 计算需要的字节数：采样点数 × 通道 × 每个采样2字节(S16)
    outputBufferSize = max_out_samples * channels * 2;
    //    outputBufferSize = 8196;
    outputBuffer = (uint8_t *) malloc(sizeof(uint8_t) * outputBufferSize);

    //返回采样率和信道
    *rate = aCodecCtx->sample_rate;
    *channel = aCodecCtx->channels;
    return 0;
}

//获取PCM数据，自动回调获取
int getPCM(void **pcm, size_t *pcmSize) {
    LOGD(">> getPCM")
    while (av_read_frame(aFormatCtx, &packet) >= 0) {
        int frameFinished = 0;
        //判断Packet（音频压缩数据）是否来自音频流，audioStream是上面记录的audioStream位置index
        if (packet.stream_index == audioStream) {
            //音频解码
            //aFrame：输出 AVFrame，解码出来的音频 PCM 数据存这里
            //frameFinished【输出】：1 = 当前 packet 解码出一帧完整音频 frame；0 = 还没有输出帧（需要继续送 packet，或者缓冲区冲刷）
            //packet：输入，包含压缩音频码流的 AVPacket
            avcodec_decode_audio4(aCodecCtx, aFrame,
                                  &frameFinished, &packet);

            if (frameFinished) {
                //data_size为音频数据所占字节数(计算音频样本总共需要多少字节)
                //aFrame->linesize：输出，会填充每个声道一行的字节大小；
                // align：内存对齐，1 = 不做对齐
                int data_size = av_samples_get_buffer_size(
                        aFrame->linesize, aCodecCtx->channels,
                        aFrame->nb_samples, aCodecCtx->sample_fmt, 1
                );
                LOGD(">> getPcm data_size=%d", data_size)
                //这里进行内存再分配可能存在问题
//                if (data_size > outputBufferSize) {
//                    outputBufferSize = data_size;
//                    outputBuffer = (uint8_t *) realloc(outputBuffer,
//                                                       sizeof(uint8_t) * outputBufferSize);
//                }
                int max_out_samples = swr_get_out_samples(swr, aFrame->nb_samples);
                int need_byte = max_out_samples
                        * av_get_bytes_per_sample(AV_SAMPLE_FMT_S16)
                        * aCodecCtx->channels;

                if (need_byte > outputBufferSize)
                {
                    outputBufferSize = need_byte;
                    uint8_t *tmp = (uint8_t *) realloc(outputBuffer, outputBufferSize);
                    if(tmp == nullptr)
                    {
                        LOGD("realloc outputBuffer fail size=%d", outputBufferSize);
                        return -1;
                    }
                    outputBuffer = tmp;
                }

                //音频格式转换
                int outSamples = swr_convert(swr, &outputBuffer,
//                            aFrame->nb_samples,//输出最大可容纳样本，用swr_get_out_samples结果
                            max_out_samples,
                            (uint8_t const **) (aFrame->extended_data),
                            aFrame->nb_samples);
                //返回PCM数据
//                *pcm = outputBuffer;
//                *pcmSize = data_size;
                if(outSamples > 0)
                {
                    // 重采样后S16的实际字节
                    int realBytes = outSamples
                            * av_get_bytes_per_sample(AV_SAMPLE_FMT_S16)
                            * aCodecCtx->channels;

                    *pcm = outputBuffer;
                    *pcmSize = realBytes;   // 使用重采样后真实字节，不要用data_size
                    return 0;
                }
                else
                {
                    *pcm = nullptr;
                    *pcmSize = 0;
                    return -1;
                }
                return 0;
            }
        }
    }

    return -1;
}

//释放相关资源
int releaseFFmpeg() {
    av_packet_unref(&packet);
    av_free(outputBuffer);
    av_free(aFrame);
    avcodec_close(aCodecCtx);
    avformat_close_input(&aFormatCtx);
    avformat_network_deinit();

    return 0;
}

#ifdef __cplusplus
}
#endif