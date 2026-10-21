package com.example.android_study_demo_project.opencv.ffmpegUsage.livePush.CameraView;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.graphics.Point;
import android.view.MotionEvent;

import com.example.android_study_demo_project.opencv.ffmpegUsage.livePush.BaseGLSurfaceView;
import com.example.android_study_demo_project.opencv.ffmpegUsage.livePush.RenderListener;

public class LivePushCameraView extends BaseGLSurfaceView implements LivePushCameraRender.RenderListener {
    private LivePushCameraHelper mCameraHelper;
    private LivePushCameraRender mCameraRender;
    private int mCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
    private boolean mIsFocusing;
    private FocusListener mFocusListener;


    public LivePushCameraView(Context context) {
        this(context,null);
    }


    public LivePushCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEGLContextClientVersion(2);
        mCameraRender = new LivePushCameraRender(context);
        mCameraHelper = new LivePushCameraHelper(context);
        setRenderer(mCameraRender);
        mCameraRender.setOnRenderListener(this);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_UP)
        {
            //聚焦
            focus((int) event.getX(),(int) event.getY());
        }
        return true;
    }

    @Override
    public void onSurfaceCreated(SurfaceTexture surfaceTexture) {
        //先等 CameraRender 把 Surface 创建好了，再进行初始化和打开相机
        mCameraHelper.init(surfaceTexture);
        open(mCameraId);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mCameraRender.setViewHeight(getMeasuredHeight());
        mCameraRender.setViewWidth(getMeasuredWidth());
        mCameraHelper.setViewHeight(getMeasuredHeight());
        mCameraHelper.setViewWidth(getMeasuredWidth());
    }

    public void open(int cameraId)
    {
        rotateCameraAngle();
        mCameraHelper.open(cameraId);
    }

    /**
     * 旋转相机的角度
     */
    private void rotateCameraAngle() {
        mCameraRender.resetMatrix();
        //前置摄像头
        if(mCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT)
        {
            mCameraRender.rotateMatrix(0,90,0,0,1);
            //绕 X 轴转 180 度 → 上下翻转（图像上下颠倒）
//            mCameraRender.rotateMatrix(0,180,1,0,0);
            //- x = `-1`：X 轴缩放 - 1 → 左右镜像翻转
            //- y = `1`：Y 轴不变，不上下翻转
            //- z = `1`：Z 轴不变
            mCameraRender.scaleMatrix(-1,1,1); //左右镜像
        }
        //后置摄像头
        else if(mCameraId == Camera.CameraInfo.CAMERA_FACING_BACK)
        {
            //Camera1 原生输出画面是横屏方向（手机竖屏拿相机，图像本身是横的）
            //绕 Z 轴 顺时针旋转 90 度
            mCameraRender.rotateMatrix(0,90,0,0,1);
            mCameraRender.scaleMatrix(1,-1,1);
        }
    }


    public void onDestroy()
    {
        mCameraHelper.close();
    }

    private void focus(final int x, final int y)
    {
        if(mIsFocusing)
        {
            return;
        }
        mIsFocusing = true;
        Point focusPoint = new Point(x,y);
        if(mFocusListener != null)
        {
            mFocusListener.beginFocus(x,y);
        }

        mCameraHelper.newCameraFocus(focusPoint, new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                mIsFocusing = false;
                if(mFocusListener != null)
                {
                    mFocusListener.endFocus(success);
                }
            }
        });
    }

    public void setOnFocusListener(FocusListener focusListener) {
        this.mFocusListener = focusListener;
    }

    public int getTextureId()
    {
        return mCameraRender.getTextureId();
    }

    interface FocusListener {
        public void beginFocus(int x,int y);
        public void endFocus(boolean success);
    }

}
