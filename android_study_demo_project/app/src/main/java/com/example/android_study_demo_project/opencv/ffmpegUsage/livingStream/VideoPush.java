package com.example.android_study_demo_project.opencv.ffmpegUsage.livingStream;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

//采集相机种的视频数据，通过预览回调，把采集到的数据回调到JNI层进行美颜或者编码
public class VideoPush extends BasePush implements SurfaceHolder.Callback, Camera.PreviewCallback {
    private static final String TAG = VideoPush.class.getSimpleName();
    private SurfaceHolder mSurfaceHolder;
    private Camera mCamera;
    private VideoInfo mVideoInfo ;
    private byte[] buffers;
    private boolean isPushing = false;
    private FFmpegLiveStreamNativePlayer nativePlayer;
    private String path;
    private WindowManager windowManager;

    public VideoPush(SurfaceHolder mSurfaceHolder, VideoInfo mVideoInfo, FFmpegLiveStreamNativePlayer nativePlayer,String path,WindowManager windowManager) {
        this.mSurfaceHolder = mSurfaceHolder;
        this.mVideoInfo = mVideoInfo;
        this.nativePlayer = nativePlayer;
        this.mSurfaceHolder.addCallback(this);
        this.windowManager = windowManager;

        this.mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        this.path = path;
    }



    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        startPreview();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {

    }

    @Override
    public void startPush() {
        //设置视频参数
        nativePlayer.setVideoOptions(mVideoInfo.getPreviewWidth(),mVideoInfo.getPreviewHeight(),
                mVideoInfo.getBitrate(), mVideoInfo.getFps());
        isPushing = true;
    }

    @Override
    public void stopPush() {
        isPushing = false;
    }

    @Override
    public void release() {
        stopPreview();
    }

    /**
     * 切换摄像头
     */
    public void switchCamera()
    {
        if(mVideoInfo.getCameraId() == Camera.CameraInfo.CAMERA_FACING_BACK)
        {
            //改为前置
            mVideoInfo.setCameraId(Camera.CameraInfo.CAMERA_FACING_FRONT);
        }else {
            mVideoInfo.setCameraId(Camera.CameraInfo.CAMERA_FACING_BACK);
        }
        //重新预览
        stopPreview();
        startPreview();
    }

    /**
     * 开始预览
     */
    void startPreview()
    {
        try {
            //这里是Camera
            //SurfaceView 初始化完成，开始相机预览
            mCamera = Camera.open(mVideoInfo.getCameraId());
            Camera.Parameters parameters = mCamera.getParameters();
            //设置相机参数
            parameters.setPreviewFormat(ImageFormat.NV21);//预览输出 YUV 格式
            //YUV预览图像的像素格式
//            parameters.setPictureSize(720, 1080);//拍照的图片尺寸，不等于预览尺寸
//            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);//打开手电筒
//            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);//持续对焦，适合拍照 / 直播预览
//            parameters.setPreviewSize(mVideoInfo.getWidth(), mVideoInfo.getHeight());//设置预览分辨率，回调拿到的 NV21 buffer 就是这个宽高

            // 获取所有支持拍照尺寸
            List<Camera.Size> pictureSizes = parameters.getSupportedPictureSizes();
            Camera.Size bestPicSize = chooseOptimalSize(pictureSizes, mVideoInfo.getPictureWidth(), mVideoInfo.getPictureHeight());
            if (bestPicSize.width != mVideoInfo.getPictureWidth())
            {
                //不是最佳拍照格式
                mVideoInfo.setPictureWidth(bestPicSize.width);
                mVideoInfo.setPictureHeight(bestPicSize.height);
            }
            Log.i("RTMP","final setPictureWidth RTMP width:" + mVideoInfo.getPictureWidth() + "  height:" +  mVideoInfo.getPictureHeight());
            parameters.setPictureSize(mVideoInfo.getPictureWidth(), mVideoInfo.getPictureHeight());//拍照的图片尺寸，不等于预览尺寸

            // 获取设备支持的所有预览尺寸列表
            List<Camera.Size> supportedSizes = parameters.getSupportedPreviewSizes();
            //写工具方法：从supportedSizes里选最接近你目标分辨率的Size，不要直接new Size
            Camera.Size bestPreviewSize = chooseOptimalSize(supportedSizes,  mVideoInfo.getPreviewWidth(), mVideoInfo.getPreviewHeight());
            if(bestPreviewSize.width != mVideoInfo.getPreviewWidth())
            {
                //不是最佳预览格式
                mVideoInfo.setPreviewWidth(bestPreviewSize.width);
                mVideoInfo.setPreviewHeight(bestPreviewSize.height);
                //和前面设置的不一致重新设置编码器参数
                nativePlayer.setVideoOptions(mVideoInfo.getPreviewWidth(),mVideoInfo.getPreviewHeight(),
                        mVideoInfo.getBitrate(), mVideoInfo.getFps());
            }
            Log.i("RTMP","final setPreviewSize RTMP width:" + mVideoInfo.getPreviewWidth() + "  height:" +  mVideoInfo.getPreviewHeight());
            parameters.setPreviewSize(mVideoInfo.getPreviewWidth(), mVideoInfo.getPreviewHeight());

            // 对焦模式安全设置
            List<String> focusModes = parameters.getSupportedFocusModes();
            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            } else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            } else {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_FIXED);
            }

            /** 预览 **/
            //预览画面宽高值
