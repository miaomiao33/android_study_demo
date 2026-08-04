#include "ffmpeg_get_frame.h"
#include <string>
#include <memory>
#include <thread>
#include <iostream>

using namespace std;
//
// Created by Machenike on 2026/7/29.
//

AVFormatContext *inputContext = NULL;
AVFormatContext *outputContext;
int64_t lastReadPacktTime;

static int interrupt_cb(void *ctx)
{
    int timeout = 3;
    if(av_gettime() - lastReadPacktTime > timeout * 1000 * 1000)
    {
        return -1;
    }
    return 0;
}
int OpenInput(string inputUrl)
{
    inputContext = avformat_alloc_context();
    lastReadPacktTime = av_gettime();
    inputContext->interrupt_callback.callback = interrupt_cb;
    int ret = avformat_open_input(&inputContext, inputUrl.c_str(),
                                  nullptr, nullptr);
    if(ret < 0)
    {
        av_log(NULL, AV_LOG_ERROR,
               "Input file open input failed.\n");
        return ret;
    }
    ret = avformat_find_stream_info(inputContext, nullptr);
    if(ret < 0)
    {
        av_log(NULL, AV_LOG_ERROR,
               "Find input file stream information failed.\n");
    } else{
        av_log(NULL, AV_LOG_FATAL,
               "Open input file %s success.\n", inputUrl.c_str());
    }

    return ret;
}

shared_ptr<AVPacket> ReadPacketFromSource()
{
    shared_ptr<AVPacket> packet(static_cast<AVPacket*> (av_malloc(sizeof(AVPacket))),
                                [&] (AVPacket *p){
                                                             av_packet_free(&p);
                                                             av_freep(&p);} );
    av_init_packet(packet.get());
    lastReadPacktTime = av_gettime();
    int ret = av_read_frame(inputContext, packet.get());
    if(ret >= 0)
    {
        return packet;
    } else
    {
        return nullptr;
    }
}

int OpenOutput(string outUrl)
{
    int ret = avformat_alloc_output_context2(&outputContext, nullptr,
                                             "singlejpeg", outUrl.c_str());
    if(ret < 0)
    {
        av_log(NULL, AV_LOG_ERROR,
               "Open output context failed.\n");
        goto Error;
    }

    ret = avio_open2(&outputContext->pb, outUrl.c_str(),AVIO_FLAG_WRITE,
                     nullptr, nullptr);
    if(ret < 0)
    {
        av_log(NULL, AV_LOG_ERROR,
               "Open avio failed.\n");
        goto Error;
    }
    for (int i = 0; i < inputContext->nb_streams; i++)
    {
        if(inputContext->streams[i]->codec->codec_type == AVMediaType::AVMEDIA_TYPE_AUDIO)
        {
            continue;
        }

        AVStream *stream = avformat_new_stream(outputContext,
                                               inputContext->streams[i]->codec->codec);
        ret = avcodec_copy_context(stream->codec, inputContext->streams[i]->codec);
        if(ret < 0)
        {
            av_log(NULL, AV_LOG_ERROR,
                   "Copy codec context failed.\n");
            goto Error;
        }
    }

    ret = avformat_write_header(outputContext, nullptr);
    if(ret < 0)
    {
        av_log(NULL, AV_LOG_ERROR,
               "Format write header failed.\n");
        goto Error;
    }

    av_log(NULL, AV_LOG_FATAL,
           "Open output file success %s.\n", outUrl.c_str());
    return ret;

    Error:
         if(outputContext)
         {
             for(int i = 0;i < outputContext->nb_streams;i++)
             {
                 avcodec_close(outputContext->streams[i]->codec);
             }
             avformat_close_input(&outputContext);
         }

    return ret;
}

void Init()
{
    av_register_all();
    avfilter_register_all();
    avformat_network_init();
    av_log_set_level(AV_LOG_WARNING);
}

void CloseInput()
{
    if(inputContext != nullptr)
    {
        avformat_close_input(&inputContext);
    }
}

void CloseOutput()
{
    if(outputContext != nullptr)
    {
        int ret = av_write_trailer(outputContext);
        for(int i = 0;i < outputContext->nb_streams;i++)
        {
            AVCodecContext *codecContext = outputContext->streams[i]->codec;
            avcodec_close(codecContext);
        }
        avformat_close_input(&outputContext);
    }
}

