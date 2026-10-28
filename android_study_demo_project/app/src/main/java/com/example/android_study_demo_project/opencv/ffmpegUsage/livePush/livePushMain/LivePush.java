package com.example.android_study_demo_project.opencv.ffmpegUsage.livePush.livePushMain;

import android.os.Handler;
import android.os.Looper;

public class LivePush {
    static {
//        System.loadLibrary("ffmpegLib");
    }

    private String mLiveUrl;
    private ConnectListener mConnectListener;
    private Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    public LivePush(String liveUrl) {
        this.mLiveUrl = liveUrl;
    }

    public void setConnectListener(ConnectListener connectListener) {
        this.mConnectListener = connectListener;
    }

    /**
     * 初始化链接
     */
    public void initConnect()
    {
        nInitConnect(mLiveUrl);
    }

    //链接的回调
    //called from JNI
    public void onConnectError(int errorCode,String errorMsg)
    {
        MAIN_HANDLER.post(new Runnable() {
            @Override
            public void run() {
                stop();
                if(mConnectListener != null)
                {
                    mConnectListener.connectError(errorCode,errorMsg);
                }
            }
        });

    }

    public void stop()
    {
        nStop();
    }

    //链接的回调
    //called from JNI
    public void onConnectSuccess()
    {
        MAIN_HANDLER.post(new Runnable() {
            @Override
            public void run() {
                if(mConnectListener != null)
                {
                    mConnectListener.connectSuccess();
                }
            }
        });

    }

    public native void pushSpsPps(byte[] spsData, int spsLen, byte[] ppsData, int ppsLen);

    public native void pushVideo(byte[] videoData, int dataLen, boolean keyFrame);

    public native void pushAudio(byte[] audioData, int DataLen);


    public interface ConnectListener{
        void connectError(int errorCode,String errorMsg);
        void connectSuccess();
    }


    private native void nInitConnect(String liveUrl);
    private native void nStop();
}
