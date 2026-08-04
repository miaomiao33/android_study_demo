#include "ffmpeg.h"
//
// Created by Machenike on 2026/7/25.
//

/**
 * ffmpeg转换MP4格式为AVI
 */
extern "C"
JNIEXPORT void JNICALL
Java_com_example_android_1study_1demo_1project_opencv_ffmpegUsage_FFmpegUsageMainActivity_changeFormat(
        JNIEnv *env, jobject thiz, jstring inputPath_j, jstring outputPath_j) {
    const char *inputPath  = env->GetStringUTFChars(inputPath_j, 0);
    const char *outputPath  = env->GetStringUTFChars(outputPath_j, 0);
    AVOutputFormat *ofmt = NULL;
    AVBitStreamFilterContext * vbsf = NULL;
    //定义输入、输出AVFormatContext
    AVFormatContext *ifmt_ctx = NULL, *ofmt_ctx = NULL;
    AVPacket pkt;
    const char *in_filename, *out_fileName;
    int ret, i ;
    int frame_index = 0;
    in_filename = inputPath;//Input file URL
    out_fileName = outputPath;//Output file URL

    //旧版 FFmpeg 用来注册所有封装格式、编解码器、协议
    av_register_all();
    //输入
    //打开多媒体文件
    if((ret = avformat_open_input(&ifmt_ctx, in_filename,0,0)) < 0)
    {
        LOGI("ffmpeg Could not open input file.");
        goto end;
    }
    //获取视频信息
    //avformat_find_stream_info:读取一部分码流数据，解析流参数：查找视频 / 音频流、获取分辨率、帧率、编码参数、extradata 等信息。
    if((ret = avformat_find_stream_info(ifmt_ctx,0)) < 0)
    {
        LOGI("ffmpeg Failed to retrieve input stream information.");
        goto end;
    }
    //把 MP4 容器内的 H.264 AVCC 码流（长度前缀 NALU） → AnnexB 码流（0x000001 起始码分隔 NAL）
    //AVCC（MP4/FLV里面用,没有 00 00 01 起始码）: [4字节长度][NALU数据]
    // 长度编码:00 00 00 12【长度：0x12 = 18字节】后面跟着18字节的一个NALU
    //AnnexB 不用长度前缀，用起始码分割 NALU。
    //00 00 01 [NALU1数据]
    //00 00 01 [NALU2数据]
    //00 00 01 [NALU3数据]
    vbsf = av_bitstream_filter_init("h264_mp4toannexb");
    //打印输入 / 输出流信息到控制台
    av_dump_format(ifmt_ctx,0,in_filename,0);


    //初始化输出视频码流的AVFormatContext
    //创建输出 AVFormatContext
    avformat_alloc_output_context2(&ofmt_ctx, NULL, NULL, out_fileName);
    if(!ofmt_ctx)
    {
        LOGI("ffmpeg Could not create output context.\n");
        ret = AVERROR_UNKNOWN;
        goto end;
    }
    ofmt = ofmt_ctx->oformat;
    //ifmt_ctx->nb_streams: 当前媒体文件/流一共有几路流
    //只有视频：nb_streams = 1
    //视频 + 音频：nb_streams = 2
    //视频 + 音频 + 字幕：nb_streams = 3
    for(i = 0;i < ifmt_ctx->nb_streams; i++)
    {
        //通过输入的AVStream 创建输出的AVStream
        //获取到第i路流
        AVStream *in_stream = ifmt_ctx->streams[i];
        //初始化AVStream
        //in_stream->codec(AVCodecContext *): 旧版称 “解码器上下文”,保存：分辨率、帧率、码率、extradata、打开的解码器状态等
        //in_stream->codec->codec: AVCodecContext 内部变量，类型：const AVCodec*（AVCodec编码器/解码器原型）
        AVStream *out_stream = avformat_new_stream(ofmt_ctx, in_stream->codec->codec);
        if(!out_stream)
        {
            LOGI("ffmpeg Failed allocating output stream.\n");
            ret = AVERROR_UNKNOWN;
            goto end;
        }
        //复制AVCodecContext的设置属性
        if(avcodec_copy_context(out_stream->codec, in_stream->codec) < 0)
        {
            LOGI("ffmpeg Failed to copy context from input to output stream codec context.\n");
            goto end;
        }
        //codec_tag 是 4CC（四字符标记，容器用来标识编码格式）
        //值为 0 ：让 FFmpeg 封装器自动选择合适的 tag，不要沿用输入的旧 tag
        out_stream->codec->codec_tag = 0;
        //AVFMT_GLOBALHEADER: 封装要求 extradata（SPS/PPS、全局编码参数）放在全局头部，不放在每个帧包内
        if(ofmt_ctx->oformat->flags & AVFMT_GLOBALHEADER)
        {
            //AV_CODEC_FLAG_GLOBAL_HEADER:编码器把全局参数（SPS/PPS）放到 extradata，不混在帧 NAL 里
            out_stream->codec->flags |= AV_CODEC_FLAG_GLOBAL_HEADER;
        }
    }
    //输出信息
    av_dump_format(ofmt_ctx, 0, out_fileName, 1);

    //打开输出文件(这里是本地文件)
    //AVFMT_NOFILE:该封装不需要本地文件 IO，自己管理底层连接
    //非 0 = 存在标记 → 网络流（rtmp://...）
    //0 = 没有标记 → 文件输出（xxx.mp4 /xxx.flv）
    if(!(ofmt->flags & AVFMT_NOFILE))
    {
        //区分本地文件 / 网络协议
        //本地文件：需要 avio_open 打开磁盘文件
        //RTMP：不能手动 avio_open，否则冲突、连接失败
        //AVIO_FLAG_WRITE：写模式
        //avio_open仅对本地文件有效；网络协议禁止手动调用
        ret = avio_open(&ofmt_ctx->pb, out_fileName, AVIO_FLAG_WRITE);
        if(ret < 0)
        {
            LOGI("ffmpeg Could not open output file '%s'.\n",out_fileName);
            goto end;
        }
    }
    //写文件头
    //往输出容器写入头部信息（文件头 / 流头）
    if((avformat_write_header(ofmt_ctx, NULL)) < 0)
    {
        LOGI("ffmpeg Error occurred when opening output file.\n");
        goto end;
    }
    //处理文件体
    while(1){
        AVStream *in_stream, *out_stream;
        //得到一个AVPacket
        //从输入媒体上下文读取一个完整的压缩数据包 AVPacket(原始压缩码流（H.264、AAC 等，未解码）)
        ret = av_read_frame(ifmt_ctx, &pkt);
        if(ret < 0)
        {
            break;
        }
        //pkt.stream_index = 输入流索引（ifmt_ctx 的下标）
        //输入流顺序：0=视频，1=音频 输出流顺序：大概率也是 0=视频，1=音频
        //正确做法：建立映射表
        in_stream = ifmt_ctx->streams[pkt.stream_index];
        out_stream = ofmt_ctx->streams[pkt.stream_index];

        //转换PTS/DTS
        //时间戳单位转换
        //pts/dts/duration 不是绝对秒，而是基于当前流 time_base 的计数
        //in_stream->time_base：输入流时间基
        //out_stream->time_base：输出流时间基（flv/rtmp 和 mp4 经常不一样）
        //av_rescale_q_rnd: 把 a 从 bq 单位换算到 cq 单位，指定舍入策略
        pkt.pts = av_rescale_q_rnd(pkt.pts, in_stream->time_base, out_stream->time_base,
                                   (AVRounding)(AV_ROUND_NEAR_INF | AV_ROUND_PASS_MINMAX));
        pkt.dts = av_rescale_q_rnd(pkt.dts, in_stream->time_base, out_stream->time_base,
                                   (AVRounding)(AV_ROUND_NEAR_INF | AV_ROUND_PASS_MINMAX));
        //av_rescale_q: 等价于 av_rescale_q_rnd 使用默认舍入 AV_ROUND_NEAR_INF
        pkt.duration = av_rescale_q(pkt.duration, in_stream->time_base, out_stream->time_base);
        //该包没有有效的文件字节偏移,本地文件读出来的包会填充 pos
        pkt.pos = -1;

        if(pkt.stream_index == 0)
        {
            //视频流
            AVPacket fpkt = pkt;
            //H.264 AVCC (mp4 格式) -> AnnexB (rtmp/flv 需要的起始码格式)
            //    uint8_t **poutbuf, int *poutbuf_size, //【输出】转换后数据指针、长度
            //    const uint8_t *buf, int buf_size,     //【输入】原始数据指针、长度
            //    int keyframe                          // 是否关键帧：1=关键帧，0=非关键帧
            int a = av_bitstream_filter_filter(vbsf,out_stream->codec, NULL,
                                               &fpkt.data, &fpkt.size,
                                               pkt.data, pkt.size,
                                               pkt.flags & AV_PKT_FLAG_KEY);
            pkt.data = fpkt.data;
            pkt.size = fpkt.size;
        }
        //写AVPacket
        //将AVPacket（存储音视频压缩码流数据）写入文件
        if(av_write_frame(ofmt_ctx, &pkt) < 0)
        {
            LOGI("ffmpeg Error muxing packet.\n");
            break;
        }
        //释放 AVPacket 内部引用的缓冲区资源，重置 pkt 成员；但不销毁 pkt 结构体本身
        av_packet_unref(&pkt);
        frame_index++;
    }
    //写入文件尾
    av_write_trailer(ofmt_ctx);

    end:
    env->ReleaseStringUTFChars(inputPath_j,inputPath);
    env->ReleaseStringUTFChars(outputPath_j,outputPath);
    avformat_close_input(&ifmt_ctx);
    /* 关闭输出 */
    if(ofmt_ctx && ofmt != nullptr && !(ofmt->flags & AVFMT_NOFILE))
        avio_close(ofmt_ctx->pb);
    avformat_free_context(ofmt_ctx);
    system("pause");
}

