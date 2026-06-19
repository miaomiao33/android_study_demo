package com.example.android_study_demo_project.opencv.liveStreaming;

import android.media.Image;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

public class VideoCodec2 {
    private MediaCodec mediaCodec;
    private MediaMuxer mMuxer;
    private boolean isRecording = false;

    private String TAG = VideoCodec2.class.getSimpleName();
    public final String MIME_TYPE = MediaFormat.MIMETYPE_VIDEO_AVC;

    public boolean isRecording() {
        return isRecording;
    }

    private int Y_WIDTH = 640;
    private int Y_HEIGHT = 480;
    private final int BIT_RATE = 2_000_000; // 2Mbps
    private final int FPS = 30;

    private boolean mHasWriteFrame = false; // 是否写入过有效帧

    private int mVideoTrackIndex = -1;
    private boolean mMuxerStarted = false;
    private Handler mHandler;

    public void startRecording(String outputPath, int yWidth, int yHeight, int degrees)
    {
        try {
            Y_WIDTH = yWidth;
            Y_HEIGHT = yHeight;
            // 1. 创建并配置MediaCodec
            mediaCodec = MediaCodec.createEncoderByType(MIME_TYPE);
            //记录编码参数，输出AVC，AVC是H264，HEVC是H265，传入的是videoWidth
            MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE,Y_WIDTH*3/2,Y_HEIGHT*3/2);
            //色彩空间 YUV
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
            //码率 500_000 = 500000
            format.setInteger(MediaFormat.KEY_BIT_RATE,BIT_RATE);
            //帧率 fps
            format.setInteger(MediaFormat.KEY_FRAME_RATE,FPS);
            //关键帧间隔，单位秒
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL,1);

