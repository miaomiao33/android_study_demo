package com.example.android_study_demo_project.opencv.ffmpegUsage.livePush.livePushMain;

import android.content.Context;
import android.opengl.GLES20;

import com.example.android_study_demo_project.R;
import com.example.android_study_demo_project.opencv.ffmpegUsage.livePush.BaseRender;
import com.example.android_study_demo_project.opencv.ffmpegUsage.livePush.LivePushUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class VideoPushRender extends BaseRender {
    private Context mContext;

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
    private int mFragmentShader = -1;

    public VideoPushRender(Context context, int textureId) {
        this.mContext = context;
        this.mTextureId = textureId;//和外面共用一个纹理ID

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
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {

        // =========  然后创建 CameraViewRender 的 Surface =========
        //获取对应顶点和纹理的代码
        String vertexSource = LivePushUtils.getGLResource(mContext, R.raw.vertex_shader);
        if(mFragmentShader == -1)
        {
            mFragmentShader = R.raw.fragment_shader;
        }
        String fragmentSource = LivePushUtils.getGLResource(mContext, mFragmentShader);
        //编译链接生成程序
        mProgram = LivePushUtils.createProgram(vertexSource,fragmentSource);
        //获取坐标
        vPosition = GLES20.glGetAttribLocation(mProgram, "v_Position");
        fPosition = GLES20.glGetAttribLocation(mProgram, "f_Position");

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

    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        //设置视口（Viewport）
        GLES20.glViewport(0,0,width,height);
    }


    @Override
    public void onDrawFrame(GL10 gl10) {
        //激活 program
        GLES20.glUseProgram(mProgram);
        //绑定纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);

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
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,0);
    }

    // 释放资源，建议在相机销毁时GL线程调用
    public void release() {
        int[] temp = new int[1];
        temp[0] = mTextureId;
        GLES20.glDeleteTextures(1, temp, 0);

        //解绑 + glDeleteBuffers 释放 VBO 显存
        temp[0] = mVboId;
        GLES20.glDeleteBuffers(1, temp, 0);

        GLES20.glDeleteProgram(mProgram);
    }

    public void setFragmentRender(int fragmentShader) {
        this.mFragmentShader = fragmentShader;
    }
}
