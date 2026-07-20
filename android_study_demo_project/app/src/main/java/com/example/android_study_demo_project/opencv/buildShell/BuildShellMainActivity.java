package com.example.android_study_demo_project.opencv.buildShell;

import android.os.Bundle;
import android.os.Environment;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android_study_demo_project.R;

import java.util.Objects;

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
        String path = Objects.requireNonNull(getExternalFilesDir(Environment.DIRECTORY_MUSIC)).getAbsolutePath()
                +"/2.dwg";
        testLibreDWG(path);
    }

    private native String getString();

    //检验LibreDWG包
    private native String testLibreDWG(String path);
}