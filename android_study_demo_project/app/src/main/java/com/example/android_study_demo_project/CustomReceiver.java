package com.example.android_study_demo_project;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class CustomReceiver extends BroadcastReceiver {

    private static final String TAG = CustomReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG,"CustomReceiver 接收者");
        Toast.makeText(context, "收到广播", Toast.LENGTH_SHORT).show();
    }
}
