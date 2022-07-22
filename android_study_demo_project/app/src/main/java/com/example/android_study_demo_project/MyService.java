package com.example.android_study_demo_project;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

public class MyService extends Service {
    private  final String TAG = MyService.class.getSimpleName();
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,TAG+"--->onCreate");
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Log.d(TAG,TAG+"--->onStart");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,TAG+"--->onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG,TAG+"--->onDestroy");
        super.onDestroy();
    }




    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG,TAG+"--->onBind");
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG,TAG+"--->onUnbind");
        return super.onUnbind(intent);
    }


}
