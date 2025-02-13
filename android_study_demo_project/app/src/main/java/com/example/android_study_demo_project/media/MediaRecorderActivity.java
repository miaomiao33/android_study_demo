package com.example.android_study_demo_project.media;

import android.Manifest;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.android_study_demo_project.R;

import java.io.File;
import java.io.IOException;

//录制视频
public class MediaRecorderActivity extends AppCompatActivity {
    Button optionButton;
    Camera camera;
    MediaRecorder mediaRecorder;
    TextureView textureView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_recorder);
        textureView = (TextureView) findViewById(R.id.ttv);
        optionButton = (Button) findViewById(R.id.bt_video_option);
        optionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                optionClick();
            }
        });
        requestPower();
    }

    //申请权限
    private void requestPower()
    {
        ActivityCompat.requestPermissions(
                MediaRecorderActivity.this,
                new String[]{Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO,},100);
    }

    //点击按钮
    private void optionClick()
    {
        CharSequence text = optionButton.getText();
        if(TextUtils.equals(text,"开始"))
        {
            //录制视频的准备
            optionButton.setText("结束");
            camera = Camera.open();
            camera.setDisplayOrientation(90);//旋转90度
            camera.unlock();
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setCamera(camera);
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);//设置音频来源 麦克风
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);//设置视频来源 摄像头
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);//指定视频文件格式
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mediaRecorder.setOrientationHint(90);//录像视角也旋转90度

            //设置输出文件
            //getExternalFilesDir获取应用程序在外部存储设备上的私有目录路径
            mediaRecorder.setOutputFile(new File(getExternalFilesDir(""),
                    "a.mp4").getAbsoluteFile());

            //mediaRecorder.setVideoSize(640,640);//直接设置可能会是不支持的宽高
            setVideoHeightAndWidth();

            //预览
            mediaRecorder.setPreviewDisplay(new Surface(textureView.getSurfaceTexture()));

            try {
                mediaRecorder.prepare();
                mediaRecorder.start();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }else
        {
            //结束录制
            //释放所有
            optionButton.setText("开始");
            mediaRecorder.stop();
            mediaRecorder.release();
            camera.stopPreview();
            camera.release();
        }
    }

    //获得支持的视频宽高并设置
    void setVideoHeightAndWidth()
    {
        CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);

        //设置视频格式
        mediaRecorder.setVideoSize(profile.videoFrameWidth,profile.videoFrameHeight);
        //每分钟录制的比特数
        mediaRecorder.setVideoEncodingBitRate(profile.videoBitRate);
        //录制视频的帧数
        mediaRecorder.setVideoFrameRate(30);
    }

}
