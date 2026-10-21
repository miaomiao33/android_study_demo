package com.example.android_study_demo_project.opencv.ffmpegUsage.livePush;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class BitmapGLSurfaceView extends GLSurfaceView {
    private BitmapRender mRender;
    public BitmapGLSurfaceView(Context context) {
        super(context);
    }

    public BitmapGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEGLContextClientVersion(2);
        mRender = new BitmapRender(context);
        setRenderer(mRender);
    }

    public BitmapRender getRender() {
        return mRender;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mRender.setViewHeight(getMeasuredHeight());
        mRender.setViewWidth(getMeasuredWidth());
    }
}