int WritePacket(shared_ptr<AVPacket> packet)
{
    auto inputStream = inputContext->streams[packet->stream_index];
    auto outputStream = outputContext->streams[packet->stream_index];
    return av_interleaved_write_frame(outputContext, packet.get());
}

int InitDecodeContext(AVStream *inputStream)
{
    auto codecId = inputStream->codec->codec_id;
    auto codec = avcodec_find_decoder(codecId);
    if(!codec)
    {
        return -1;
    }
    int ret = avcodec_open2(inputStream->codec, codec, NULL);
    return ret;
}

int InitEncoderCodec(AVStream *inputStream, AVCodecContext **encodeContext)
{
    AVCodec *picCodec;
    picCodec = avcodec_find_encoder(AV_CODEC_ID_MJPEG);
    (*encodeContext) = avcodec_alloc_context3(picCodec);

    (*encodeContext)->codec_id = picCodec->id;
    (*encodeContext)->time_base.num = inputStream->codec->time_base.num;
    (*encodeContext)->time_base.den = inputStream->codec->time_base.den;
    (*encodeContext)->pix_fmt = *picCodec->pix_fmts;
    (*encodeContext)->width = inputStream->codec->width;
    (*encodeContext)->height = inputStream->codec->height;
    int ret = avcodec_open2((*encodeContext), picCodec, nullptr);
    if(ret < 0)
    {
        std::cout<<"Open video codec failed.\n"<<endl;
        return ret;
    }
    return 1;
}

bool Decode(AVStream *inputStream, AVPacket *packet, AVFrame *frame)
{
    int gotFrame = 0;
    auto hr = avcodec_decode_video2(inputStream->codec, frame,
                                    &gotFrame, packet);
    if(hr >= 0 && gotFrame != 0)
    {
        return true;
    }
    return false;
}

std::shared_ptr<AVPacket> Encode(AVCodecContext *encodeContext, AVFrame *frame)
{
    int gotOutput = 0;
    std::shared_ptr<AVPacket> pkt(static_cast<AVPacket*>(av_malloc(sizeof(AVPacket))),
                                  [&](AVPacket *p){
                                                             av_packet_free(&p);
                                                             av_freep(&p);
                                                             });
    av_init_packet(pkt.get());
    pkt->data = NULL;
    pkt->size = 0;
    int ret = avcodec_encode_video2(encodeContext, pkt.get(),
                                    frame, &gotOutput);
    if(ret >= 0 && gotOutput)
    {
        return pkt;
    } else
    {
        return nullptr;
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_android_1study_1demo_1project_opencv_ffmpegUsage_FFmpegUsageMainActivity_getFrameFromStream(
        JNIEnv *env, jobject thiz, jstring path_j) {
    const char* path = env->GetStringUTFChars(path_j, 0);
    Init();
    int ret = OpenInput("http://weblive.hebtv.com/live/hbws_bq/index.m3u8");
    if(ret >= 0)
    {
        ret = OpenOutput(path);
    }

    AVCodecContext *encodecContext = nullptr;
    AVFrame *videoFrame = av_frame_alloc();
    //C++ 新增的检查,goto 不能跳过带初始化的局部变量
    if(ret < 0)
        goto Error;

    InitDecodeContext(inputContext->streams[0]);
    InitEncoderCodec(inputContext->streams[0], &encodecContext);

    while (true)
    {
        auto packet = ReadPacketFromSource();
        if(packet && (packet->stream_index == 0))
        {
            if(Decode(inputContext->streams[0], packet.get(), videoFrame))
            {
                auto packetEncode = Encode(encodecContext, videoFrame);
                if(packetEncode)
                {
                    ret = WritePacket(packetEncode);
                    if(ret >= 0)
                    {
                        break;
                    }
                }
            }
        }
    }
    cout<<"Get Picture End"<<endl;
    av_frame_free(&videoFrame);
    avcodec_close(encodecContext);

    Error:
    CloseInput();
    CloseOutput();
    env->ReleaseStringUTFChars(path_j, path);

    while(true)
    {
        this_thread::sleep_for(chrono::seconds(100));
    }
}