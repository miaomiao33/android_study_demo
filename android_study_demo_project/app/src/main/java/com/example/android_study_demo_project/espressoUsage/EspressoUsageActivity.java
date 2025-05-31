package com.example.android_study_demo_project.espressoUsage;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android_study_demo_project.R;

import java.util.Arrays;
import java.util.List;

public class EspressoUsageActivity extends AppCompatActivity {
    private TextView textView;
    private Button changeButton;
    private ListView listView;
    private List<String> dataList = Arrays.asList("item1","item2",
            "item3","item4","item5","item6","item7","item8","item9","item10");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_espresso_usage);

        initView();
    }

    //初始化
    void initView()
    {
        textView = (TextView) findViewById(R.id.tv_espresso_text);
        changeButton = (Button) findViewById(R.id.bt_change_espresso_text);
        listView = (ListView) findViewById(R.id.lv_espresso_listview);


        EspressoListViewAdapter adapter = new EspressoListViewAdapter(
                dataList, EspressoUsageActivity.this, new EspressoListViewClickCallBack() {
            @Override
            public void CallBack(View view, String text) {
                //点击了ListView中的item
                textView.setText(text);
            }
        });
        listView.setAdapter(adapter);
        EspressoUsageClick click = new EspressoUsageClick();
        changeButton.setOnClickListener(click);
    }


    class EspressoUsageClick implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId())
            {
                case R.id.bt_change_espresso_text:
                {
                    textView.setText("Hello Espresso");
                }
            }
        }
    }
}