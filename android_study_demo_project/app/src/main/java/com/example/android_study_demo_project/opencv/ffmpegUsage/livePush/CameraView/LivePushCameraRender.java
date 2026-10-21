package com.example.android_study_demo_project.opencv.ffmpegUsage.livePush.CameraView;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.example.android_study_demo_project.R;
import com.example.android_study_demo_project.opencv.ffmpegUsage.livePush.BaseRender;
import com.example.android_study_demo_project.opencv.ffmpegUsage.livePush.FboRender;
import com.example.android_study_demo_project.opencv.ffmpegUsage.livePush.LivePushUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class LivePushCameraRender extends BaseRender {
    private Context mContext;
    private FboRender mFboRender;

    /**
     * 顶点坐标
     */
    private float[] mVertexCoordinate = new float[]{
            -1f,-1f,
            1f,-1f,
            -1f,1f,
            1f,1f,
    };
    private FloatBuffer mVertexBuffer;
    /**
     * 纹理坐标
     */
    private float[] mFragmentCoordinate = new float[]{
            0f,1f,
            1f,1f,
            0f,0f,
            1f,0f,
    };
    private int mVboId;

    private int mTextureId;
    private FloatBuffer mFragmentBuffer;
    private int mProgram;
    private int vPosition;
    private int fPosition;
    private int sTexture;
    private SurfaceTexture mCameraSt;
    private int uMatrix;
    private float[] matrix = new float[16];
    private RenderListener mRenderListener;

    public LivePushCameraRender(Context context) {
        this.mContext = context;

        mVertexBuffer = ByteBuffer.allocateDirect(mVertexCoordinate.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(mVertexCoordinate);
        mVertexBuffer.position(0);

        mFragmentBuffer = ByteBuffer.allocateDirect(mFragmentCoordinate.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(mFragmentCoordinate);
        mFragmentBuffer.position(0);

        mFboRender = new FboRender(mContext);

        //fbo 的坐标是标准坐标
//        Matrix.orthoM(matrix,0,-1,1,-1f,1f,-1f,1f);
        // 初始单位矩阵
        Matrix.setIdentityM(matrix, 0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        // =========  先创建 FBO 的 Surface =========
        mFboRender.onSurfaceCreated(mViewWidth,mViewHeight);

        // =========  然后创建 CameraViewRender 的 Surface =========
        String vertexSource = LivePushUtils.getGLResource(mContext, R.raw.vertex_shader_matrix);
        String fragmentSource = LivePushUtils.getGLResource(mContext, R.raw.fragment_shader_camera);
        mProgram = LivePushUtils.createProgram(vertexSource,fragmentSource);
        //获取坐标
        vPosition = GLES20.glGetAttribLocation(mProgram, "v_Position");
        fPosition = GLES20.glGetAttribLocation(mProgram, "f_Position");
        uMatrix = GLES20.glGetUniformLocation(mProgram, "u_Matrix");
        sTexture = GLES20.glGetUniformLocation(mProgram, "sTexture");

        //创建 vbos
        int[] vBos = new int[1];
        GLES20.glGenBuffers(1,vBos,0);
        //绑定 vbos
        mVboId = vBos[0];
        //绑定 VBO 到 GL_ARRAY_BUFFER（状态机：接下来所有 buffer 操作都作用这个 VBO）
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,mVboId);
        //开辟 vbos
        // 分配总显存大小（顶点数组字节 + 纹理坐标数组字节）
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER,(mVertexCoordinate.length+mFragmentCoordinate.length) * 4,
                null,GLES20.GL_STATIC_DRAW);
        //赋值 vbos
        // 第一段：顶点坐标，偏移0
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER,0,mVertexCoordinate.length * 4,
                mVertexBuffer);
        // 第二段：纹理坐标，偏移 = 顶点数据总字节
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER,mVertexCoordinate.length * 4,
                mFragmentCoordinate.length * 4,mFragmentBuffer);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        //===== OES外部纹理（相机专用）=====
        //生成绑定纹理
        int[] textures = new int[1];//生成1个纹理
        GLES20.glGenTextures(1, textures,0);
        mTextureId = textures[0];
        //绑定纹理
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureId);

        //设置纹理环绕方式
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        //设置纹理过滤方式(线性过滤，线性放大等)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        mCameraSt = new SurfaceTexture(mTextureId);

        if(mRenderListener != null)
        {
            //Surface 创建好了回调回去打开相机
            mRenderListener.onSurfaceCreated(mCameraSt);
        }

        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_BINDING_EXTERNAL_OES,0);

        //激活 program
        GLES20.glUseProgram(mProgram);
        GLES20.glUniform1i(sTexture, 0);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        //设置FBO视口（和外面保持一致）
        mFboRender.onSurfaceChanged(width,height);

        //设置视口（Viewport）
        GLES20.glViewport(0,0,width,height);

