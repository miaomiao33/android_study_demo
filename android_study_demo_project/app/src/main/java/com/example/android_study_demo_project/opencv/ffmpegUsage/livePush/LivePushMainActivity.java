package com.example.android_study_demo_project.opencv.ffmpegUsage.livePush;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android_study_demo_project.R;

public class LivePushMainActivity extends AppCompatActivity {
    private LivePush mLivePush ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_live_push_main);
        mLivePush = new LivePush("rtmp://192.168.0.173:1935/live/teststream");
        mLivePush.setmConnectListener(new LivePush.ConnectListener() {
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
        mLivePush.initConnect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLivePush.stop();
    }
}