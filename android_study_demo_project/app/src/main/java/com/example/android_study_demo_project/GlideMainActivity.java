package com.example.android_study_demo_project;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class GlideMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glide_main);

        ImageView iv = findViewById(R.id.iv_glide);
        String url = "https://tse2-mm.cn.bing.net/th/id/OIP-C.ChsdSBqdo4A8njZejqg2hwHaEK?w=289&h=180&c=7&r=0&o=5&dpr=1.25&pid=1.7";
        //图片生成和销毁跟着GlideMainActivity生命周期
        Glide.with(this).load(url).into(iv);
    }
}
