package com.example.android_study_demo_project.opencv.ffmpegUsage.MusicPlayer;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android_study_demo_project.R;

import java.io.File;

public class MusicPlayerMainActivity extends AppCompatActivity {

    File mMusicFile = new File("http://downsc.chinaz.net/Files/DownLoad/sound1/201906/11582.mp3");
    private DarrenPlayer mPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player_main);

        mPlayer = new DarrenPlayer();
        mPlayer.setDataSource(mMusicFile.getAbsolutePath());
        mPlayer.play();
    }
}