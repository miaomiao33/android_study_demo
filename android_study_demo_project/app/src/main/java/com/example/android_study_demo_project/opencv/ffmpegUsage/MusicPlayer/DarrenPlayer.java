package com.example.android_study_demo_project.opencv.ffmpegUsage.MusicPlayer;

import android.text.TextUtils;

public class DarrenPlayer {
    static {
//        System.loadLibrary("ffmpegLib");
    }

    /***
     * url 可以是本地地址，或者是 http 链接
     */
    private String url;
    private MediaErrorListener mErrorListener;

    public void setOnErrorListener(MediaErrorListener mErrorListener) {
        this.mErrorListener = mErrorListener;
    }
    //called from jni
    private void onError(int code,String msg)
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
    private void onPrepared()
    {
        if(mPreparedListener != null)
        {
            mPreparedListener.onPrepared();
        }
    }

    public void setDataSource(String url) {
        this.url = url;
    }

    //使用AudioTrack播放
    public void play()
    {
        if(TextUtils.isEmpty(url))
        {
            throw new NullPointerException("url is null, please call method setDataSource");
        }

        nPlay(url);
    }

    //使用OpenSLES播放
    public void playOpenSLES()
    {
        if(TextUtils.isEmpty(url))
        {
            throw new NullPointerException("url is null, please call method setDataSource");
        }

        nPlayOpenSLES(url);
    }

    public void prepare() {
        if(TextUtils.isEmpty(url))
        {
            throw new NullPointerException("url is null, please call method setDataSource");
        }
        nPrepare(url);
    }

    /**
     * 异步准备
     */
    public void prepareAsync() {
        if(TextUtils.isEmpty(url))
        {
            throw new NullPointerException("url is null, please call method setDataSource");
        }
        nPrepareAsync(url);
    }

    //使用OpenSLES播放
    public void playOpenSLESWithQueue()
    {
        if(TextUtils.isEmpty(url))
        {
            throw new NullPointerException("url is null, please call method setDataSource");
        }

        nPlayOpenSLESWithQueue(url);
    }

    /**
     * 异步准备带队列的方式
     */
    public void prepareAsyncWithQueue() {
        if(TextUtils.isEmpty(url))
        {
            throw new NullPointerException("url is null, please call method setDataSource");
        }
        nPrepareAsyncWithQueue(url);
    }


    //使用普通的AudioTrack播放
    private native void nPlay(String url);

    //使用OpenSLES进行播放
    private native void nPlayOpenSLES(String url);

    private native void nPrepare(String url);

    //异步准备方法
    private native void nPrepareAsync(String url);

    //带队列的异步播放
    private native void nPlayOpenSLESWithQueue(String url);
    //带队列的异步准备方法

    private native void nPrepareAsyncWithQueue(String url);
}
