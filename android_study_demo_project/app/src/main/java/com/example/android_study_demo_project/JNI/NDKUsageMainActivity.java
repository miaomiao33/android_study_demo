package com.example.android_study_demo_project.JNI;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android_study_demo_project.R;

public class NDKUsageMainActivity extends AppCompatActivity {
    private Button getJNIStringButton;
    private final NDKUsageClick click = new NDKUsageClick();
    //JNI：Java调用C
    JNIJavaCallC jniJavaCallC = new JNIJavaCallC();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jni_main);
        getJNIStringButton = (Button) findViewById(R.id.bt_get_JNI_string);
        getJNIStringButton.setOnClickListener(click);
    }

    public void getJNIString()
    {
        String JNIString = jniJavaCallC.stringFromJNI();
        TextView textView = (TextView) findViewById(R.id.tv_NDK_text);
        textView.setText(JNIString == null ? "null" : JNIString);
    }

    private class NDKUsageClick implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId())
            {
                case R.id.bt_get_JNI_string:
                    getJNIString();
                    break;
                default:
            }
        }
    }
}