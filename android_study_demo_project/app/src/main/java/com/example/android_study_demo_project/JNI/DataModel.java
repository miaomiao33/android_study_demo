package com.example.android_study_demo_project.JNI;
import android.graphics.PointF;
import android.graphics.Rect;
import java.util.Arrays;

public class DataModel {

    public Rect rect; // 其他类
    public PointF[] points; // 其它类数组
    public Inner inner; // 静态内部类

    public int id; // 整型
    public float score; // 浮点型
    public byte[] data; // 基本类型数组
    public int[][] doubleDimenArray; // 二维数组

    public static class Inner {
        public String message; // 字符串
    }

    @Override
    public String toString() {
        return "DataModel{" +
                "rect=" + rect +
                ", points=" + Arrays.toString(points) +
                (inner == null ? ", inner=null" : ", inner=" + inner.message) +
                ", id=" + id +
                ", score=" + score +
                ", data=" + Arrays.toString(data) +
                ", doubleDimenArray=" + Arrays.toString(doubleDimenArray) +
                '}';
    }
}
