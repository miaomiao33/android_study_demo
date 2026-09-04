package com.example.android_study_demo_project.opencv.ffmpegUsage.livingStream;

/**
 * 视频数据参数
 */
public class VideoInfo {
    private int width;
    private int height;
    //码率为 480 kb/s
    private int bitrate = 480_000;
    //帧频默认为 25 fps
    private int fps = 25;
    private int cameraId;

    public VideoInfo(int width, int height, int cameraId) {
        super();
        this.width = width;
        this.height = height;
        this.cameraId = cameraId;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getBitrate() {
        return bitrate;
    }

    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }

    public int getFps() {
        return fps;
    }

    public void setFps(int fps) {
        this.fps = fps;
    }

    public int getCameraId() {
        return cameraId;
    }

    public void setCameraId(int cameraId) {
        this.cameraId = cameraId;
    }
}
