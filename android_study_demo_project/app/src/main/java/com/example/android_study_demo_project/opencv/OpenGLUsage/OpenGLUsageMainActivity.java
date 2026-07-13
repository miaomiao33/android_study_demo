package com.example.android_study_demo_project.opencv.OpenGLUsage;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;
import com.example.android_study_demo_project.R;

/***
 * OpenGL ES 显示人脸图片以及眼睛部分放大，并使用OpenCV识别眼睛中心
 */
public class OpenGLUsageMainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {
    private MyGLView myGLView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_glusage_main);

        myGLView = findViewById(R.id.mgv_MyGLView);
        SeekBar seekBar = findViewById(R.id.sb_set_eyes_scale);
        seekBar.setOnSeekBarChangeListener(this);

        myGLView.setScale(((float) seekBar.getProgress())/seekBar.getMax());
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.small_eyes);
        myGLView.setBitmap(bitmap);
    }

    @Override
    protected void onDestroy() {
        myGLView.release();
        super.onDestroy();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        myGLView.setScale(((float) progress)/seekBar.getMax());
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}