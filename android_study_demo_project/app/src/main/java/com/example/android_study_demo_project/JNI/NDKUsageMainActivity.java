package com.example.android_study_demo_project.JNI;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android_study_demo_project.R;

public class NDKUsageMainActivity extends AppCompatActivity {
    private Button getJNIStringButton;
    private Button changNameAndAgeButton;
    private Button cCallJavaMethodButton;
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

        getJNIStringButton.setOnClickListener(click);
        changNameAndAgeButton.setOnClickListener(click);
        cCallJavaMethodButton.setOnClickListener(click);

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
                default:
            }
        }
    }
}