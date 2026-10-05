package com.example.android_study_demo_project.opencv.ffmpegUsage.pullRTMP;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.android_study_demo_project.R;

import java.util.concurrent.ExecutorService;

public class PullRtmpMainActivity extends AppCompatActivity {
//    static {
//        System.loadLibrary("ffmpegLib");
//    }
    private SurfaceView surfaceView;
    private ExecutorService executorService;
    private TextView videoTimeTV;
    private TextView audioTimeTV;
    private TextView totalTimeTV;
    private long startTime;
    String rtmpUrl = "rtsp://47.92.198.97:554/live/test";
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pull_rtmp_main);

        videoTimeTV = findViewById(R.id.tv_ffmpeg_pull_rtmp_video_time);
        audioTimeTV = findViewById(R.id.tv_ffmpeg_pull_rtmp_audio_time);
        totalTimeTV = findViewById(R.id.tv_ffmpeg_pull_rtmp_time);
        surfaceView = findViewById(R.id.sv_ffmpeg_pull_rtmp_surface);

        setupSurfaceView(surfaceView, rtmpUrl);
        startUpdateTimer();
    }

    private void setupSurfaceView(SurfaceView surfaceView, String rtspUrl) {
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                // 初始化拉流，传入rtmp地址、Surface
                rtmpPullInit(rtmpUrl, holder.getSurface());
                executorService.submit(() -> {
                    //2.开启音频播放器（OpenSL）
                    rtmpAudioPlayStart();
                    //3.启动拉流线程
                    rtmpPullStart();
                });
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                stop();
            }
        });
    }

    void stop()
    {
        rtmpPullStop();
        rtmpAudioPlayStop();
    }

    // 初始化拉流，传入rtmp地址、Surface
    public native void rtmpPullInit(String rtmpUrl, Surface surface);
    // 开始拉流
    public native void rtmpPullStart();
    // 停止拉流释放资源
    public native void rtmpPullStop();

    //开启音频播放器（OpenSL）
    public native void rtmpAudioPlayStart();
    //停止音频
    public native void rtmpAudioPlayStop();

    public native double getGlobalAudioClock(); // 获取全局音频时钟

    public native double getGlobalVideoPtsInSeconds(); // 获取全局视频时间戳

    private void startUpdateTimer() {
        Runnable updateTask = new Runnable() {
            @Override
            public void run() {
                double audioClock = getGlobalAudioClock();
                double videoPts = getGlobalVideoPtsInSeconds();
                long elapsedTime = SystemClock.elapsedRealtime() - startTime;
                handler.post(() -> {
                    videoTimeTV.setText("Video PTS: " + String.format("%.1f", videoPts));
                    audioTimeTV.setText("Audio Clock: " + String.format("%.1f", audioClock));
                    totalTimeTV.setText("Timer: " + String.format("%.1f", elapsedTime / 1000.0) + "s");
                });
                handler.postDelayed(this, 500); // 每 500 毫秒更新一次
            }
        };
        handler.postDelayed(updateTask, 500);
    }
}