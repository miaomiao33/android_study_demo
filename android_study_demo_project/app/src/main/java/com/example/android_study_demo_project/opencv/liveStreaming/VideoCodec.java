package com.example.android_study_demo_project.opencv.liveStreaming;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import android.media.MediaCodecList;

public class VideoCodec {
    private MediaCodec mediaCodec;
    private MediaMuxer mMuxer;
    private Handler mHandler;
    private boolean isRecording = false;

    private int videoTrack;
    private String TAG = VideoCodec.class.getSimpleName();
    public final String MIME_TYPE = MediaFormat.MIMETYPE_VIDEO_AVC;
    private int colorFormat = -1;//设备支持的色彩格式

    public VideoCodec() {
        colorFormat = getSupportedFormat();
    }

    public boolean isRecording() {
        return isRecording;
    }

    public int getColorFormat() {
        return colorFormat;
    }

    public void startRecording(String outputPath, int width, int height, int degrees)
    {
        try {
            // 1. 创建并配置MediaCodec
            mediaCodec = MediaCodec.createEncoderByType(MIME_TYPE);
            //记录编码参数，输出AVC，AVC是H264，HEVC是H265
            MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE,width,height);
            //色彩空间 YUV
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat);
            //码率 500_000 = 500000
            format.setInteger(MediaFormat.KEY_BIT_RATE,500_000);
            //帧率 fps
            format.setInteger(MediaFormat.KEY_FRAME_RATE,20);
            //关键帧间隔，单位秒
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL,2);

            mediaCodec.configure(format,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);
            mediaCodec.start();

            // 2. 创建MediaMuxer
            //混合器 音频 + 视频 mp4
            mMuxer = new MediaMuxer(outputPath,
                    MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            mMuxer.setOrientationHint(degrees);
            videoTrack = -1;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //定义一个子线程
        HandlerThread handlerThread = new HandlerThread("videoCodec");
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());
        isRecording = true;
    }

    public void stopRecording()
    {
        if(isRecording)
        {
            isRecording = false;
            if(mediaCodec != null)
            {
                mediaCodec.stop();
                mediaCodec.release();
            }
            if(mMuxer != null)
            {
                mMuxer.stop();
                mMuxer.release();
            }
        }
    }

    //实现I420/NV12 -> H264
    public void queueEncode(byte[] nv21Bytes,CameraHelper mCameraHelper)
    {
        if(isRecording)
        {
            byte[] bytesData ;
            if(colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar)
            {
                //设备支持NV21和NV12
                //bytes是NV21
                Log.i(TAG,"format: 420SP");
//                bytesData = nv21Bytes;
                bytesData = mCameraHelper.nv21ToNv12(nv21Bytes);
            } else if (colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar) {
                //设备支持I420和YU12，需要把NV21转为I420
                Log.i(TAG,"format: 420P");
                //bytes是I420
                bytesData = mCameraHelper.nv21ToI420(nv21Bytes);
            }else {
                Log.i(TAG,"获取设备色彩模式失败");
                return;
            }
            if(colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar
                    || colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar)
            {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        //先放数据，然后取数据
                        inputData(bytesData, new GetDataCallBack() {
                            @Override
                            public void onCallback() {
                                //取数据
                                getData();
                            }
                        });
                    }
                });
            }
        }
    }
    private void inputData(byte[] bytesData,GetDataCallBack onCallBack)
    {
        //送数据
        //立即得到有效输入缓冲区
        //先拿到输入队列下标
        int inputBufferIndex = mediaCodec.dequeueInputBuffer(1000*10);//1秒后超时
        if(inputBufferIndex < 0)
        {
            Log.w(TAG, "No available input buffer, skipping frame");
        }else
        {
            //通过下标拿到输入队列中的容器
            ByteBuffer inputBuffer = mediaCodec.getInputBuffer(inputBufferIndex);
            if(inputBuffer == null)
            {
                return;
            }
            inputBuffer.clear();
            //把数据放入容器中
            inputBuffer.put(bytesData,0,bytesData.length);
            //填充数据后再加入队列
            mediaCodec.queueInputBuffer(inputBufferIndex,0,bytesData.length,
                    System.nanoTime()/1000,0);
            //放数据完成，回调进行取数据
            onCallBack.onCallback();
        }
    }
    //取数据
    public void getData()
    {
        //获得输出缓冲区（编码后的数据从输出缓冲区获取）
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

        int encoderStatus = mediaCodec.dequeueOutputBuffer(bufferInfo,10_000);
        //稍后重试(正在编码过程中，需要稍后尝试)
        if(encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER)
        {

        } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
            //输出格式发生变化，第一次总会调用，所以在这里开启混合器（上面指定的格式YUV变化时）
            //第一次时，获取编码器输出格式
            MediaFormat newFormat = mediaCodec.getOutputFormat();
            //将输出格式添加到混合器中，告诉混合器格式
            //混合器会返回视频下标，后续写出为MP4文件时会用到
            videoTrack = mMuxer.addTrack(newFormat);
            mMuxer.start();
        } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
            //可以忽略，输出缓冲区数组更新，无需release
        }else {
            //正常时encoderStatus是输出队列下标，从缓冲区取出数据
            int bufferIndex = encoderStatus;
            ByteBuffer outputBuffer = mediaCodec.getOutputBuffer(bufferIndex);
            if(outputBuffer == null)
            {
                Log.e(TAG, "Output buffer was null");
            }else
            {
                //如果当前的buffer是配置信息，则忽略，不写出去
                if((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0)
                {
                    bufferInfo.size = 0;
                }

                if(bufferInfo.size != 0)
                {
                    //设置从哪里开始读取数据（读出来就是编码后的数据）
                    outputBuffer.position(bufferInfo.offset);
                    //设置能读取数据的总长度
                    outputBuffer.limit(bufferInfo.offset + bufferInfo.size);
                    //混合器写出为MP4
                    mMuxer.writeSampleData(videoTrack,outputBuffer,bufferInfo);
                }
            }

            //释放这个缓冲区，后续就可以存放新的编码后的数据
            if (bufferIndex >= 0) {
                // 合法buffer才释放
                mediaCodec.releaseOutputBuffer(bufferIndex, false);
            }
            // 帧编码完成
        }
    }

    //获得编码器支持的颜色格式
    private int getSupportedFormat()
    {
        MediaCodecList mediaCodecList = new MediaCodecList(MediaCodecList.ALL_CODECS);
        MediaCodecInfo[] codecInfos = mediaCodecList.getCodecInfos();
        for (MediaCodecInfo codecInfo : codecInfos) {
            if (codecInfo.isEncoder()) {
                //编码器
                try {
                    MediaCodecInfo.CodecCapabilities capabilities = codecInfo.getCapabilitiesForType(MIME_TYPE);
                    for (int format:capabilities.colorFormats) {
                        Log.i(TAG,"format:  "+format);
                        if(format == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar)
                        {
                            return format;
                        } else if (format == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar) {
                            return format;
                        }
                    }

                } catch (IllegalArgumentException e) {
                    // 该编解码器不支持指定的MIME类型
                }
            }
        }

        return -1;
    }

    public interface GetDataCallBack{
        void onCallback();
    }
}
