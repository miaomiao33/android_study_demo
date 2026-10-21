package com.example.android_study_demo_project.opencv.ffmpegUsage.livePush;

import static android.opengl.EGL14.EGL_CONTEXT_CLIENT_VERSION;
import static android.opengl.EGL14.EGL_NONE;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

/**
 * 扩展 GLSurfaceView，暴露 EGLContext
 */
public class BaseGLSurfaceView extends GLSurfaceView {
    /**
     * EGL 环境上下文
     */
    public EGLContext mEglContext;
    public BaseGLSurfaceView(Context context) {
        this(context, null);
    }

    public BaseGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //利用 setEGLContextFactory 这种扩展方式把 EGLContext 暴露出去
        setEGLContextFactory(new EGLContextFactory() {
            @Override
            public EGLContext createContext(EGL10 egl10, EGLDisplay eglDisplay, EGLConfig eglConfig) {
                int[] attrib_list = {EGL_CONTEXT_CLIENT_VERSION,2,EGL_NONE};
                mEglContext = egl10.eglCreateContext(eglDisplay,eglConfig,EGL10.EGL_NO_CONTEXT,attrib_list);
                return mEglContext;
            }

            @Override
            public void destroyContext(EGL10 egl10, EGLDisplay eglDisplay, EGLContext eglContext) {
                if(!egl10.eglDestroyContext(eglDisplay,mEglContext))
                {
                    Log.e("BaseGLSurfaceView","display:"+eglDisplay+" context:"+eglContext);
                }
            }
        });
    }

    /**
     * 通过此方法可以获取 EGL 环境上下文，可用于共享渲染同一个纹理
     * @return
     */
    public EGLContext getmEglContext()
    {
        return mEglContext;
    }
}