//        BitmapFactory.Options opts = new BitmapFactory.Options();
//        opts.inJustDecodeBounds = true;
//        BitmapFactory.decodeResource(mContext.getResources(),R.mipmap.s_1,opts);
//        float imageWidth = opts.outWidth;
//        float imageHeight = opts.outHeight;
//
//        if(width > height)
//        {
//            Matrix.orthoM(matrix,0,-width/((height / imageHeight) * imageWidth),
//                    width/((height / imageHeight) * imageWidth),-1f,1f,-1f,1f);
//        }else {
//            Matrix.orthoM(matrix,0,-1f,1f,-height/((width / imageWidth) * imageHeight),
//                    height/((width / imageWidth) * imageHeight),-1f,1f);
//        }
//
//        //fbo 的坐标是标准坐标
//        Matrix.rotateM(matrix,0,180,1,0,0);
    }


    @Override
    public void onDrawFrame(GL10 gl10) {
        // 更新相机纹理，必须调用
        if (mCameraSt != null) {
            mCameraSt.updateTexImage();
        }

        // =========  绑定FBO：切换绘制目标！核心 =========
        mFboRender.onBindFbo();

        //========= 后面所有 glDraw 不再画到手机屏幕，全部画到 FBO 的那张 2D 纹理（mFboTextureId）上 =========
        //清屏并绘制红色
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(0f,0f,0f,1f);

        //激活 program
        GLES20.glUseProgram(mProgram);
        //绑定纹理
        // 绑定OES外部纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureId);

        //设置正交矩阵的值
        GLES20.glUniformMatrix4fv(uMatrix,1,false,matrix,0);

        //绑定 VBO 到 GL_ARRAY_BUFFER（状态机：接下来所有 buffer 操作都作用这个 VBO）
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,mVboId);
        /**
         * 设置坐标
         * 2：2个为一个点
         * GLES20.GL_FLOAT：float类型
         * false：不做归一化
         * 8：步长为8
         */
        // 启用属性
        GLES20.glEnableVertexAttribArray(vPosition);
        // v_Position：顶点坐标，2个float，步长8字节，偏移 0
        GLES20.glVertexAttribPointer(vPosition,2,GLES20.GL_FLOAT,false,8,0);
        GLES20.glEnableVertexAttribArray(fPosition);
        // f_Position：纹理坐标，2个float，偏移是顶点数组总字节
        GLES20.glVertexAttribPointer(fPosition,2,GLES20.GL_FLOAT,false,8,mVertexCoordinate.length * 4);
        //绘制到屏幕(实际上是绘制到FBO内存，输出写入 FBO 纹理)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP,0,4);//两个三角形，4个点
        //解绑
        GLES20.glDisableVertexAttribArray(vPosition);
        GLES20.glDisableVertexAttribArray(fPosition);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,0);

        // =========  解绑FBO，画布切回默认屏幕帧缓冲 =========
        mFboRender.onUnBindFbo();
        // =========  把FBO里已经渲染好的画面，画到屏幕给用户预览 =========
        //Fbo绘制到屏幕上
        mFboRender.onDrawFrame();
    }


    public void setOnRenderListener(RenderListener mRenderListener) {
        this.mRenderListener = mRenderListener;
    }

    public int getTextureId() {
        return mTextureId;
    }

    // ========== 上层LivePushCameraView调用的矩阵方法 ==========
    //把变换矩阵重置为单位矩阵
    public void resetMatrix() {
        Matrix.setIdentityM(matrix, 0);
    }

    //在当前矩阵基础上，叠加一次旋转变换
    //x,y,z：旋转轴
    //- `0,0,1` → 绕 Z 轴旋转（平面旋转，2D 最常用，转正相机画面）
    //- `1,0,0` → 绕 X 轴旋转（上下翻转）
    //- `0,1,0` → 绕 Y 轴旋转（左右翻转 / 镜像）
    public void rotateMatrix(int dummy, float angle, float x, float y, float z) {
        Matrix.rotateM(matrix, 0, angle, x, y, z);
    }

    // 用来翻转
    public void scaleMatrix(float x, float y, float z) {
        Matrix.scaleM(matrix, 0, x, y, z);
    }
    // ========== 上层LivePushCameraView调用的矩阵方法 ==========

    public interface RenderListener {
        void onSurfaceCreated(SurfaceTexture surfaceTexture);
    }

    // 释放资源，建议在相机销毁时GL线程调用
    public void release() {
        if (mCameraSt != null) {
            mCameraSt.release();
            mCameraSt = null;
        }
        int[] temp = new int[1];
        temp[0] = mTextureId;
        GLES20.glDeleteTextures(1, temp, 0);

        //解绑 + glDeleteBuffers 释放 VBO 显存
        temp[0] = mVboId;
        GLES20.glDeleteBuffers(1, temp, 0);

        GLES20.glDeleteProgram(mProgram);
        mFboRender.release();
    }
}
