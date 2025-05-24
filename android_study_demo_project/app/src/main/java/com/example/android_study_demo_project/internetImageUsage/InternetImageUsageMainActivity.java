package com.example.android_study_demo_project.internetImageUsage;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.android_study_demo_project.R;
import com.example.android_study_demo_project.internetImageUsage.control.PermissionTool;
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

/**
 * 图片的上传和加载网络图片
 */
public class InternetImageUsageMainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    InternetImageRecyclerViewAdapter recyclerViewAdapter;
    StaggeredGridLayoutManager staggeredGridLayoutManager;
    private Button postButton;
    private Button getButton;
    private List<InternetImageModel> modelList = new ArrayList<>();;
    InternetImageDataBaseHelper dataBaseHelper;
    private static String QINIU_IMAGE_HEAD = "http://ssuldoa98.hn-bkt.clouddn.com/";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_internet_image_usage_main);

        //初始化控件 initView
        initView();
        //创建瀑布流适配器
        createAdapter();
        //初始化数据
        initModelList();//读取存储可能会慢于页面创建，需要刷新
    }

    /**
     * todo 初始化控件 initView
     */
    private void initView() {
        recyclerView = (RecyclerView)findViewById(R.id.rv_internet_image_usage);
        postButton = (Button) findViewById(R.id.bt_post_internet_image);
        getButton = (Button) findViewById(R.id.bt_insert_internet_image);
        InternetImageUsageMainActivityClick activityClick =
                new InternetImageUsageMainActivityClick();
        postButton.setOnClickListener(activityClick);
        getButton.setOnClickListener(activityClick);
    }

    /**
     * todo 创建瀑布流适配器
     */
    private void createAdapter(){
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
//                    postInternetImage();//上传图片
                    createPopupWindow(view);//打开弹窗（上传图片方式选择）
                    break;
                case R.id.bt_insert_internet_image:
                    internetDefaultImage();//插入六条预定图片
                    break;
                //拍照上传
                case R.id.bt_select_image_camera:
                    popupWindow.dismiss();
                    //在权限审核结果中进行后续的拍照
                    //6.0才用动态权限
                    if (Build.VERSION.SDK_INT >= 23) {
                        checkPermission();
                    }
                    break;

                //从相册中选择
                case R.id.bt_select_image_photo_album:
                    popupWindow.dismiss();
                    //在权限审核结果中进行后续的相册选取
                    //6.0才用动态权限
                    if (Build.VERSION.SDK_INT >= 23) {
                        if(ContextCompat.checkSelfPermission(InternetImageUsageMainActivity.this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {
                            // 申请读写内存卡内容的权限
                            ActivityCompat.requestPermissions(InternetImageUsageMainActivity.this,
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    WRITE_SDCARD_PERMISSION_REQUEST_CODE);
                        }else{
                            Intent intent = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(intent, REQUEST_CODE_FROM_PHOTO);
                        }
                    }

                    break;
                case R.id.bt_select_image_cancel: //点击取消按钮，关闭弹窗
                    popupWindow.dismiss();
                    break;
//                case R.id.eval_commit_btn:
//                    submitComment();  //提交
//                    break;

            }
        }
    }

    //上传图片测试
    void postInternetImage()
    {
        //在pictures中找第一张图片上传
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
            public Void CallBackURL(String url,JSONObject response) {
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
    protected void uploadImageToQiNiu(String imagePath,CallBackParameter callBackURL)
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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");

        String filePath = imagePath;    // 文件路径
        String key = nowTime.format(formatter)+".jpg";         //在服务器的文件名 文件 key
        if(filePath == null || filePath.isEmpty())
        {
            Toast.makeText(InternetImageUsageMainActivity.this,"上传的文件路径为空",Toast.LENGTH_SHORT).show();
        }else {
            Log.i("ImagePath---->",filePath);
            uploadManager.put(filePath, key, uploadToken, new UpCompletionHandler() {
                @Override
                public void complete(String key, ResponseInfo info, JSONObject response) {
                    if (info != null && info.isOK()) {
                        // 上传成功
                        Log.i("uploadManager response--->", response.toString());
                        Toast.makeText(InternetImageUsageMainActivity.this,"上传成功",Toast.LENGTH_SHORT).show();
                        try {
                            String internetImageURL = QINIU_IMAGE_HEAD + response.get("key");
                            //下面判断成功状态，以及handler中取用url
                            response.put("status",1000);
                            response.put("url",internetImageURL);
                            callBackURL.CallBackURL(internetImageURL,response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        // 上传失败
                        Toast.makeText(InternetImageUsageMainActivity.this,"上传失败",Toast.LENGTH_SHORT).show();
                        Log.i("ImagePath error ResponseInfo--->",info.toString());

                    }
                }
            }, options);
        }
    }

    //用于内部类返回参数
    interface CallBackParameter{
        Void CallBackURL(String url,JSONObject response);
    }

    View popupView;
    PopupWindow popupWindow;

    /**
     * todo 创建弹窗(用于上传图片方式选择）
     * author wang
     * @param view
     */
    private void createPopupWindow(View view) {
        if(popupView==null){
            popupView = getLayoutInflater().inflate(R.layout.select_image_view,null);
        }
        popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,true);
//        popupWindow.showAsDropDown(view, view.getWidth(),view.getHeight());
        popupWindow.showAtLocation(findViewById(R.id.internet_image_usage_main), Gravity.BOTTOM,0,0);  //底部显示弹窗
        popupWindow.setBackgroundDrawable(getResources().getDrawable(R.color.white));

        PermissionTool.getInstance().setAlpha(0.3f,InternetImageUsageMainActivity.this);
        //把背景还原
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                PermissionTool.getInstance().setAlpha(1.0f,InternetImageUsageMainActivity.this);
            }
        });

        initPopupView();
    }

    /**
     * todo 初始化弹窗的控件
     */
    private void initPopupView() {
        Button camera_btn = popupView.findViewById(R.id.bt_select_image_camera);
        Button pic_btn = popupView.findViewById(R.id.bt_select_image_photo_album);
        Button cancel_btn = popupView.findViewById(R.id.bt_select_image_cancel);
        InternetImageUsageMainActivityClick internetImageUsageMainActivityClick =
                new InternetImageUsageMainActivityClick();
        camera_btn.setOnClickListener(internetImageUsageMainActivityClick);
        pic_btn.setOnClickListener(internetImageUsageMainActivityClick);
        cancel_btn.setOnClickListener(internetImageUsageMainActivityClick);
    }

    private final int TAKE_PHOTO_PERMISSION_REQUEST_CODE = 0;  //拍照的权限处理返回码
    private final int WRITE_SDCARD_PERMISSION_REQUEST_CODE = 1; // 读储存卡内容的权限处理返回码
    private final int REQUEST_CODE_FROM_PHOTO = 2; //相册选取返回的requestCode
    private final int REQUEST_CODE_FROM_CAMERA = 1;//拍照返回的requestCode

    //使用相机拍摄功能的权限检查并设置
    private void checkPermission() {
        PermissionTool.getInstance().checkPermission(this, this,
                new PermissionTool.PermissionResultCallBackController() {
            @Override
            public void checkPermissionCallBack() {
                //说明权限都已经通过，调起相机拍摄
                openCamera();
            }
        });
    }

    AlertDialog alertDialog;
    //手动打开设置应用权限
    private void permissionDialog() {
        if (alertDialog == null) {
            alertDialog = new AlertDialog.Builder(this)
                    .setTitle("提示信息")
                    .setMessage("当前应用缺少必要权限，该拍摄功能暂时无法使用。" +
                            "如若需要，请单击【设置】按钮前往设置中心进行权限授权。")
                    .setPositiveButton("设置", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            cancelPermissionDialog();
                            //跳转到权限设置
                            Uri packageURI = Uri.parse("package:" + getPackageName());
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            cancelPermissionDialog();
                        }
                    })
                    .create();
        }
        alertDialog.show();
    }
    //用户取消授权，关闭对话款
    private void cancelPermissionDialog() {
        alertDialog.cancel();
    }

    private String selectImagePath = ""; //选取的要上传的图片路径
    private String cameraImageFilePath="";  //拍照得到的原图保存的图片路径
    //打开相机拍照
    private void openCamera() {
        // 获取SD卡路径
        cameraImageFilePath = getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();
//        mFilePath = Environment.getExternalStorageDirectory().getPath();//外部存储目录
        // 保存图片的文件名
        cameraImageFilePath = cameraImageFilePath + "/" +
                "IMG"+ Calendar.getInstance(TimeZone.getTimeZone("GMT+8")).getTimeInMillis() +".png";

        //打开相机拍照并保存文件
        //android7.0以上版本
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            takePhotoBiggerThan7((new File(cameraImageFilePath)).getAbsolutePath());
        }else{
            //7.0以下拍照
            Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri cameraUri = Uri.fromFile(new File(cameraImageFilePath));

            openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,cameraUri);
            startActivityForResult(openCameraIntent,REQUEST_CODE_FROM_CAMERA);
        }
