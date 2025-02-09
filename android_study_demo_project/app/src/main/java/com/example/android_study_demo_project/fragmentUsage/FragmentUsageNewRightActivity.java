package com.example.android_study_demo_project.fragmentUsage;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.android_study_demo_project.R;

public class FragmentUsageNewRightActivity extends Fragment {
    View newRightView;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(newRightView != null)
        {
            //已经实例化过了
            ViewGroup parent = (ViewGroup)newRightView.getParent();
            if(parent != null)
            {
                parent.removeView(newRightView);
            }
        }else
        {
            newRightView = inflater.inflate(R.layout.activity_new_right_fragment,container,false);

        }
        return newRightView;
    }
}