//新版写法
extern "C"
JNIEXPORT void JNICALL
Java_com_example_android_1study_1demo_1project_opencv_ffmpegUsage_FFmpegUsageMainActivity_changeFormat2(
        JNIEnv *env, jobject thiz, jstring inputPath_j, jstring outputPath_j) { const char *inputPath  = env->GetStringUTFChars(inputPath_j, nullptr);
    const char *outputPath = env->GetStringUTFChars(outputPath_j, nullptr);
    const char *in_filename  = inputPath;
    const char *out_fileName = outputPath;

    AVFormatContext *ifmt_ctx = nullptr;
    AVFormatContext *ofmt_ctx = nullptr;
    AVPacket pkt;
    int ret = 0;
    int frame_index = 0;
    int stream_mapping_size = 0;
    int *stream_mapping = nullptr;
    int out_stream_idx = 0;
    AVBSFContext *bsf_ctx = nullptr;
    AVOutputFormat *ofmt = nullptr;

    // ====================== 打开输入 ======================
    ret = avformat_open_input(&ifmt_ctx, in_filename, nullptr, nullptr);
    if (ret < 0) {
        LOGI("ffmpeg Could not open input file.");
        goto end;
    }
    ret = avformat_find_stream_info(ifmt_ctx, nullptr);
    if (ret < 0) {
        LOGI("ffmpeg Failed to retrieve input stream information.");
        goto end;
    }
    av_dump_format(ifmt_ctx, 0, in_filename, 0);

    // ====================== 创建输出上下文 ======================
    ret = avformat_alloc_output_context2(&ofmt_ctx, nullptr, nullptr, out_fileName);
    if (!ofmt_ctx) {
        LOGI("ffmpeg Could not create output context.");
        ret = AVERROR_UNKNOWN;
        goto end;
    }
    ofmt = ofmt_ctx->oformat;

    stream_mapping_size = ifmt_ctx->nb_streams;
    stream_mapping = (int *)av_mallocz(sizeof(int) * stream_mapping_size);

    for (int i = 0; i < ifmt_ctx->nb_streams; i++) {
        AVStream *in_stream = ifmt_ctx->streams[i];
        if (in_stream->codecpar->codec_type != AVMEDIA_TYPE_VIDEO &&
            in_stream->codecpar->codec_type != AVMEDIA_TYPE_AUDIO) {
            stream_mapping[i] = -1;
            continue;
        }
        stream_mapping[i] = out_stream_idx++;

        AVStream *out_stream = avformat_new_stream(ofmt_ctx, nullptr);
        if (!out_stream) {
            LOGI("ffmpeg Failed allocating output stream.");
            ret = AVERROR_UNKNOWN;
            goto end;
        }
        ret = avcodec_parameters_copy(out_stream->codecpar, in_stream->codecpar);
        if (ret < 0) {
            LOGI("ffmpeg Failed to copy codecpar");
            goto end;
        }
        out_stream->codecpar->codec_tag = 0;

        // ========= 重点：只初始化视频BSF（h264_mp4toannexb）=========
        if (in_stream->codecpar->codec_type == AVMEDIA_TYPE_VIDEO && !bsf_ctx) {
            const char *bsf_name = "h264_mp4toannexb";
            ret = av_bsf_alloc(av_bsf_get_by_name(bsf_name), &bsf_ctx);
            if (ret < 0) {
                LOGI("av_bsf_alloc failed");
                goto end;
            }
            avcodec_parameters_copy(bsf_ctx->par_in, in_stream->codecpar);
            ret = av_bsf_init(bsf_ctx);
            if (ret < 0) {
                LOGI("av_bsf_init failed");
                av_bsf_free(&bsf_ctx);
                bsf_ctx = nullptr;
                goto end;
            }
        }
    }

    av_dump_format(ofmt_ctx, 0, out_fileName, 1);

    // ====================== 打开输出文件 ======================
    if (!(ofmt->flags & AVFMT_NOFILE)) {
        ret = avio_open(&ofmt_ctx->pb, out_fileName, AVIO_FLAG_WRITE);
        if (ret < 0) {
            LOGI("ffmpeg Could not open output file %s", out_fileName);
            goto end;
        }
    }

    // ====================== 写文件头 ======================
    ret = avformat_write_header(ofmt_ctx, nullptr);
    if (ret < 0) {
        LOGI("ffmpeg avformat_write_header error");
        goto end;
    }
    LOGI("ffmpeg write header ok");

    // ====================== 循环读取Packet ======================
    av_init_packet(&pkt);
    while (true) {
        ret = av_read_frame(ifmt_ctx, &pkt);
        if (ret < 0) break;

        int in_idx = pkt.stream_index;
        int out_idx = stream_mapping[in_idx];
        if (out_idx < 0) {
            av_packet_unref(&pkt);
            continue;
        }

        AVStream *in_stream  = ifmt_ctx->streams[in_idx];
        AVStream *out_stream = ofmt_ctx->streams[out_idx];
        pkt.stream_index = out_idx;

        // 时间戳缩放
        pkt.pts = av_rescale_q_rnd(pkt.pts, in_stream->time_base, out_stream->time_base,
                                   (AVRounding)(AV_ROUND_NEAR_INF | AV_ROUND_PASS_MINMAX));
        pkt.dts = av_rescale_q_rnd(pkt.dts, in_stream->time_base, out_stream->time_base,
                                   (AVRounding)(AV_ROUND_NEAR_INF | AV_ROUND_PASS_MINMAX));
        pkt.duration = av_rescale_q(pkt.duration, in_stream->time_base, out_stream->time_base);
        pkt.pos = -1;

        // ========== 视频包经过BSF转换 AVCC -> AnnexB ==========
        if (in_stream->codecpar->codec_type == AVMEDIA_TYPE_VIDEO && bsf_ctx) {
            ret = av_bsf_send_packet(bsf_ctx, &pkt);
            av_packet_unref(&pkt);
            if (ret < 0) {
                LOGI("bsf send packet fail");
                break;
            }
            AVPacket out_pkt;
            av_init_packet(&out_pkt);
            while ((ret = av_bsf_receive_packet(bsf_ctx, &out_pkt)) == 0) {
                out_pkt.stream_index = out_idx;
                ret = av_write_frame(ofmt_ctx, &out_pkt);
                if (ret < 0) {
                    char errbuf[AV_ERROR_MAX_STRING_SIZE];
                    av_strerror(ret, errbuf, sizeof(errbuf));
                    LOGI("ffmpeg Error muxing packet. ret=%d, msg=%s", ret, errbuf);
                    av_packet_unref(&out_pkt);
                    break;
                }
                LOGI("ffmpeg Write frame %d", frame_index++);
                av_packet_unref(&out_pkt);
            }
            continue;
        }

        // ========== 音频包直接写入 ==========
        ret = av_write_frame(ofmt_ctx, &pkt);
        if (ret < 0) {
            char errbuf[AV_ERROR_MAX_STRING_SIZE];
            av_strerror(ret, errbuf, sizeof(errbuf));
            LOGI("ffmpeg Error muxing packet. ret=%d, msg=%s", ret, errbuf);
            av_packet_unref(&pkt);
            break;
        }
        LOGI("ffmpeg Write frame %d", frame_index++);
        av_packet_unref(&pkt);
    }

    // 冲刷BSF剩余缓存
    if (bsf_ctx) {
        ret = av_bsf_send_packet(bsf_ctx, nullptr);
        if (ret >= 0) {
            AVPacket out_pkt;
            av_init_packet(&out_pkt);
            while ((ret = av_bsf_receive_packet(bsf_ctx, &out_pkt)) == 0) {
                ret = av_write_frame(ofmt_ctx, &out_pkt);
                av_packet_unref(&out_pkt);
            }
        }
    }

    av_write_trailer(ofmt_ctx);

    end:
    // 释放资源
    env->ReleaseStringUTFChars(inputPath_j, inputPath);
    env->ReleaseStringUTFChars(outputPath_j, outputPath);

    if (bsf_ctx) av_bsf_free(&bsf_ctx);
    if (ifmt_ctx) avformat_close_input(&ifmt_ctx);
    if (ofmt_ctx) {
        if (ofmt && !(ofmt->flags & AVFMT_NOFILE)) {
            avio_close(ofmt_ctx->pb);
        }
        avformat_free_context(ofmt_ctx);
    }
    if (stream_mapping) av_free(stream_mapping);
    system("pause");
}