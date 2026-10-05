extern "C" {
#include <jni.h>
#include <android/native_window_jni.h>
#include <android/native_window.h>
#include <libavformat/avformat.h>
#include <libavcodec/avcodec.h>
#include <libswscale/swscale.h>
#include <libswresample/swresample.h>
#include <libavutil/imgutils.h>
#include <libavutil/time.h>
#include <pthread.h>
#include <malloc.h>
// OpenSL ES头文件
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>
#include "../../headerFile/LogUtils.h"

#define AUDIO_QUEUE_MAX 8
typedef struct {
    uint8_t* data;
    int size;
}PcmBlock;

// 拉流上下文
typedef struct {
    AVFormatContext* fmt_ctx;
    AVCodecContext* video_codec_ctx;
    AVCodecContext* audio_codec_ctx;

    int video_stream_idx;      // 视频流索引
    int audio_stream_idx;      // 音频流索引

    SwsContext* sws_ctx;       // 视频格式转换 YUV420P
    SwsContext* sws_rgba;       // 视频格式转换 RGBA8888
    SwrContext* swr_ctx;       // 音频重采样 PCM

    AVFrame* frame;            // 源图像
    AVFrame* frame_yuv;        // 输出YUV420P
    AVPacket* pkt;

    ANativeWindow* native_window;
    int video_width;
    int video_height;

    int audio_sample_rate;      // 采样数
    int audio_channels;         // 声道

    pthread_t pull_thread_id;
    int is_running;            // 拉流线程运行标记
    pthread_mutex_t mutex;

    //===== 音频PCM环形队列 =====
    PcmBlock pcm_queue[AUDIO_QUEUE_MAX];
    int q_head;
    int q_tail;
    pthread_mutex_t pcm_mtx;
    pthread_cond_t  pcm_cond;
    int audio_player_started;

    pthread_mutex_t time_mtx; // 专门保护时间戳读写
    double audio_clock;       // 音频时钟(秒)
    double video_pts_sec;     // 当前视频帧pts(秒)
} RtmpPullContext;

RtmpPullContext* g_pull_ctx = nullptr;

void* pull_thread(void* arg);

// OpenSL 音频回调函数，系统音频线程，只取PCM入队播放
void pcmPlayCallback(SLAndroidSimpleBufferQueueItf bq, void *context);
//初始化OpenSL播放器
static int createOpenSLPlayer(RtmpPullContext* ctx);
static void destroyOpenSLPlayer();

// ========= OpenSL ES 全局播放器句柄 =========
static SLObjectItf           sl_engine_obj = nullptr;
static SLEngineItf           sl_engine = nullptr;
static SLObjectItf           sl_player_obj = nullptr;
static SLPlayItf             sl_player = nullptr;
static SLAndroidSimpleBufferQueueItf sl_buffer_queue = nullptr;

//初始化拉流，传入rtmp地址、Surface
extern "C"
JNIEXPORT void JNICALL
Java_com_example_android_1study_1demo_1project_opencv_ffmpegUsage_pullRTMP_PullRtmpMainActivity_rtmpPullInit(
        JNIEnv *env, jobject thiz, jstring url, jobject surface) {

    //是否已经初始化过
    if(g_pull_ctx != nullptr){
        LOGE("already init");
        return;
    }
    const char* url_cstr = env->GetStringUTFChars(url, JNI_FALSE);

    g_pull_ctx = (RtmpPullContext*)malloc(sizeof(RtmpPullContext));
    memset(g_pull_ctx, 0, sizeof(RtmpPullContext));

    //初始化互斥锁
    pthread_mutex_init(&g_pull_ctx->mutex, nullptr);

    //初始化时间
    pthread_mutex_init(&g_pull_ctx->time_mtx, nullptr);
    g_pull_ctx->audio_clock = 0.0;
    g_pull_ctx->video_pts_sec = 0.0;

    //====初始化音频队列锁与条件变量====
    pthread_mutex_init(&g_pull_ctx->pcm_mtx, nullptr);
    pthread_cond_init(&g_pull_ctx->pcm_cond, nullptr);
    g_pull_ctx->q_head = 0;
    g_pull_ctx->q_tail = 0;
    g_pull_ctx->audio_player_started = 0;

    //初始化拉流线程运行标记
    g_pull_ctx->is_running = 0;

    // 获取ANativeWindow
    if(surface != nullptr){
        g_pull_ctx->native_window = ANativeWindow_fromSurface(env, surface);
    }

    // 1. 打开RTMP流
    AVDictionary* opts = nullptr;//参数
    // RTMP超时配置
    av_dict_set(&opts, "rtmp_timeout", "5000000", 0);
    av_dict_set(&opts, "stimeout", "5000000", 0);

    //打开媒体输入流 / 文件
    int ret = avformat_open_input(&g_pull_ctx->fmt_ctx, url_cstr, nullptr, &opts);
    av_dict_free(&opts);
    if(ret < 0){
        char err[AV_ERROR_MAX_STRING_SIZE]={0};
        av_strerror(ret, err, sizeof(err));
        LOGE("avformat_open_input failed:%s", err);
        goto fail;
    }

    // 获取流信息
    ret = avformat_find_stream_info(g_pull_ctx->fmt_ctx, nullptr);
    if(ret <0){
        LOGE("avformat_find_stream_info fail");
        goto fail;
    }

    // 查找视频、音频流索引
    g_pull_ctx->video_stream_idx = -1;
    g_pull_ctx->audio_stream_idx = -1;
    for(int i=0; i<g_pull_ctx->fmt_ctx->nb_streams; i++){
        AVStream* st = g_pull_ctx->fmt_ctx->streams[i];
        if(st->codecpar->codec_type == AVMEDIA_TYPE_VIDEO){
            g_pull_ctx->video_stream_idx = i;
        }else if(st->codecpar->codec_type == AVMEDIA_TYPE_AUDIO){
            g_pull_ctx->audio_stream_idx = i;
        }
    }

    // ========== 视频解码器 ==========
    if(g_pull_ctx->video_stream_idx != -1){
        /** 获取解码器并打开 **/
        AVCodecParameters* codecpar = g_pull_ctx->fmt_ctx->streams[g_pull_ctx->video_stream_idx]->codecpar;
        //根据编码 ID，在 FFmpeg 内置解码器库中，找到对应的解码器描述信息
        const AVCodec* codec = avcodec_find_decoder(codecpar->codec_id);
        if(!codec){
            LOGE("find video decoder null");
            goto fail;
        }
        //根据解码器模板，分配解码器上下文实例
        g_pull_ctx->video_codec_ctx = avcodec_alloc_context3(codec);
        //将流的编码参数复制到解码器上下文
        avcodec_parameters_to_context(g_pull_ctx->video_codec_ctx, codecpar);
        //正式打开解码器，初始化解码器内部状态，让解码器就绪，可以开始接收数据包解码
        ret = avcodec_open2(g_pull_ctx->video_codec_ctx, codec, nullptr);
        if(ret <0){
            LOGE("open video decoder fail");
            goto fail;
        }
        g_pull_ctx->video_width = g_pull_ctx->video_codec_ctx->width;
        g_pull_ctx->video_height = g_pull_ctx->video_codec_ctx->height;

        // 分配帧
        //分配两个AVFrame结构体
        g_pull_ctx->frame = av_frame_alloc();
        g_pull_ctx->frame_yuv = av_frame_alloc();
        //计算YUV420P图像需要多少字节内存
        int yuv_size = av_image_get_buffer_size(AV_PIX_FMT_YUV420P,
                                                g_pull_ctx->video_width, g_pull_ctx->video_height, 1);
        //申请一块连续内存，用来存放YUV420P数据
        uint8_t* yuv_buf = (uint8_t*)av_malloc(yuv_size);
        //把这块内存绑定到 frame_yuv 的 data/linesize 指针
        av_image_fill_arrays(g_pull_ctx->frame_yuv->data,
                             g_pull_ctx->frame_yuv->linesize,
                             yuv_buf,
                             AV_PIX_FMT_YUV420P,
                             g_pull_ctx->video_width, g_pull_ctx->video_height, 1);

        // 格式转换上下文：解码出来的原始像素格式 -> YUV420P,循环解码拿到frame后，执行转换
        //(sws_getContext: FFmpeg 里面 libswscale)
        g_pull_ctx->sws_ctx = sws_getContext(
                g_pull_ctx->video_codec_ctx->width,//源图像宽
                g_pull_ctx->video_codec_ctx->height,//源图像高
                g_pull_ctx->video_codec_ctx->pix_fmt,//源像素格式
                g_pull_ctx->video_width,//目标宽
                g_pull_ctx->video_height,//目标高
                AV_PIX_FMT_YUV420P,//目标像素格式
                SWS_BILINEAR, nullptr, nullptr, nullptr
        );

        // 源：YUV420P；目标：RGBA8888
        g_pull_ctx->sws_rgba = sws_getContext(
                g_pull_ctx->video_width,
                g_pull_ctx->video_height,
                AV_PIX_FMT_YUV420P,      //输入：frame_yuv 是YUV420P
                g_pull_ctx->video_width,
                g_pull_ctx->video_height,
                AV_PIX_FMT_RGBA,         //输出RGBA，给ANativeWindow
                SWS_BILINEAR,
                nullptr,nullptr,nullptr
        );
    }

    // ========== 音频解码器 ==========
    if(g_pull_ctx->audio_stream_idx != -1){
        /** 获取解码器并打开 **/
        AVCodecParameters* codecpar = g_pull_ctx->fmt_ctx->streams[g_pull_ctx->audio_stream_idx]->codecpar;
        //根据编码 ID，在 FFmpeg 内置解码器库中，找到对应的解码器描述信息
        const AVCodec* codec = avcodec_find_decoder(codecpar->codec_id);
        if(!codec){
            LOGE("find audio decoder null");
            goto fail;
        }
        //根据解码器模板，分配解码器上下文实例
        g_pull_ctx->audio_codec_ctx = avcodec_alloc_context3(codec);
        //将流的编码参数复制到解码器上下文
        avcodec_parameters_to_context(g_pull_ctx->audio_codec_ctx, codecpar);
        //打开解码器
        ret = avcodec_open2(g_pull_ctx->audio_codec_ctx, codec, nullptr);
        if(ret <0){
            LOGE("open audio decoder fail");
            goto fail;
        }
        g_pull_ctx->audio_sample_rate = g_pull_ctx->audio_codec_ctx->sample_rate;//采样数
        g_pull_ctx->audio_channels = g_pull_ctx->audio_codec_ctx->channels;// 声道

        // 音频重采样：输出 16bit PCM 立体声
        g_pull_ctx->swr_ctx = swr_alloc_set_opts(nullptr,
                                                 av_get_default_channel_layout(g_pull_ctx->audio_channels),// 输出：声道布局
                                                 AV_SAMPLE_FMT_S16,// 输出：采样格式
                                                 g_pull_ctx->audio_sample_rate,// 输出：采样率
                                                 av_get_default_channel_layout(g_pull_ctx->audio_channels),// 输入：声道布局
                                                 g_pull_ctx->audio_codec_ctx->sample_fmt,// 输入：采样格式
                                                 g_pull_ctx->audio_sample_rate,// 输入：采样率
                                                 0, nullptr);
        //分配完参数配置后，初始化重采样器内部状态
        swr_init(g_pull_ctx->swr_ctx);
    }

    //分配一个 `AVPacket` 结构体实例
    g_pull_ctx->pkt = av_packet_alloc();

    env->ReleaseStringUTFChars(url, url_cstr);
    LOGI("rtmp pull init ok, w=%d h=%d sample_rate=%d channels=%d",
         g_pull_ctx->video_width, g_pull_ctx->video_height,
         g_pull_ctx->audio_sample_rate, g_pull_ctx->audio_channels);
    return;

    fail:
    // 清理
    if(g_pull_ctx){
        if(g_pull_ctx->fmt_ctx) avformat_close_input(&g_pull_ctx->fmt_ctx);
        if(g_pull_ctx->video_codec_ctx) avcodec_free_context(&g_pull_ctx->video_codec_ctx);
        if(g_pull_ctx->audio_codec_ctx) avcodec_free_context(&g_pull_ctx->audio_codec_ctx);
        av_frame_free(&g_pull_ctx->frame);
        av_frame_free(&g_pull_ctx->frame_yuv);
        av_packet_free(&g_pull_ctx->pkt);
        sws_freeContext(g_pull_ctx->sws_ctx);
        sws_freeContext(g_pull_ctx->sws_rgba);
        swr_free(&g_pull_ctx->swr_ctx);
        pthread_mutex_destroy(&g_pull_ctx->mutex);
        pthread_mutex_destroy(&g_pull_ctx->pcm_mtx);
        pthread_cond_destroy(&g_pull_ctx->pcm_cond);
        pthread_mutex_destroy(&g_pull_ctx->time_mtx);
        free(g_pull_ctx);
        g_pull_ctx = nullptr;
    }
    env->ReleaseStringUTFChars(url, url_cstr);
}

//==== 新增JNI：开启音频播放器 ====
extern "C" JNIEXPORT void JNICALL Java_com_example_android_1study_1demo_1project_opencv_ffmpegUsage_pullRTMP_PullRtmpMainActivity_rtmpAudioPlayStart(
        JNIEnv *env, jobject thiz)
{
    if(g_pull_ctx == nullptr || g_pull_ctx->audio_player_started) return;
    int ret = createOpenSLPlayer(g_pull_ctx);
    if(ret ==0){
        g_pull_ctx->audio_player_started = 1;
        LOGI("OpenSL create ok");
    }else{
        LOGE("OpenSL create failed");
    }
}
//==== 新增JNI：停止音频播放器 ====
extern "C" JNIEXPORT void JNICALL Java_com_example_android_1study_1demo_1project_opencv_ffmpegUsage_pullRTMP_PullRtmpMainActivity_rtmpAudioPlayStop(
        JNIEnv *env, jobject thiz)
{
    if(g_pull_ctx){
        g_pull_ctx->audio_player_started = 0;
        pthread_mutex_lock(&g_pull_ctx->pcm_mtx);
        pthread_cond_signal(&g_pull_ctx->pcm_cond);
        pthread_mutex_unlock(&g_pull_ctx->pcm_mtx);
    }
    destroyOpenSLPlayer();
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_android_1study_1demo_1project_opencv_ffmpegUsage_pullRTMP_PullRtmpMainActivity_rtmpPullStart(
        JNIEnv *env, jobject thiz) {
    if(g_pull_ctx == nullptr || g_pull_ctx->is_running){
        return;
    }
    //把新线程加入内核调度队列
    pthread_create(&g_pull_ctx->pull_thread_id, nullptr, pull_thread, g_pull_ctx);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_android_1study_1demo_1project_opencv_ffmpegUsage_pullRTMP_PullRtmpMainActivity_rtmpPullStop(
        JNIEnv *env, jobject thiz) {
    //没有初始化
    if(g_pull_ctx == nullptr) return;

    //修改当前状态
    g_pull_ctx->is_running = 0;

    //冲刷swr残留音频缓存，把剩下PCM送入队列
    if(g_pull_ctx->swr_ctx && g_pull_ctx->audio_player_started)
    {
        uint8_t* out_buf = nullptr;
        int out_samples = 4096;
        av_samples_alloc(&out_buf, nullptr, g_pull_ctx->audio_channels, out_samples, AV_SAMPLE_FMT_S16,0);
        int ret_swr;
        while(1)
        {
            ret_swr = swr_convert(g_pull_ctx->swr_ctx, &out_buf, out_samples, nullptr,0);
            if(ret_swr <= 0) break;
            int out_bytes = ret_swr * g_pull_ctx->audio_channels * 2;
            //送入PCM队列
            pthread_mutex_lock(&g_pull_ctx->pcm_mtx);
            int next_tail = (g_pull_ctx->q_tail + 1) % AUDIO_QUEUE_MAX;
            if(next_tail != g_pull_ctx->q_head)
            {
                uint8_t* copy_data = (uint8_t*)malloc(out_bytes);
                memcpy(copy_data, out_buf, out_bytes);
                g_pull_ctx->pcm_queue[g_pull_ctx->q_tail].data = copy_data;
                g_pull_ctx->pcm_queue[g_pull_ctx->q_tail].size = out_bytes;
                g_pull_ctx->q_tail = next_tail;
                pthread_cond_signal(&g_pull_ctx->pcm_cond);
            }
            pthread_mutex_unlock(&g_pull_ctx->pcm_mtx);
        }
        av_freep(&out_buf);
    }

    //阻塞等待(执行到这一行，卡住不动，一直等到pull_thread子线程执行结束退出，才继续往下执行后面代码)
    pthread_join(g_pull_ctx->pull_thread_id, nullptr);

    //清空PCM队列内存
    pthread_mutex_lock(&g_pull_ctx->pcm_mtx);
    while(g_pull_ctx->q_head != g_pull_ctx->q_tail)
    {
        free(g_pull_ctx->pcm_queue[g_pull_ctx->q_head].data);
        g_pull_ctx->pcm_queue[g_pull_ctx->q_head].data = nullptr;
        g_pull_ctx->pcm_queue[g_pull_ctx->q_head].size = 0;
        g_pull_ctx->q_head = (g_pull_ctx->q_head + 1) % AUDIO_QUEUE_MAX;
    }
    pthread_mutex_unlock(&g_pull_ctx->pcm_mtx);

    // 释放所有资源
    if(g_pull_ctx->fmt_ctx) avformat_close_input(&g_pull_ctx->fmt_ctx);
    if(g_pull_ctx->video_codec_ctx) avcodec_free_context(&g_pull_ctx->video_codec_ctx);
    if(g_pull_ctx->audio_codec_ctx) avcodec_free_context(&g_pull_ctx->audio_codec_ctx);

    av_frame_free(&g_pull_ctx->frame);
    av_frame_free(&g_pull_ctx->frame_yuv);
    av_packet_free(&g_pull_ctx->pkt);

    sws_freeContext(g_pull_ctx->sws_ctx);
    swr_free(&g_pull_ctx->swr_ctx);

    if(g_pull_ctx->native_window){
        ANativeWindow_release(g_pull_ctx->native_window);
        g_pull_ctx->native_window = nullptr;
    }

    pthread_mutex_destroy(&g_pull_ctx->pcm_mtx);
    pthread_cond_destroy(&g_pull_ctx->pcm_cond);
    pthread_mutex_destroy(&g_pull_ctx->mutex);//销毁互斥锁，释放 mutex 占用的系统资源
    pthread_mutex_destroy(&g_pull_ctx->time_mtx);//销毁时间
    free(g_pull_ctx);//释放结构体
    g_pull_ctx = nullptr;
}
extern "C"
JNIEXPORT jdouble JNICALL
Java_com_example_android_1study_1demo_1project_opencv_ffmpegUsage_pullRTMP_PullRtmpMainActivity_getGlobalAudioClock(
        JNIEnv *env, jobject thiz) {
    if (!g_pull_ctx) {
        return 0.0;
    }
    pthread_mutex_lock(&g_pull_ctx->time_mtx);
    double ret = g_pull_ctx->audio_clock;
    pthread_mutex_unlock(&g_pull_ctx->time_mtx);
    return ret;
}
extern "C"
JNIEXPORT jdouble JNICALL
Java_com_example_android_1study_1demo_1project_opencv_ffmpegUsage_pullRTMP_PullRtmpMainActivity_getGlobalVideoPtsInSeconds(
        JNIEnv *env, jobject thiz) {
    if (!g_pull_ctx) {
        return 0.0;
    }
    pthread_mutex_lock(&g_pull_ctx->time_mtx);
    double ret = g_pull_ctx->video_pts_sec;
    pthread_mutex_unlock(&g_pull_ctx->time_mtx);
    return ret;
}

/** 拉流线程函数 **/
//循环读取 packet，解码视频 / 音频。
//视频解码后输出 YUV420P，可以渲染到 ANativeWindow；
//音频解码重采样输出 S16 PCM，交给音频播放。
void* pull_thread(void* arg)
{
    //启动线程时，把全局的g_pull_ctx（RtmpPullContext 结构体指针）作为参数传入，变成 arg
    RtmpPullContext* ctx = (RtmpPullContext*)arg;
    //修改拉流线程运行标记
    ctx->is_running = 1;

    while(ctx->is_running){
        //从媒体封装上下文（fmt_ctx，也就是你的 RTMP 流）读取下一个压缩音视频包，存入 AVPacket pkt。
        //读到的是还没有解码的 H264/AAC 压缩码流，不是 YUV/PCM 原始帧
        int ret = av_read_frame(ctx->fmt_ctx, ctx->pkt);
        if(ret < 0){
            // 流结束或者网络断开
            LOGE("av_read_frame ret=%d", ret);
            break;
        }

        // 视频流
        if(ctx->pkt->stream_index == ctx->video_stream_idx){
            //把压缩的 H264 数据包（AVPacket）送入视频解码器，排队等待解码
            ret = avcodec_send_packet(ctx->video_codec_ctx, ctx->pkt);
            if(ret < 0){
                // 送入失败，释放这个包的数据，跳过本次，继续下一轮循环读取包
                av_packet_unref(ctx->pkt);
                continue;
            }
            //成功
            while(ret >=0){
                //从解码器取出已经解码完成的原始音视频帧（AVFrame）
                ret = avcodec_receive_frame(ctx->video_codec_ctx, ctx->frame);
                if(ret == AVERROR(EAGAIN) || ret == AVERROR_EOF){
                    break;
                }else if(ret <0){
                    LOGE("video receive frame fail");
                    break;
                }
                // frame->pts 转为秒
                double pts_sec = ctx->frame->pts * av_q2d(ctx->video_codec_ctx->pkt_timebase);

                pthread_mutex_lock(&ctx->time_mtx);
                ctx->video_pts_sec = pts_sec;
                pthread_mutex_unlock(&ctx->time_mtx);

                // 转换为YUV420P
                sws_scale(ctx->sws_ctx,
                          ctx->frame->data,//源图像各个平面指针数组（解码器输出的原始 AVFrame，data[0]=Y，data[1]=U，data[2]=V）
                          ctx->frame->linesize,//源每个平面的行跨度 stride（一行占用字节，有可能大于图像宽度，内存对齐造成）
                          0,//从源图像第几行开始转换，0 = 从图像最顶部第一行开始
                          ctx->video_codec_ctx->height,//一共转换多少行，整张图像的高度，一次性转换完整一帧
                          ctx->frame_yuv->data,//目标图像平面指针，输出结果（分配好的 YUV420P 缓冲区）
                          ctx->frame_yuv->linesize);//目标图像行跨度

                // ========== 渲染到ANativeWindow ==========
                if(ctx->native_window != nullptr){
                    ANativeWindow_Buffer buf;
//                    //ANativeWindow_lock 直接往 Android Surface 上绘制图像
//                    if(ANativeWindow_lock(ctx->native_window, &buf, nullptr) == 0){
//                        // 把YUV420P拷贝到window buffer（注意：window是RGBA，需要额外转换；
//                        // 如果你要直接渲染YUV，建议用OpenGL ES；ANativeWindow只支持RGB格式）
//                        // 这里只是示意，实际需要sws_scale转RGBA
//                        ANativeWindow_unlockAndPost(ctx->native_window);
//                    }
                    //锁定Surface缓冲区
                    int lockRet = ANativeWindow_lock(ctx->native_window, &buf, nullptr);
                    if(lockRet == 0)
                    {
                        // ANativeWindow_Buffer 的bits是RGBA内存，stride是行跨度
                        uint8_t *dst_data[1];
                        int dst_linesize[1];

                        // 目标buffer：buf.bits，行stride使用buf.stride（非常重要！不能直接用width）
                        dst_data[0] = (uint8_t*)buf.bits;
                        dst_linesize[0] = buf.stride * 4; // RGBA每个像素4字节，stride单位是像素

                        // sws_scale：YUV420P → RGBA，直接输出到Surface buffer
                        sws_scale(ctx->sws_rgba,
                                  ctx->frame_yuv->data,
                                  ctx->frame_yuv->linesize,
                                  0,
                                  ctx->video_height,
                                  dst_data,
                                  dst_linesize);

                        //解锁并提交画面上屏
                        ANativeWindow_unlockAndPost(ctx->native_window);
                    }

                }

                // 【这里可以把frame_yuv数据回调给Java层，做画面处理】
            }
        }
        // 音频流
        else if(ctx->pkt->stream_index == ctx->audio_stream_idx){
            //送入音频解码器，排队等待解码
            ret = avcodec_send_packet(ctx->audio_codec_ctx, ctx->pkt);
            if(ret <0){
                //失败，释放这个包资源
                av_packet_unref(ctx->pkt);
                continue;
            }
            while(ret >=0){
                //从解码器取出已经解码完成的原始音频帧
                ret = avcodec_receive_frame(ctx->audio_codec_ctx, ctx->frame);
                if(ret == AVERROR(EAGAIN) || ret == AVERROR_EOF){
                    break;
                }else if(ret <0){
                    LOGE("audio receive frame fail");
                    break;
                }
                // 重采样输出 S16 PCM
                uint8_t* out_buf = nullptr;
                //每个声道的采样点数(预估值)
                //swr_get_out_samples:预估输入in_nb_samples个采样点，经过重采样器之后最多能输出多少采样点
                //av_rescale(a,b,c) = (int64_t)a * b / c，FFmpeg 安全乘法除法，防止 int64 溢出
                int out_samples = av_rescale(swr_get_out_samples(ctx->swr_ctx, ctx->frame->nb_samples),//swr 预估的输出采样数
                                             ctx->audio_sample_rate,//目标采样率
                                             ctx->audio_codec_ctx->sample_rate//源采样率
                                             );
                //分配一块连续的音频 PCM 内存，适配 FFmpeg 音频帧的内存布局
                //&out_buf: 输出参数，存放各声道数据起始指针的数组
                //对于平面格式 (FLTP)：每个声道独立一块内存
                //对于打包格式 S16（AV_SAMPLE_FMT_S16，packed）：所有声道数据交错存在同一块连续内存，out_buf[0]就是整块 PCM 起始地址
                av_samples_alloc(&out_buf,
                                 nullptr,// 输出每个声道一行字节大小，不需要就传 nullptr
                                 ctx->audio_channels,// 声道数量（1 单声道 / 2 立体声）
                                 out_samples,// 每个声道的采样点数
                                 AV_SAMPLE_FMT_S16,//采样格式
                                 0);//内存对齐 align，0 = 使用 FFmpeg 默认对齐；1 = 不对齐

                // 执行音频重采样、采样格式转换（FLTP → S16 packed）
                //返回值 = 实际输出的单声道采样点数
                int ret_swr = swr_convert(ctx->swr_ctx,
                                          &out_buf,// 输出缓冲区数组
                                          out_samples,//输出端**最多能存放多少采样点（单声道）**
                                          (const uint8_t**)ctx->frame->data, //输入源
                                          ctx->frame->nb_samples);//输入这一帧，每个声道有多少采样点
                if(ret_swr >0){
                    //2：AV_SAMPLE_FMT_S16，每个采样占 2 字节
                    int out_bytes = ret_swr * ctx->audio_channels * 2;
                    // out_buf 就是16bit PCM，可以喂OpenSL ES播放

                    // out_buf 就是16bit PCM，送入PCM环形队列
                    pthread_mutex_lock(&ctx->pcm_mtx);
                    int next_tail = (ctx->q_tail + 1) % AUDIO_QUEUE_MAX;
                    if(next_tail != ctx->q_head && ctx->audio_player_started)
                    {
                        uint8_t* pcm_copy = (uint8_t*)malloc(out_bytes);
                        memcpy(pcm_copy, out_buf, out_bytes);
                        ctx->pcm_queue[ctx->q_tail].data = pcm_copy;
                        ctx->pcm_queue[ctx->q_tail].size = out_bytes;
                        ctx->q_tail = next_tail;
                        pthread_cond_signal(&ctx->pcm_cond);
                    }
                    pthread_mutex_unlock(&ctx->pcm_mtx);
                }
                //释放申请的out_buf区域
                av_freep(&out_buf);
            }
        }
        //释放 AVPacket 内部引用的压缩码流缓冲区
        av_packet_unref(ctx->pkt);
    }

    ctx->is_running = 0;
    return nullptr;
}

// OpenSL 音频回调函数，系统音频线程，只取PCM入队播放
void pcmPlayCallback(SLAndroidSimpleBufferQueueItf bq, void *context)
{
    RtmpPullContext* ctx = (RtmpPullContext*)context;
    uint8_t* pcm_data = nullptr;
    int pcm_size = 0;

    pthread_mutex_lock(&ctx->pcm_mtx);
    while(ctx->q_head == ctx->q_tail && ctx->audio_player_started)
    {
        pthread_cond_wait(&ctx->pcm_cond, &ctx->pcm_mtx);
    }
    if(ctx->q_head != ctx->q_tail)
    {
        PcmBlock* blk = &ctx->pcm_queue[ctx->q_head];
        pcm_data = blk->data;
        pcm_size = blk->size;
        ctx->q_head = (ctx->q_head + 1) % AUDIO_QUEUE_MAX;
    }
    pthread_mutex_unlock(&ctx->pcm_mtx);

    if(pcm_data && pcm_size>0)
    {
        (*bq)->Enqueue(bq, pcm_data, pcm_size);

        //更新音频时钟
        // ========== 新增：计算这段PCM时长，更新audio_clock ==========
        // S16 每个采样2字节，总采样数 = 总字节 / (声道 * 2)
        int samples_per_block = pcm_size / (ctx->audio_channels * 2);
        double block_duration_sec = (double)samples_per_block / ctx->audio_sample_rate;

        pthread_mutex_lock(&ctx->time_mtx);
        ctx->audio_clock += block_duration_sec;
        pthread_mutex_unlock(&ctx->time_mtx);
        // ============================================================
    }
}

//初始化OpenSL播放器
static int createOpenSLPlayer(RtmpPullContext* ctx)
{
    SLresult ret;
    //1. 创建引擎
    ret = slCreateEngine(&sl_engine_obj,0,
                         nullptr,0,nullptr,nullptr);
    if(ret != SL_RESULT_SUCCESS) return -1;
    (*sl_engine_obj)->Realize(sl_engine_obj, SL_BOOLEAN_FALSE);
    (*sl_engine_obj)->GetInterface(sl_engine_obj, SL_IID_ENGINE, &sl_engine);

    //输出混音器
    SLObjectItf mix_obj = nullptr;
    SLEffectSendItf mix_effect_send;
    SLVolumeItf mix_vol;
    ret = (*sl_engine)->CreateOutputMix(sl_engine, &mix_obj,0,nullptr,nullptr);
    (*mix_obj)->Realize(mix_obj, SL_BOOLEAN_FALSE);
    (*mix_obj)->GetInterface(mix_obj, SL_IID_EFFECTSEND, &mix_effect_send);
    (*mix_obj)->GetInterface(mix_obj, SL_IID_VOLUME, &mix_vol);

    //PCM格式配置，S16小端
    SLDataFormat_PCM pcmFormat;
    pcmFormat.formatType = SL_DATAFORMAT_PCM;
    pcmFormat.numChannels = ctx->audio_channels;
    pcmFormat.samplesPerSec = ctx->audio_sample_rate * 1000; //重点！milliHz
    pcmFormat.bitsPerSample = SL_PCMSAMPLEFORMAT_FIXED_16;
    pcmFormat.containerSize = SL_PCMSAMPLEFORMAT_FIXED_16;
    if(ctx->audio_channels == 1){
        pcmFormat.channelMask = SL_SPEAKER_FRONT_CENTER;
    }else{
        pcmFormat.channelMask = SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT;
    }
    pcmFormat.endianness = SL_BYTEORDER_LITTLEENDIAN;

    SLDataSource src;
    SLDataSink sink;
    SLAndroidSimpleBufferQueueItf bufferQueueItf;
    SLInterfaceID ids[2] = {SL_IID_BUFFERQUEUE, SL_IID_VOLUME};
    SLboolean req[2] = {SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE};

    src.pLocator = nullptr;
    src.pFormat = &pcmFormat;
    // ========= 修复这一段 =========
    SLDataLocator_OutputMix outputMixLocator;
    outputMixLocator.locatorType = SL_DATALOCATOR_OUTPUTMIX;
    outputMixLocator.outputMix = mix_obj;
    sink.pLocator = &outputMixLocator;
    sink.pFormat = nullptr;
    // ==============================

    ret = (*sl_engine)->CreateAudioPlayer(sl_engine, &sl_player_obj, &src, &sink, 2, ids, req);
    if(ret != SL_RESULT_SUCCESS) return -1;
    (*sl_player_obj)->Realize(sl_player_obj, SL_BOOLEAN_FALSE);
    (*sl_player_obj)->GetInterface(sl_player_obj, SL_IID_PLAY, &sl_player);
    (*sl_player_obj)->GetInterface(sl_player_obj, SL_IID_ANDROIDSIMPLEBUFFERQUEUE, &bufferQueueItf);
    sl_buffer_queue = bufferQueueItf;

    //注册回调，传入上下文ctx
    (*sl_buffer_queue)->RegisterCallback(sl_buffer_queue, pcmPlayCallback, ctx);
    (*sl_player)->SetPlayState(sl_player, SL_PLAYSTATE_PLAYING);

    //预触发一次回调
    pcmPlayCallback(sl_buffer_queue, ctx);
    return 0;
}
//释放OpenSL
static void destroyOpenSLPlayer()
{
    if(sl_player_obj){
        (*sl_player_obj)->Destroy(sl_player_obj);
        sl_player_obj = nullptr;
        sl_player = nullptr;
        sl_buffer_queue = nullptr;
    }
    if(sl_engine_obj){
        (*sl_engine_obj)->Destroy(sl_engine_obj);
        sl_engine_obj = nullptr;
        sl_engine = nullptr;
    }
}

}