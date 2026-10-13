package com.example.android_study_demo_project.opencv.ffmpegUsage.MusicAndVideoPlayer;

import android.os.Bundle;
import android.os.Environment;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android_study_demo_project.R;
import com.example.android_study_demo_project.opencv.ffmpegUsage.MusicPlayer.DarrenPlayer;
import com.example.android_study_demo_project.opencv.ffmpegUsage.MusicPlayer.MediaPreparedListener;

import java.io.File;

public class MusicAndVideoPlayerMainActivity extends AppCompatActivity {
    File mVideoFile = new File(Environment.getExternalStorageDirectory(),".mp4");
    private DarrenPlayer mPlayer;
    private DZVideoView mVideoView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_and_video_player_main);

        Button playButton = findViewById(R.id.bt_video_player_play);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                play(view);
            }
        });

        mVideoView = findViewById(R.id.sf_video_view);
    }



    public void play(View view) {
        //播放音频
        mVideoView.play(mVideoFile.getAbsolutePath());

        //播放视频
        decodeVideo(mVideoFile.getAbsolutePath(),mVideoView.getHolder().getSurface());
    }

    private native void decodeVideo(String absolutePath, Surface surface);
}