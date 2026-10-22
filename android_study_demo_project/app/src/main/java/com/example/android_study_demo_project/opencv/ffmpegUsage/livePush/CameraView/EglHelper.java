package com.example.android_study_demo_project.opencv.ffmpegUsage.livePush.CameraView;

import android.util.Log;
import android.view.Surface;
import android.graphics.SurfaceTexture;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

public class EglHelper {
    private static final String TAG = "EglHelper10";

    private EGL10 mEgl;
    private EGLDisplay mEglDisplay;
    private EGLConfig mEglConfig;
    private EGLContext mEglContext;
    private EGLSurface mEglSurface;

    /** OpenGL ES 2.0 */
    private static final int EGL_CONTEXT_CLIENT_VERSION = 2;
    private static final int EGL_OPENGL_ES2_BIT = 4;

    public EGLConfig getEglConfig() {
        return mEglConfig;
    }

    public EGL10 getEgl() {
        return mEgl;
    }

    /**
     * 初始化EGL环境，创建Display + Config + Context
     * @param sharedContext 共享EGLContext，不传就 EGL10.EGL_NO_CONTEXT
     */
    public boolean init(Surface surface,EGLContext sharedContext) {
        // 1. 获取EGL入口实例
        mEgl = (EGL10) EGLContext.getEGL();
        // 2. 获取默认Display
        mEglDisplay = mEgl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        if (mEglDisplay == EGL10.EGL_NO_DISPLAY) {
            Log.e(TAG, "eglGetDisplay fail");
            return false;
        }
        // 3. 初始化egl版本
        int[] version = new int[2];
        if (!mEgl.eglInitialize(mEglDisplay, version)) {
            Log.e(TAG, "eglInitialize fail, err=" + mEgl.eglGetError());
            return false;
        }
        // 4. 配置属性，RGBA8888，深度缓冲区不开启（相机预览不需要深度）
        int[] configAttr = {
                EGL10.EGL_RED_SIZE, 8,
                EGL10.EGL_GREEN_SIZE, 8,
                EGL10.EGL_BLUE_SIZE, 8,
                EGL10.EGL_ALPHA_SIZE, 8,
                EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
                EGL10.EGL_SURFACE_TYPE, EGL10.EGL_WINDOW_BIT,
                EGL10.EGL_NONE
        };
        EGLConfig[] configs = new EGLConfig[1];
        int[] numConfigs = new int[1];
        if (!mEgl.eglChooseConfig(mEglDisplay, configAttr, configs, 1, numConfigs)) {
            Log.e(TAG, "eglChooseConfig fail");
            return false;
        }
        mEglConfig = configs[0];
        // 5. 创建 EGLContext
        int[] ctxAttr = {
                0x3098, EGL_CONTEXT_CLIENT_VERSION, // EGL_CONTEXT_CLIENT_VERSION
                EGL10.EGL_NONE
        };
        if(sharedContext != null)
        {
            mEglContext = mEgl.eglCreateContext(mEglDisplay, mEglConfig, sharedContext, ctxAttr);
        }else {
            mEglContext = mEgl.eglCreateContext(mEglDisplay, mEglConfig, EGL10.EGL_NO_CONTEXT, ctxAttr);
        }
        if (mEglContext == null || mEglContext == EGL10.EGL_NO_CONTEXT) {
            Log.e(TAG, "eglCreateContext fail");
            return false;
        }
        createWindowSurface(surface);
        Log.i(TAG, "egl init success");
        return true;
    }

    /**
     * 创建WindowSurface，绑定 Surface
     */
    public boolean createWindowSurface(Surface surface) {
        if (surface == null) {
            Log.e(TAG, "createWindowSurface: surface is null");
            return false;
        }
        int[] surfaceAttr = {EGL10.EGL_NONE};
        mEglSurface = mEgl.eglCreateWindowSurface(mEglDisplay, mEglConfig, surface, surfaceAttr);
        if (mEglSurface == null || mEglSurface == EGL10.EGL_NO_SURFACE) {
            Log.e(TAG, "createWindowSurface(Surface) fail, err=" + mEgl.eglGetError());
            return false;
        }
        return makeCurrent();
    }

