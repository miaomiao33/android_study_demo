package com.example.android_study_demo_project.opencv.ffmpegUsage.MusicAndVideoPlayer;

import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android_study_demo_project.R;

import java.io.File;

public class MusicAndVideoPlayerMainActivity extends AppCompatActivity {
    File mVideoFile = new File(getExternalFilesDir(Environment.DIRECTORY_MOVIES),"videoTest.mp4");
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
        //播放
        mVideoView.play(mVideoFile.getAbsolutePath());
    }

}