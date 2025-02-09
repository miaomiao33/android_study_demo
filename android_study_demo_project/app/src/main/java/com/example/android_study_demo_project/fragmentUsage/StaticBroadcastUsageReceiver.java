package com.example.android_study_demo_project.fragmentUsage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

//静态注册广播使用
public class StaticBroadcastUsageReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context,"静态注册接收到广播了",Toast.LENGTH_SHORT).show();
    }
}
