package com.example.android_study_demo_project.opencv.OpenGLUsage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.AttributeSet;

import com.example.android_study_demo_project.R;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL;
import javax.microedition.khronos.opengles.GL10;

public class MyGLView extends GLSurfaceView implements GLSurfaceView.Renderer {
    private int vTexture;
    private int mTextureId;
    private float scale;
    private Bitmap mBitmap;
    // 存储正交矩阵
    private float[] mProjectionMatrix = new float[16];

    public MyGLView(Context context) {
        this(context,null);
    }

    public MyGLView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //设置EGL版本
        setEGLContextClientVersion(2);
        //设置渲染器（画笔）
        setRenderer(this);
        //设置按需渲染
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }

    private void initGLSL()
    {
        //读取着色器源码
        //顶点 画形状
        //直接就是从main下开始的，使用getAssets，故前面不用加/assets/
        String vertex_code = OpenGLUtils.loadStringFromAssetFile(getContext(),"filters/basic.vert");
        //片元 贴图
        String fragment_code = OpenGLUtils.loadStringFromAssetFile(getContext(),"filters/basic.frag");

        /***
         * 1、使用GLSL
         */
        int program = OpenGLUtils.glUseProgram(vertex_code,fragment_code);
        /***
         * 2、找到着色器的各个变量索引
         */

        //获取属性的位置
        int vPosition = GLES20.glGetAttribLocation(program,"vPosition");
        int vCoord = GLES20.glGetAttribLocation(program,"vCoord");
        vTexture = GLES20.glGetAttribLocation(program,"vTexture");

        /***
         * 3、设置顶点与纹理坐标数据
         */
        //顶点坐标数据
        OpenGLUtils.initCoord(vPosition,vCoord);

        // 加载图片生成纹理（res/drawable下test图片）
        mTextureId = OpenGLUtils.createTexture(BitmapFactory.decodeResource(getResources(), R.drawable.small_eyes));
    }

    //GL子线程：画布创建
    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        //清理画布和缓存
        // 清屏颜色
        GLES20.glClearColor(0,0,0,0);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        initGLSL();
    }

    //GL子线程：画布改变
    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        //屏幕切换了
        //改变画布大小
        GLES20.glViewport(0,0,width,height);
    }

    //GL子线程：绘画
    @Override
    public void onDrawFrame(GL10 gl10) {
        if(mBitmap == null || mBitmap.isRecycled())
        {
            return;
        }

        GLES20.glClearColor(0,0,0,0);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        /***
         * 把图片传给GLSL
         */
        //激活纹理单元，在openGL中创建一个画布
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        //操作创建的纹理空间（准备开始在这个画布上画画），在画布上开始画画
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,mTextureId);
        //生成2D纹理 将图片附加到上一步的纹理中
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D,0,mBitmap,0);
        //绑定纹理采样器与纹理单元
        //将创建的画布赋值给vTexture变量（vTexture是GLSL中代表图片的变量）
        GLES20.glUniform1i(vTexture,0);

        //通知GLSL画画（画到屏幕上）开始从画框中获取画面 画到屏幕上
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP,0,4);
    }

    /***
     * 设置缩放系数
     * @param scale
     */
    public void setScale(float scale) {
        this.scale = scale;
        //主动触发一次 OpenGL 渲染，让 Renderer.onDrawFrame(GL10 gl) 立即执行绘制。
        requestRender();
    }

    /***
     * 设置处理的图片
     * @param bitmap
     */
    public void setBitmap(Bitmap bitmap) {
        if(mBitmap != null && !mBitmap.isRecycled())
        {
            mBitmap.recycle();
        }
        mBitmap = bitmap;
        requestRender();
    }

    public void release()
    {
        //释放OpenCV获取眼睛坐标
//        EyeDectorManager.getInstance().release();
    }

}
