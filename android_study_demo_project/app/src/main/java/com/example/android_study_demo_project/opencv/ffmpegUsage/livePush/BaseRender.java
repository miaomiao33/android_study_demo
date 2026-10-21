package com.example.android_study_demo_project.opencv.ffmpegUsage.livePush;

import android.opengl.GLSurfaceView;

public abstract class BaseRender implements GLSurfaceView.Renderer {
    protected int mViewWidth;
    protected int mViewHeight;

    public void setViewWidth(int mViewWidth) {
        this.mViewWidth = mViewWidth;
    }

    public void setViewHeight(int mViewHeight) {
        this.mViewHeight = mViewHeight;
    }
}
