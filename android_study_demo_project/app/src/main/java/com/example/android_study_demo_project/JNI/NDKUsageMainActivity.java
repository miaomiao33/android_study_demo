package com.example.android_study_demo_project.JNI;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android_study_demo_project.R;

import java.nio.Buffer;
import java.nio.ByteBuffer;

public class NDKUsageMainActivity extends AppCompatActivity {
    String TAG = this.getClass().getSimpleName();
    private Button getJNIStringButton;
    private Button changNameAndAgeButton;
    private Button cCallJavaMethodButton;
    private Button cFileWriteTimeButton;
    private Button cFolderTraversalButton;
    private Button cModelToJavaButton;
    private Button javaModelToCButton;
    private Button javaPassBitmapToCButton;
    private Button cCreateBitmapButton;
    private TextView changeNameAndAgeTV;
    private final NDKUsageClick click = new NDKUsageClick();
    //JNI：Java调用C
    JNIJavaCallC jniJavaCallC = new JNIJavaCallC();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jni_main);

        getJNIStringButton = (Button) findViewById(R.id.bt_get_JNI_string);
        changNameAndAgeButton = (Button) findViewById(R.id.bt_change_name_and_age);
        cCallJavaMethodButton = (Button) findViewById(R.id.bt_c_call_java_method);
        cFileWriteTimeButton = (Button) findViewById(R.id.bt_jni_file_write_time);
        cFolderTraversalButton = (Button) findViewById(R.id.bt_c_folder_traversal);
        cModelToJavaButton = (Button) findViewById(R.id.bt_c_to_java);
        javaModelToCButton = (Button) findViewById(R.id.bt_java_to_c);
        javaPassBitmapToCButton = (Button) findViewById(R.id.bt_java_pass_bitmap_to_c);
        cCreateBitmapButton = (Button) findViewById(R.id.bt_c_create_bitmap);

        getJNIStringButton.setOnClickListener(click);
        changNameAndAgeButton.setOnClickListener(click);
        cCallJavaMethodButton.setOnClickListener(click);
        cFileWriteTimeButton.setOnClickListener(click);
        cFolderTraversalButton.setOnClickListener(click);
        cModelToJavaButton.setOnClickListener(click);
        javaModelToCButton.setOnClickListener(click);
        javaPassBitmapToCButton.setOnClickListener(click);
        cCreateBitmapButton.setOnClickListener(click);

        changeNameAndAgeTV = (TextView) findViewById(R.id.tv_change_name_and_age);
        changeNameAndAgeTV.setText(jniJavaCallC.name + jniJavaCallC.age);
    }

    /***
     * 获取来自C++的string
     */
    public void getJNIString()
    {
        String JNIString = jniJavaCallC.stringFromJNI();
        TextView textView = (TextView) findViewById(R.id.tv_NDK_text);
        textView.setText(JNIString == null ? "null" : JNIString);
    }

    /***
     * C++修改Java的变量值
     */
    public void changeNameAndAge()
    {
        jniJavaCallC.changeName();
        jniJavaCallC.changeAge();
        changeNameAndAgeTV.setText(jniJavaCallC.name + jniJavaCallC.age);
    }

    /***
     * C++调用Java的函数
     */
    public void cCallJavaMethod()
    {
        jniJavaCallC.cCallJavaMethod();
        TextView textView = (TextView) findViewById(R.id.tv_c_call_java_method);
        textView.setText(jniJavaCallC.resultStr == null ? "null" : jniJavaCallC.resultStr);
    }

    /***
     * C++文件写入耗时
     */
    public void cFileWriteTime()
    {
        String data = "这是写入的信息";
        String path = getExternalFilesDir(Environment.DIRECTORY_MUSIC).getAbsolutePath() + "test2.txt";
        jniJavaCallC.cFileWriteTime(path,data,data.length());
    }

    /***
     * C++文件夹遍历（只显示文件）
     */
    public void cFolderTraversal()
    {
        String path = getExternalFilesDir(null).getAbsolutePath();
        jniJavaCallC.cShowDir(path);
    }

    /***
     * Java 传递一个 Bitmap 给 JNI
     */
    public void javaPassBitmapToC()
    {
        Bitmap bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(0xff336699); // AARRGGBB
        //字节数组（用于保存所有bitmap字节）,固定4个字节的ARGB8888
        byte[] bytes = new byte[bitmap.getWidth() * bitmap.getHeight() * 4];
        //ByteBuffer.wrap:将一个字节数组包装到 ByteBuffer 中,
        // 创建的 ByteBuffer 与原始数组共享同一数据,对 ByteBuffer 的修改会反映到数组中
        Buffer dst = ByteBuffer.wrap(bytes);
        //调用copyPixelsToBuffer复制所有像素到数组中，注意就可以
        bitmap.copyPixelsToBuffer(dst);
        // ARGB_8888 真实的存储顺序是 R-G-B-A
        Log.d(TAG, "R: " + Integer.toHexString(bytes[0] & 0xff));
        Log.d(TAG, "G: " + Integer.toHexString(bytes[1] & 0xff));
        Log.d(TAG, "B: " + Integer.toHexString(bytes[2] & 0xff));
        Log.d(TAG, "A: " + Integer.toHexString(bytes[3] & 0xff));
        jniJavaCallC.passBitmap(bitmap);
    }

    /***
     * C++ 创建一个 Bitmap
     */
    public void cCreateBitmap()
    {
        Bitmap bitmap = jniJavaCallC.createBitmap(2,2);
        if(bitmap == null)
        {
            Log.d(TAG,"bitmap is null");
        }else
        {

            Log.d(TAG, "width: "+bitmap.getWidth()+", height: "+bitmap.getHeight());
        }
    }


    /***
     * C++的model转Java
     */
    public void cModelToJava()
    {

        DataModel model = jniJavaCallC.getDataFromNative();
        TextView textView = (TextView) findViewById(R.id.tv_c_struct_usage);
        textView.setText(model == null ?"model is null":model.inner.message);
    }

    /***
     * Java的model转C++
     */
    public void javaModelToC()
    {
        DataModel model = new DataModel();
        model.rect = new Rect(0, 0, 640, 480);
        model.points = new PointF[]{
                new PointF(0.0F, 1.0F),
                new PointF(1.0F, 2.0F),
                new PointF(2.0F, 3.0F),
                new PointF(3.0F, 4.0F)};
        model.points[0] = new PointF(0.0F, 1.0F);
        model.inner = new DataModel.Inner();
        model.inner.message = "data from Java";
        model.id = 0;
        model.score = 1.0f;
        model.data = new byte[]{0, 1, 2, 3};
        model.doubleDimenArray = new int[][]{
                {0, 1},
                {2, 3}
        };

        jniJavaCallC.transferDataToNative(model);
    }

    private class NDKUsageClick implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId())
            {
                case R.id.bt_get_JNI_string:
                    getJNIString();
                    break;
                case R.id.bt_change_name_and_age:
                    changeNameAndAge();
                    break;
                case R.id.bt_c_call_java_method:
                    cCallJavaMethod();
                    break;
                case R.id.bt_jni_file_write_time:
                    cFileWriteTime();
                    break;
                case R.id.bt_c_folder_traversal:
                    cFolderTraversal();
                    break;
                case R.id.bt_java_pass_bitmap_to_c:
                    javaPassBitmapToC();
                    break;
                case R.id.bt_c_create_bitmap:
                    cCreateBitmap();
                    break;
                case R.id.bt_c_to_java:
                    cModelToJava();
                    break;
                case R.id.bt_java_to_c:
                    javaModelToC();
                    break;
                default:
            }
        }
    }
}