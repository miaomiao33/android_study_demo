package com.example.android_study_demo_project.opencv.ffmpegUsage.livingStream;

import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;

import androidx.annotation.NonNull;

public class AVStreamPush implements Callback {

    private SurfaceHolder surfaceHolder;
    private VideoPush videoPush;
    private AudioPush audioPush;
    private FFmpegLiveStreamNativePlayer nativePlayer;
    private String path;

    public AVStreamPush(SurfaceHolder surfaceHolder) {
        this.surfaceHolder = surfaceHolder;
        surfaceHolder.addCallback(this);
        prepare();
    }

    /**
     * 预览准备
     */
    private void prepare()
    {
        nativePlayer = new FFmpegLiveStreamNativePlayer();

        //实例化视频推流器
        VideoInfo videoParam = new VideoInfo(480, 320, Camera.CameraInfo.CAMERA_FACING_BACK);
        videoPush = new VideoPush(surfaceHolder, videoParam, nativePlayer,path);

        //实例化音频推流器
        AudioInfo audioInfo = new AudioInfo();
        audioPush = new AudioPush(audioInfo, nativePlayer);
    }

    /**
     * 切换摄像头
     */
    public void switchCamera(){
        videoPush.switchCamera();
    }

    /**
     * 开始推流
     */
    public void startPush(String url,String path)
    {
        this.path = path;
        //设置视频参数，video在onPreviewFrame中直接发送RTMP ConnectStream failed视频packet
        videoPush.startPush();
        //启动一个录音子线程，并发送音频packet
        audioPush.startPush();
        //从队列中不断拉取 RTMPPacket 并发送给流媒体服务器
        nativePlayer.startPush(url,path);
    }

    /**
     * 停止推流
     */
    public void stopPush()
    {
        videoPush.stopPush();
        audioPush.stopPush();
        nativePlayer.stopPush();
    }

    /**
     * 释放资源
     */
    private void release()
    {
        videoPush.release();
        audioPush.release();
        nativePlayer.release();
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        //打开相机
        videoPush.startPreview();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
        videoPush.stopPreview();
        stopPush();
        release();
    }
}
