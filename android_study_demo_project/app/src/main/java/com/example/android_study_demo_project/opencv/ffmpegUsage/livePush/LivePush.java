package com.example.android_study_demo_project.opencv.ffmpegUsage.livePush;

public class LivePush {
    static {
        System.loadLibrary("ffmpegLib");
    }

    private String mLiveUrl;

    public LivePush(String liveUrl) {
        this.mLiveUrl = liveUrl;
    }

    /**
     * 初始化链接
     */
    public void initConnect()
    {
        nInitConnect(mLiveUrl);
    }

    private native void nInitConnect(String liveUrl);

    //链接的回调
}
