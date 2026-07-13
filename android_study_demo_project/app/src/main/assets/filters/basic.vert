//顶点着色器
//attribute 属性变量，只能用于顶点着色器中
//含四个浮点型数据的向量 顶点坐标 物体形状组成的点
attribute vec4 vPosition;
//纹理坐标 从图片上采集像素的位置 根据vPosition形状对应的采集图片像素的点
attribute vec2 vCoord;
//易变变量 通过 vCoord 传递给fragment varying修饰的变量才能传值
varying vec2 aCoord;

// 纹理宽高，用来把0~1UV转像素坐标
uniform vec2 texSize;

varying vec2 texSizeFrag; // 纹理宽高（像素）

void main()
{
    // vCoord是转化好的归一化坐标（0-1）
//    aCoord = vCoord ;

    // UV(0~1) 转像素坐标
    aCoord = vCoord * texSize;
    texSizeFrag = texSize;

    //gl_Position是着色器内部变量，只要将坐标给它opengl就会自动处理
    //gl_Position虚拟坐标化
    gl_Position = vPosition;
}