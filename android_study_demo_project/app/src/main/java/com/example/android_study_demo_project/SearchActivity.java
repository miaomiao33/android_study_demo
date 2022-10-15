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

import com.amap.api.maps.AMapException;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Poi;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AmapNaviPage;
import com.amap.api.navi.AmapNaviParams;
import com.amap.api.navi.AmapNaviType;
import com.amap.api.navi.AmapPageType;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;

import java.util.ArrayList;
import java.util.List;


public class SearchActivity extends AppCompatActivity implements TextWatcher,Inputtips.InputtipsListener, RvListener {
    private EditText ediText;
    private androidx.recyclerview.widget.RecyclerView recyclerView;
    private MyRvAdapter rvAdapter;
    private List<Tip> searchResult = new ArrayList<Tip>();
    private Inputtips inputTips;
    private AMapNavi mapNavi;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

         ediText = findViewById(R.id.edit_query_search);
         ediText.addTextChangedListener(this);

         recyclerView = findViewById(R.id.recyclerview_search);
         recyclerView.setLayoutManager(new LinearLayoutManager(this));
         rvAdapter = new MyRvAdapter(SearchActivity.this,searchResult,this);
         recyclerView.setAdapter(rvAdapter);

         inputTips = new Inputtips(this, (InputtipsQuery)null);
         inputTips.setInputtipsListener(this);

         //导航
        try {
            mapNavi = AMapNavi.getInstance(this);
            Log.e("AMapException:","1");
        } catch (AMapException e) {
            e.printStackTrace();
            Log.e("AMapException:","3");
            Log.e("AMapException:",e.getErrorMessage());
        }
        Log.e("AMapException:","2");
        //设置内置语音播报
        mapNavi.setUseInnerVoice(true,false);


    }


    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
        //第二个参数传入null或者“”代表在全国进行检索，否则按照传入的city进行检索
        Log.d("searchtext",String.valueOf(charSequence));
        InputtipsQuery inputquery = new InputtipsQuery(String.valueOf(charSequence), "440100");
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



    @Override
    public void onItemClick(int id, int position, Tip location) {
        //点击item后
        LatLonPoint point = location.getPoint();
        Poi poi = new Poi(location.getName(),new LatLng(point.getLatitude(),point.getLongitude()),
                location.getPoiID());

        AmapNaviParams params = new AmapNaviParams(null,null,poi,
                AmapNaviType.DRIVER, AmapPageType.ROUTE);

            AmapNaviPage.getInstance().showRouteActivity(getApplicationContext(),params,null);
    }
}
