package com.example.android_study_demo_project.opencv.ffmpegUsage.livingStream;

/**
 * 视频数据参数
 */
public class VideoInfo {
    //注意编码器、buffer等以previewWidth、previewHeight为准
    private int previewWidth = 640;
    private int previewHeight = 480;
    private int pictureWidth = 640;
    private int pictureHeight = 480;
    //码率为 480 kb/s
    private int bitrate = 480_000;
    //帧频默认为 25 fps
    private int fps = 25;
    //后置CAMERA_FACING_BACK = 0;  前置CAMERA_FACING_FRONT = 1;
    private int cameraId;

    public VideoInfo(int previewWidth, int previewHeight, int cameraId) {
        super();
        this.previewWidth = previewWidth;
        this.previewHeight = previewHeight;
        this.cameraId = cameraId;
    }

    public VideoInfo( int cameraId) {
        super();
        this.cameraId = cameraId;
    }

    public int getPreviewWidth() {
        return previewWidth;
    }

    public void setPreviewWidth(int previewWidth) {
        this.previewWidth = previewWidth;
    }

    public int getPreviewHeight() {
        return previewHeight;
    }

    public void setPreviewHeight(int previewHeight) {
        this.previewHeight = previewHeight;
    }

    public int getPictureWidth() {
        return pictureWidth;
    }

    public void setPictureWidth(int pictureWidth) {
        this.pictureWidth = pictureWidth;
    }

    public int getPictureHeight() {
        return pictureHeight;
    }

    public void setPictureHeight(int pictureHeight) {
        this.pictureHeight = pictureHeight;
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
