package com.example.android_study_demo_project;
/*
使用高德poi进行搜索
 */

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;

import java.util.ArrayList;
import java.util.List;


public class SearchActivity extends AppCompatActivity implements TextWatcher,Inputtips.InputtipsListener  {
    private EditText ediText;
    private androidx.recyclerview.widget.RecyclerView recyclerView;
    private MyRvAdapter rvAdapter;
    private List<Tip> searchResult = new ArrayList<Tip>();
    private Inputtips inputTips;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

         ediText = findViewById(R.id.edit_query_search);
         ediText.addTextChangedListener(this);

         recyclerView = findViewById(R.id.recyclerview_search);
         recyclerView.setLayoutManager(new LinearLayoutManager(this));
         rvAdapter = new MyRvAdapter(SearchActivity.this,searchResult);
         recyclerView.setAdapter(rvAdapter);

         inputTips = new Inputtips(this, (InputtipsQuery)null);
         inputTips.setInputtipsListener(this);
    }


    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
        //第二个参数传入null或者“”代表在全国进行检索，否则按照传入的city进行检索
        Log.d("searchtext",String.valueOf(charSequence));
        InputtipsQuery inputquery = new InputtipsQuery(String.valueOf(charSequence), "023");
        inputquery.setCityLimit(true);//限制在当前城市
        inputTips.setQuery(inputquery);
        inputTips.requestInputtipsAsyn();
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    @Override
    public void onGetInputtips(List<Tip> list, int i) {
        //获取到高德poi的数据
        rvAdapter.setData(list);
    }
}