            format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, Y_WIDTH * Y_HEIGHT * 3 / 2);

            mediaCodec.configure(format,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);
            mediaCodec.start();

            // 2. 创建MediaMuxer
            //混合器 音频 + 视频 mp4
            mMuxer = new MediaMuxer(outputPath,
                    MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            mMuxer.setOrientationHint(degrees);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //定义一个子线程
        HandlerThread handlerThread = new HandlerThread("videoCodec");
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());
        isRecording = true;
    }

    //实现I420/NV12 -> H264
    public void queueEncode(byte[] nv21Bytes,CameraHelper mCameraHelper)
    {
        if(isRecording)
        {
            byte[] bytesData  = nv21Bytes;
            //转为NV12
//            bytesData = mCameraHelper.nv21ToNv12(nv21Bytes);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    inputData(bytesData, new GetDataCallBack() {
                        @Override
                        public void onCallback() {
                            //取数据
                            startEncodeLoop();
                        }
                    });
                }
            });
        }
    }

    // 传入原始YUV字节数组，送入编码器
    private void inputData(byte[] yuvData, GetDataCallBack onCallBack) {
        if (mediaCodec == null) return;

        int inputBufferIndex = mediaCodec.dequeueInputBuffer(1000*10);

        if(inputBufferIndex < 0)
        {
            Log.w(TAG, "No available input buffer, skipping frame");
        }else
        {
            //输入数据队列
            Image image = mediaCodec.getInputImage(inputBufferIndex);
            assert image != null;
            Image.Plane[] planes = image.getPlanes();

            // 填充Y平面 plane[0]
            //把yuvData中的Y填充到yBuf中
            int yTotalSize = copyYPlane(yuvData, planes[0]);
            int uvTotalSize = 0;

            if(planes.length == 2)
            {
                //底层是NV12
                // UV通道
                uvTotalSize = copyUVPlane(yuvData, planes[1]);
            }else {
                //底层是I420
                uvTotalSize = copyUOrV(yuvData, planes);
            }

            // Flexible自动兼容NV12(plane2空) / I420(plane2有V)，无需手动处理
            image.close(); // 必须close，否则缓冲区锁定

            if(yTotalSize == 0 || uvTotalSize == 0)
            {
                Log.i(TAG,"YUV解码失败");
                return;
            }

            // 送入编码器队列
            mediaCodec.queueInputBuffer(inputBufferIndex,
                    0,
                    yuvData.length,
                    System.nanoTime()/1000,
                    0);

            //放数据完成，回调进行取数据
            onCallBack.onCallback();
        }
    }

    private void startEncodeLoop() {
        new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

                int encoderStatus = mediaCodec.dequeueOutputBuffer(bufferInfo,10_000);

                if(encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER)
                {

                }else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    //可以忽略，输出缓冲区数组更新，无需release
                }else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    // 编码器输出格式就绪，添加视频轨道到Muxer
                    MediaFormat outFormat = mediaCodec.getOutputFormat();
                    mVideoTrackIndex = mMuxer.addTrack(outFormat);
                    mMuxer.start();
                    mMuxerStarted = true;
                }else {
                    if (encoderStatus >= 0) {
                        ByteBuffer encodedBuf = mediaCodec.getOutputBuffer(encoderStatus);
                        if (mMuxerStarted && bufferInfo.size > 0) {
                            assert encodedBuf != null;
                            encodedBuf.position(bufferInfo.offset);
                            encodedBuf.limit(bufferInfo.offset + bufferInfo.size);
                            mMuxer.writeSampleData(mVideoTrackIndex, encodedBuf, bufferInfo);
                            mHasWriteFrame = true; // 标记成功写入一帧
                        }
                        mediaCodec.releaseOutputBuffer(encoderStatus, false);

                        // 收到结束标记，退出循环
                        if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                            break;
                        }
                    }
                }
            }
        }).start();
    }

    public void stopRecord() {
        isRecording = false;
        // 1. 给编码器发送EOS，等待编码完成所有缓存帧
        if (mediaCodec != null) {
            try {
                int inputId = mediaCodec.dequeueInputBuffer(100);
                if (inputId >= 0) {
                    mediaCodec.queueInputBuffer(
                            inputId,
                            0,
                            0,
                            System.currentTimeMillis() * 1000,
                            MediaCodec.BUFFER_FLAG_END_OF_STREAM
                    );
                }
                // 等待编码器输出剩余缓存帧，留出足够时间
                Thread.sleep(300);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 2. 安全停止Muxer，前置校验
        if (mMuxer != null) {
            // 必须同时满足两个条件才能stop：已启动 + 写入过至少一帧数据
            if (mMuxerStarted && mHasWriteFrame) {
                try {
                    mMuxer.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            mMuxer.release();
            mMuxer = null;
        }

        // 3. 释放编码器
        if (mediaCodec != null) {
            try {
                mediaCodec.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mediaCodec.release();
            mediaCodec = null;
        }

        // 重置所有标记
        mMuxerStarted = false;
        mHasWriteFrame = false;
        mVideoTrackIndex = -1;
    }

    // 拷贝到Y通道
    private int copyYPlane(byte[] nv21YUV, Image.Plane yPlane) {
        //pixelStride（uvPixelStride）：同一行里，左右两个像素间隔多少字节（横向）
        //rowStride：上下两行开头相隔多少字节（纵向，存在内存对齐填充空白）
        //rowStride ≥ uvWidth * 2（存在内存对齐填充）

        ByteBuffer yBuffer = yPlane.getBuffer();//获得Y的buffer
        yBuffer.clear();
        int yRowStride = yPlane.getRowStride();

        for (int y = 0; y < Y_HEIGHT; y++) {
            // 写入当前行有效像素
            for (int x = 0; x < Y_WIDTH; x++) {
                yBuffer.put(nv21YUV[ y*Y_WIDTH + x]);
            }
            // 跳到下一行起始位置，跳过内对齐的padding
            yBuffer.position((y + 1) * yRowStride);
        }

        return yBuffer.capacity();
    }

    private int copyUVPlane(byte[] nv21, Image.Plane uvPlane) {
        //pixelStride（uvPixelStride）：同一行里，左右两个像素间隔多少字节（横向）
        //rowStride：上下两行开头相隔多少字节（纵向，存在内存对齐填充空白）
        //rowStride ≥ uvWidth * 2（存在内存对齐填充）
        if (nv21 == null) return 0;
        ByteBuffer uvBuffer = uvPlane.getBuffer();
        uvBuffer.clear();
        int uvRowStride = uvPlane.getRowStride();
        //uvPixelStride = 1表示I420，= 2表示NV12
        int uvPixelStride = uvPlane.getPixelStride();

        for (int y = Y_HEIGHT-1 ; y < Y_HEIGHT * 3/2; y++) {
            // 写入当前行有效像素
            for (int x = 0; x < Y_WIDTH ; x += 2) {
                //NV21是VU->NV12,UV
                uvBuffer.put(nv21[ y*Y_WIDTH + x + 1 ]);
                uvBuffer.put(nv21[ y*Y_WIDTH + x ]);
            }
            // 跳到下一行起始位置，跳过padding
            uvBuffer.position((y + 1) * uvRowStride);
        }

        return uvBuffer.capacity();
    }

    private int copyUOrV(byte[] nv21, Image.Plane[] planes) {
        //rewind将缓冲区的 position 重置为 0
        if (nv21 == null) return 0;

        // U通道
        Image.Plane uPlane = planes[1];
        ByteBuffer uBuffer = uPlane.getBuffer();

        // V通道
        Image.Plane vPlane = planes[2];
        ByteBuffer vBuffer = vPlane.getBuffer();

        //uvPixelStride = 1表示I420，= 2表示NV12
        uBuffer.clear();
        vBuffer.clear();
        //NV21是V0,U0,V1,U1->I420,U0,U1,U2,U3
        //                        V0,V1,V2,V3
        for (int y = Y_HEIGHT-1; y < Y_HEIGHT * 3/2; y++) {
            // 写入当前行有效像素
            for (int x = 0; x < Y_WIDTH ;x += 2) {
                vBuffer.put(nv21[ y*Y_WIDTH + x ]);
                uBuffer.put(nv21[ y*Y_WIDTH + x + 1 ]);
            }
        }
//        for (int y = Y_HEIGHT; y < Y_HEIGHT * 3/2; y++){
//            for (int i = 0; i < Y_WIDTH * 3/2;i += 2)
//            {
//                //U
//                inputDataBuf.put(nv21[i + 1]);
//            }
//        }
//        for (int y = Y_HEIGHT; y < Y_HEIGHT * 3/2; y++){
//            for (int i = 0; i < Y_WIDTH * 3/2;i += 2)
//            {
//                //V
//                inputDataBuf.put(nv21[ i ]);
//            }
//        }

        return uBuffer.capacity()+vBuffer.capacity();
    }

    public interface GetDataCallBack{
        void onCallback();
    }
}
