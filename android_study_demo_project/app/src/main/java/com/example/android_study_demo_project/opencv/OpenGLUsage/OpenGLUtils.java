package com.example.android_study_demo_project.opencv.OpenGLUsage;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class OpenGLUtils {
    private static final String TAG = OpenGLUtils.class.getSimpleName();

    //读取着色器源码
    public static String loadStringFromAssetFile(Context context,String filePath)
    {
        StringBuilder  shaderSource = new StringBuilder();

        try {
            BufferedReader reader=  new BufferedReader(new InputStreamReader(context.getAssets().open(filePath)));
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

    public static int glUseProgram(String vertex_code, String fragment_code)
    {
        /***
         * 编译着色器
         */

        /** 1、编译创建顶点着色器 */
        int vertexShader = compileShader(GLES20.GL_VERTEX_SHADER, vertex_code);

        /*** 2、编译创建片元着色器 */
        int fragmentShader = compileShader(GLES20.GL_FRAGMENT_SHADER, fragment_code);

        /***
         * 把着色器链接到程序
         */
        /*** 3、创建着色器程序 */
        //创建程序对象
        int programId = GLES20.glCreateProgram();
        if (programId == 0) {
            Log.d(TAG, "创建program失败");
            return 0;
        }
        //依附着色器
        GLES20.glAttachShader(programId, vertexShader);
        GLES20.glAttachShader(programId, fragmentShader);
        //链接程序
        GLES20.glLinkProgram(programId);

        //检查链接状态
        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(programId, GLES20.GL_LINK_STATUS, linkStatus, 0);
        Log.d(TAG, "链接程序" + GLES20.glGetProgramInfoLog(programId));
        if (linkStatus[0] == 0) {
            GLES20.glDeleteProgram(programId);
            Log.d(TAG, "链接program失败");
            return 0;
        }
        //验证opengl对象
        boolean volidated = volidateProgram(programId);
        if(!volidated)
        {
            GLES20.glDeleteProgram(programId);
            Log.d(TAG, "当前program不支持");
            return 0;
        }
        //使用着色器
        GLES20.glUseProgram(programId);

        return programId;
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

    public static boolean volidateProgram(int program) {
        GLES20.glValidateProgram(program);
        int[] validateStatus = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_VALIDATE_STATUS, validateStatus, 0);
        Log.d(TAG, "当前opengl情况" + validateStatus[0] + "/" + GLES20.glGetProgramInfoLog(program));

        return validateStatus[0] != 0;
    }

    //顶点坐标
    static float[] VERTEX = {
            -1.0f,-1.0f,// bottom left
            1.0f,-1.0f,// bottom right
            -1.0f,1.0f,// top left
            1.0f,1.0f,// top right
    };

    //纹理坐标  对应顶点坐标  与之映射
    static float[] TEXTURE = {
            0.0f,1.0f,
            1.0f,1.0f,
            0.0f,0.0f,
            1.0f,0.0f,
    };

    public static void initCoord(int vPosition, int vCoord)
    {
        //位置
        FloatBuffer vertexBuffer = ByteBuffer.allocateDirect(VERTEX.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(VERTEX);
        vertexBuffer.position(0);
        /**
         * index，顶点属性位置编号
         * size，单个属性包含几个数值，只能是 1/2/3/4，vec1 → 1 vec2（纹理坐标）→ 2 vec3（xyz 坐标）→ 3 vec4（rgba 颜色）→ 4
         * type，缓冲区内存放的数据类型 GLES20.GL_FLOAT 浮点（坐标、纹理最常用）
         * normalized，仅对整型数据生效，浮点数据固定写 false，忽略
         * stride，步长：相邻两个顶点之间相隔的总字节数。公式：单个顶点所有属性的 float 个数 × 4（1 个 float 占 4 字节），这里传0
         * offset，字节偏移：当前属性在单个顶点内部，从顶点开头跳过多少字节
         */
        //调用GLES20.glVertexAttribPointer()方法告诉opengl，他可以在缓冲区 vertexBuffer 中找 vPosition 对应的数据
        GLES20.glVertexAttribPointer(vPosition,2,GLES20.GL_FLOAT,false,0,vertexBuffer);
        //使用顶点
        GLES20.glEnableVertexAttribArray(vPosition);

        //片元坐标数据
        //纹理
        FloatBuffer textureBuffer = ByteBuffer.allocateDirect(TEXTURE.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(TEXTURE);
        textureBuffer.position(0);
        GLES20.glVertexAttribPointer(vCoord,2,GLES20.GL_FLOAT,false,0,textureBuffer);
        GLES20.glEnableVertexAttribArray(vCoord);
    }

    public static int glTexParameteris() {
        // 1. 创建纹理ID
        int[] textureIds = new int[1];
        GLES20.glGenTextures(1, textureIds, 0);
        if (textureIds[0] == 0) {
            Log.d("mmm", "纹理加载失败");
            return 0;
        }
        int texture = textureIds[0];

        return texture;
    }

    // 创建纹理，加载Bitmap
    public static int createTexture(Bitmap bitmap) {
        int[] texId = new int[1];
        GLES20.glGenTextures(1, texId,0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texId[0]);

        // 纹理参数
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

//        // bitmap传入纹理
//        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D,0,bitmap,0);
//        bitmap.recycle();
//
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,0);
        return texId[0];
    }
}
