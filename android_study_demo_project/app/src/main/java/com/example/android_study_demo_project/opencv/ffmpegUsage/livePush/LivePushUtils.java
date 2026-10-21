package com.example.android_study_demo_project.opencv.ffmpegUsage.livePush;


import android.content.Context;
import android.graphics.Rect;
import android.opengl.GLES20;
import android.os.Build;
import android.util.Log;
import android.view.WindowManager;

import com.example.android_study_demo_project.opencv.OpenGLUsage.OpenGLUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class LivePushUtils {
    private static final String TAG = LivePushUtils.class.getSimpleName();


    //读取着色器源码
    //直接就是从main下开始的，使用getAssets，故前面不用加/assets/
    public static String loadStringFromRawFile(Context context, Integer rawResourceId)
    {
        StringBuilder  shaderSource = new StringBuilder();

        try {
            BufferedReader reader=  new BufferedReader(new InputStreamReader(context.getResources().openRawResource(rawResourceId)));
            String line;
            while ((line = reader.readLine() )!= null)
            {
                shaderSource.append(line).append("\n");
            }
            reader.close();
            return shaderSource.toString();

        } catch (IOException e) {
            Log.e(TAG,"Could not load shader file");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 创建一个 program
     * @param vertexSource 顶点源码资源
     * @param fragmentSource 片元源码资源
     * @return
     */
    public static int createProgram(String vertexSource, String fragmentSource) {
        //分别加载创建着色器
        int vertexShader = compileShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        int fragmentShader = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);

        if(vertexShader != 0 && fragmentShader != 0)
        {
            //创建 program
            int programId = GLES20.glCreateProgram();
            //将着色器程序添加到渲染程序中
            GLES20.glAttachShader(programId, vertexShader);
            GLES20.glAttachShader(programId, fragmentShader);
            //链接源程序
            GLES20.glLinkProgram(programId);

            //检查链接状态（判断有没有错误）
            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(programId, GLES20.GL_LINK_STATUS, linkStatus, 0);
            Log.d(TAG, "链接程序" + GLES20.glGetProgramInfoLog(programId));
            if (linkStatus[0] != GLES20.GL_TRUE) {
                Log.d(TAG, "链接program失败");
                GLES20.glDeleteProgram(programId);
                programId = 0;
            }

            return programId;
        }

        return 0;
    }

    // 编译单个着色器
    private static int compileShader(int type, String sourceCode) {
        int shader = GLES20.glCreateShader(type);
        if (shader == 0) {
            Log.d(TAG, "创建shader失败");
            return 0;
        }
        //上传shader源码
        GLES20.glShaderSource(shader, sourceCode);
        //编译shader源代码
        GLES20.glCompileShader(shader);
        //取出编译结果
        int[] compileStatus = new int[1];
        //取出shaderId的编译状态并把他写入compileStatus的0索引
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
        Log.d("Shader编译状态", GLES20.glGetShaderInfoLog(shader));

        if (compileStatus[0] == 0) {
            GLES20.glDeleteShader(shader);
            Log.d(TAG, "创建shader失败");
            return 0;
        }
        return shader;
    }

    /**
     * 获取glsl资源
     * @param context
     * @param rawId
     * @return
     */
    public static String getGLResource(Context context,int rawId)
    {
        InputStream inputStream = context.getResources().openRawResource(rawId);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuffer sb = new StringBuffer();
        String line;
        try {
            while ((line = reader.readLine()) != null){
                sb.append(line).append("\n");
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

    /**
     * 加载着色器
     * @param shaderType 着色器类型
     * @param source 资源源码
     * @return
     */
    private static int loadShader(int shaderType, String source)
    {
        //创建 Shader
        int shader = GLES20.glCreateShader(shaderType);
        if (shader == 0) {
            Log.d(TAG, "创建shader失败");
            return 0;
        }
        //设置资源
        GLES20.glShaderSource(shader, source);
        //编译shader源代码
        GLES20.glCompileShader(shader);
        //取出编译结果
        int[] compileStatus = new int[1];
        //取出shaderId的编译状态并把他写入compileStatus的0索引
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
        Log.d("Shader编译状态", GLES20.glGetShaderInfoLog(shader));

        if (compileStatus[0] != GLES20.GL_TRUE) {
            Log.d(TAG, "创建shader失败");
            GLES20.glDeleteShader(shader);
            shader = 0;
        }
        return shader;
    }

    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int width, height;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ API30 新WindowMetrics
            Rect bounds = wm.getMaximumWindowMetrics().getBounds();
            width = bounds.width();
            height = bounds.height();
        } else {
            // 低版本兼容
            android.graphics.Point point = new android.graphics.Point();
            wm.getDefaultDisplay().getRealSize(point);
            width = point.x;
            height = point.y;
        }

        return width;
    }

    public static int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int width, height;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ API30 新WindowMetrics
            Rect bounds = wm.getMaximumWindowMetrics().getBounds();
            width = bounds.width();
            height = bounds.height();
        } else {
            // 低版本兼容
            android.graphics.Point point = new android.graphics.Point();
            wm.getDefaultDisplay().getRealSize(point);
            width = point.x;
            height = point.y;
        }

        return height;
    }
}
