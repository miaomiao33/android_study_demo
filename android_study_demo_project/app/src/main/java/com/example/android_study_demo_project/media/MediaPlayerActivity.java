package com.example.android_study_demo_project.media;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.android_study_demo_project.R;

import java.io.File;
import java.io.IOException;

//MediaPlayer播放视频
public class MediaPlayerActivity extends AppCompatActivity {
    Button optionButton;
    MediaPlayer mediaPlayer;
    TextureView textureView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_player);
        textureView = (TextureView) findViewById(R.id.ttv_media_player);
        optionButton = (Button) findViewById(R.id.bt_media_player_option);
        optionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayerOptionClick();
            }
        });
    }

    //点击按钮
    private void mediaPlayerOptionClick()
    {
        CharSequence text = optionButton.getText();
        if(TextUtils.equals(text,"开始"))
        {
            //播放视频的准备
            optionButton.setText("结束");
            mediaPlayer = new MediaPlayer();
            //异步准备
            mediaPlayer.setOnPreparedListener(new MediaPlayerActivityPreparedListener());
            //播放完毕
            mediaPlayer.setOnCompletionListener(new MediaPlayerActivityCompletionListener());

            try {
                mediaPlayer.setDataSource(new File(getExternalFilesDir(""),"a.mp4").getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }

            //设置画布播放视频
            mediaPlayer.setSurface(new Surface(textureView.getSurfaceTexture()));
            mediaPlayer.prepareAsync();
        }else
        {
            //结束录制
            //释放所有
            optionButton.setText("开始");
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    private class MediaPlayerActivityPreparedListener implements MediaPlayer.OnPreparedListener  {

        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            //异步准备完毕
            mediaPlayer.start();
        }
    }

    private class MediaPlayerActivityCompletionListener implements MediaPlayer.OnCompletionListener  {

        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            //播放完毕释放
            optionButton.setText("开始");
            mediaPlayer.release();
        }
    }
}
