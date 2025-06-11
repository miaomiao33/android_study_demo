package com.example.android_study_demo_project.dataBindingUsage.recyclerViewDataBinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android_study_demo_project.MainActivity;
import com.example.android_study_demo_project.R;
import com.example.android_study_demo_project.databinding.ItemRecyclerviewDatabindingBinding;

import java.util.List;

public class RecyclerViewDataBindingAdapter extends RecyclerView.Adapter
        <RecyclerViewDataBindingAdapter.MyRecyclerViewDataBindingViewHolder> {
    List<String> dataList;

    public RecyclerViewDataBindingAdapter(List<String> dataList) {
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public MyRecyclerViewDataBindingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRecyclerviewDatabindingBinding itemDataBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),//获取一个 LayoutInflater 实例，以便将 XML 布局文件解析为对应的视图对象
                R.layout.item_recyclerview_databinding,
                parent,
                false);//指定父视图容器。如果需要将生成的视图直接添加到父容器中，则可以设置为 true；否则设置为 false
        MyRecyclerViewDataBindingViewHolder viewHolder =
                new MyRecyclerViewDataBindingViewHolder(itemDataBinding.getRoot());
        viewHolder.itemDataBindingBinding = itemDataBinding;
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyRecyclerViewDataBindingViewHolder holder, int position) {
        String dataText = dataList.get(position);
        holder.itemDataBindingBinding.tvRecyclerviewDatabindingItemText.setText(dataText);
    }


    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public static class MyRecyclerViewDataBindingViewHolder extends RecyclerView.ViewHolder{
        ItemRecyclerviewDatabindingBinding itemDataBindingBinding;
        public MyRecyclerViewDataBindingViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
