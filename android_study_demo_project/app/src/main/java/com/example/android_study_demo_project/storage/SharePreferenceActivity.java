package com.example.android_study_demo_project.storage;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.android_study_demo_project.R;

public class SharePreferenceActivity extends AppCompatActivity {
    EditText userIdEditText;
    EditText userPasswordEditText;
    Button loginButton;
    CheckBox rememberCheckBox;
    SharedPreferences pre;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sharepreference_usage);
        userIdEditText = (EditText) findViewById(R.id.et_user_id);
        userPasswordEditText = (EditText) findViewById(R.id.et_user_passWord);
        loginButton = (Button) findViewById(R.id.bt_share_preference_login);
        rememberCheckBox = (CheckBox) findViewById(R.id.cb_remember);
        pre = PreferenceManager.getDefaultSharedPreferences(SharePreferenceActivity.this);

        loginButton.setOnClickListener(view -> {
            String accountId = userIdEditText.getText().toString();
            String passWord = userPasswordEditText.getText().toString();
            if(accountId.isEmpty())
            {
                Toast.makeText(SharePreferenceActivity.this,"请输入账户名",Toast.LENGTH_SHORT).show();
            }else if(passWord.isEmpty())
            {
                Toast.makeText(SharePreferenceActivity.this,"请输入密码",Toast.LENGTH_SHORT).show();
            }else if(accountId.equals("10086")&&passWord.equals("123456"))
            {
                //判断是否点击了记住密码
                rememberPassword(rememberCheckBox.isChecked());
                //跳转到登录成功页面
                Intent intent = new Intent(SharePreferenceActivity.this,SharePreferenceLoginActivity.class);
                startActivity(intent);
            }else{
                Toast.makeText(SharePreferenceActivity.this,"登录失败",Toast.LENGTH_SHORT).show();
            }
        });

        //初始化
        initEditText();
    }

    //保存密码等信息
    private void rememberPassword(boolean isRemember)
    {
        SharedPreferences.Editor editor = pre.edit();
        String accountId = userIdEditText.getText().toString();
        String passWord = userPasswordEditText.getText().toString();
        if(isRemember)
        {
            editor.putBoolean("rememberPassword",true);
            editor.putString("accountId",accountId);
            editor.putString("password",passWord);
        }else {
            //取消记住就清空记住的信息
            editor.clear();
        }

        editor.apply();
    }

    //初始化账户名和密码状态
    private void initEditText()
    {
        boolean isRemember = pre.getBoolean("rememberPassword",false);
        if(isRemember)
        {
            String accountId = pre.getString("accountId",null);
            String passWord = pre.getString("password",null);
            rememberCheckBox.setChecked(true);
            userIdEditText.setText(accountId);
            userPasswordEditText.setText(passWord);
            userIdEditText.setSelection(accountId.length());
            userPasswordEditText.setSelection(passWord.length());
        }
    }
}
