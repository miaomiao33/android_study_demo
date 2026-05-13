package com.example.android_study_demo_project.JNI;

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
}
