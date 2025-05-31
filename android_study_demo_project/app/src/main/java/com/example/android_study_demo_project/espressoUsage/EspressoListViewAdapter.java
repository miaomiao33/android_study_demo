package com.example.android_study_demo_project.espressoUsage;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.List;

public class EspressoListViewAdapter extends BaseAdapter {

    private List<String> dataList;
    private final Context context;
    private final EspressoListViewClickCallBack callBack;
    public EspressoListViewAdapter(List<String> dataList,Context context
            ,EspressoListViewClickCallBack callBack) {
        this.dataList = dataList;
        this.context = context;
        this.callBack = callBack;
    }

    @Override
    public int getCount() {
        // 返回数据源的大小
        return dataList.size();
    }

    @Override
    public Object getItem(int position) {
        // 返回指定位置的数据对象
        return dataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        // 返回位置作为唯一标识符
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //生成或复用视图以显示数据项。position 表示当前项的位置
        // convertView 是可复用的视图（如果存在），parent 是包含视图的父容器
        TextView textView;
        if (convertView == null) {
            textView = new TextView(context);
            textView.setPadding(16, 16, 16, 16); // 设置内边距
            textView.setTextSize(16); // 设置字体大小

            // 创建 SpannableString 并设置样式
            String text = dataList.get(position);
            SpannableString spannableString = new SpannableString(text);

            // 设置部分文本的颜色
            spannableString.setSpan(new ForegroundColorSpan(Color.RED),
                    0,
                    text.length() / 2,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            // 设置部分文本的点击事件
            spannableString.setSpan(new ClickableSpan() {
                @Override
                public void onClick(@NonNull View view) {
                    //点击了
                    callBack.CallBack(view,text);
                }
            }, text.length() / 2, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            // 将 SpannableString 设置到 TextView 中
            textView.setText(spannableString);
            textView.setMovementMethod(LinkMovementMethod.getInstance()); // 启用点击事件
        } else {
            // 如果 convertView 不为空，则复用已有视图
            textView = (TextView) convertView;
        }

        return textView;
    }
}

interface EspressoListViewClickCallBack{
    void CallBack(View view,String text);
}

