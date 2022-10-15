package com.example.android_study_demo_project;


import com.amap.api.services.help.Tip;

//RecyclerView的item点击事件
public interface RvListener {
    void onItemClick(int id, int position, Tip location);
}