package com.example.android_study_demo_project.frameLayoutUsage;

import static androidx.core.content.ContextCompat.getSystemService;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.android_study_demo_project.R;

import java.util.Objects;

/***
 * 自定义跟踪视图主页面
 */
public class FrameLayoutMainActivity extends AppCompatActivity {
    //鼠标视图主页
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_framelayout_usage_main);
        FrameLayout mainFrameLayout = (FrameLayout) findViewById(R.id.myLayout);

        // 获取 WindowManager
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        // 获取 DisplayMetrics 对象
        DisplayMetrics displayMetrics = new DisplayMetrics();
        // 获取当前 window 的宽高信息
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        final MouseTrackingView mouseTrackingView = new MouseTrackingView(this,
                displayMetrics.widthPixels,displayMetrics.heightPixels - 70);

        mainFrameLayout.addView(mouseTrackingView);
    }
}