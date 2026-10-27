package com.example.android_study_demo_project.opencv.ffmpegUsage.livePush.livePushMain;

import android.content.Context;

import com.example.android_study_demo_project.R;

import javax.microedition.khronos.egl.EGLContext;

public class GrayVideoPush extends BaseVideoPush {
    private VideoPushRender mVideoPushRender;

    public GrayVideoPush(Context context, EGLContext eglContext, int textureId) {
        //和 cameraView 共用一个 eglContext 来进行渲染
        super(context, eglContext);

        mVideoPushRender = new VideoPushRender(context,textureId);
        mVideoPushRender.setFragmentRender(R.raw.fragment_shader_gray);

        //(每隔16ms)会不断调用 RecorderRender 的 onDrawFrame，然后 swapBuffers ，不断 swapBuffers 到 surface 上
        //和cameraView共用一个 FBO 的纹理Id
        setRenderer(mVideoPushRender);
    }
}
