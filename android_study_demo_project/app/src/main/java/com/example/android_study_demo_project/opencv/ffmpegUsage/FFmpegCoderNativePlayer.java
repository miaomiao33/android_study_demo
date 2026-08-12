package com.example.android_study_demo_project.opencv.ffmpegUsage;

public class FFmpegCoderNativePlayer {
//    static {
//        System.loadLibrary("FFmpegCoderPlayerLib");
//    }
    public static native void play(String url);
    public static native void stop();
}
