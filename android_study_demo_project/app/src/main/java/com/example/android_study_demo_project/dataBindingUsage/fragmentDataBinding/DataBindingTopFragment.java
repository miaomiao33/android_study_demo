package com.example.android_study_demo_project.dataBindingUsage.fragmentDataBinding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.android_study_demo_project.databinding.FragmentItemDatabindingTopBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataBindingTopFragment extends Fragment {
    FragmentItemDatabindingTopBinding binding;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(binding == null)
        {
            //没有实例化
            //把顶部的FragmentItemDataBindingTopBinding的view返回给顶部的fragment
            binding = FragmentItemDatabindingTopBinding.inflate(inflater, container, false);
            String[] array = {"这是array第一项"};
            List<String> list = new ArrayList<>();
            list.add("这是list第一个");
            Map<String,String> map = new HashMap<>();
            map.put("mapKey","这是map的value");
            binding.setArray(array);
            binding.setList(list);
            binding.setMap(map);
            binding.setKey("mapKey");
            binding.setIndex(0);
        }
        return binding.getRoot();
    }
}
