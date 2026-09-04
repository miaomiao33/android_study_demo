package com.example.android_study_demo_project.opencv.ffmpegUsage.livingStream;

import android.os.Bundle;
import android.os.Environment;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android_study_demo_project.R;

public class FFmpegLiveStreamMainActivity extends AppCompatActivity {
    private static String URL = "";
    private AVStreamPush mAvStreamPush;
    private Button mStartLive;
    private Button mChangePreview;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ffmpeg_live_stream_main);
        mStartLive = (Button) findViewById(R.id.bt_ffmpeg_start_live);
        mChangePreview = (Button) findViewById(R.id.bt_ffmpeg_change_camera);
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.sv_ffmpeg_live_stream_surface);

        //相机图像的预览
        String address = "rtmp://sendhw3.douyu.com/live/";
        String livingCode = "12865925rWe8mTZH?dyPRI=0&noforward=1&origin=hw&record=flv&roirecognition=0&stemp_id=12898962&tw=0&wm=0&wsSecret=66877ca639c725b1104ad34108b22d80&wsSeek=off&wsTime=6a9a8b53";
        URL = address+livingCode;
        mAvStreamPush = new AVStreamPush(surfaceView.getHolder());
        mStartLive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button btn = (Button) view;
                if(btn.getText().equals("开始直播"))
                {
                    mAvStreamPush.startPush(URL,getExternalFilesDir(Environment.DIRECTORY_MOVIES).getPath());
                    btn.setText("停止直播");
                }else {
                    mAvStreamPush.stopPush();
                    btn.setText("开始直播");
                }
            }
        });

        mChangePreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAvStreamPush.switchCamera();
            }
        });
    }
}