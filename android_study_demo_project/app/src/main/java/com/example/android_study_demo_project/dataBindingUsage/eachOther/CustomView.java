package com.example.android_study_demo_project.dataBindingUsage.eachOther;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

//用于自定义双向绑定的自定义view
public class CustomView extends View {
    String data;
    public CustomView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        data = "";
    }
}
