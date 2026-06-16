package com.example.android_study_demo_project.opencv.liveStreaming;


import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;

import java.io.IOException;
import java.util.List;

public class CameraHelper implements Camera.PreviewCallback {
    private static final String TAG = "CameraHelper";
    private int width;
    private int height;
    private int cameraId;
    private Camera mCamera;
    private byte[] buffer;
    private byte[] i420;
    private Camera.PreviewCallback mPreviewCallback;//将转码好的I420回调出去
    private SurfaceTexture mSurfaceTexture;

    public CameraHelper(int width,int height)
    {
        this.width = width;
        this.height = height;
        cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
    }

    public void startPreview(SurfaceTexture surfaceTexture)
    {
        try {
            stopPreview();
            mSurfaceTexture = surfaceTexture;
            //获得Camera对象
            mCamera = Camera.open(cameraId);
            //配置Camera属性
            Camera.Parameters parameters = mCamera.getParameters();
            //设置预览数据格式为nv21
            parameters.setPreviewFormat(getCameraSupportedFormat());
            boolean isSupportSize = false;
            List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
            for (Camera.Size supportedPreviewSize: supportedPreviewSizes) {
                if(supportedPreviewSize.width == width &&
                        supportedPreviewSize.height == height)
                {
                    isSupportSize = true;
                    break;
                }
            }
            if(!isSupportSize)
            {
                Camera.Size size = supportedPreviewSizes.get(0);
                width =size.width;
                height = size.height;
            }
            //摄像头宽高
            parameters.setPreviewSize(width,height);
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            //设置摄像头、图像传感器的角度、方向
            mCamera.setDisplayOrientation(90);
            mCamera.setParameters(parameters);
            //数据缓存区
            buffer = new byte[width * height * 3 / 2];
            i420 = new byte[width * height * 3 / 2];
            mCamera.addCallbackBuffer(buffer);
            mCamera.setPreviewCallbackWithBuffer(this);
            //设置预览画面
            mCamera.setPreviewTexture(surfaceTexture);
            mCamera.startPreview();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    public void stopPreview()
    {
        if(mCamera != null)
        {
            //停止预览
            mCamera.stopPreview();
            //释放摄像头
            mCamera.release();
            mCamera = null;
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public byte[] getI420() {
        return i420;
    }

    public void setPreviewCallback(Camera.PreviewCallback previewCallback)
    {
        mPreviewCallback = previewCallback;
    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        //这里返回的是nv21的byte，回调出去由外面根据支持的颜色
        if(mPreviewCallback != null)
        {
//            nv21ToI420(bytes);
            mPreviewCallback.onPreviewFrame(bytes,camera);
        }
        camera.addCallbackBuffer(buffer);
    }

    //手机支持的预览格式
    private int getCameraSupportedFormat()
    {
        List<Integer> previewFormats = mCamera.getParameters().getSupportedPreviewFormats();
        for (int format:previewFormats) {
            Log.i(TAG,"CameraSupportedFormat:"+format);
            if(format == ImageFormat.NV21)
            {
                Log.i(TAG,"CameraSupportedFormat:17 - NV21");
                return format;
            }
        }
        Log.i(TAG,"CameraSupportedFormat:该设备不支持NV21");
        return -1;
    }

    //NV21转I420
    public byte[] nv21ToI420(byte[] data)
    {
        //NV21数据和I420的前面Y数据相同（Y长度width * height）
        System.arraycopy(data,0,i420,0,width * height);
        int index = width * height;
        //U数据（长度width/2 * height/2）
        for (int i = width * height; i < data.length;i += 2)
        {
            i420[index++] = data[i + 1];
        }
        //V数据（长度width/2 * height/2）
        for (int i = width * height; i < data.length;i += 2)
        {
            i420[index++] = data[i];
        }
        return i420;
    }

    //NV21转NV12
    public byte[] nv21ToNv12(byte[] nv21) {
        if (nv21 == null) return null;
        int size = nv21.length;
        byte[] nv12 = new byte[size];
        //Y长度
        int yLen = (int) (size * 2.0 / 3.0);
        //复制Y
        System.arraycopy(nv21, 0, nv12, 0, yLen);
        int i = yLen;
        //交换UV
        while (i < size - 1) {
            nv12[i + 1] = nv21[i];
            nv12[i] = nv21[i + 1];
            i += 2;
        }
        return nv12;
    }

}
