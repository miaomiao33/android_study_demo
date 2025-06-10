package com.example.android_study_demo_project.espressoUsage;

import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EspressoRecyclerViewAdapter extends
        RecyclerView.Adapter<EspressoRecyclerViewAdapter.EspressoRecyclerViewHolder> {

    private List<String> dataList;
    private EspressoRecyclerViewClickCallBack callBack;

    public EspressoRecyclerViewAdapter(List<String> dataList,EspressoRecyclerViewClickCallBack callBack) {
        this.dataList = dataList;
        this.callBack = callBack;
    }

    @NonNull
    @Override
    public EspressoRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TextView textView = new TextView(parent.getContext());
//        textView = new TextView(context);
        textView.setPadding(16, 16, 16, 16); // 设置内边距
        textView.setTextSize(16); // 设置字体大小

        return new EspressoRecyclerViewHolder(textView);
    }

    @Override
    public void onBindViewHolder(@NonNull EspressoRecyclerViewHolder holder, int position) {
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
        holder.textView.setText(spannableString);
        holder.textView.setMovementMethod(LinkMovementMethod.getInstance()); // 启用点击事件

    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public static class EspressoRecyclerViewHolder extends RecyclerView.ViewHolder{
        TextView textView;
        public EspressoRecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            //整个itemView就一个textView
            this.textView = (TextView) itemView;
        }
    }
}
