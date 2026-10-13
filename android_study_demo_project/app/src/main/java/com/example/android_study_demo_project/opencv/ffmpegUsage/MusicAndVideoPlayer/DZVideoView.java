package com.example.android_study_demo_project.opencv.ffmpegUsage.MusicAndVideoPlayer;

import android.content.Context;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.android_study_demo_project.opencv.ffmpegUsage.MusicPlayer.MediaPreparedListener;

public class DZVideoView extends SurfaceView implements MediaPreparedListener {
    private DarrenPlayer4 mPlayer;

    public DZVideoView(Context context) {
        this(context,null);
    }

    public DZVideoView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public DZVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //设置像素显示格式
        SurfaceHolder holder = getHolder();
        holder.setFormat(PixelFormat.RGBA_8888);

        mPlayer = new DarrenPlayer4();
        mPlayer.setOnPreparedListener(this);
    }

    public void play(String uri)
    {
        stop();
        mPlayer.setDataSource(uri);
        mPlayer.prepareAsync();//准备好了会回调下面的onPrepared
    }

    /***
     * 停止方法释放上一个视频内存
     */
    public void stop(){

    }

    @Override
    public void onPrepared() {
        mPlayer.play();
    }
}
