package com.example.android_study_demo_project.opencv.OpenGLUsage;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;

/***
 * 获取眼睛中心点坐标
 */
public class EyeDectorManager {
    private static volatile EyeDectorManager instance;
//    private Context mContext;
    private WeakReference<Context> mContextWeakReference;//防止Context泄露
    private static long nativePtr; // native端检测器句柄地址
    private final String TAG = EyeDectorManager.class.getSimpleName();

    static {
        System.loadLibrary("eyeLib");
    }

    private EyeDectorManager() {}

    public static EyeDectorManager getInstance() {
        if(instance == null)
        {
            synchronized (EyeDectorManager.class){
                if(instance == null)
                {
                    instance = new EyeDectorManager();
                }
            }
        }
        return instance;
    }

    public void init(Context context) {
        mContextWeakReference = new WeakReference<>(context);
        nativePtr = nativeCreateDetector();
    }


    //获取左右眼睛的中心坐标（像素化坐标）
    public Eye[] getEyes(Bitmap mBitmap) {
        //初始化级联检测器（人脸+眼睛）
        initCascade();
        int texWidth = mBitmap.getWidth();
        int texHeight = mBitmap.getHeight();
        //获取到眼睛中心坐标（像素坐标）
        //float[4] {leftX,leftY,rightX,rightY} 无检测返回null
        float[] result = detectPupil(mBitmap,texWidth,texHeight);
        if(result == null)
        {
            Log.e(TAG,"get eye error,result is null");
            return null;
        }

        // 1. 像素坐标转 0~1 纹理UV（和aCoord坐标系匹配）
        Log.e(TAG,"_eyes: leftXY:"+result[0]+"-"+result[1]+"rightXY:"+result[2]+"-"+ result[3]);
//        //归一化坐标
//        float leftX = result[0] / texWidth;
//        float leftY = result[1] / texHeight;
//        float rightX = result[2] / texWidth;
//        float rightY = result[3] / texWidth;

        //像素坐标
        float leftX = result[0] ;
        float leftY = result[1] ;
        float rightX = result[2] ;
        float rightY = result[3] ;

        Eye leftEye = new Eye();
        Eye rightEye = new Eye();
        leftEye.pos = new float[]{leftX,leftY};
        rightEye.pos = new float[]{rightX,rightY};

        return new Eye[]{rightEye,leftEye};
    }

    // 初始化级联检测器（人脸+眼睛）
    public void initCascade() {
        String facePath = copyAsset("haarcascade_frontalface_default.xml");
        String eyePath = copyAsset("haarcascade_eye_tree_eyeglasses.xml");
        nativeLoadCascade(nativePtr, facePath, eyePath);
    }

    /**
     * 处理BGR帧，输出双眼瞳孔坐标
     * @param bgrData 图像字节数组
     * @param w 宽
     * @param h 高
     * @return float[4] {leftX,leftY,rightX,rightY} 无检测返回null
     */
    public float[] detectPupil(Bitmap bgrData, int w, int h) {
        return nativeDetectFrame(nativePtr, bgrData, w, h);
    }

    // 释放native资源
    public void release() {
        nativeRelease(nativePtr);
        nativePtr = 0;
    }

    // 拷贝assets分类器到手机本地
    private String copyAsset(String name) {
        Context mContext = mContextWeakReference.get();
        if(mContext == null)
        {
            Log.e(TAG,"Context 已被回收");
            return null;
        }
        File dir = new File(mContext.getCacheDir(), "cascade");

        if (!dir.exists() && dir.getParentFile() != null){
            dir.getParentFile().mkdirs();
        }

        File dst = new File(dir, name);
        try {
            InputStream is = mContext.getAssets().open("tessdata/"+name);
            FileOutputStream os = new FileOutputStream(dst);
            byte[] buf = new byte[2048];
            int len;
            while ((len = is.read(buf)) != -1)
                os.write(buf, 0, len);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return dst.getAbsolutePath();
    }

    // native方法声明
    private native long nativeCreateDetector();
    private native void nativeLoadCascade(long ptr, String faceXml, String eyeXml);
    private native float[] nativeDetectFrame(long ptr, Bitmap src, int w, int h);
    private native void nativeRelease(long ptr);
    
}
