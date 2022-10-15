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

public class MyRvAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private  Context searchActivity;
    private List<Tip> searchResultList =  new ArrayList<Tip>();
    private  RvListener mRvListener;
    public MyRvAdapter(Context activity,List<Tip> list, RvListener rvListener) {
        searchActivity = activity;
        searchResultList = list;
        mRvListener = rvListener;
    }

    @NonNull
    @Override
    public MyViewHoder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = View.inflate(searchActivity, R.layout.search_item, null);
        MyViewHoder myViewHoder = new MyViewHoder(view);
        return myViewHoder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((MyViewHoder) holder).searchInfo.setText(searchResultList.get(position).getName());
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

   private class MyViewHoder extends RecyclerView.ViewHolder {
        TextView searchInfo;

        public MyViewHoder(@NonNull View itemView) {
            super(itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mRvListener.onItemClick(view.getId(), getAdapterPosition(), searchResultList.get(getAdapterPosition()));
                }
            });
            Log.e("AMapException:","345");
            searchInfo = itemView.findViewById(R.id.textView);
        }
    }

}



