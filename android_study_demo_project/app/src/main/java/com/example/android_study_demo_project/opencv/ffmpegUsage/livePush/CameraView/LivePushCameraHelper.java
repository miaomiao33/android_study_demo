package com.example.android_study_demo_project.opencv.ffmpegUsage.livePush.CameraView;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LivePushCameraHelper {
    private SurfaceTexture mSurfaceTexture;
    private Camera mCamera;
    private int mViewWidth;
    private int mViewHeight;
    private Context mContext;

    public LivePushCameraHelper(Context mContext) {
        this.mContext = mContext;
    }

    public void setViewWidth(int mViewWidth) {
        this.mViewWidth = mViewWidth;
    }

    public void setViewHeight(int mViewHeight) {
        this.mViewHeight = mViewHeight;
    }

    public void init(SurfaceTexture mSurfaceTexture) {
        this.mSurfaceTexture = mSurfaceTexture;
    }


    public void open(int cameraId)
    {
        try {
            close();
            mCamera = Camera.open(cameraId);
            //预览的 SurfaceTexture 设为了 OpenGL 中创建和绑定的 mSurfaceTexture 的纹理上（相机把画面渲染到纹理上）
            mCamera.setPreviewTexture(mSurfaceTexture);

            Camera.Parameters parameters = mCamera.getParameters();

            parameters.setFlashMode("off");
            //OES SurfaceTexture 预览不走 NV21 回调，这个参数无效，还会造成部分设备参数异常
//            parameters.setPreviewFormat(ImageFormat.NV21);

            //直接设置 View 的大小，会有一定兼容性问题，怎么拿最合适的需要根据具体场景来选择
            Camera.Size size = getOptimalSize(parameters.getSupportedPictureSizes(),mViewWidth,mViewHeight);
            parameters.setPictureSize(size.width,size.height);
            size = getOptimalSize(parameters.getSupportedPreviewSizes(),mViewWidth,mViewHeight);
            parameters.setPreviewSize(size.width,size.height);

            mCamera.setParameters(parameters);

            mCamera.startPreview();
            mCamera.autoFocus(null);

            Log.i("TAG","开始预览相机"+cameraId);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public void close()
    {
        if(mCamera != null)
        {
            mCamera.stopPreview();
            mCamera.release();
            Log.i("TAG","停止预览相机");
            mCamera = null;
        }
    }

    public void change(int cameraId)
    {
        close();
        open(cameraId);
    }


//    /**
//     * 获取最合适的尺寸
//     * @param supportedList
//     * @param width
//     * @param height
//     * @return
//     */
//    private Camera.Size getOptimalSize(List<Camera.Size> supportedList, int width, int height) {
//        //camera 的宽度是大于高度的，这里要保证 expectWidth > expectHeight
//        int expectWidth = Math.max(width,height);
//        int expectHeight = Math.min(width,height);
//
//        //根据宽度进行排序
//        Collections.sort(supportedList,(Comparator<? super Camera.Size>) (pre,after)->{
//            if(pre.width > after.width)
//            {
//                return 1;
//            }else if(pre.width < after.width)
//            {
//                return -1;
//            }
//
//            return 0;
//        });
//
//        Camera.Size result = supportedList.get(0);
//        boolean widthOrHeight = false;//判断存在宽和高相等的Size
//        //计算宽高最接近的值
//        for(Camera.Size size:supportedList)
//        {
//            //宽高相等，直接返回
//            if(size.width == expectWidth && size.height == expectHeight)
//            {
//                result = size;
//                break;
//            }
//            //宽度相等，计算接近的高度
//            else if(size.width == expectWidth)
//            {
//                widthOrHeight = true;
//                if(Math.abs(result.height - expectHeight) >
//                Math.abs(size.height - expectHeight))
//                {
//                    result = size;
//                }
//            }
//            //高度相等，计算接近的宽度
//            else if(size.height == expectHeight)
//            {
//                widthOrHeight = true;
//                if(Math.abs(result.width - expectWidth) >
//                        Math.abs(size.width - expectWidth))
//                {
//                    result = size;
//                }
//            }
//            //之前的查找都不存在宽和高相等的情况，计算宽和高都最接近的值
//            else if(!widthOrHeight)
//            {
//                if(Math.abs(result.width - expectWidth) >
//                        Math.abs(size.width - expectWidth)
//                        && Math.abs(result.height - expectHeight) >
//                        Math.abs(size.height - expectHeight))
//                {
//                    result = size;
//                }
//            }
//
//        }
//
//        return result;
//    }

    private Camera.Size getOptimalSize(List<Camera.Size> supportedList, int viewW, int viewH) {
        float targetRatio = (float) viewW / viewH;
        Camera.Size bestSize = null;
        float minDiffRatio = Float.MAX_VALUE;
        int minArea = viewW * viewH;

        for (Camera.Size size : supportedList) {
            float ratio = (float) size.width / size.height;
            float diff = Math.abs(ratio - targetRatio);
            int area = size.width * size.height;
            // 优先比例接近，面积 >= 目标面积
            if (diff < minDiffRatio && area >= minArea) {
                minDiffRatio = diff;
                bestSize = size;
            }
        }
        // 兜底：找不到满足面积的，选比例最接近的
        if (bestSize == null) {
            minDiffRatio = Float.MAX_VALUE;
            for (Camera.Size size : supportedList) {
                float ratio = (float) size.width / size.height;
                float diff = Math.abs(ratio - targetRatio);
                if (diff < minDiffRatio) {
                    minDiffRatio = diff;
                    bestSize = size;
                }
            }
        }
        if (bestSize == null) {
            bestSize = supportedList.get(0);
        }
        Log.i("CameraHelper", "select preview size: " + bestSize.width + "x" + bestSize.height);
        return bestSize;
    }


    /**
     * 聚焦
     * @param focusPoint 焦点位置
     * @param callback 对焦成功或者失败的回调
     * @return
     */
    public boolean newCameraFocus(Point focusPoint, Camera.AutoFocusCallback callback)
    {
        if(mCamera == null)
        {
            throw new RuntimeException("mCamera is null");
        }
        Point cameraFocusPoint = convertToCameraPoint(focusPoint);
        Rect cameraFocusRec = convertToCameraRect(cameraFocusPoint,100);
        Camera.Parameters parameters = mCamera.getParameters();
        if(parameters.getMaxNumFocusAreas() <= 0)
        {
            return focus(callback);
        }
        clearCameraFocus();
        List<Camera.Area> focusArea = new ArrayList<>();
        //100是权重
        focusArea.add(new Camera.Area(cameraFocusRec,100));
        parameters.setFocusAreas(focusArea);
        //设置感光区域
        parameters.setMeteringAreas(focusArea);
        try {
            mCamera.setParameters(parameters);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return focus(callback);
    }

    /**
     * 将屏幕坐标转换为camera坐标
     * @param focusPoint
     * @return
     */
    private Point convertToCameraPoint(Point focusPoint) {
        int newX = focusPoint.y * 2000 / mViewHeight - 1000;
        int newY = -(focusPoint.x * 2000 / mViewWidth - 1000);
        return new Point(newX,newY);
    }

    private Rect convertToCameraRect(Point centerPoint, int radius) {
        int left = limit(centerPoint.x - radius, 1000,-1000);
        int right = limit(centerPoint.x + radius, 1000,-1000);
        int top = limit(centerPoint.y - radius, 1000,-1000);
        int bottom = limit(centerPoint.y + radius, 1000,-1000);
        return new Rect(left,top,right,bottom);
    }

    private static int limit(int s, int max, int min) {
        if(s > max)
        {
            return max;
        }
        if(s < min)
        {
            return min;
        }
        return s;
    }

    private boolean focus(Camera.AutoFocusCallback callback)
    {
        mCamera.cancelAutoFocus();
        mCamera.autoFocus(callback);
        return true;
    }

    public void clearCameraFocus()
    {
        if(mCamera == null)
        {
            throw new RuntimeException("mCamera is null");
        }
        mCamera.cancelAutoFocus();
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setFocusAreas(null);
        parameters.setMeteringAreas(null);
        try {
            mCamera.setParameters(parameters);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

}
