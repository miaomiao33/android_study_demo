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

    public void setDataSource(String url) {
        this.url = url;
    }

    public void play()
    {
        if(TextUtils.isEmpty(url))
        {
            throw new NullPointerException("url is null, please call method setDataSource");
        }

        nPlay(url);
    }

    private native void nPlay(String url);
}
