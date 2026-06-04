package com.example.android_study_demo_project.opencv;

import android.graphics.Bitmap;

public class OpenCVJNIJavaCallC {
    //Used to load the 'native-lib' library on application startup
    static {
        System.loadLibrary("androidStudyNDKLib");
    }

    /**
     * 获取到OpenCV的信息
     * @return
     */
    public native String getOpenCVInfoFromJNI();//获取来自C++的string

    /***
     * 获取到传入身份证的图片的身份证号码截图
     * @param src
     * @param config
     * @return
     */

    public static native Bitmap getIDCardImage(Bitmap src, Bitmap.Config config);
}
