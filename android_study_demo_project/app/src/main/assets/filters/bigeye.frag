//大眼效果片元着色器
//数据精度 lowp、highp
precision mediump float;

varying vec2 aCoord;

//2D纹理采样器（代表一层纹理）
uniform sampler2D vTexture;

uniform float a;//缩放系数，0无缩放，大于0则放大
uniform vec2 left_eye;//左眼中心点
uniform vec2 right_eye;//右眼中心点

varying vec2 texSizeFrag; // 纹理宽高像素

//coord = 当前要采样的点，eye = 眼睛点，rmax = 最大作用半径
vec2 newCoord(vec2 coord, vec2 eye, float rmax)
{
    vec2 p = coord;
    //获得当前点和眼睛中心点距离
    float r = distance(coord, eye);
    //在rmax范围内 需要缩放
    // 极小距离跳过，避免 0/0 NaN 黑点
    if( r < rmax && r > 0.0001)
    {
        //获得缩放后的距离
        float fsr = 1.0 - pow(r/rmax - 1.0, 2.0) * a;//*r和下面的fsr/r抵消了
        //fsr是个float 是距离eye中心点距离
        //根据比例关系
        //新点与eye的向量差 / 老点与eye的差 = 新点与eye的距离 / 老点与eye的距离
        //(newCoord - eye) / (coord - eye) = fsr / r
        //newCoord - eye = (fsr / r ) * (coord - eye)
        p = fsr  * (coord - eye) + eye;
    }

    return p;
}

void main()
{
    //最大作用半径
    float rmax = distance(left_eye, right_eye) / 2.;
    //p 不是单个像素点，是当前这个片元像素对应的采样坐标；两只眼睛要分别扭曲，必须叠加两次形变，不能各自独立算
    //某个像素刚好在两眼中间重叠区域：两次都会拉伸，双重放大。
    vec2 p = newCoord(aCoord, left_eye, rmax);
    //再次newCoord防止是在右眼放大范围内情况(注意p是像素坐标，gl_FragColor用的是归一化坐标)
    // 关键：传入上一步扭曲后的p，叠加右眼效果
    p = newCoord(p, right_eye, rmax);

    //采集vTexture（画布/图片）对应位置的像素并赋值给gl_FragColor
    // 像素坐标转回归一化UV用于纹理采样
    vec2 sampleUV = p / texSizeFrag;
    gl_FragColor = texture2D(vTexture, sampleUV);
}