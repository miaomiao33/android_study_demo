//顶点着色器
//attribute 属性变量，只能用于顶点着色器中
//含四个浮点型数据的向量 顶点坐标 物体形状组成的点
attribute vec4 vPosition;
//纹理坐标 从图片上采集像素的位置 根据vPosition形状对应的采集图片像素的点
attribute vec2 vCoord;
//易变变量 通过 vCoord 传递给fragment varying修饰的变量才能传值
varying vec2 aCoord;

void main()
{
    aCoord = vCoord;
    //gl_Position是着色器内部变量，只要将坐标给它opengl就会自动处理
    //gl_Position虚拟坐标化
    gl_Position = vPosition;
}