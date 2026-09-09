package com.example.android_study_demo_project.opencv.ffmpegUsage.livingStream;

public class FFmpegLiveStreamNativePlayer {
    static {
//        System.loadLibrary("ffmpegLib");
    }
    //JNI端创建推送线程，并开始推送
    public native void startPush(String url,String sdkPath);
    public native void stopPush();
    public native void release();

    /***
     * 设置视频参数
     * @param width
     * @param height
     * @param bitrate
     * @param fps
     */
    public native void setVideoOptions(int width, int height, int bitrate, int fps);

    /***
     * 设置音频参数
     * @param sampleRateInHz
     * @param channel
     */
    public native void setAudioOptions(int sampleRateInHz, int channel);

    /***
     * 发送视频数据
     * @param data
     */
    public native void sendVideoPacket(byte[] data,int fps);

    /***
     * 发送音频数据
     * @param data
     * @param len
     */
    public native void sendAudioPacket(byte[] data, int len);
}
