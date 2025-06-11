package com.example.android_study_demo_project.dataBindingUsage.fragmentDataBinding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.android_study_demo_project.databinding.FragmentItemDatabindingBottomBinding;

public class DataBindingBottomFragment extends Fragment {
    private FragmentItemDatabindingBottomBinding binding;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(binding == null)
        {
            //没有实例化才进行实例化
            binding = FragmentItemDatabindingBottomBinding.inflate(inflater,container,false);
        }
        return binding.getRoot();
    }
}
