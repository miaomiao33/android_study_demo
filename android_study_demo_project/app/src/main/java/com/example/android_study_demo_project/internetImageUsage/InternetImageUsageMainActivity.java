package com.example.android_study_demo_project.internetImageUsage;

import android.content.ContentValues;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
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
import com.qiniu.android.common.FixedZone;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.Configuration;
import com.qiniu.android.storage.FileRecorder;
import com.qiniu.android.storage.Recorder;
import com.qiniu.android.storage.UpCancellationSignal;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UpProgressHandler;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.android.storage.UploadOptions;
import com.qiniu.android.utils.Utils;
import com.qiniu.util.Auth;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private static String QINIU_IMAGE_HEAD = "http://ssuldoa98.hn-bkt.clouddn.com/";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_internet_image_usage_main);

        recyclerView = (RecyclerView)findViewById(R.id.rv_internet_image_usage);
        postButton = (Button) findViewById(R.id.bt_post_internet_image);
        getButton = (Button) findViewById(R.id.bt_insert_internet_image);
        InternetImageUsageMainActivityClick activityClick =
                new InternetImageUsageMainActivityClick();
        postButton.setOnClickListener(activityClick);
        getButton.setOnClickListener(activityClick);

        modelList = new ArrayList<>();
        staggeredGridLayoutManager =
                new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);

        recyclerView.setLayoutManager(staggeredGridLayoutManager);
        recyclerViewAdapter = new InternetImageRecyclerViewAdapter(modelList,this,recyclerView);
        recyclerViewAdapter.setOnItemClickListener(new InternetImageRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(InternetImageModel model,int position) {
                //点击了某个model
                List<InternetImageModel> deleteModelList = new ArrayList<>();
                deleteModelList.add(model);
                deleteDataBase(deleteModelList);
                refreshModelList(false,position);//刷新
            }
        });
        recyclerView.setAdapter(recyclerViewAdapter);

        //初始化数据
        initModelList();//读取存储可能会慢于页面创建，需要刷新
    }

    //初始化数据
    void initModelList()
    {
        //从数据库中读取数据
        dataBaseHelper = InternetImageDataBaseHelper.getInstance(this);
        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        if(db.isOpen())
        {
            Cursor cursor = db.query(InternetImageDataBaseHelper.getTableName(),null,
                    null,null,null,null,null);
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
        recyclerViewAdapter.notifyItemRangeChanged(0,modelList.size());
    }

    /**
     * 刷新数据
     * @param isInsert 是否时插入刷新
     * @param position 操作的位置
     */
    void refreshModelList(boolean isInsert,int position)
     {
        //从数据库中读取数据
         //相当于模拟后端请求数据
        dataBaseHelper = InternetImageDataBaseHelper.getInstance(this);
        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        if(db.isOpen())
        {
            Cursor cursor = db.query(InternetImageDataBaseHelper.getTableName(),null,
                    null,null,null,null,null);
            int size = modelList.size();
            modelList.clear();
            recyclerViewAdapter.notifyItemRangeRemoved(0, size);
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
        if(isInsert == false)
        {
            //删除刷新
            recyclerViewAdapter.notifyItemRemoved(position);
        }else
        {
            //插入刷新
            recyclerViewAdapter.notifyItemRangeChanged(position,modelList.size());
        }
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
                case R.id.bt_insert_internet_image:
                    internetDefaultImage();//插入六条预定图片
                    break;
            }
        }
    }

    //上传图片
    void postInternetImage()
    {
        //到相册选择图片
        String dataPath = getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath(); //要上传的文件路径
        File file = new File(dataPath);
        File[] fileList = file.listFiles();
        String name = null;
        String filePath = null;
        for (File fileItem:fileList) {
            name = fileItem.getName();
            if(name.endsWith(".jpg"))
            {
                filePath = fileItem.getAbsolutePath();
                Log.i("ImagePath--->",fileItem.getAbsolutePath());
                break;
            }
        }

        uploadImageToQiNiu(filePath, new CallBackParameter() {
            @Override
            public Void CallBackURL(String url) {
                //上传成功插入返回的URL
                InternetImageModel model = new InternetImageModel(url);
                List<InternetImageModel> dataList = Arrays.asList(model);
                Log.i("internetImageURL--->",model.getUrl());
                insertDataBase(dataList);
                refreshModelList(true,modelList.size());//刷新
                return null;
            }
        });
    }

    //插入五条预定图片
    void internetDefaultImage()
    {
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
        insertDataBase(dataList);
        refreshModelList(true,modelList.size());
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

    //删除杂乱的数据
    void deleteDataBase(List<InternetImageModel> modelList)
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
                        db.delete(InternetImageDataBaseHelper.getTableName(),
                                "id=?",new String[]{String.valueOf(model.getId())});
                    }
                }
            }
            db.close();
        }
        Toast.makeText(this,"删除成功",Toast.LENGTH_SHORT).show();
        dataBaseHelper.close();
    }

    //将本地文件上传到七牛
    protected void uploadImageToQiNiu(String path,CallBackParameter callBackURL)
    {
        //指定zone的具体区域
        //FixedZone.zone0   华东机房
        //FixedZone.zone1   华北机房
        //FixedZone.zone2   华南机房
        //FixedZone.zoneNa0 北美机房

        // 文件分片上传时断点续传信息保存
        Recorder recorder = null;
        try {
            recorder = new FileRecorder(Utils.sdkDirectory() + "/recorder");
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 指定上传区域为 华南-广东
        FixedZone zone = (FixedZone) FixedZone.zone2;
        Configuration configuration = new Configuration.Builder()
                .zone(zone)                         // 配置上传区域
                .putThreshold(4 * 1024 * 1024) // 分片上传阈值：4MB，大于 4MB 采用分片上传，小于 4MB 采用表单上传
                .useConcurrentResumeUpload(true)   // 开启分片上传
                .recorder(recorder)  // 文件分片上传时断点续传信息保存，表单上传此配置无效
                .resumeUploadVersion(Configuration.RESUME_UPLOAD_VERSION_V2) // 使用分片 V2
                .build();
        UploadManager uploadManager = new UploadManager(configuration);; // UploadManager对象只需要创建一次重复使用

        //监测上传进度的
        UploadOptions options = new UploadOptions(null, null, true,
                new UpProgressHandler() {
                    @Override
                    public void progress(String key, double percent) {
                        // 上传进度
                    }
                }, new UpCancellationSignal() {
            @Override
            public boolean isCancelled() {
                // 当需要取消时，此处返回 true，SDK 内部会多次检查返回值，当返回值为 true 时会取消上传操作
                return false;
            }
        });


        /**
         * 生成token
         * create()方法的两个参数分别是 AK SK
         * uoloadToken()方法的参数是 要上传到的空间(bucket)
         */
        String uploadToken = "";// 上传的 Token
        try {
            //从AndroidManifest的meta-data获取配置的对应key
            PackageManager packageManager = getPackageManager();
            ApplicationInfo applicationInfo = packageManager.
                    getApplicationInfo(getPackageName(),packageManager.GET_META_DATA);
            String accessKey = applicationInfo.metaData.getString("QiNiu_accessKey");
            String secretKey = applicationInfo.metaData.getString("QiNiu_secretKey");
            String bucket = applicationInfo.metaData.getString("QiNiu_bucket");
            //生成token
            uploadToken = Auth.create(accessKey,
                    secretKey)
                    .uploadToken(bucket);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        Log.i("ImagePath,token--->",uploadToken);

        /**
         * 调用put方法上传
         * 第一个参数 data：可以是字符串，是要上传图片的路径
         *                可以是File对象，是要上传的文件
         *                可以是byte[]数组，要是上传的数据
         * 第二个参数 key：字符串，是图片在服务器上的名称，要具有唯一性，可以用UUID
         * 第三个参数 token：根据开发者的 AK和SK 生成的token，这个token 应该在后端提供一个接口，然后android代码中发一个get请求获得这个tocken，但这里为了演示，直接写在本地了.
         * 第四个参数：UpCompletionHandler的实例，有个回调方法
         * 第五个参数：可先参数
         */
        //年-月-日-时-分-秒格式传输图片
        LocalDateTime nowTime = LocalDateTime.now();
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");

        String filePath = path;    // 文件路径
        String key = nowTime.format(formatter2)+".jpg";         //在服务器的文件名 文件 key
        uploadManager.put(filePath, key, uploadToken, new UpCompletionHandler() {
            @Override
            public void complete(String key, ResponseInfo info, JSONObject response) {
                if (info != null && info.isOK()) {
                    // 上传成功
                    Log.i("uploadManager info--->",info.toString());
                    Log.i("uploadManager response--->", String.valueOf(response));
                    try {
                        String internetImageURL = QINIU_IMAGE_HEAD + response.get("key");
                        callBackURL.CallBackURL(internetImageURL);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    // 上传失败
                    Log.i("ImagePath error ResponseInfo--->",info.toString());
                }
            }
        }, options);
    }

    //用于内部类返回参数
    interface CallBackParameter{
        Void CallBackURL(String url);
    }
}
