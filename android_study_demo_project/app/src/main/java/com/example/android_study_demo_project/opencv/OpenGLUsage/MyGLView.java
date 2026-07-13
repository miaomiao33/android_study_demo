package com.example.android_study_demo_project.opencv.OpenGLUsage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.util.AttributeSet;
import android.util.Log;

import com.example.android_study_demo_project.R;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MyGLView extends GLSurfaceView implements GLSurfaceView.Renderer {
    private int vPosition;
    private int vCoord;
    private int vTexture;
    private int a;
    private int left_eye;
    private int right_eye;

    private int mTextureId;
    private float mScale;
    private Bitmap mBitmap;

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
        //眼睛定位器
        EyeDectorManager.getInstance().init(context);
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

    private int texSize;
    private void initGLSL()
    {
        /*** 1、创建着色器程序 */
        //读取着色器源码
        //顶点 画形状
        //直接就是从main下开始的，使用getAssets，故前面不用加/assets/
        String vertex_code = OpenGLUtils.loadStringFromAssetFile(getContext(),"filters/basic.vert");
        //片元 贴图
        String fragment_code = OpenGLUtils.loadStringFromAssetFile(getContext(),"filters/bigeye.frag");

        //编译单个着色器并生成program
        int program = OpenGLUtils.glCompileAndUseProgram(vertex_code,fragment_code);

        /***2、找到着色器的各个变量索引*/
        //获取属性的位置
        vPosition = GLES20.glGetAttribLocation(program,"vPosition");
        vCoord = GLES20.glGetAttribLocation(program,"vCoord");
        vTexture = GLES20.glGetUniformLocation(program,"vTexture");
        a = GLES20.glGetUniformLocation(program,"a");
        left_eye = GLES20.glGetUniformLocation(program,"left_eye");
        right_eye = GLES20.glGetUniformLocation(program,"right_eye");

        texSize = GLES20.glGetUniformLocation(program, "texSize");

        /*** 3、设置顶点与纹理坐标数据 */
        //顶点坐标数据
        OpenGLUtils.initCoord(vPosition,vCoord);

        // 配置纹理参数（res/drawable下test图片）
        mTextureId = OpenGLUtils.createTexture();
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

        Eye[] eyes = EyeDectorManager.getInstance().getEyes(mBitmap);
        if(eyes == null)
        {
            return;
        }

        // 清屏颜色
        GLES20.glClearColor(0,0,0,0);
        //清空帧缓存中指定类型的缓冲区
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        //传入数据
        // 传入纹理宽高，供顶点着色器UV转像素
        GLES20.glUniform2f(texSize, mBitmap.getWidth(), mBitmap.getHeight());
        GLES20.glUniform2fv(left_eye, 1, eyes[0].pos, 0);
        GLES20.glUniform2fv(right_eye, 1, eyes[1].pos, 0);
        GLES20.glUniform1f(a, mScale);
        Log.i("mScale_eyes: ","left : "+eyes[0].pos[0]+"-"+eyes[0].pos[1]);
        Log.i("mScale_eyes: ","right : "+eyes[1].pos[0]+"-"+eyes[1].pos[1]);
        Log.i("mScale_eyes: ","mScale : "+mScale);

        /***
         * 把图片传给GLSL
         */
        //激活纹理单元，在openGL中创建一个画布
        // 绑定纹理单元0
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        //操作创建的纹理空间（准备开始在这个画布上画画），在画布上开始画画
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,mTextureId);
        //生成2D纹理 将图片附加到上一步的纹理中
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D,0,mBitmap,0);
        //绑定纹理采样器与纹理单元
        //将创建的画布赋值给vTexture变量（vTexture是GLSL中代表图片的变量）
        GLES20.glUniform1i(vTexture,0);

//        /*** 3、设置顶点与纹理坐标数据 */
//        //顶点坐标数据
//        OpenGLUtils.initCoord(vPosition,vCoord);

        //通知GLSL画画（画到屏幕上）开始从画框中获取画面 画到屏幕上
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP,0,4);
    }

    /***
     * 设置缩放系数
     * @param scale
     */
    public void setScale(float scale) {
        this.mScale = scale;
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
        //获取眼睛中心点
        Eye[] eyes = EyeDectorManager.getInstance().getEyes(mBitmap);
        if(eyes == null)
        {
            return;
        }
//        //pos[0]存储的归一化坐标
//        Bitmap temp = drawSimplePoint(mBitmap, eyes[0].pos[0]*mBitmap.getWidth(), eyes[0].pos[1]*mBitmap.getHeight(), Color.RED);
//        Bitmap finalBmp = drawSimplePoint(temp, eyes[1].pos[0]*mBitmap.getWidth(), eyes[1].pos[1]*mBitmap.getHeight(), Color.GREEN);
        //pos[0]存储的像素坐标
        //绘制眼睛中心点，方便观察
        Bitmap temp = drawSimplePoint(mBitmap, eyes[0].pos[0], eyes[0].pos[1], Color.RED);
        Bitmap finalBmp = drawSimplePoint(temp, eyes[1].pos[0], eyes[1].pos[1], Color.GREEN);
        mBitmap = finalBmp;
        requestRender();
    }


    //绘制眼睛中心点
    public static Bitmap drawSimplePoint(Bitmap origin, float x, float y, int color) {
        //把传入的原图复制一份，生成新位图bmp ARGB_8888：带透明通道的 32 位图片格式
        //第二个参数true：可变位图（允许绘图修改像素）
        Bitmap bmp = origin.copy(Bitmap.Config.ARGB_8888, true);
        //创建画布 Canvas，绑定到复制出来的图片bmp
        Canvas canvas = new Canvas(bmp);
        //Paint.ANTI_ALIAS_FLAG：开启抗锯齿，圆点边缘不会有锯齿、更平滑
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(x, y, 6, paint);
        return bmp;
    }

    public void release()
    {
        //释放OpenCV获取眼睛坐标
        EyeDectorManager.getInstance().release();
    }

}
