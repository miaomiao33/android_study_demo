package com.example.android_study_demo_project;

/*
显示搜索结果adapter
 */
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amap.api.services.help.Tip;

import java.util.ArrayList;
import java.util.List;

public class MyRvAdapter extends RecyclerView.Adapter<MyViewHoder> {
    private  Context searchActivity;
    private List<Tip> searchResultList =  new ArrayList<Tip>();
    public MyRvAdapter(Context activity,List<Tip> list) {
        searchActivity = activity;
        searchResultList = list;
    }

    @NonNull
    @Override
    public MyViewHoder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = View.inflate(searchActivity, R.layout.search_item, null);
        MyViewHoder myViewHoder = new MyViewHoder(view);
        return myViewHoder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHoder holder, int position) {

        holder.searchInfo.setText(searchResultList.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return searchResultList.size();
    }

    public void setData(List<Tip> list) {
        this.searchResultList.clear();
        Log.d("searchData","12");
        if(list == null || list.isEmpty())
        {
            Log.d("searchData","null");
            notifyDataSetChanged();//刷新数据
            return;
        }
        Log.d("searchData","123");
        this.searchResultList.addAll(list);
        notifyDataSetChanged();//刷新数据
    }
}

class MyViewHoder extends RecyclerView.ViewHolder {
    TextView searchInfo;

    public MyViewHoder(@NonNull View itemView) {
        super(itemView);
        searchInfo = itemView.findViewById(R.id.textView);
    }
}

