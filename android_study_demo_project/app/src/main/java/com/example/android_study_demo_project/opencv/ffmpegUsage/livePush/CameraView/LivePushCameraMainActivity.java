package com.example.android_study_demo_project.opencv.ffmpegUsage.livePush.CameraView;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android_study_demo_project.R;

public class LivePushCameraMainActivity extends AppCompatActivity implements BaseVideoRecorder.RecordListener {
    private LivePushCameraView mCameraView;
    private Button startButton;
    private BaseVideoRecorder mVideoRecorder;//使用多态
    private TextView timeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_push_camera_main);
        mCameraView = findViewById(R.id.live_push_camera_view);
        startButton = findViewById(R.id.bt_live_push_camera_start);
        timeTextView = findViewById(R.id.tv_camera_time);
        mCameraView.setOnFocusListener(new LivePushCameraView.FocusListener() {
            @Override
            public void beginFocus(int x, int y) {
                Log.i("TAG","开始聚焦");
            }

            @Override
            public void endFocus(boolean success) {
                Log.i("TAG","结束聚焦");
            }
        });

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button button = (Button) view;
                if("开始录制".contentEquals(button.getText()))
                {
                    button.setText("结束录制");
                    //正常滤镜
                    mVideoRecorder = new DefaultVideoRecorder(LivePushCameraMainActivity.this,
                            mCameraView.getEglContext(),
                            mCameraView.getTextureId());
//                    //保存为黑白滤镜
//                    mVideoRecorder = new GrayVideoRecorder(LivePushCameraMainActivity.this,
//                            mCameraView.getEglContext(),
//                            mCameraView.getTextureId());
                    mVideoRecorder.initVideo(getExternalFilesDir(Environment.DIRECTORY_MUSIC)
                                    + "/四月是你的谎言.mp3",
                            getExternalFilesDir(Environment.DIRECTORY_MOVIES)+"/videoRecord.mp4",
                            720,1280);
                    mVideoRecorder.setOnRecordListener(LivePushCameraMainActivity.this);
                    mVideoRecorder.startRecord();
                    Log.i("TAG","开始录制");
                }else {
                    button.setText("开始录制");
                    mVideoRecorder.stopRecord();

                    Log.i("TAG","结束录制");
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCameraView.onDestroy();
    }

    @Override
    public void onTime(long times) {
        //在主线程进行修改
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                timeTextView.setText("当前时间:" + times);
            }
        });
    }
}