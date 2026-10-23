package com.example.android_study_demo_project.opencv.ffmpegUsage.livePush.CameraView;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android_study_demo_project.R;

import java.io.File;

public class MusicPlayerMainActivity extends AppCompatActivity {

//    File mMusicFile = new File("http://downsc.chinaz.net/Files/DownLoad/sound1/201906/11582.mp3");
    File mMusicFile ;
    private RecordVideoDarrenPlayer mPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player_main);
        //播放 /storage/emulated/0/Android/data/com.example.android_study_demo_project/files/Music 下的 四月是你的谎言.mp3
        mMusicFile = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC),"四月是你的谎言.mp3");

        mPlayer = new RecordVideoDarrenPlayer();
        mPlayer.setDataSource(mMusicFile.getAbsolutePath());
        mPlayer.setOnErrorListener(new MediaErrorListener() {
            @Override
            public void onError(int code, String msg) {
                Log.e("TAG","error code:"+code);
                Log.e("TAG","error msg:"+msg);
                //Java 的逻辑代码，处理失败时
            }
        });

//        //使用AudioTrack播放音频
//        mPlayer.play();

//        //使用OpenSLES播放音频
//        mPlayer.prepare();
//        mPlayer.playOpenSLES();

//        //异步准备（不带队列）
//        mPlayer.setOnPreparedListener(new MediaPreparedListener() {
//            @Override
//            public void onPrepared() {
//                Log.i("TAG","准备完毕");
//                mPlayer.playOpenSLES();
//            }
//        });
//        mPlayer.prepareAsync();
//
        //异步准备（带队列）
        mPlayer.setOnPreparedListener(new MediaPreparedListener() {
            @Override
            public void onPrepared() {
                Log.i("TAG","准备完毕");
                mPlayer.playOpenSLESWithQueue();
            }
        });
        mPlayer.prepareAsyncWithQueue();
    }
}