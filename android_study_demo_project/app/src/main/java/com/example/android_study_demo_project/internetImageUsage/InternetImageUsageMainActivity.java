package com.example.android_study_demo_project.internetImageUsage;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.android_study_demo_project.R;
import com.example.android_study_demo_project.internetImageUsage.model.InternetImageModel;
import com.example.android_study_demo_project.internetImageUsage.storage.InternetImageDataBaseHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 图片的上传和加载网络图片
 */
public class InternetImageUsageMainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    InternetImageRecyclerViewAdapter recyclerViewAdapter;
    StaggeredGridLayoutManager staggeredGridLayoutManager;
    private Button postButton;
    private Button getButton;
    private List<InternetImageModel> modelList;
    InternetImageDataBaseHelper dataBaseHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_internet_image_usage_main);

        recyclerView = (RecyclerView)findViewById(R.id.rv_internet_image_usage);
        postButton = (Button) findViewById(R.id.bt_post_internet_image);
        getButton = (Button) findViewById(R.id.bt_get_internet_image);
        InternetImageUsageMainActivityClick activityClick =
                new InternetImageUsageMainActivityClick();
        postButton.setOnClickListener(activityClick);
        getButton.setOnClickListener(activityClick);

        modelList = new ArrayList<>();
        staggeredGridLayoutManager =
                new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);

        recyclerView.setLayoutManager(staggeredGridLayoutManager);
        recyclerViewAdapter = new InternetImageRecyclerViewAdapter(modelList,this);
        recyclerView.setAdapter(recyclerViewAdapter);


        //初始化数据
        refreshModelList();//读取存储可能会慢于页面创建，需要刷新
    }

    //刷新数据
    void refreshModelList()
    {
        //从数据库中读取数据
        dataBaseHelper = InternetImageDataBaseHelper.getInstance(this);
        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        if(db.isOpen())
        {
            Cursor cursor = db.query(InternetImageDataBaseHelper.getTableName(),null,
                    null,null,null,null,null);
            modelList.clear();
            if(cursor.moveToFirst())
            {
                do{
                    InternetImageModel model = new InternetImageModel();
                    model.setId(cursor.getInt(cursor.getColumnIndex("id")));
                    model.setUrl(cursor.getString(cursor.getColumnIndex("url")));
                    modelList.add(model);
                }while (cursor.moveToNext());
            }
            cursor.close();
        }

        db.close();
        dataBaseHelper.close();
        recyclerViewAdapter.notifyItemInserted(modelList.size());
    }

    //点击按钮处理
    private class InternetImageUsageMainActivityClick implements View.OnClickListener{
        @Override
        public void onClick(View view) {
            switch (view.getId())
            {
                case R.id.bt_post_internet_image:
                    postInternetImage();//上传图片
                    break;
                case R.id.bt_get_internet_image:
                    getInternetImage();//获取图片
                    break;
            }
        }
    }

    //上传图片
    void postInternetImage()
    {
        //上传成功写入本地数据库
        InternetImageModel model = new InternetImageModel(
                "https://img-s-msn-com.akamaized.net/tenant/amp/entityid/AA1zpU0h.img?w=768&h=578&m=6");
        InternetImageModel model2 = new InternetImageModel(
                "https://img-s-msn-com.akamaized.net/tenant/amp/entityid/AA1zq7PC.img?w=768&h=547&m=6");
        InternetImageModel model3 = new InternetImageModel(
                "https://img-s-msn-com.akamaized.net/tenant/amp/entityid/AA1zpW2x.img?w=768&h=547&m=6");
        InternetImageModel model4 = new InternetImageModel(
                "https://img-s-msn-com.akamaized.net/tenant/amp/entityid/AA1zpW2A.img?w=768&h=547&m=6");
        InternetImageModel model5 = new InternetImageModel(
                "https://img-s-msn-com.akamaized.net/tenant/amp/entityid/AA1zpW2E.img?w=768&h=547&m=6");
        InternetImageModel model6 = new InternetImageModel(
                "https://img-s-msn-com.akamaized.net/tenant/amp/entityid/AA1zpRPj.img?w=768&h=467&m=6");
        List<InternetImageModel> dataList = Arrays.asList(model,model2,model3,model4,model5,model6);
        Log.i("dataList:",dataList.size()+"---"+dataList.toString());
        insertDataBase(dataList);
        refreshModelList();//刷新
    }

    //获取图片
    void getInternetImage()
    {
        refreshModelList();
    }

    //上传成功写入本地数据库
    void insertDataBase(List<InternetImageModel> modelList)
    {
        dataBaseHelper = InternetImageDataBaseHelper.getInstance(this);
        if(modelList != null)
        {
            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
            for (InternetImageModel model:modelList) {
                if(model == null ||model.getUrl() == null)
                {
                    Toast.makeText(this,"图片对象或者图片URL获取失败",Toast.LENGTH_SHORT).show();
                }else
                {

                    if(db.isOpen())
                    {
                        ContentValues values = new ContentValues();
                        //插入数据
                        values.put("url",model.getUrl());
                        db.insert(InternetImageDataBaseHelper.getTableName(),null,values);
                    }
                }
            }
            db.close();
        }
        Toast.makeText(this,"写入成功",Toast.LENGTH_SHORT).show();
        dataBaseHelper.close();
    }
}
