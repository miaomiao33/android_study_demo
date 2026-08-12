//
// Created by Machenike on 2026/8/6.
//

#ifndef ANDROID_STUDY_DEMO_PROJECT_FFMPEGCORE_H
#define ANDROID_STUDY_DEMO_PROJECT_FFMPEGCORE_H

#ifdef __cplusplus
extern "C" {
#endif

#include "../headerFile/LogUtils.h"
#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"
#include "libswscale/swscale.h"
#include "libswresample/swresample.h"
#include "libavutil/samplefmt.h"
#include "libavutil/opt.h"
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>

// 全局变量声明，cpp中定义
extern uint8_t *outputBuffer;
extern size_t outputBufferSize;

extern AVPacket packet;
extern int audioStream;
extern AVFrame *aFrame;
extern SwrContext *swr;
extern AVFormatContext *aFormatCtx;
extern AVCodecContext *aCodecCtx;

//对外接口函数声明
int initFFmpeg(int *rate, int *channel, char *url);
int getPCM(void **pcm, size_t *pcmSize);
int releaseFFmpeg();

#ifdef __cplusplus
}
#endif

#endif //ANDROID_STUDY_DEMO_PROJECT_FFMPEGCORE_H