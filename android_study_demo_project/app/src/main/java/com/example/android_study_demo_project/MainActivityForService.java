package com.example.android_study_demo_project;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivityForService extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_for_servicer);

        //Use java to register receiver
        UpdateIpSelectCity updateIpSelectCity = new UpdateIpSelectCity();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ActionUtils.ACTIONS_EQUES_UPDATE_IP);
        registerReceiver(updateIpSelectCity,filter);
    }

    //send service by static method to receiver
    public void sendAction2(View view) {
        Intent intent = new Intent("com.example.android_study_demo_project");
        intent.setAction(ActionUtils.ACTIONS_FLAG);
        intent.setComponent(
                new ComponentName("com.example.android_study_demo_project",
                        "com.example.android_study_demo_project.CustomReceiver"));

        // 上面这一行在Android 7.0及以下版本不是必须的，但是Android 8.0或者更高版本，
        // 发送广播的条件更加严苛，必须添加这一行内容。
        // 创建的ComponentName实例化对象有两个参数，第1个参数是指接收广播类的包名，第2个参数是指接收广播类的完整类名。

        sendBroadcast(intent);
    }

    //send activity receiver
    public void sendAction1(View view) {
        Intent intent = new Intent();
        intent.setAction(ActionUtils.ACTIONS_EQUES_UPDATE_IP);
        sendBroadcast(intent);
    }
}