//            setDisplay(parameters, mCamera);//设置相机旋转角度
            setDisplay(mCamera); // 传入camera实例即可
            mCamera.setParameters(parameters);
            mCamera.setPreviewDisplay(mSurfaceHolder);//把预览输出绑定到 SurfaceView 的 SurfaceHolder
            //获取预览图像数据
//            //NV21 真实最小字节数：w * h * 3 / 2，*4是扩大了容量
//            buffers = new byte[mVideoInfo.getWidth() * mVideoInfo.getHeight() * 4];
//            mCamera.addCallbackBuffer(buffers);
//            mCamera.setPreviewCallbackWithBuffer(this);
//            mCamera.startPreview();//开始录制

//            int realW = bestPreviewSize.width;
//            int realH = bestPreviewSize.height;
//            int bufferByteCount = realW * realH * 3 / 2;
            int bufferByteCount = mVideoInfo.getPreviewWidth() * mVideoInfo.getPreviewHeight() * 3 / 2;
            buffers = new byte[bufferByteCount];

            mCamera.addCallbackBuffer(buffers);
            mCamera.setPreviewCallbackWithBuffer(this);
            mCamera.startPreview();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void setDisplay(Camera camera) {
        // 获取当前摄像头的信息
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        // 假设您使用的是后置摄像头，如果是前置，请传入对应的 cameraId
        // 通常后置是0，前置是1，但最好通过遍历或传入参数来确定
        android.hardware.Camera.getCameraInfo(0, info);

        // 获取当前屏幕的旋转角度
        int rotation = windowManager.getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            // 前置摄像头：先加上屏幕角度，再取反以补偿镜像效果
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {
            // 后置摄像头：传感器角度减去屏幕角度
            result = (info.orientation - degrees + 360) % 360;
        }

        // 直接调用公开API设置预览方向
        camera.setDisplayOrientation(result);
    }

    //设置相机旋转角度
    private void setDisplay(Camera.Parameters parameters, Camera camera)
    {
        if(Integer.parseInt(Build.VERSION.SDK) >= 8)
        {
            setDisplayOrientation(camera, 90);
        }else {
            parameters.setRotation(90);
        }
    }

    private void setDisplayOrientation(Camera camera, int i)
    {
        Method downPolymorphic;
        try {
            downPolymorphic = camera.getClass().getMethod("setDisplayOrientation",
                    new Class[]{int.class});
            if(downPolymorphic != null)
            {
                downPolymorphic.invoke(camera, new Object[]{i});
            }

        } catch (Exception ignored) {
        }
    }

    /**
     * 停止预览
     */
    void stopPreview()
    {
        if(mCamera != null)
        {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        //相机读取到数据
        if (data == null) {
            if (mCamera != null) {
                mCamera.addCallbackBuffer(data);
            }
            return;
        }

        //复制一份，隔离相机复用buffer
        byte[] nv21Copy = Arrays.copyOf(data, data.length);

        //归还buffer给相机，让相机继续采集下一帧
        if(mCamera != null) {
            mCamera.addCallbackBuffer(data);
        }

        if(isPushing)
        {
            //在回调函数中获取图像数据，然后给 Native 代码编码
            nativePlayer.sendVideoPacket(nv21Copy, mVideoInfo.getFps());
        }
    }

    /**
     * 从支持列表选出最接近目标宽高的预览Size
     */
    private Camera.Size chooseOptimalSize(List<Camera.Size> sizes, int targetW, int targetH) {
        double targetRatio = (double) targetW / targetH;
        Camera.Size bestSize = sizes.get(0);
        double minDiff = Double.MAX_VALUE;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            Log.i("RTMP","RTMP width:" + size.width + "  height:" + size.height);
            double diff = Math.abs(ratio - targetRatio);
            if (diff < minDiff) {
                minDiff = diff;
                bestSize = size;
            }
        }
        return bestSize;
    }
}
