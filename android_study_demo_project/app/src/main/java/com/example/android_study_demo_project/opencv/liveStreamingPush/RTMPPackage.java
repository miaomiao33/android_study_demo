package com.example.android_study_demo_project.opencv.liveStreamingPush;

public class RTMPPackage {
    public static final int RTMP_PACKET_TYPE_VIDEO = 0;//视频数据
    public static final int RTMP_PACKET_TYPE_AUDIO_HEAD = 1;//音频头数据（在创建音频包时和音频数据区分）
    public static final int RTMP_PACKET_TYPE_AUDIO_DATA = 2;//音频数据

    private byte[] buffer;
    private int type;//视频包、音频包
    private long tms;
    public static RTMPPackage EMPTY_PACKAGE = new RTMPPackage();

    public byte[] getBuffer() {
        return buffer;
    }

    public void setBuffer(byte[] buffer) {
        this.buffer = buffer;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getTms() {
        return tms;
    }

    public void setTms(long tms) {
        this.tms = tms;
    }
}
