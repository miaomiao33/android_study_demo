package com.example.android_study_demo_project.opencv.ffmpegUsage.videoDecoder;

public class FFmpegVideoDecoderNativePlayer {
//    static {
//        System.loadLibrary("FFmpegCoderPlayerLib");
//    }
    public static native int playVideo(String url, Object surface);
}
