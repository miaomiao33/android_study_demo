package com.example.android_study_demo_project.opencv;

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
}
