package com.example.android_study_demo_project.espressoUsage;

import android.os.Handler;
import android.os.Looper;

import androidx.test.espresso.IdlingResource;

public class IdlingDelayResources implements IdlingResource {
    private boolean timesUp;
    private ResourceCallback mCallback;

    public IdlingDelayResources(int delayMillis) {
        //模拟访问网络延迟delayMillis毫秒后通知状态修改（delayMillis自动去检测）
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                timesUp = true;
            }
        },delayMillis);
    }

    @Override
    public String getName() {
        return IdlingDelayResources.class.getSimpleName();
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback callback) {
        //通知idle状态的变化回调
        mCallback = callback;
    }

    @Override
    public boolean isIdleNow() {
        //返回当前idlingresource的idle状态
        //true，onTransitionToIdle()上注册的ResourceCallback必须必须在之前已经调用
        if (timesUp && mCallback != null) {//结束了加载时间
            mCallback.onTransitionToIdle();
        }
        return timesUp;
    }

    //也可以手动设置
    public void setIdleState(boolean isIdle) {
        this.timesUp = isIdle;
        if (isIdle && mCallback != null) {
            mCallback.onTransitionToIdle();
        }
    }

}
