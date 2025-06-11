package com.example.android_study_demo_project.dataBindingUsage.recyclerViewDataBinding;

import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android_study_demo_project.R;
import com.example.android_study_demo_project.databinding.ActivityRecyclerviewDatabindingUsageBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RecyclerViewDataBindingUsageActivity extends AppCompatActivity {
    private ActivityRecyclerviewDatabindingUsageBinding activityBinding;
    private RecyclerView recyclerView;
    private List<String> recyclerViewDataList = new ArrayList<>();
    private RecyclerViewDataBindingAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityBinding = DataBindingUtil.setContentView(
                        RecyclerViewDataBindingUsageActivity.this,
                        R.layout.activity_recyclerview_databinding_usage);
        initView();
    }

    void initView()
    {
        for(int i = 0;i < 10;i++)
        {
            recyclerViewDataList.add("item"+i);
        }
        RecyclerViewDataBindingPresenter presenter = new RecyclerViewDataBindingPresenter();
        activityBinding.setRecyclerViewDataBindingPresenter(presenter);

        recyclerView = activityBinding.rvRecyclerViewDataBinding;
        adapter = new RecyclerViewDataBindingAdapter(recyclerViewDataList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(activityBinding.getRoot().getContext()));
    }

    public class RecyclerViewDataBindingPresenter{
        public void addPerson()
        {
            //添加人数
            int size = recyclerViewDataList.size();
            recyclerViewDataList.add("itemAdd");
            adapter.notifyItemRangeChanged(size-1,size);
        }
    }
}
