package com.example.android_study_demo_project.fragmentUsage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.android_study_demo_project.R;

public class FragmentUsageRightActivity extends Fragment {
    View rightView;
    NetworkChangeReceiver networkChangeReceiver;
    TextView textView;
    int count = 0;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(rightView != null)
        {
            //已经实例化过了
            ViewGroup parent = (ViewGroup)rightView.getParent();
            if(parent != null)
            {
                parent.removeView(rightView);
            }
        }else
        {
            rightView = inflater.inflate(R.layout.activity_right_fragment,container,false);
            textView = (TextView)rightView.findViewById(R.id.tv_broadcast_info);
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            intentFilter.addAction("com.example.broadcast.TEST");
            networkChangeReceiver = new NetworkChangeReceiver();
            //注册监听
            getActivity().getApplicationContext().registerReceiver(networkChangeReceiver,intentFilter);
        }
        return rightView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().getApplicationContext().unregisterReceiver(networkChangeReceiver);
    }

    //初始化广播接收器
    public class NetworkChangeReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action){
                case "com.example.broadcast.TEST":
                    count++;
                    textView.setText("收到广播次数"+count);
                    break;
                case "android.net.conn.CONNECTIVITY_CHANGE":
                    Toast.makeText(context,"网络变化",Toast.LENGTH_SHORT).show();
                    break;
            }

        }
    }
}
