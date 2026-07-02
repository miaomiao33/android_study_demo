package com.example.android_study_demo_project.opencv.liveStreamingPush;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.android_study_demo_project.R;

public class LiveStreamingMainActivity extends AppCompatActivity {
    private Button startButton;
    private Button stopButton;
    private ScreenLive mScreenLive;
    private MediaProjectionManager mediaProjectionManager;
    private LiveTaskManager.TaskBinder taskBinder;
    private LiveTaskManager liveTaskManager;

    private ServiceConnection connection;
    private String TAG = LiveStreamingMainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_streaming_main);

        startButton = (Button)findViewById(R.id.bt_start_living_stream);
        stopButton = (Button)findViewById(R.id.bt_stop_living_stream);
        LiveStreamingMainActivityClick click = new LiveStreamingMainActivityClick();

        startButton.setOnClickListener(click);
        stopButton.setOnClickListener(click);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        mScreenLive.onActivityResult(requestCode, resultCode, data,taskBinder);
        super.onActivityResult(requestCode, resultCode, data);
    }

    //开启直播
    public void startLive()
    {
        startLiveTaskService(() -> {
            mScreenLive = new ScreenLive();
            //斗鱼地址
            String address = "rtmp://sendhw3a.douyu.com/live/";
            String livingCode = "12865925rwemDOUh?dyPRI=0&noforward=1&origin=hw&record=flv&roirecognition=0&stemp_id=12898962&tw=0&wm=0&wsSecret=57ba232c2c986241b3da2b55afb1089a&wsSeek=off&wsTime=6a463f37";
            mScreenLive.startInitLive(mediaProjectionManager, address + livingCode,
                    this::stopService);//stopService，解绑service，并停止service
        });
    }

    //后台开启屏幕录制Service
    public void startLiveTaskService(LiveStreamingActivityCallBack liveStreamingActivityCallBack)
    {
        //Android 10（API29）及以上系统，录屏 MediaProjection 强制要求：
        //获取录屏权限、运行录屏逻辑的 Service 必须是前台服务，
        // 且前台类型指定为 FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION，否则直接抛 SecurityException。
        Intent serviceIntent = new Intent(this, LiveTaskManager.class);
        startService(serviceIntent);
        // 启动前台任务服务
        connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                // 提交录屏/推流任务
                //投屏管理器
                taskBinder = (LiveTaskManager.TaskBinder)service;
                liveTaskManager = taskBinder.getService();
                mediaProjectionManager = (MediaProjectionManager)
                        getSystemService(Context.MEDIA_PROJECTION_SERVICE);
                //准备好后回调，需要在onActivityResult之前回调初始化
                liveStreamingActivityCallBack.callBack();
                //创建投屏请求，然后回调onActivityResult
                Intent captureIntent = mediaProjectionManager.createScreenCaptureIntent();
                startActivityForResult(captureIntent,100);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                liveTaskManager = null;
            }
        };
        // 绑定服务
        Intent bindIntent = new Intent(this, LiveTaskManager.class);
        boolean bindOk = bindService(bindIntent, connection, BIND_AUTO_CREATE);
    }


    public void stopLive()
    {
        Log.i(TAG,"stopService mScreenLive.stopLive()");
        mScreenLive.stopLive();
        //上面还有一部分停止逻辑（停止Service）
    }

    public void stopService()
    {
        // 解绑
        unbindService(connection);
        Log.i(TAG,"stopService unbindService");
        // 兜底停止
        Intent stopIntent = new Intent(this, LiveTaskManager.class);
        stopService(stopIntent);
        Log.i(TAG,"stopService stopService");
    }

    private class LiveStreamingMainActivityClick implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            switch (view.getId())
            {
                case R.id.bt_start_living_stream:
                    startLive();
                    break;
                case R.id.bt_stop_living_stream:
                    stopLive();
                    break;
            }
        }
    }

    public static interface LiveStreamingActivityCallBack{
        void callBack();
    }
}