//
// Created by Machenike on 2026/7/27.
//

#ifndef ANDROID_STUDY_DEMO_PROJECT_FFMPEG_H
#define ANDROID_STUDY_DEMO_PROJECT_FFMPEG_H
extern "C"{
#include <jni.h>
#include "../headerFile/LogUtils.h"
#include "libavutil/opt.h"
#include "libavutil/channel_layout.h"
#include "libavutil/common.h"
#include "libavutil/imgutils.h"
#include "libavutil/mathematics.h"
#include "libavutil/samplefmt.h"
#include "libavutil/time.h"
#include "libavutil/fifo.h"
// 编解码器（解码、编码、AVCodecContext）
#include "libavcodec/avcodec.h"
// 封装/解封装（IO、打开文件、读取包）
#include "libavformat/avformat.h"
#include "libavformat/avio.h"
#include "libavfilter/avfilter.h"
#include "libavfilter/buffersink.h"
#include "libavfilter/buffersrc.h"
// 缩放、图像转换（像素格式转换，sws_scale）
#include <libswscale/swscale.h>
// 音频重采样（音频格式转换、采样率转换）
#include <libswresample/swresample.h>
}

#endif //ANDROID_STUDY_DEMO_PROJECT_FFMPEG_H
