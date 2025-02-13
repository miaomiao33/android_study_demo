package com.example.android_study_demo_project.media;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.android_study_demo_project.R;

import java.io.File;

//videoView播放视频
public class MediaVideoViewActivity extends AppCompatActivity {
    VideoView videoView;
    MediaController mediaController;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_video_view);
        videoView = (VideoView)findViewById(R.id.vv_video_player);
        initVideoPlayer();
    }

    //初始化
    private void initVideoPlayer()
    {
        mediaController = new MediaController(MediaVideoViewActivity.this);
        mediaController.setPrevNextListeners(new MediaPlayerClick(),new MediaPlayerClick());//点击上一曲和下一曲
        videoView.setMediaController(mediaController);
        videoView.setVideoPath(new File(getExternalFilesDir(""),"a.mp4").getAbsolutePath());
        videoView.start();
    }

    private class MediaPlayerClick implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            Log.i(MediaVideoViewActivity.class.getSimpleName(),"next or pre");
        }
    }
}
