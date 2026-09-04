package com.example.android_study_demo_project.opencv.ffmpegUsage;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import com.example.android_study_demo_project.R;
import com.example.android_study_demo_project.opencv.ffmpegUsage.livingStream.FFmpegLiveStreamMainActivity;
import com.example.android_study_demo_project.opencv.ffmpegUsage.videoDecoder.FFmpegVideoDecoderMainActivity;

import java.util.Objects;

public class FFmpegUsageMainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("ffmpegLib");
    }

    private Button changeFormatButton;
    private Button getFrameButton;
    private Button gotToFFmpgeUsageButton;
    private Button gotToFFmpgeVideoDecoderButton;
    private Button gotToFFmpgeLivingStreamButton;
    private final String TAG = FFmpegUsageMainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ffmpeg_usage_main);
        changeFormatButton = (Button)findViewById(R.id.bt_change_mp4_format);
        getFrameButton = (Button)findViewById(R.id.bt_get_frame_from_stream);
        gotToFFmpgeUsageButton = (Button)findViewById(R.id.bt_goto_ffmpeg_coder);
        gotToFFmpgeVideoDecoderButton = (Button)findViewById(R.id.bt_goto_ffmpeg_video_decoder);
        gotToFFmpgeLivingStreamButton = (Button)findViewById(R.id.bt_goto_ffmpeg_living_stream);

        FFmpegUsageClick click = new FFmpegUsageClick();
        changeFormatButton.setOnClickListener(click);
        getFrameButton.setOnClickListener(click);
        gotToFFmpgeUsageButton.setOnClickListener(click);
        gotToFFmpgeVideoDecoderButton.setOnClickListener(click);
        gotToFFmpgeLivingStreamButton.setOnClickListener(click);
    }

    void changeMP4toAVI()
    {
        //需要先在/storage/emulated/0/Android/data/com.example.android_study_demo_project/files/Movies中放一个ffmpeg.mp4文件
        String inputPath = Objects.requireNonNull(getExternalFilesDir(Environment.DIRECTORY_MOVIES))
                .getAbsolutePath()+"/ffmpeg.mp4";
        String outputPath = Objects.requireNonNull(getExternalFilesDir(Environment.DIRECTORY_MOVIES))
                .getAbsolutePath()+"/ffmpeg.avi";
        //旧版写法
//        changeFormat(inputPath,outputPath);
        //新版写法
        changeFormat2(inputPath,outputPath);
    }

    void getVideoFrameFromStream()
    {
        String outputPath = Objects.requireNonNull(getExternalFilesDir(Environment.DIRECTORY_MOVIES))
                        .getPath()+"/test/test.jpg";
        getFrameFromStream(outputPath);
    }
    void gotoFFmpegCoderUsagePage()
    {
        Intent intent = new Intent(this, FFmpegCoderUsageMainActivity.class);
        startActivity(intent);
    }

    void gotoFFmpegVideoDecoderUsagePage()
    {
        Intent intent = new Intent(this, FFmpegVideoDecoderMainActivity.class);
        startActivity(intent);
    }

    void gotoFFmpegLivingStreamUsagePage()
    {
        Intent intent = new Intent(this, FFmpegLiveStreamMainActivity.class);
        startActivity(intent);
    }

    private class FFmpegUsageClick implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.bt_change_mp4_format:
                    changeMP4toAVI();
                    break;
                case R.id.bt_get_frame_from_stream:
                    getVideoFrameFromStream();
                    break;
                case R.id.bt_goto_ffmpeg_coder:
                    gotoFFmpegCoderUsagePage();
                    break;
                case R.id.bt_goto_ffmpeg_video_decoder:
                    gotoFFmpegVideoDecoderUsagePage();
                    break;
                case R.id.bt_goto_ffmpeg_living_stream:
                    gotoFFmpegLivingStreamUsagePage();
                    break;
            }
        }
    }

    public native void changeFormat(String inputPath,String outputPath);
    public native void changeFormat2(String inputPath,String outputPath);

    public native void getFrameFromStream(String path);
}