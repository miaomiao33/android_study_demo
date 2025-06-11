package com.example.android_study_demo_project.dataBindingUsage.fragmentDataBinding;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.android_study_demo_project.R;
import com.example.android_study_demo_project.databinding.ActivityDatabindingUsageBinding;
import com.example.android_study_demo_project.databinding.ActivityFragementDatabindingBinding;

public class FragmentDataBindingActivity extends AppCompatActivity {
    private ActivityFragementDatabindingBinding binding;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_fragement_databinding);
        binding.btChangeBottomFragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fl_databinding_usage_bottom,new DataBindingBottomFragment());
                fragmentTransaction.commit();
            }
        });
    }
}
