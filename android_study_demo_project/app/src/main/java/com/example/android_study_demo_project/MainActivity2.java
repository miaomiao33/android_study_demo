package com.example.android_study_demo_project;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity2 extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_2);
    }

    public void startService(View view) {
        startService(new Intent(this,MyService.class));
    }

    public void stopService(View view) {
        stopService(new Intent(this,MyService.class));
    }

    public void bindService(View view) {
        //绑定服务
        //构建绑定服务的Intent对象
        Intent intent = new Intent(this,MyService.class);
        //绑定服务
        bindService(intent,connection, Context.BIND_AUTO_CREATE);
    }

    public void unBindService(View view) {
        //解绑服务
        unbindService(connection);
    }


    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            //Activity与Service关联时
            MyService.MyBinder myBinder = (MyService.MyBinder) iBinder;
            //在Activity中调用Service中的方法
            myBinder.service_connect_activity();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            //失去关联时
        }
    };

    //when activity is destroyed , voluntarily unbindService
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }
}
