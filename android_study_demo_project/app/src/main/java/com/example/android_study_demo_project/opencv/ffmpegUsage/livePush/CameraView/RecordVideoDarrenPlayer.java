package com.example.android_study_demo_project.opencv.ffmpegUsage.livePush.CameraView;

import android.text.TextUtils;

public class RecordVideoDarrenPlayer {
    static {
//        System.loadLibrary("ffmpegLib");
    }

    /***
     * url 可以是本地地址，或者是 http 链接
     */
    private String url;
    private MediaErrorListener mErrorListener;
    private MediaInfoListener mInfoListener;

    public void setInfoListener(MediaInfoListener infoListener) {
        this.mInfoListener = infoListener;
    }

    public void setOnErrorListener(MediaErrorListener mErrorListener) {
        this.mErrorListener = mErrorListener;
    }
    //called from jni
    public void onError(int code,String msg)
    {
        if(mErrorListener != null)
        {
            mErrorListener.onError(code,msg);
        }
    }

    private MediaPreparedListener mPreparedListener;
    public void setOnPreparedListener(MediaPreparedListener preparedListener) {
        this.mPreparedListener = preparedListener;
    }
    //called from jni
    public void onPrepared()
    {
        if(mPreparedListener != null)
        {
            mPreparedListener.onPrepared();
        }
    }

    public void setDataSource(String url) {
        this.url = url;
    }


    //使用OpenSLES播放
    public void playOpenSLESWithQueue()
    {
        if(TextUtils.isEmpty(url))
        {
            throw new NullPointerException("url is null, please call method setDataSource");
        }

        nPlayOpenSLESWithQueueRecordVideo(url);
    }

    /**
     * 异步准备带队列的方式
     */
    public void prepareAsyncWithQueue() {
        if(TextUtils.isEmpty(url))
        {
            throw new NullPointerException("url is null, please call method setDataSource");
        }
        nPrepareAsyncWithQueueRecordVideo(url);
    }

    //call from jni
    private void musicInfo(int sampleRate,int channels){
        if(mInfoListener != null)
        {
            mInfoListener.musicInfo(sampleRate,channels);
        }
    }

    private void callbackPcm(byte[] pcmData,int size){
        if(mInfoListener != null)
        {
            mInfoListener.callbackPcm(pcmData,size);
        }
    }

    public void stop()
    {
        nStop();
    }


    //带队列的异步准备方法
    private native void nPrepareAsyncWithQueueRecordVideo(String url);

    //带队列的异步播放
    private native void nPlayOpenSLESWithQueueRecordVideo(String url);

    private native void nStop();
}
