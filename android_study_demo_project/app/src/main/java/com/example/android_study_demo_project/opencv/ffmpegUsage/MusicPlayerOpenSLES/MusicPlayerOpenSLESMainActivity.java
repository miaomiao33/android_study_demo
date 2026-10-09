package com.example.android_study_demo_project.opencv.ffmpegUsage.MusicPlayerOpenSLES;

import android.os.Bundle;
import android.os.Environment;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android_study_demo_project.R;

import java.io.File;

/***
 * 使用OpenSLGS进行播放
 */
public class MusicPlayerOpenSLESMainActivity extends AppCompatActivity {
    File mMusicFile = new File(Environment.getExternalStorageDirectory(),"input.mp3");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_music_player_open_slesmain);
        playPCM(mMusicFile.getAbsolutePath());
    }

    private native void playPCM(String absolutePath);
}