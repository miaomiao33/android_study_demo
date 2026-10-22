package com.example.android_study_demo_project.opencv.ffmpegUsage.livePush.CameraView;

import android.content.Context;

import com.example.android_study_demo_project.R;

import javax.microedition.khronos.egl.EGLContext;

public class GrayVideoRecorder extends BaseVideoRecorder{
    private RecorderRender mRecorderRender;
    public GrayVideoRecorder(Context context, EGLContext eglContext, int textureId) {
        //和 cameraView 共用一个 eglContext 来进行渲染
        super(context, eglContext);

        mRecorderRender = new RecorderRender(context,textureId);
        mRecorderRender.setFragmentRender(R.raw.fragment_shader_gray);

        //(每隔16ms)会不断调用 RecorderRender 的 onDrawFrame，然后 swapBuffers ，不断 swapBuffers 到 surface 上
        //和cameraView共用一个 FBO 的纹理Id
        setRenderer(mRecorderRender);
    }
}