    /**
     * 创建WindowSurface，绑定 SurfaceTexture
     * 注意：EGL10老接口对SurfaceTexture兼容性不如EGL14，部分机型有坑
     */
    public boolean createWindowSurface(SurfaceTexture surfaceTexture) {
        if (surfaceTexture == null) {
            Log.e(TAG, "createWindowSurface: surfaceTexture is null");
            return false;
        }
        int[] surfaceAttr = {EGL10.EGL_NONE};
        mEglSurface = mEgl.eglCreateWindowSurface(mEglDisplay, mEglConfig, surfaceTexture, surfaceAttr);
        if (mEglSurface == null || mEglSurface == EGL10.EGL_NO_SURFACE) {
            Log.e(TAG, "createWindowSurface(SurfaceTexture) fail, err=" + mEgl.eglGetError());
            return false;
        }
        return makeCurrent();
    }

    /**
     * 创建离屏PBufferSurface
     */
    public boolean createPBufferSurface(int width, int height) {
        int[] surfaceAttr = {
                EGL10.EGL_WIDTH, width,
                EGL10.EGL_HEIGHT, height,
                EGL10.EGL_NONE
        };
        mEglSurface = mEgl.eglCreatePbufferSurface(mEglDisplay, mEglConfig, surfaceAttr);
        if (mEglSurface == null || mEglSurface == EGL10.EGL_NO_SURFACE) {
            Log.e(TAG, "createPBufferSurface fail, err=" + mEgl.eglGetError());
            return false;
        }
        return makeCurrent();
    }

    /**
     * 将当前eglSurface + eglContext绑定到当前线程
     */
    public boolean makeCurrent() {
        boolean ret = mEgl.eglMakeCurrent(mEglDisplay, mEglSurface, mEglSurface, mEglContext);
        if (!ret) {
            Log.e(TAG, "makeCurrent fail, err=" + mEgl.eglGetError());
        }
        return ret;
    }

    /**
     * 解除当前线程的EGL绑定
     */
    public void detachCurrent() {
        mEgl.eglMakeCurrent(mEglDisplay,
                EGL10.EGL_NO_SURFACE,
                EGL10.EGL_NO_SURFACE,
                EGL10.EGL_NO_CONTEXT);
    }

    /**
     * 交换缓冲区，提交画面
     */
    public boolean swapBuffers() {
        boolean ret = mEgl.eglSwapBuffers(mEglDisplay, mEglSurface);
        if (!ret) {
            Log.e(TAG, "swapBuffers fail, err=" + mEgl.eglGetError());
        }
        return ret;
    }

    /**
     * 销毁Surface，保留Context
     */
    public void destroySurface() {
        if (mEglSurface != null && mEglSurface != EGL10.EGL_NO_SURFACE) {
            detachCurrent();
            mEgl.eglDestroySurface(mEglDisplay, mEglSurface);
            mEglSurface = EGL10.EGL_NO_SURFACE;
        }
    }

    /**
     * 全部释放：Surface + Context + Display
     */
    public void release() {
        destroySurface();
        if (mEglContext != null && mEglContext != EGL10.EGL_NO_CONTEXT) {
            mEgl.eglDestroyContext(mEglDisplay, mEglContext);
            mEglContext = EGL10.EGL_NO_CONTEXT;
        }
        if (mEglDisplay != null && mEglDisplay != EGL10.EGL_NO_DISPLAY) {
            mEgl.eglTerminate(mEglDisplay);
            mEglDisplay = EGL10.EGL_NO_DISPLAY;
        }
        mEglConfig = null;
        mEgl = null;
    }

    public EGLContext getEglContext() {
        return mEglContext;
    }

    public EGLDisplay getEglDisplay() {
        return mEglDisplay;
    }

    public EGLSurface getEglSurface() {
        return mEglSurface;
    }

    public boolean hasSurface() {
        return mEglSurface != null && mEglSurface != EGL10.EGL_NO_SURFACE;
    }
}
