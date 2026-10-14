package com.example.android_study_demo_project.opencv.ffmpegUsage.MusicAndVideoPlayer;

import android.text.TextUtils;
import android.view.Surface;

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
    private MediaPreparedListener mPreparedListener;
    public void setOnPreparedListener(MediaPreparedListener preparedListener) {
        this.mPreparedListener = preparedListener;
    }

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

        nPlay4();
    }

    public void prepare() {
        if(TextUtils.isEmpty(url))
        {
            throw new NullPointerException("url is null, please call method setDataSource");
        }
        nPrepare4(url);
    }

    /***
     * 异步准备
     */
    public void prepareAsync() {
        if(TextUtils.isEmpty(url))
        {
            throw new NullPointerException("url is null, please call method setDataSource");
        }
        nPrepareAsync4(url);
    }

    public void stop() {
    }

    private native void nPlay4();
    private native void nPrepare4(String url);
    private native void nPrepareAsync4(String url);

    //在准备之后，播放之前调用的
    public native void setSurface(Surface surface);
}
