package com.example.android_study_demo_project.androidCleanArchitecture.CompositionRoot;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.android_study_demo_project.androidCleanArchitecture.Entities.User;
import com.example.android_study_demo_project.androidCleanArchitecture.UseCases_Interactor.GetUserUseCase;

import java.util.List;

public class UserListViewAdapter extends BaseAdapter {
    private List<User> users;
    private Context context;
    public UserListViewAdapter(GetUserUseCase userInteractor, Context context) {
        this.users = userInteractor.getUsers();
        this.context = context;
    }

    @Override
    public int getCount() {
        return users.size();
    }

    @Override
    public Object getItem(int position) {
        return users.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView textView = null;
        if(convertView == null)
        {
            textView = new TextView(context);
            textView.setPadding(16, 16, 16, 16); // 设置内边距
            textView.setTextSize(16); // 设置字体大小

            // 创建 SpannableString 并设置样式
            User user = users.get(position);
            String text = user.getName()+"--"+user.getId();
            SpannableString ss = new SpannableString(text);

            ss.setSpan(new ForegroundColorSpan(Color.RED),
                    0,
                    user.getName().length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ss.setSpan(new ForegroundColorSpan(Color.GREEN),
                    user.getName().length(),
                    text.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            textView.setText(ss);
        }else {
            textView = (TextView) convertView;
        }

        return textView;
    }
}
