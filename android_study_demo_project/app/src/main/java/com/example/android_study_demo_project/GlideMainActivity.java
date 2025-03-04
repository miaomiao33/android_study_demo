package com.example.android_study_demo_project;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners;
import com.bumptech.glide.load.resource.bitmap.Rotate;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;

public class GlideMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glide_main);

        ImageView iv = findViewById(R.id.iv_glide);
        //图片地址
        String url = "https://tse2-mm.cn.bing.net/th/id/OIP-C.ChsdSBqdo4A8njZejqg2hwHaEK?w=289&h=180&c=7&r=0&o=5&dpr=1.25&pid=1.73";

        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.drawable.ic_brightness_5_black_24dp)//loading时显示
                .error(R.drawable.ic_launcher_background)
                .fallback(R.drawable.ic_launcher_background)//加载后图片为空显示
                .override( 100,100);//指定加载图片的大小

        DrawableCrossFadeFactory factory = new DrawableCrossFadeFactory.Builder()
                                                .setCrossFadeEnabled(true)//动画结束后占位符消失
                                                .build();

        //图片生成和销毁跟着GlideMainActivity生命周期
        Glide.with(this)
                .load(url)
                .apply(requestOptions)
                //过度
                .transition(DrawableTransitionOptions.withCrossFade(factory))
                //圆角、变换角度等
                .transform(new GranularRoundedCorners(30,30,30,30),
                        new Rotate(60))
                .into(iv);

        //除了transition，Glide更方便的使用，便于写成工具类，且解耦了
//        GlideApp.with(this).load(url).defaultImg().into(iv);

    }
}
