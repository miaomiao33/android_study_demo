package com.example.android_study_demo_project.opencv.liveStreamingPush;

import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.projection.MediaProjection;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

/***
 * 使用MediaProject录制手机屏幕，并进行编码
 */
public class VideoCodecLiveStream extends Thread{

    private MediaProjection mediaProjection;
    private MediaCodec mediaCodec;
    private Surface surface;
    private VirtualDisplay virtualDisplay;
    private boolean isLiving = false;
    private long timeStamp = System.currentTimeMillis();
    private long startTime = 0;
    private ScreenLive screenLive;
    private final String TAG = VideoCodecLiveStream.class.getSimpleName();

    public VideoCodecLiveStream(ScreenLive screenLive_) {
        this.screenLive = screenLive_;
    }

    @Override
    public void run() {
        //开始编码
        isLiving = true;
        mediaCodec.start();
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        while (isLiving)
        {
            if(timeStamp != 0)
            {
                //2000毫秒后 mediacodec虽然设置了关键帧间隔，但是没用 需要手动强制请求
                if(System.currentTimeMillis() - timeStamp >= 2_000)
                {
                    Bundle params = new Bundle();
                    //立即刷新 让下一帧是关键帧
                    params.putInt(MediaCodec.PARAMETER_KEY_REQUEST_SYNC_FRAME,0);
                    mediaCodec.setParameters(params);
                    timeStamp = System.currentTimeMillis();
                }
            }else {
                timeStamp = System.currentTimeMillis();
            }

            //取数据
            int index = mediaCodec.dequeueOutputBuffer(bufferInfo,10);

            if(index >= 0)
            {
                ByteBuffer outputBuffer = mediaCodec.getOutputBuffer(index);
                if(outputBuffer == null)
                {
                    Log.i(TAG,"inputBuffer 为null");
                    break;
                }
                byte[] outData = new byte[bufferInfo.size];
                outputBuffer.get(outData);
                
                //这样也可以拿到SPS PPS
                //ByteBuffer spsBuf = mediaCodec.getOutputFormat().getByteBuffer("csd-0");
                //ByteBuffer ppsBuf = mediaCodec.getOutputFormat().getByteBuffer("csd-1");

                if(startTime == 0 )
                {
                    //微秒转换为毫秒
                    startTime = bufferInfo.presentationTimeUs / 1000;
                }
                //设置RTMP参数
                RTMPPackage rtmpPackage = new RTMPPackage();
                rtmpPackage.setBuffer(outData);
                rtmpPackage.setType(RTMPPackage.RTMP_PACKET_TYPE_VIDEO);
                long tms = (bufferInfo.presentationTimeUs / 1000) - startTime;
                rtmpPackage.setTms(tms);//当前帧的相对时间戳
                screenLive.addPackage(rtmpPackage);
                mediaCodec.releaseOutputBuffer(index,false);
                index = mediaCodec.dequeueOutputBuffer(bufferInfo,10);
            }
        }

        mediaCodec.stop();
        mediaCodec.release();
        mediaCodec = null;

        virtualDisplay.release();
        virtualDisplay = null;

        isLiving = false;
        startTime = 0;
    }

    public void startLive(MediaProjection mediaProjection_)
    {
        this.mediaProjection = mediaProjection_;
        try {
            //记录编码参数，输出AVC，AVC是H264，HEVC是H265
            MediaFormat format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC,
                    640,480);
            //色彩空间 YUV
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            //码率 500_000 = 500000
            format.setInteger(MediaFormat.KEY_BIT_RATE,500_000);
            //帧率 fps
            format.setInteger(MediaFormat.KEY_FRAME_RATE,20);
            //关键帧间隔，单位秒
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL,2);

            // 创建并配置MediaCodec
            mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
            mediaCodec.configure(format,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);
            //从编码器创建一个画布，画布上的图像会被编码器自动编码
            //不需要再向MediaCodec喂数据
            surface = mediaCodec.createInputSurface();

            virtualDisplay = mediaProjection.createVirtualDisplay(
                    "Screen_Codec",640,480,
                    1, DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                    surface,null,null
            );

        } catch (IOException e) {
            e.printStackTrace();
        }
        start();
    }

    public void stopLive()
    {
        isLiving = false;
        try {
            //当前AudioCodec是一个Thread
            //等待当前线程执行完毕。当一个线程调用另一个线程的 join() 方法时，调用线程会被阻塞，直到被调用的线程执行结束
            join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
