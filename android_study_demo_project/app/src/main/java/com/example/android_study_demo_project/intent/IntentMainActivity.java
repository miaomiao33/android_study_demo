package com.example.android_study_demo_project.intent;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.android_study_demo_project.R;

public class IntentMainActivity extends AppCompatActivity {
    private Button URLButton;
    private Button dialPadButton;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intent);
        //获取传递的数据
        Intent intent = getIntent();
        //方法1
//        String data = intent.getStringExtra("data");
//        int number = intent.getIntExtra("number",0);

        //方法2
        String data = intent.getStringExtra("data");
        int number = intent.getIntExtra("number",0);//没有就是默认值0

        Toast.makeText(IntentMainActivity.this,data+number,Toast.LENGTH_SHORT).show();

        //访问网址和吊起拨号键盘
        URLButton = (Button)findViewById(R.id.bt_visitURL);
        dialPadButton = (Button)findViewById(R.id.bt_evoke_dial_pad);
        URLButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                visitURL("http://www.baidu.com");
            }
        });

        dialPadButton.setOnClickListener(view -> {
            evokeDialPad("10086");
        });
    }

    //访问URL
    public void visitURL(String url)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    //唤起拨号键盘并输入phoneNumber
    public void evokeDialPad(String phoneNumber)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("tel:"+phoneNumber));
        startActivity(intent);
    }
}
