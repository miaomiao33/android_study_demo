package com.example.android_study_demo_project.JNI;

/***
 * Java调用 C/C++
 */
public class JNIJavaCallC {
    //Used to load the 'native-lib' library on application startup
    static {
        System.loadLibrary("androidStudyNDKLib");
    }

    /**
     * A native method that is implemented by the 'androidStudyNDKLib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();//获取来自C++的string
}
