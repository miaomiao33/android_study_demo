package com.example.android_study_demo_project;

import com.bumptech.glide.annotation.GlideExtension;
import com.bumptech.glide.annotation.GlideOption;
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners;
import com.bumptech.glide.load.resource.bitmap.Rotate;
import com.bumptech.glide.request.BaseRequestOptions;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;

@GlideExtension
public class MyAppExtendsion  {
    private  MyAppExtendsion(){

    }
    @GlideOption
    public  static BaseRequestOptions<?> defaultImg(BaseRequestOptions<?> options){
        DrawableCrossFadeFactory factory =
                new DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build();

        return options.placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_brightness_5_black_24dp)
                .fallback(R.drawable.ic_brightness_high_black_24dp)
                .override( 100,100)
                .transform(new GranularRoundedCorners(30,30,30,30),
                        new Rotate(90));
    }
}
