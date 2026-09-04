package com.example.android_study_demo_project.opencv.ffmpegUsage.livingStream;


//基类，定义推拉的简单接口
public abstract class BasePush {
    public abstract void startPush();
    public abstract void stopPush();
    public abstract void release();
}
