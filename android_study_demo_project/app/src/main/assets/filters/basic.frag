//片元着色器
//数据精度 lowp、highp
precision mediump float;
varying vec2 aCoord;

//2D纹理采样器（代表一层纹理）
uniform sampler2D vTexture;

void main()
{
    //采集vTexture（画布/图片）的aCoord位置的像素并赋值给gl_FragColor
    //这里传入的aCoord是归一化坐标（0-1）
    gl_FragColor = texture2D(vTexture, aCoord);
}