package com.example.android_study_demo_project.opencv.ffmpegUsage.livePush;

public interface IFboRender {
    void onBindFbo();
    void onUnBindFbo();
    void onSurfaceCreated(int viewWidth, int viewHeight);
    void onSurfaceChanged(int width, int height);
    void onDrawFrame();
}
