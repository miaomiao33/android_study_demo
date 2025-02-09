package com.example.android_study_demo_project.fragmentUsage;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.android_study_demo_project.R;

public class FragmentUsageLeftActivity extends Fragment {
    View leftView;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if(leftView != null)
        {
            //已经实列化过了
            ViewGroup parent = (ViewGroup) leftView.getParent();
            if(parent != null)
            {
                parent.removeView(leftView);
            }

        }else
        {
            leftView = inflater.inflate(R.layout.activity_left_fragment, container,false);
            Button sendBroadcastButton = leftView.findViewById(R.id.bt_sendBroadcast);
            Button changeFragmentButton = leftView.findViewById(R.id.bt_changeFragment);
            sendBroadcastButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //发送Broadcast
                    Intent intent = new Intent("com.example.broadcast.TEST");
                    //android8.0之后要求设置范围
                    intent.setPackage(getActivity().getApplicationContext().getPackageName());
                    getActivity().getApplicationContext().sendBroadcast(intent);
                }
            });

            changeFragmentButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //修改右边fragment
                    replaceFragment(new FragmentUsageNewRightActivity());
                }
            });
        }

        return leftView;
    }

    //替换FrameLayout
    private void replaceFragment(Fragment fragment)
    {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fl_replace_fragment,fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

}
