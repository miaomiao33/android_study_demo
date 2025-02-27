package com.example.android_study_demo_project.media;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.android_study_demo_project.R;

public class MediaMainActivity extends AppCompatActivity {

    Button recorderButton;
    Button playerButton;
    Button videoViewButton;
    Button soundPoolButton;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_main);
        recorderButton = (Button) findViewById(R.id.bt_media_recorder);
        playerButton = (Button) findViewById(R.id.bt_media_player);
        videoViewButton = (Button) findViewById(R.id.bt_media_video_view);
        soundPoolButton = (Button) findViewById(R.id.bt_media_sound_pool);

        recorderButton.setOnClickListener(new MediaMainClick());
        playerButton.setOnClickListener(new MediaMainClick());
        videoViewButton.setOnClickListener(new MediaMainClick());
        soundPoolButton.setOnClickListener(new MediaMainClick());
    }

    private class MediaMainClick implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.bt_media_recorder:
                    gotoMediaRecorder();
                    break;
                case R.id.bt_media_player:
                    gotoMediaPlayer();
                    break;
                case R.id.bt_media_video_view:
                    gotoVideoView();
                    break;
                case R.id.bt_media_sound_pool:
                    gotoMediaSoundPool();
                    break;
            }
        }
    }

    //跳转到录制视频
    private void gotoMediaRecorder()
    {
        Intent intent = new Intent(MediaMainActivity.this,MediaRecorderActivity.class);
        startActivity(intent);
    }

    //跳转到MediaPlayer播放视频
    private void gotoMediaPlayer()
    {
        Intent intent = new Intent(MediaMainActivity.this,MediaPlayerActivity.class);
        startActivity(intent);
    }

    //跳转到VideoView播放视频
    private void gotoVideoView()
    {
        Intent intent = new Intent(MediaMainActivity.this,MediaVideoViewActivity.class);
        startActivity(intent);
    }

    //跳转到录制和播放音频
    private void gotoMediaSoundPool()
    {
        Intent intent = new Intent(MediaMainActivity.this,MediaSoundPoolActivity.class);
        startActivity(intent);
    }
}
