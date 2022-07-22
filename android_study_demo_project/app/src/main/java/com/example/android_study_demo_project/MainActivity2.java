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
        bindService(new Intent(this,MyService.class),connection, Context.BIND_AUTO_CREATE);
    }

    public void unBindService(View view) {
        unbindService(connection);
    }


    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    //when activity is destroyed , voluntarily unbindService
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }
}
