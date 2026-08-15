package com.example.android_study_demo_project.opencv.ffmpegUsage.videoDecoder;

import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.android_study_demo_project.R;

public class FFmpegVideoDecoderMainActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    private SurfaceHolder mSurfaceHolder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ffmpeg_video_decoder_main);
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.sf_ffmpeg_video_decoder);
        mSurfaceHolder = surfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = "http://mirror.aarnet.edu.au/pub/TED-talks/911Mothers_2010W-480p.mp4";
                FFmpegVideoDecoderNativePlayer.playVideo(url,mSurfaceHolder.getSurface());
            }
        }).start();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {

    }
}