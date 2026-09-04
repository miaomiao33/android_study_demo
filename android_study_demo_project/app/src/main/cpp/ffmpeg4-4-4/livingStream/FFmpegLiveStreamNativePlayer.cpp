//
// Created by Machenike on 2026/8/19.
//
extern "C" {
#include <jni.h>
#include "com_example_android_study_demo_project_opencv_ffmpegUsage_livingStream_FFmpegLiveStreamNativePlayer.h"
#include "../../headerFile/LogUtils.h"
#include <android/native_window_jni.h>
#include <android/native_window.h>

#include <pthread.h>
#include "DoubleQueue.h"
#include "x264.h"
#include "../../librtmp/rtmp.h"
#include "faac.h"
#include <malloc.h>

#ifndef TRUE
#define TRUE 1
#define FALSE 0
#endif

#define CONNECT_FAILED 101
#define INIT_FAILED 102

//x264 编码输入图像 YUV420p
x264_picture_t pic_in;
x264_picture_t pic_out;
//YUV个数
int y_len, u_len, v_len;
int totalWidth, totalHeight;
//x264编码处理器
x264_t *video_encode_handle;
unsigned int start_time;
//线程处理
pthread_mutex_t mutex;
pthread_cond_t cond;
//RTMP 流媒体地址
char *rtmp_path;
//是否直播
int is_pushing = FALSE;
//faac 音频编码处理器
faacEncHandle audio_encode_handle;

unsigned long nInputSamples;//输入的采样个数
unsigned long nMaxOutputBytes;//编码输出之后的字节数

RTMPPacket *spsPPSPacket;
bool isHeaderSent = false; // 推流初始化时置为 false

const char *SDKpath;
uint32_t current_audio_timestamp_ms = 0;
static x264_param_t g_x264_param;
bool isFirstIDRArrived = FALSE;

//jobject jobj_push_native;//Global ref
//jclass jcls_push_native;
//jmethodID jmid_throw_native_error;
//JavaVM *javaVm;

void add_rtmp_packet(RTMPPacket *packet);
void add_264_sequence_header(unsigned char* pps, unsigned char* sps, int pps_len, int sps_len);
/**
 * 添加AAC头信息
 */
 void add_aac_sequence_header()
{
     //获取 AAC 头信息的长度
     unsigned char *buf;
     unsigned long len;//长度
     faacEncGetDecoderSpecificInfo(audio_encode_handle, &buf, &len);
     int body_size = 2 + len;
     RTMPPacket *packet = static_cast<RTMPPacket *>(malloc(sizeof(RTMPPacket)));
     //RTMPPacket初始化
     RTMPPacket_Alloc(packet, body_size);
     RTMPPacket_Reset(packet);
     unsigned char *body = reinterpret_cast<unsigned char *>(packet->m_body);
     //头信息配置
     /*AF 00 + AAC RAW data*/
     body[0] = 0xAF;
     //10 5 SoundFormat(4bits):10 = AAC,SoundRate(2bits):3 = 44kHz,
     //SoundSize(1bit):1 = 16-bit samples, SoundType(1bit):1 = Stereo sound
     body[1] = 0x00;//AACPacketType:0 表示 AAC sequence header
     memcpy(&body[2], buf, len);/*spec_buf 是AAC sequence header 数据 */
     packet->m_packetType = RTMP_PACKET_TYPE_AUDIO;
     packet->m_nBodySize = body_size;
     packet->m_nChannel = 0x04;
     packet->m_hasAbsTimestamp = 0;
     packet->m_nTimeStamp = 0;
     packet->m_headerType = RTMP_PACKET_SIZE_MEDIUM;
     add_rtmp_packet(packet);
     free(buf);
}
/**
 * 添加 AAC RTMP Packet
 */
 void add_aac_body(unsigned char *buf, int len,uint32_t audio_timestamp_ms)
{
     int body_size = 2 + len;
     RTMPPacket *packet = static_cast<RTMPPacket *>(malloc(sizeof(RTMPPacket)));
     //RTMPPacket 初始化
     RTMPPacket_Alloc(packet, body_size);//给 `packet->m_body` 分配内存,大小 `body_size` 字节
     RTMPPacket_Reset(packet);//重置 RTMPPacket 各个字段，把协议头成员置 0
     //`reinterpret_cast`：C++ 底层原始指针重解释转换，不做任何类型检查、不做值转换，仅仅改变指针的解析类型
     //`char*` → `unsigned char*`，内存地址完全不变，只是把字节视为无符号
     unsigned char *body = reinterpret_cast<unsigned char *>(packet->m_body);
     //头信息配置
    /*AF 00 + AAC RAW data*/
     body[0] = 0xAF;
     //按 FLV AudioTag 头部定义，拆成 4 个字段：
    //- 高 4 位 `1010`(0xA)：**SoundFormat =10 → AAC 编码**
    //- 接下来 2 位 `11`(0x3)：**SoundRate =3 →44100Hz**
    //- 接下来 1 位 `1`：**SoundSize =1 →16bit 采样**
    //- 最低 1 位 `1`：**SoundType =1 →Stereo 立体声**
    //10 5 SoundFormat(4bits):10 = AAC,SoundRate(2bits):3 = 44kHz,
    //SoundSize(1bit):1 = 16-bit samples, SoundType(1bit):1 = Stereo sound
    body[1] = 0x01;//AACPacketType:1 表示 AAC raw, AAC 原始帧数据（普通音频帧）
    memcpy(&body[2], buf, len);/*spec_buf 是AAC raw 数据 */
    packet->m_packetType = RTMP_PACKET_TYPE_AUDIO;//音频包
    packet->m_nBodySize = body_size;
    packet->m_nChannel = 0x04;//音频默认channel 4，rtmp规范
    packet->m_hasAbsTimestamp = 0;//时间戳是相对时间，不是绝对时间
    packet->m_headerType = RTMP_PACKET_SIZE_LARGE;//完整大rtmp包头
//    packet->m_nTimeStamp = RTMP_GetTime() - start_time;//相对时间戳，单位ms
    // 【关键修改】使用外部传入的真实时间戳
    packet->m_nTimeStamp = audio_timestamp_ms;

    add_rtmp_packet(packet);
}
/**
 * 从队列中不断拉取 RTMPPacket 并发送给流媒体服务器
 */
 void *push_thread(void *arg)
 {
//     JNIEnv *env;//获取当前线程 JNIEnv
//     (*javaVM)->AttachCurrentThread(javaVM, &env, NULL);
     //建立RTMP连接
     RTMP *rtmp = RTMP_Alloc();
     if(!rtmp)
     {
         LOGE("RTMP 初始化失败")
         goto end;
     }
     //初始化
     RTMP_Init(rtmp);
     rtmp->Link.timeout = 5;//连接超时的时间
     //设置流媒体地址
     RTMP_SetupURL(rtmp, rtmp_path);
     //发布RTMP 数据流，开启输出模式
     RTMP_EnableWrite(rtmp);
     //建立连接，连接服务器
     if(!RTMP_Connect(rtmp, NULL))
     {
         LOGE("RTMP 连接失败")
         goto end;
     }
     //计时
     start_time = RTMP_GetTime();
     //连接流
     if(!RTMP_ConnectStream(rtmp,0))
     {
         LOGE("RTMP ConnectStream failed")
         goto end;
     }
     is_pushing = TRUE;
     //发送AAC头信息
//     add_aac_sequence_header();

     while (is_pushing)
     {
         //发送
         pthread_mutex_lock(&mutex);
//         pthread_cond_wait(&cond, &mutex);
         // 防止虚假唤醒：队列空就继续等待
         while(queue_is_empty() && is_pushing){
             pthread_cond_wait(&cond, &mutex);
         }

         //取出队列中的RTMPPacket
         RTMPPacket *packet = static_cast<RTMPPacket *>(queue_get_first());
         if(packet)
         {
             queue_delete_first();//移除队头
             //RTMP协议，stream_id 数据
             packet->m_nInfoField2 = rtmp->m_stream_id;

             unsigned char *body = (unsigned char *)packet->m_body;
             if (packet->m_packetType == RTMP_PACKET_TYPE_VIDEO && packet->m_nBodySize > 1) {
                 if (body[1] == 0x00) {
                     //SPS和PPS
//                     spsPPSPacket = packet;
                     LOGI("RTMP >>> 发送的是 SPS/PPS (AVC Sequence Header), Timestamp: %d", packet->m_nTimeStamp);
//                     pthread_mutex_unlock(&mutex);
//                     continue;
                 } else if (body[1] == 0x01) {
                     //普通帧
                     if (body[0] == 0x17) {
                         //关键帧
//                         if(spsPPSPacket)
//                         {
//                             //先发送一次PPS和SPS
//                             int i = RTMP_SendPacket(rtmp, spsPPSPacket, FALSE);
//                             LOGI("RTMP >>> 发送的是 SPS/PPS (AVC Sequence Header), Timestamp: %d", spsPPSPacket->m_nTimeStamp);
//                             //再继续发送关键帧
//                         } else{
//                             LOGI("RTMP SPS和PPS为null")
//                         }
                         isFirstIDRArrived = TRUE;
                         LOGI("RTMP >>> 发送的是 I 视频帧 (AVC NALU), Timestamp: %d", packet->m_nTimeStamp);
                     } else{
                         if(!isFirstIDRArrived)
                         {
                             //还未收到I帧，丢弃P帧，防止SPS/PPS后先跑出普通帧
                             LOGW("RTMP drop P frame, waiting first IDR");
                             RTMPPacket_Free(packet);
                             pthread_mutex_unlock(&mutex);
                             continue;
                         }
                         //非关键帧
                         LOGI("RTMP >>> 发送的是普通视频帧 (AVC NALU), Timestamp: %d", packet->m_nTimeStamp);
                     }
                 }
             }


             //发送数据包（普通帧和关键帧）

//             int i = RTMP_SendPacket(rtmp, packet, TRUE);
//             //TRUE 将放入librtmp 队列中，并不立即发送
             //FALSE：阻塞发送，函数返回代表socket已经发送完成
             int i = RTMP_SendPacket(rtmp, packet, FALSE);
             if(!i)
             {
                 LOGE("RTMP 断开")
                 RTMPPacket_Free(packet);
                 pthread_mutex_unlock(&mutex);
                 goto end;
             } else{
                 LOGI("rtmp send packet")
                 RTMPPacket_Free(packet);
             }
         }

         pthread_mutex_unlock(&mutex);
     }

     end:
     LOGI("释放资源")
     free(rtmp_path);
     RTMPPacket_Free(spsPPSPacket);
     RTMP_Close(rtmp);
     RTMP_Free(rtmp);
//    (*javaVM)->DetachCurrentThread(javaVM);
    return 0;
 }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_android_1study_1demo_1project_opencv_ffmpegUsage_livingStream_FFmpegLiveStreamNativePlayer_startPush(
        JNIEnv *env, jobject thiz, jstring url, jstring path) {
    const char *url_cstr = env->GetStringUTFChars(url,JNI_FALSE);
    const char *sdk_cstr = env->GetStringUTFChars(path,JNI_FALSE);
    SDKpath = sdk_cstr;
    //复制 url_cstr 内容到rtmp_path
    rtmp_path = (char *)malloc(strlen(url_cstr) + 1);//strlen() 返回的是有效字符个数，不包含末尾 C 语言字符串结束符 '\0'
    memset(rtmp_path, 0, strlen(url_cstr) + 1);//把刚 malloc 出来的`rtmp_path`整块内存全部置 0，rtmp_path[len]等于'\0'
    memcpy(rtmp_path, url_cstr, strlen(url_cstr));//只拷贝有效可见字符，不拷贝源字符串末尾的`\0`

    //初始化互斥锁与条件变量
    pthread_mutex_init(&mutex, NULL);//初始化互斥锁，`NULL` 使用默认属性。用来保护共享资源，多线程下做临界区互斥访问
    pthread_cond_init(&cond, NULL);//初始化条件变量，`NULL` 使用默认属性。用来做线程等待‑唤醒

    //创建队列
    create_queue();
    //启动消费者线程（从线程中不断拉取 RTMPPacket 并发送给流媒体服务器）
    pthread_t push_thread_id;//新线程 ID
    //attr：线程属性，`NULL`使用默认属性,start_routine: 线程入口函数指针, `NULL`：传给线程函数的参数
    //创建成功把新线程加入内核调度队列
    pthread_create(&push_thread_id, NULL, push_thread, NULL);

    env->ReleaseStringUTFChars(url,url_cstr);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_android_1study_1demo_1project_opencv_ffmpegUsage_livingStream_FFmpegLiveStreamNativePlayer_stopPush(
        JNIEnv *env, jobject thiz) {
    is_pushing = FALSE;
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_android_1study_1demo_1project_opencv_ffmpegUsage_livingStream_FFmpegLiveStreamNativePlayer_release(
        JNIEnv *env, jobject thiz) {
    // TODO: implement release()
}
/**
 * 设置视频参数
 */
extern "C"
JNIEXPORT void JNICALL
Java_com_example_android_1study_1demo_1project_opencv_ffmpegUsage_livingStream_FFmpegLiveStreamNativePlayer_setVideoOptions(
        JNIEnv *env, jobject thiz, jint width, jint height, jint bitrate, jint fps) {
    is_pushing = TRUE;//修改当前状态为pushing
    x264_param_t param;
    //x264_param_default_preset 设置
    // 给`x264_param_t`编码参数结构体加载预设 (preset) 与调优 (tune) 配置
    //ultrafast：最快编码，压缩效率最差，码率高，CPU 占用极低，适合实时推流、zerolatency 场景
    // `zerolatency`零延迟
    x264_param_default_preset(&param, "ultrafast","zerolatency");
    //编码输入的像素格式YUV420P
    param.i_csp = X264_CSP_I420;//给x264_encoder_encode()的原始帧是 I420(YUV420P)
    param.i_width = width;
    param.i_height = height;

    //总像素：Y (w×h) + U (w/2 × h/2) + V (w/2 × h/2)
    //获得YUV的数量
    y_len = width * height;
    u_len = y_len / 4;
    v_len = u_len;
    totalWidth = width;
    totalHeight = height;

    //参数 i_rc_method 表示码率控制，包括CQP（恒定质量）、CRF（恒定码率）、
    // ABR（平均码率）恒定码率，会尽量控制在固定码率
    param.rc.i_rc_method = X264_RC_CRF;//码率控制算法,对应 FFmpeg `-crf xx`，恒定质量模式（Constant Rate Factor）
    param.rc.i_bitrate = bitrate / 1000;//码率（比特率， 单位kb/s）
    param.rc.i_vbv_max_bitrate = bitrate / 1000 * 1.2;//瞬时最大码率,给画面剧烈运动留一点码率余量，避免帧糊
//    param.rc.i_vbv_buffer_size = param.rc.i_vbv_max_bitrate;   //zerolatency建议等于max_bitrate

    //码率控制不是通过 timebase 和 timestamp，而是通过 fps
    param.b_vfr_input = 0; //可变帧率输入开关, 0：CFR 恒定帧率输入，1：VFR 可变帧率输入
    //实际帧率：fps = i_fps_num / i_fps_den
    param.i_fps_num = fps; //帧率分子
    param.i_fps_den = 1; //帧率分母
    //timebase = i_timebase_num / i_timebase_den，代表 1 个 pts tick 等于多少秒
    //timebase = 1 / 30 s，每一帧 pts+1，时间推进 1/30 秒。
    param.i_timebase_den = param.i_fps_num;
    param.i_timebase_num = param.i_fps_den;
    param.i_threads = 1;//并行编码线程数量，0 默认表示多线程

    //是否把 SPS 和 PPS放入每一个关键帧前
    //SPS Sequence Parameter Set 是序列参数集，PPS Picture Parameter Set 是图像参数集
    //为了提高图像的纠错能力
    //每一关键帧 (I 帧) 前面是否重复输出 SPS/PPS 头,1：开启
    param.b_repeat_headers = 1;
    //设置 Level 级别, 51 代表 Level 5.1,4K@60，高码率 4K
    param.i_level_idc = 51;
    //设置 Profile 档次
    //baseline 级别，没有 B 帧
    x264_param_apply_profile(&param, "baseline");//根据传入 profile 名字，强制约束编码器参数

    //x264_picture_t （输入图像）初始化
    x264_picture_alloc(&pic_in, param.i_csp, param.i_width, param.i_height);
    pic_in.i_pts = 0;//输入帧的 PTS 显示时间戳，这一帧什么时候显示，以 param.i_timebase_num / param.i_timebase_den 为时间单位
    //打开编码器
    video_encode_handle = x264_encoder_open(&param);
    if(video_encode_handle)
    {
        LOGI("成功打开编码器......")
        //回读编码器实际生效参数拷贝到全局
        x264_encoder_parameters(video_encode_handle, &g_x264_param);
    }else {
        LOGE("x264_encoder_open failed");
    }
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_android_1study_1demo_1project_opencv_ffmpegUsage_livingStream_FFmpegLiveStreamNativePlayer_setAudioOptions(
        JNIEnv *env, jobject thiz, jint sampleRateInHz, jint numChannels) {
    is_pushing = TRUE;
    audio_encode_handle = faacEncOpen(sampleRateInHz, numChannels,
                                      &nInputSamples, &nMaxOutputBytes);
    if(!audio_encode_handle)
    {
        LOGE("音频编码器打开失败")
        return;
    }
    //设置音频编码参数
    faacEncConfigurationPtr p_config = faacEncGetCurrentConfiguration(audio_encode_handle);
    p_config->mpegVersion = MPEG4;
    p_config->allowMidside = 1;
    p_config->aacObjectType = LOW;
    p_config->outputFormat = 0;//输出是否包含ADTS头
    p_config->useTns = 1; //时域噪声控制，大概就是消爆音
    p_config->useLfe = 0;
    //p_config->inputFormat = FAAC_INPUT_16BIT;
    p_config->quantqual = 100;
    p_config->bandWidth = 0;//频宽
    p_config->shortctl = SHORTCTL_NORMAL;

    if(!faacEncSetConfiguration(audio_encode_handle, p_config))
    {
        LOGE("音频编码器配置失败")
        return;
    }

    LOGI("音频编码器配置成功")
}

/**
 * 加入 RTMPPacket 队列，等待发送线程发送
 */
extern "C"
void add_rtmp_packet(RTMPPacket *packet)
{
    //加互斥锁，保护共享资源
    pthread_mutex_lock(&mutex);

    int size_before = queue_size();
    int pushed = 0;

    if(is_pushing)
    {
        pushed = queue_append_last(packet);
    }

    int size_after = queue_size();

    //打印关键信息：入队前后size，是否push成功，当前包的时间戳和类型
//    LOGI("RTMP [add_rtmp_packet] size_before=%d pushed=%d size_after=%d ts=%d type=%hhu is_pushing=%d",
//         size_before, pushed, size_after, packet->m_nTimeStamp, packet->m_packetType,is_pushing);


    //唤醒等待在`pthread_cond_wait(&cond, &mutex)`上的推流子线程
    pthread_cond_signal(&cond);
    //释放互斥锁
    pthread_mutex_unlock(&mutex);
}

/**
 * 发送 H.264 SPS 与 PPS 参数集
 */
extern "C"
void add_264_sequence_header(unsigned char* pps, unsigned char* sps, int pps_len, int sps_len)
{
    int body_size = 16 + sps_len + pps_len;
    //按照 H264 标准配置 SPS 和 PPS，共使用16字节
    RTMPPacket *packet = (RTMPPacket *)malloc(sizeof(RTMPPacket));
    //初始化 RTMPPacket
    RTMPPacket_Alloc(packet, body_size);
    RTMPPacket_Reset(packet);

    unsigned char *body = (unsigned char *)packet->m_body;
    int i = 0;
    //二进制表示：0001 0111
    body[i++] = 0x17;
    //VideoHeaderTag:FrameType(1=key frame)+CodecID(7=AVC)
    body[i++] = 0x00;
    //AVCPacketType = 0 表示设置 AVCDecoderConfigurationRecord
    body[i++] = 0x00;
    body[i++] = 0x00;
    body[i++] = 0x00;

    /*AVCDecoderConfigurationRecord*/
    body[i++] = 0x01;//configurationVersion,版本为1
    body[i++] = sps[1];//AVCProfileIndication
    body[i++] = sps[2];//profile_compatibility
    body[i++] = sps[3];//AVCLevelIndication
//    body[i++] = 0xFF;//lengthSizeMinusOne，H264视频中 NALU 的长度，计算方法是
    body[i++] = 0xFF & 0xFC | 0x03; // lengthSizeMinusOne =3，4字节NALU长度
    // 1 + (lengthSizeMinusOne & 3)，实际测试时发现值总为FF，计算结果为 4

    /*SPS*/
//    body[i++] = 0xE1;//numOfSequenceParametersSets 表示 SPS 的个数，计算方法是
    body[i++] = 0x01 | 0xE0; // SPS count =1
    // numOfSequenceParametersSets & 0x1F，实际测试时发现值总为 E1，计算结果为1
    body[i++] = (sps_len >> 8) & 0xff;
    //sequenceParameterSetLength 表示 SPS 的长度
    body[i++] = sps_len & 0xff;//sequenceParameterSetNALUnits
    memcpy(&body[i], sps, sps_len);
    i += sps_len;

    /*PPS*/
    body[i++] = 0x01;//numOfPictureParameterSets 表示 PPS 的个数，计算方法是
    // numOfPictureParameterSets & 0x1F，实际测试时发现值总为1F，计算结果为1
    body[i++] = (pps_len >> 8) & 0xff;
    //pictureParameterSetLength 表示 PPS 的长度
    body[i++] = (pps_len) & 0xff;//PPS
    memcpy(&body[i], pps, pps_len);
    i += pps_len;

    //Message Type (消息类型)，RTMP_PACKET_TYPE_VIDEO: 0x09
    packet->m_packetType = RTMP_PACKET_TYPE_VIDEO;
    //PayLoad Length (Payload 长度)
    packet->m_nBodySize = body_size;
    //Time Stamp (时间戳)：4字节
    //记录了每一个 tag 相对于第一个 tag（File Header）的相对时间
    //以 ms 为单位。而File Header 的 timestamp 永远为0
    packet->m_nTimeStamp = 0;
    packet->m_hasAbsTimestamp = 0;
    packet->m_nChannel = 0x04;//Channel ID， Audio 和 Video通道
    packet->m_headerType = RTMP_PACKET_SIZE_MEDIUM;
    //加入 RTMPPacket
    LOGI("RTMP Video Header")
    add_rtmp_packet(packet);
}

/**
 * 发送 H264 帧信息
 */
extern "C"
void add_264_body(unsigned char *buf, int len, uint32_t video_timestamp_ms)
{
    //去掉起始码（界定符）
    if(buf[2] == 0x00)//00 00 00 01
    {
        buf += 4;
        len -= 4;
    } else if(buf[2] == 0x01)//00 00 01
    {
        buf += 3;
        len -= 3;
    }

    if(len <= 0)
    {
        LOGI("RTMP len <= 0")
    }

    int body_size = len + 9;
    RTMPPacket *packet = (RTMPPacket*)malloc(sizeof(RTMPPacket));
    RTMPPacket_Alloc(packet, body_size);

    unsigned char* body = (unsigned char*)packet->m_body;
    //在 NAL 头信息中，type（5位）等于5，说明这是关键帧 NAL 单元
    //buf[0] NAL Header 与运算， 获取type，根据type 判断关键帧和普通帧
    //00000101 & 00011111（0x1f）= 00000101
    int type = buf[0] & 0x1F;
    //Inter Frame,帧间压缩
    body[0] = 0x27;//非关键帧
    //VideoHeaderTag:FrameType(2=Inter Frame)+CodecID(7=AVC)
    //IDR，I 帧图像
    if(type == NAL_SLICE_IDR)
    {
        LOGI("RTMP I 帧图像")
        body[0] = 0x17;
        //VideoHeaderTag:FrameType(1=key frame)+CodecID(7=AVC)
    }
    //AVCPacketType = 1
    body[1] = 0x01;/*nal unit, NALUs(AVCPacketType == 1)*/
    body[2] = 0x00;//composition time 0x000000 24bit
    body[3] = 0x00;
    body[4] = 0x00;

    //写入 NALU 信息，右移 8 位，一个字节的读取
    body[5] = (len >> 24) & 0xFF;
    body[6] = (len >> 16) & 0xFF;
    body[7] = (len >> 8) & 0xFF;
    body[8] = (len) & 0xFF;

    /*复制数据*/
    memcpy(&body[9], buf, len);

    packet->m_hasAbsTimestamp = 0;
    packet->m_nBodySize = body_size;
    packet->m_packetType = RTMP_PACKET_TYPE_VIDEO;
    //当前 Packet 的类型为 Video
    packet->m_nChannel = 0x04;
    packet->m_headerType = RTMP_PACKET_SIZE_LARGE;
//    packet->m_nTimeStamp = RTMP_GetTime() - start_time;

//    packet->m_nTimeStamp = video_timestamp_ms;
//    video_timestamp_ms += (1000 / fps); // 例如 25fps
    // 【关键修复】使用 x264 输出的真实 PTS，彻底解决时间戳跳变
    packet->m_nTimeStamp = video_timestamp_ms;
    //记录了每一个 tag 相对于第一个 tag（File Header）的相对时间
    add_rtmp_packet(packet);
}

/**
 * 对采集到的视频数据进行编码
 */
extern "C"
JNIEXPORT void JNICALL
Java_com_example_android_1study_1demo_1project_opencv_ffmpegUsage_livingStream_FFmpegLiveStreamNativePlayer_sendVideoPacket(
        JNIEnv *env, jobject thiz, jbyteArray buffer,jint fps) {
    //将视频数据转为 YUV420P
    /**NV21->YUV420P(I420)**/
    jbyte *nv21_buffer = env->GetByteArrayElements( buffer, JNI_FALSE);
//    jbyte *u = reinterpret_cast<jbyte *>(pic_in.img.plane[1]);
//    jbyte *v = reinterpret_cast<jbyte *>(pic_in.img.plane[2]);
//    //nv21 4:2:0 Formats, 12 Bits per Pixel
//    //nv21 与 yuv420p，y个数一致，uv 位置不同
//    //nv21 转 yuv420p  y = w*h, u/v = w*h/4
//    //nv21=yuv  yuv420p=yuv y=y u=y+1+1 v=y+1
//    //NV21：V0,U0,V1,U1
//    //      V2,U2,V3,U3
//    //I420：  U0,U1,U2,U3
//    //        V0,V1,V2,V3
    int i;
    // 1. 拷贝 Y 平面，必须考虑 stride
    //    memcpy(pic_in.img.plane[0], nv21_buffer, y_len);

    int src_y_stride = totalWidth; // 假设源数据是紧凑的
    int dst_y_stride = pic_in.img.i_stride[0];
    if (dst_y_stride == src_y_stride) {
        LOGI("RTMP dst_y_stride == src_y_stride ")
        memcpy(pic_in.img.plane[0], nv21_buffer, y_len);
    } else {
        // 逐行拷贝，防止内存错位
        for (int h = 0; h < totalHeight; h++) {
            memcpy(pic_in.img.plane[0] + h * dst_y_stride,
                   nv21_buffer + h * src_y_stride,
                   src_y_stride);
        }
    }

//    //复制UV
//    int i;
//    for(i = 0; i < u_len;i++)
//    {
//        *(u + i) = *(nv21_buffer + y_len + i * 2 + 1);
//        *(v + i) = *(nv21_buffer + y_len + i * 2);
//    }

    // 2. 转换 UV 平面 (注意 NV21 是 VUVU，NV12 是 UVUV)
    // 当前是 NV21 (VUVU)
    jbyte *src_uv = nv21_buffer + y_len;
    jbyte *dst_u = reinterpret_cast<jbyte *>(pic_in.img.plane[1]);
    jbyte *dst_v = reinterpret_cast<jbyte *>(pic_in.img.plane[2]);

    int src_uv_width = totalWidth / 2;
    int dst_u_stride = pic_in.img.i_stride[1];
    int dst_v_stride = pic_in.img.i_stride[2];
    int uv_height = totalHeight / 2;

    for(int row = 0; row < uv_height; row++)
    {
        //源NV21 uv行起始
        jbyte* src_row = src_uv + row * src_uv_width * 2;
        //目标U/V行起始
        jbyte* dst_u_row = dst_u + row * dst_u_stride;
        jbyte* dst_v_row = dst_v + row * dst_v_stride;

        for(int col = 0; col < src_uv_width; col++)
        {
            dst_v_row[col] = src_row[col*2];
            dst_u_row[col] = src_row[col*2 + 1];
        }
    }


    pic_in.i_pts += g_x264_param.i_fps_den;//根据i_fps_den顺序累加, 显示时间戳, 基于 x264 的 timebase

    //通过 H264 编码得到 NALU 数组
    x264_nal_t *nal = NULL;//NAL
    int n_nal = -1; //NALU 的个数
    //进行 H264 编码（NV21->I420）
    //pic_in：输入图像，I420
    //pic_out：输出图片信息，不是 YUV 图像！只存编码后的帧属性
    if(x264_encoder_encode(video_encode_handle, &nal, &n_nal, &pic_in, &pic_out) < 0)
    {
        LOGE("编码失败")
        env->ReleaseByteArrayElements(buffer, nv21_buffer, JNI_FALSE);
        return;
    }

//    char bufPath[256];   // 预先分配足够可写缓冲区
//    strcpy(bufPath,SDKpath);   // 先拷贝原始字符串
//    strcat(bufPath, "/test.h264");       // 拼接后缀
//    LOGI("RTMP SDKpath %s",bufPath)
//    ///storage/emulated/0/Android/data/com.example.android_study_demo_project/files/Movies/test.h264
//    FILE *fp = fopen(bufPath, "ab+");
//    if(fp) {
//        fwrite(nal[i].p_payload, 1, nal[i].i_payload, fp);
//        fclose(fp);
//    }
    // 【关键修改】将 x264 的帧序号 (PTS) 转换为毫秒级时间戳
    // 公式：毫秒时间戳 = PTS * (1000 / 帧率)
    // 因为你的 timebase 是 1/fps，所以 pic_out.i_pts 就是帧序号
    uint32_t video_timestamp_ms = (uint32_t)(pic_out.i_pts * 1000 / fps);

    //使用 RTMP 协议将 H264 编码的视频数据发送给流媒体服务器
    //帧分为关键帧和普通帧，为了提高画面的纠错率，关键帧应包含 SPS 和 PPS 数据
    int sps_len, pps_len;
    unsigned char sps[100];
    unsigned char pps[100];
    memset(sps, 0, 100);
    memset(pps, 0, 100);
//    pic_in.i_pts += 1;//根据i_fps_den顺序累加, 显示时间戳, 基于 x264 的 timebase
    //遍历 NALU 数组，根据 NALU 的类型判断
    for(i = 0; i < n_nal; i++)
    {
        if(nal[i].i_type == NAL_SPS)
        {
            //复制SPS数据
            sps_len = nal[i].i_payload - 4 ;//nal[i].i_payload : NAL单元有效字节长度

            //nal[i].p_payload : NAL单元数据起始指针
            memcpy(sps, nal[i].p_payload + 4, sps_len);//不复制 4 字节起始码
        } else if(nal[i].i_type == NAL_PPS)
        {
            //复制 PPS 数据
            pps_len = nal[i].i_payload - 4;

            memcpy(pps, nal[i].p_payload + 4, pps_len);//不复制 4 字节起始码
            //发送序列信息
            //H264 关键帧会包含 SPS 和 PPS 数据
//            add_264_sequence_header(pps, sps, pps_len, sps_len);
            // 【关键修复】只在第一次发送 Sequence Header
            if (!isHeaderSent) {
                add_264_sequence_header(pps, sps, pps_len, sps_len);
                isHeaderSent = true;
            }
        } else{
            //发送帧信息(I 帧和普通帧)
            add_264_body(nal[i].p_payload, nal[i].i_payload,video_timestamp_ms);
        }
    }

    //释放数组
    env->ReleaseByteArrayElements(buffer,nv21_buffer,JNI_FALSE);
}

/**
 * 发送音频packet
 */
extern "C"
JNIEXPORT void JNICALL
Java_com_example_android_1study_1demo_1project_opencv_ffmpegUsage_livingStream_FFmpegLiveStreamNativePlayer_sendAudioPacket(
        JNIEnv *env, jobject thiz, jbyteArray buffer, jint len) {
    int *pcmbuf;
    unsigned char *bitbuf;
    jbyte *b_buffer = env->GetByteArrayElements( buffer, JNI_FALSE);
    pcmbuf = (int*) malloc(nInputSamples * sizeof(int));
    bitbuf = (unsigned char*)malloc(nMaxOutputBytes * sizeof(unsigned char));
    int nByteCount = 0;//已经处理完的采样点计数
    //- `buffer` 是 Java 层 `byte[]`，存放**16bit PCM 音频数据（short，小端）
    //- `len` 是 Java 字节数组的长度，`env->GetArrayLength(buffer)`，单位：字节
    //- 16 位 PCM：每一个音频采样占 2 个字节
    unsigned int nBufferSize = (unsigned int) len / 2;//总采样点的数量,44100Hz，代表 1 秒有 44100 个采样点
    unsigned short *buf = (unsigned short*) b_buffer;
    while (nByteCount < nBufferSize)
    {
        //nByteCount: 已经处理完的采样点计数
        //nInputSamples: 单次希望处理的最大采样数
        //audioLength: 本次循环实际要处理多少个采样点
        int audioLength = nInputSamples;
        if((nByteCount + nInputSamples) >= nBufferSize)
        {
            //已经超过了最大值，则取剩下的采样点
            audioLength = nBufferSize - nByteCount;
        }
        int i;
        for(i = 0;i < audioLength; i++)
        {
            //每次从实时的 PCM 音频队列中读出量化位数为 8 的 PCM 数据
            int s = ((int16_t *)buf + nByteCount)[i];//+ nByteCount: 指针按采样点偏移（不是字节偏移），跳到这一轮分块的起始位置
            //把 16bit 采样左移 8 位，提升到 32bit 整数高位,
            //s << 8: 很多音频库（例如`swr_convert`，部分 aac 编码器）接收 32bit 有符号整数采样 `int32_t`，采样有效位放在高 16 位，低 16 位补 0
            //- 原始：`int16_t`：`SSSS_SSSS_SSSS_SSSS`（16bit 有效）
            //- `s <<8` 后存入 int：`SSSS_SSSS_SSSS_SSSS_0000_0000`，有效数据在高 16 位。
            //`s`是有符号`int16_t`，左移后赋值给 int，会做符号扩展，负数不会错乱。
            //每次都覆盖旧数据
            pcmbuf[i] = s << 8;//用 8 个二进制位来表示一个采样量化点（模数转换）
        }
        nByteCount += audioLength;
        //利用 FAAC 进行编码，pcmbuf 为转换后的 PCM 数据流，audioLength 为调用
        // faacEncOpen 时得到的输入采样数，bitbuf 为编码后的数据 buff，nMaxOutputBytes 为
        // 调用 faacEncOpen 时得到的最大输出字节数
        int byteslen = faacEncEncode(audio_encode_handle,
                                     pcmbuf,   // 当前块起始位置,
                                     audioLength,
                                     bitbuf, nMaxOutputBytes);
        if(byteslen < 1)
        {
            continue;
        }
        //添加 AAC RTMP Packet
        add_aac_body(bitbuf, byteslen,current_audio_timestamp_ms);

        // 假设你的采样率是 44100Hz（如果是 48000Hz 请替换）
        int sample_rate = 44100;
        // 【关键修改】累加下一帧的时间戳
        // 公式：(1024个采样点 * 1000ms) / 采样率
        current_audio_timestamp_ms += (1024 * 1000) / sample_rate;
        //从 bitbuf 中得到编码后的 AAC 数据流，放到数据队列中
    }
    env->ReleaseByteArrayElements(buffer, b_buffer, NULL);
    if(bitbuf)
    {
        free(bitbuf);
    }
    if(pcmbuf)
    {
        free(pcmbuf);
    }
}