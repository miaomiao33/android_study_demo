package com.example.android_study_demo_project.JNI;

import android.graphics.Bitmap;

/***
 * Java调用 C/C++
 */
public class JNIJavaCallC {
    //Used to load the 'native-lib' library on application startup
    static {
        System.loadLibrary("androidStudyNDKLib");
    }
    public String resultStr;//Java收到C++的参数形成的string

    //C++进行修改的属性
    public String name = "oldName";
    public static int age = 20;
    /**
     * A native method that is implemented by the 'androidStudyNDKLib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();//获取来自C++的string

    //修改NDKUsageMainActivity的name和age属性
    public native void changeName();

    public native static void changeAge();

    public native void cCallJavaMethod();

    public String javaMethod(String str,int num2)
    {
        resultStr = "parameters of java method from C++:"+ str + num2;
        return resultStr;
    }
    //用于验证类型描述符作用
    public String javaMethod(int num1)
    {
        return "parameters of java method from C++:" + num1;

    }

    /***
     *使用TimeUtils.h计算C文件写入时间
     * @param path 文件路径
     * @param data 写入的字符串
     * @param len 长度
     * @return
     */
    public native int cFileWriteTime(String path,String data,int len);

    /***
     * C++文件夹遍历（只显示文件）
     * @param dirPath
     */
    public native void cShowDir(String dirPath);

    /***
     * Java 传递一个 Bitmap 给 JNI
     * @param bitmap
     */
    public native void passBitmap(Bitmap bitmap);

    /***
     * 将C结构体转为Java类(C++中会初始化数据)
     * @return
     */
    public static native DataModel getDataFromNative();
    /***
     * 将Java类转为C结构体
     * @return
     */
    public static native void transferDataToNative(DataModel dataModel);
}
