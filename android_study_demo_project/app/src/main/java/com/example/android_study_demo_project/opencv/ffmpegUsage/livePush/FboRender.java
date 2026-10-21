package com.example.android_study_demo_project.opencv.ffmpegUsage.livePush;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import com.example.android_study_demo_project.R;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * 离谱渲染 FboRender
 */
public class FboRender implements IFboRender {
    private static final String TAG = "FboRender";
    // FBO纹理尺寸：等于相机预览分辨率（推流分辨率）
    private int mFboWidth = 0;
    private int mFboHeight = 0;

    public final String mVertexShaderStr = "attribute vec4 v_Position;\n" +
            "attribute vec2 f_Position;\n" +
            "varying vec2 ft_Position;\n" +
            "void main() {\n" +
            "    ft_Position = f_Position;\n" +
            "    gl_Position = v_Position;\n" +
            "}";
    public final String mFragmentShadeStr = "precision mediump float;\n" +
            "varying vec2 ft_Position;\n" +
            "uniform sampler2D sTexture;\n" +
            "void main() {\n" +
            "    gl_FragColor = texture2D(sTexture, ft_Position);\n" +
            "}";

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
    private FloatBuffer mFragmentBuffer;
    private int mVboId;
    private int mProgram;

    private int mFboId;            // FBO容器id
    private int mFboTextureId;     // FBO颜色附件纹理【重点：推流拿这个纹理】
    private int vPosition;
    private int fPosition;
//    private int uMatrix;
    private int sTexture;          // 提升为成员变量
//    private float[] matrix = new float[16];
    private Context mContext;

    public FboRender(Context context) {
        this.mContext = context;
        mFboWidth = LivePushUtils.getScreenWidth(context);
        mFboHeight = LivePushUtils.getScreenHeight(context);
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
    public void onBindFbo() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFboId);
    }

    @Override
    public void onUnBindFbo() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    @Override
    public void onSurfaceCreated(int viewWidth, int viewHeight) {
        mProgram = LivePushUtils.createProgram(mVertexShaderStr,mFragmentShadeStr);
        //获取坐标
        vPosition = GLES20.glGetAttribLocation(mProgram, "v_Position");
        fPosition = GLES20.glGetAttribLocation(mProgram, "f_Position");
        sTexture = GLES20.glGetUniformLocation(mProgram, "sTexture");
//        uMatrix = GLES20.glGetUniformLocation(mProgram, "u_Matrix");


        //创建 vbos
        int[] vBos = new int[1];
        GLES20.glGenBuffers(1,vBos,0);
        //绑定 vbos
        mVboId = vBos[0];
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,mVboId);
        //开辟 vbos
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER,(mVertexCoordinate.length+mFragmentCoordinate.length) * 4,
                null,GLES20.GL_STATIC_DRAW);
        //赋值 vbos
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER,0,mVertexCoordinate.length * 4,
                mVertexBuffer);
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER,mVertexCoordinate.length * 4,
                mFragmentCoordinate.length * 4,mFragmentBuffer);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        // ========= 新建FBO + FBO颜色纹理 =========
        createFbo(mFboWidth, mFboHeight);

        GLES20.glUseProgram(mProgram);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glUniform1i(sTexture, 0);
    }

    /**
     * 创建FBO和FBO离屏纹理
     */
    private void createFbo(int width, int height) {
        destroyFbo();

        //1. 创建FBO
        int[] fboArr = new int[1];
        GLES20.glGenFramebuffers(1, fboArr, 0);
        mFboId = fboArr[0];
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFboId);

        //2. 创建FBO的颜色附件纹理
        int[] texArr = new int[1];
        GLES20.glGenTextures(1, texArr, 0);
        mFboTextureId = texArr[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFboTextureId);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        // 分配纹理内存，没有图片，由GPU渲染填充
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
                width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);

        //3. 将纹理挂载到FBO颜色附着点
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, mFboTextureId, 0);

        // 校验FBO完整性（开发调试必加）
        int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
        if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("FBO create failed, status=" + status);
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
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

    /**
     * 把FBO里面渲染好的纹理绘制到屏幕（onUnBindFbo之后调用）
     */
    @Override
    public void onDrawFrame() {

        //清屏并绘制红色
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(0f,0f,0f,1f);

        //激活 program
        GLES20.glUseProgram(mProgram);
        // 重点：这里绑定【FBO纹理】，把FBO画面输出到屏幕
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFboTextureId);

//        //设置正交矩阵的值
//        GLES20.glUniformMatrix4fv(uMatrix,1,false,matrix,0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,mVboId);

        /**
         * 设置坐标
         * 2：2个为一个点
         * GLES20.GL_FLOAT：float类型
         * false：不做归一化
         * 8：步长为8
         */
        GLES20.glEnableVertexAttribArray(vPosition);
        GLES20.glVertexAttribPointer(vPosition,2,GLES20.GL_FLOAT,false,8,0);
        GLES20.glEnableVertexAttribArray(fPosition);
        GLES20.glVertexAttribPointer(fPosition,2,GLES20.GL_FLOAT,false,8,mVertexCoordinate.length * 4);
        //绘制到屏幕
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP,0,4);//两个三角形，4个点
        //解绑
        GLES20.glDisableVertexAttribArray(vPosition);
        GLES20.glDisableVertexAttribArray(fPosition);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,0);
    }


    // ========== 对外接口：获取FBO纹理，给推流编码使用 ==========
    public int getFboTextureId() {
        return mFboTextureId;
    }

    // 销毁资源（建议在GL线程调用，你可以加到IFboRender）
    public void release() {
        int[] temp = new int[1];
        temp[0] = mFboId;
        GLES20.glDeleteFramebuffers(1, temp, 0);

        temp[0] = mFboTextureId;
        GLES20.glDeleteTextures(1, temp, 0);

        temp[0] = mVboId;
        GLES20.glDeleteBuffers(1, temp, 0);

        GLES20.glDeleteProgram(mProgram);
    }

    /**
     * 销毁旧FBO，新建
     */
    private void destroyFbo() {
        int[] temp = new int[1];
        temp[0] = mFboId;
        GLES20.glDeleteFramebuffers(1, temp,0);
        temp[0] = mFboTextureId;
        GLES20.glDeleteTextures(1, temp,0);
        mFboId = 0;
        mFboTextureId = 0;
    }
}
