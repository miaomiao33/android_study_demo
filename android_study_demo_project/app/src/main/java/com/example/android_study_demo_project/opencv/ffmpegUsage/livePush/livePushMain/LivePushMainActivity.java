package com.example.android_study_demo_project.opencv.ffmpegUsage.livePush.livePushMain;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android_study_demo_project.R;
import com.example.android_study_demo_project.opencv.ffmpegUsage.livePush.CameraView.LivePushCameraView;

public class LivePushMainActivity extends AppCompatActivity {
    private LivePushCameraView mCameraView;
    private Button startButton;
    private BaseVideoPush mVideoPush;//使用多态
    private TextView timeTextView;
    private String liveUrl = "rtmp://192.168.0.173:1935/live/teststream";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_push_main);

        mCameraView = findViewById(R.id.lcv_live_push_camera_view);
        startButton = findViewById(R.id.bt_live_push_start);
        timeTextView = findViewById(R.id.tv_live_push_time);
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
                if("开始推流".contentEquals(button.getText()))
                {
                    button.setText("结束推流");
                    startLivePush();
                    Log.i("TAG","开始推流");
                }else {
                    button.setText("开始推流");
                    mVideoPush.stopPush();
                    Log.i("TAG","结束录制");
                }
            }
        });

    }

    private void startLivePush() {
        //默认彩色滤镜
        mVideoPush = new DefaultVideoPush(LivePushMainActivity.this,
                mCameraView.getEglContext(), mCameraView.getTextureId());
        //黑白滤镜
//        mVideoPush = new GrayVideoPush(LivePushMainActivity.this,
//                mCameraView.getEglContext(), mCameraView.getTextureId());
        //这里是 BaseVideoPush 的连接监听
        mVideoPush.setConnectListener(new LivePush.ConnectListener() {
            @Override
            public void connectError(int errorCode, String errorMsg) {
                Log.e("TAG", "connectError:  errorCode:"+ errorCode);
                Log.e("TAG", "connectError:  errorMsg:"+ errorMsg);
            }

            @Override
            public void connectSuccess() {
                Log.e("TAG", "connectSuccess: 可以推流了");
            }
        });

        mVideoPush.initVideo(liveUrl,
//                LivePushUtils.getScreenWidth(LivePushMainActivity.this),
//                LivePushUtils.getScreenHeight(LivePushMainActivity.this));
                720/2,1280/2);
        mVideoPush.startPush();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mVideoPush.stopPush();
    }
}