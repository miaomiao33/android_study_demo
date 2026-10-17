package com.example.android_study_demo_project.opencv.ffmpegUsage.MusicAndVideoPlayer;

import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android_study_demo_project.R;

import java.io.File;

public class MusicAndVideoPlayerMainActivity extends AppCompatActivity {
    File mVideoFile ;
    private DZVideoView mVideoView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_and_video_player_main);
        //播放 /storage/emulated/0/Android/data/com.example.android_study_demo_project/files/Movie 下的 videoTest.mp4
        mVideoFile = new File(getExternalFilesDir(Environment.DIRECTORY_MOVIES),"videoTest.mp4");

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
//        mVideoView.play("rtmp://192.168.0.173:1935/live/teststream");
    }

}