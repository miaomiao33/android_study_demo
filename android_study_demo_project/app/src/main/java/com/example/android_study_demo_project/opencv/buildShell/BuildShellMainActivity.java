package com.example.android_study_demo_project.opencv.buildShell;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android_study_demo_project.R;

/***
 * 库文件编译测试
 */
public class BuildShellMainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("x264Lib");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_build_shell_main);
        getString();
    }

    private native String getString();
}