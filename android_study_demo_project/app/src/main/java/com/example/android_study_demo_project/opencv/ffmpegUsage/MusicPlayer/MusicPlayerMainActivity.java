package com.example.android_study_demo_project.opencv.ffmpegUsage.MusicPlayer;

import android.os.Bundle;
import android.util.Log;

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
        mPlayer.setOnErrorListener(new MediaErrorListener() {
            @Override
            public void onError(int code, String msg) {
                Log.e("TAG","error code:"+code);
                Log.e("TAG","error msg:"+msg);
                //Java 的逻辑代码，处理失败时
            }
        });
        mPlayer.play();
    }
}