//        intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        startActivityForResult(intent, REQUEST_CODE_FROM_CAMERA);
    }

    /**
     * android版本7.0以上的拍照
     * @param absolutePath 保存图片路径
     */
    private void takePhotoBiggerThan7(String absolutePath) {
        Uri mCameraTempUri;
        try {
            ContentValues values = new ContentValues(1);//使用给定的初始大小创建一组空值
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");
            values.put(MediaStore.Images.Media.DATA, absolutePath);
            //“主”外部存储卷的样式URI。
            mCameraTempUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            //
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            if (mCameraTempUri != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mCameraTempUri);
                intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
            }
            startActivityForResult(intent, REQUEST_CODE_FROM_CAMERA);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * todo 对用户权限授予结果处理
     * @param requestCode 权限要求码，即我们申请权限时传入的常量 如： TAKE_PHOTO_PERMISSION_REQUEST_CODE
     * @param permissions  保存权限名称的 String 数组，可以同时申请一个以上的权限
     * @param grantResults 每一个申请的权限的用户处理结果数组(是否授权)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case TAKE_PHOTO_PERMISSION_REQUEST_CODE://拍照权限请求
                boolean hasPermission = true;
                for(int i=0;i<grantResults.length;i++){
                    if (grantResults[i] == -1){
                        hasPermission = false;
                        break;
                    }
                }
                if(hasPermission){
                    //全部权限通过，可以进行下一步操作（调起相机拍摄）
                    openCamera();
                }else{
                    //跳转到系统设置权限页面（或者直接关闭页面，不让他继续访问）
                    permissionDialog();
                }
                break;
            case WRITE_SDCARD_PERMISSION_REQUEST_CODE://内存读取权限请求
                if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                }else{
                    Toast.makeText(InternetImageUsageMainActivity.this,"读内存卡内容权限被拒绝",Toast.LENGTH_SHORT);
//                    ToolUtils.midToast(this,"读内存卡内容权限被拒绝",1000);
                }
                break;
        }
    }

    /**
     * todo 对拍照、相册选择图片的返回结果进行处理
     * @param requestCode 返回码，用于确定是哪个 Activity 返回的数据
     * @param resultCode 返回结果，一般如果操作成功返回的是 RESULT_OK
     * @param data 返回对应 activity 返回的数据
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            // 表示 调用照相机拍照返回
            case REQUEST_CODE_FROM_CAMERA:
                if(resultCode == RESULT_OK){
                    try {
                        // 获取输入流
                        FileInputStream fileInputStream = new FileInputStream(cameraImageFilePath);
                        // 把流解析成bitmap,此时就得到了清晰的原图
                        Bitmap imageBitmap = BitmapFactory.decodeStream(fileInputStream);
                        //压缩图片
                        Bitmap newImageBitmap = PermissionTool.getInstance().scaleBitmap(imageBitmap,(float)0.5);
                        Uri imageUri = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(),
                                newImageBitmap,
                                "IMG"+ Calendar.getInstance(TimeZone.getTimeZone("GMT+8")).getTimeInMillis(),
                                null));
                        //uri 转 file
                        selectImagePath = PermissionTool.getInstance().UriToFile(imageUri,this);
                        upLoadImg(selectImagePath,false); //调用接口把图片上传到服务器
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
            //从相册中选择图片返回
            case REQUEST_CODE_FROM_PHOTO:
                if(resultCode == RESULT_OK){
                    try {
                        Uri uri = data.getData();
                        Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),uri);
                        //压缩图片
                        Bitmap newImageBitmap = PermissionTool.getInstance().scaleBitmap(imageBitmap,(float)0.5);
                        Uri newUri = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(),
                                newImageBitmap,
                                "IMG"+ Calendar.getInstance(TimeZone.getTimeZone("GMT+8")).getTimeInMillis(),
                                null));
                        //uri 转 file
                        selectImagePath = PermissionTool.getInstance().UriToFile(newUri,this);
                        Log.i("imgPath",selectImagePath);
                        upLoadImg(selectImagePath,true);//上传图片
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    /**
     * todo handler 上传图片成功后的处理
     */
    @SuppressLint("HandlerLeak")
    Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    try{
                        JSONObject resObj = (JSONObject) msg.obj;
                        Log.i("uploadImageToQiNiu--JSONObject-->",resObj.toString());
                        //七牛不会返回状态
                        if(resObj !=null && resObj.getInt("status")==1000){
                            //String imgUrl = Helper.fixImgUrl(resObj.getString("data"));
                            String imgUrl = resObj.getString("url");
                            //上传成功插入返回的URL
                            InternetImageModel model = new InternetImageModel(imgUrl);
                            List<InternetImageModel> dataList = Arrays.asList(model);
                            Log.i("internetImageURL--->",model.getUrl());
                            insertDataBase(dataList);
                            refreshModelList(true,modelList.size());//刷新
                        }
                    }catch (JSONException je){
                        je.printStackTrace();
                    }
                    break;
                case 2:
                    break;

            }
        }
    };

    /**
     * todo 上传图片(api)
     * @param imagePath 图片地址
     * @param deleteOriginalImage 是否删除原片
     */
    private void upLoadImg(String imagePath,boolean deleteOriginalImage) {
        try{
            new Thread(){
                @Override
                public void run() {
                    super.run();
                    //上传图片到七牛
                    Log.i("uploadImageToQiNiu","imagePath--->"+imagePath);
                    uploadImageToQiNiu(imagePath, new CallBackParameter() {
                        @Override
                        public Void CallBackURL(String url,JSONObject response) {
                            //成功后通过发射handler的方式去处理
                            //JSONObject retObj = Helper.imgUpload(imgString,userToken);
                            if(deleteOriginalImage == true)
                            {
                                File file = new File(imagePath);
                                file.delete();
                            }
                            JSONObject retObj = response ;
                            Message msg = handler.obtainMessage();
                            msg.what = 1;
                            msg.obj = retObj;
                            handler.sendMessage(msg);
                            return null;
                        }
                    });
                }
            }.start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
