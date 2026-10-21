package com.example.android_study_demo_project.opencv.ffmpegUsage.livePush;

import android.os.Handler;
import android.os.Looper;

import com.example.android_study_demo_project.JNI.NDKUsageMainActivity;

public class LivePush {
    static {
        System.loadLibrary("ffmpegLib");
    }

    private String mLiveUrl;
    private ConnectListener mConnectListener;
    private Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    public LivePush(String liveUrl) {
        this.mLiveUrl = liveUrl;
    }

    public void setmConnectListener(ConnectListener connectListener) {
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
    private void onConnectError(int errorCode,String errorMsg)
    {
        stop();
        if(mConnectListener != null)
        {
            mConnectListener.connectError(errorCode,errorMsg);
        }
    }

    public void stop()
    {
        MAIN_HANDLER.post(new Runnable() {
            @Override
            public void run() {
                nStop();
            }
        });
    }

    //链接的回调
    //called from JNI
    private void onConnectSuccess()
    {
        if(mConnectListener != null)
        {
            mConnectListener.connectSuccess();
        }
    }


    public interface ConnectListener{
        void connectError(int errorCode,String errorMsg);
        void connectSuccess();
    }


    private native void nInitConnect(String liveUrl);
    private native void nStop();
}
