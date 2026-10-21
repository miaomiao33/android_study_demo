
//设置精度，中等精度
precision mediump float;
//varying 可用于相互传值
varying vec2 ft_Position;
//2D 纹理，uniform 用于 application 向 gl 传值
uniform sampler2D sTexture;

void main() {
    gl_FragColor = texture2D(sTexture, ft_Position);
}