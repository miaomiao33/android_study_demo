package com.example.android_study_demo_project.opencv.ffmpegUsage.livingStream;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;

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

    public VideoPush(SurfaceHolder mSurfaceHolder, VideoInfo mVideoInfo, FFmpegLiveStreamNativePlayer nativePlayer,String path) {
        this.mSurfaceHolder = mSurfaceHolder;
        this.mVideoInfo = mVideoInfo;
        this.nativePlayer = nativePlayer;
        this.mSurfaceHolder.addCallback(this);

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
        nativePlayer.setVideoOptions(mVideoInfo.getWidth(),mVideoInfo.getHeight(),
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
            Camera.Size bestPicSize = chooseOptimalSize(pictureSizes, 720,1080);
            parameters.setPictureSize(bestPicSize.width, bestPicSize.height);//拍照的图片尺寸，不等于预览尺寸

            // 获取设备支持的所有预览尺寸列表
            List<Camera.Size> supportedSizes = parameters.getSupportedPreviewSizes();
            // 写工具方法：从supportedSizes里选最接近你目标分辨率的Size，不要直接new Size
            Camera.Size bestPreviewSize = chooseOptimalSize(supportedSizes, 720, 1080);
            parameters.setPreviewSize(bestPreviewSize.width, bestPreviewSize.height);

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
            setDisplay(parameters, mCamera);//设置相机旋转角度
            mCamera.setParameters(parameters);
            mCamera.setPreviewDisplay(mSurfaceHolder);//把预览输出绑定到 SurfaceView 的 SurfaceHolder
            //获取预览图像数据
//            //NV21 真实最小字节数：w * h * 3 / 2，*4是扩大了容量
//            buffers = new byte[mVideoInfo.getWidth() * mVideoInfo.getHeight() * 4];
//            mCamera.addCallbackBuffer(buffers);
//            mCamera.setPreviewCallbackWithBuffer(this);
//            mCamera.startPreview();//开始录制

            int realW = bestPreviewSize.width;
            int realH = bestPreviewSize.height;
            int bufferByteCount = realW * realH * 3 / 2;
            buffers = new byte[bufferByteCount];

            mCamera.addCallbackBuffer(buffers);
            mCamera.setPreviewCallbackWithBuffer(this);
            mCamera.startPreview();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
            double diff = Math.abs(ratio - targetRatio);
            if (diff < minDiff) {
                minDiff = diff;
                bestSize = size;
            }
        }
        return bestSize;
    }
}
