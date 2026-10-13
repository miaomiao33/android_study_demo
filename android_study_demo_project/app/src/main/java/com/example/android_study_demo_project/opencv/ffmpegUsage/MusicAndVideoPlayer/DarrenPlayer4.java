package com.example.android_study_demo_project.opencv.ffmpegUsage.MusicAndVideoPlayer;

import android.text.TextUtils;

import com.example.android_study_demo_project.opencv.ffmpegUsage.MusicPlayer.MediaErrorListener;
import com.example.android_study_demo_project.opencv.ffmpegUsage.MusicPlayer.MediaPreparedListener;

public class DarrenPlayer4 {
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

    public void play()
    {
        if(TextUtils.isEmpty(url))
        {
            throw new NullPointerException("url is null, please call method setDataSource");
        }

        nPlay4(url);
    }
    private native void nPlay4(String url);

    public void prepareAsync() {
        if(TextUtils.isEmpty(url))
        {
            throw new NullPointerException("url is null, please call method setDataSource");
        }
        nPrepare4(url);
    }

    private native void nPrepare4(String url);

}
