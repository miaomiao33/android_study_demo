package com.example.android_study_demo_project.opencv.ffmpegUsage.livePush.CameraView;

public interface MediaInfoListener {
    void musicInfo(int sampleRate,int channels);

    void callbackPcm(byte[] pcmData,int size);
}
