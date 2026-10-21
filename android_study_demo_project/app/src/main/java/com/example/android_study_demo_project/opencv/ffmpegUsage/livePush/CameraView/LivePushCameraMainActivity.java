package com.example.android_study_demo_project.opencv.ffmpegUsage.livePush.CameraView;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android_study_demo_project.R;

public class LivePushCameraMainActivity extends AppCompatActivity {
    private LivePushCameraView mCameraView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_push_camera_main);
        mCameraView = findViewById(R.id.live_push_camera_view);
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

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCameraView.onDestroy();
    }
}