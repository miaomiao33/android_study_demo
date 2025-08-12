package com.example.android_study_demo_project.JiGuangUsage;

import android.content.Context;

import cn.jpush.android.api.CmdMessage;
import cn.jpush.android.service.JPushMessageReceiver;

public class JGPushMessageReceiver extends JPushMessageReceiver {
    @Override
    public void onCommandResult(Context context, CmdMessage cmdMessage) {
        super.onCommandResult(context, cmdMessage);
    }
}
