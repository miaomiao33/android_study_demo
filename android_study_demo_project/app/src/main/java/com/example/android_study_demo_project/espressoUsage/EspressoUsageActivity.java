package com.example.android_study_demo_project.espressoUsage;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;

import com.example.android_study_demo_project.MainActivity;
import com.example.android_study_demo_project.R;
import com.example.android_study_demo_project.androidCleanArchitecture.CompositionRoot.CleanArchitectureActivity;
import com.qiniu.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class EspressoUsageActivity extends AppCompatActivity {
    private TextView textView;
    private Button changeButton;
    private ListView listView;
    private List<String> listViewDataList = Arrays.asList("item1","item2",
            "item3","item4","item5","item6","item7","item8","item9","item10");
    private List<String> recyclerViewDataList = Arrays.asList("item1","item2",
            "item3","item4","item5","item6","item7","item8","item9","item10");
    private RecyclerView recyclerView;

    private Button gotoContactsButton;

    private Button callButton;
    final int CONTACTS_REQUEST_CODE = 1;//读取联系人列表请求码
    private String phoneNumber;//返回的电话号码
    private ImageView imageView;
    private Button takePhotoButton;
    final int ACTION_IMAGE_CAPTURE_CODE = 2;//打开摄像机请求码
    private String cameraImageFilePath;//存储的拍照路径

    private IdlingDelayResources mIdlingResource;//用于测试网络加载延时操作
    private Button loadInternetTextButton;
    private TextView internetTextView;

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
        recyclerView = (RecyclerView) findViewById(R.id.rv_espresso_recyclerView);
        gotoContactsButton = (Button) findViewById(R.id.bt_goto_contacts);
        callButton = (Button) findViewById(R.id.bt_call);
        imageView = (ImageView) findViewById(R.id.iv_espresso_take_photo);
        takePhotoButton = (Button) findViewById(R.id.bt_espresso_take_photo);
        loadInternetTextButton = (Button) findViewById(R.id.bt_espresso_test_internet_load);
        internetTextView = (TextView) findViewById(R.id.tv_espresso_internet_text);

        EspressoListViewAdapter adapter = new EspressoListViewAdapter(
                listViewDataList, EspressoUsageActivity.this, new EspressoListViewClickCallBack() {
            @Override
            public void CallBack(View view, String text) {
                //点击了ListView中的item
                textView.setText("点击了ListView中："+text);
            }
        });
        listView.setAdapter(adapter);
        EspressoUsageClick click = new EspressoUsageClick();
        changeButton.setOnClickListener(click);
        gotoContactsButton.setOnClickListener(click);
        callButton.setOnClickListener(click);
        takePhotoButton.setOnClickListener(click);
        loadInternetTextButton.setOnClickListener(click);

        EspressoRecyclerViewAdapter recyclerViewAdapter = new EspressoRecyclerViewAdapter(
                recyclerViewDataList,
                new EspressoRecyclerViewClickCallBack() {
            @Override
            public void CallBack(View view, String text) {
                //点击了RecyclerView中的item
                textView.setText("点击了RecyclerView中："+text);
            }
        });
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }


    class EspressoUsageClick implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId())
            {
                case R.id.bt_change_espresso_text:
                {
                    textView.setText("Hello Espresso");
                    break;
                }
                case R.id.bt_goto_contacts:
                {
                    //跳转到联系人页面获取号码（暂未找到系统联系人列表目标包）
//                    Intent intent = new Intent(Intent.ACTION_VIEW);
//                    intent.setData(ContactsContract.Contacts.CONTENT_URI);
//                    startActivityForResult(intent, CONTACTS_REQUEST_CODE); //CONTACTS_REQUEST_CODE  是请求码
                    //随便一个页面返回就是返回自己设定的intent
                    Intent intent = new Intent(EspressoUsageActivity.this, CleanArchitectureActivity.class);
                    startActivityForResult(intent,CONTACTS_REQUEST_CODE);//CONTACTS_REQUEST_CODE  是请求码
                    break;
                }
                case R.id.bt_call:
                {
                    //跳转拨打电话
                    Uri uri = Uri.parse("tel:"+phoneNumber);
                    System.out.println("Uri-----------Uri:"+uri);
                    if(!StringUtils.isNullOrEmpty(phoneNumber))
                    {
                        Intent intent = new Intent(Intent.ACTION_CALL,uri);
                        startActivity(intent);
                    }
                    break;
                }
                case R.id.bt_espresso_take_photo:
                {
                    //点击拍照
                    Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    //外部存储方式
//                    // 获取SD卡路径
//                    cameraImageFilePath = getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();
//                    // 保存图片的文件名
//                    cameraImageFilePath = cameraImageFilePath + "/" +
//                            "IMG"+ Calendar.getInstance(TimeZone.getTimeZone("GMT+8")).getTimeInMillis() +".png";
//                    Uri cameraUri = Uri.fromFile(new File(cameraImageFilePath));

                    //模拟器建议使用内部存储，外部存储可能会找不到
                    File internalPicturesDir = getFilesDir();
                    File pictureFile = new File(internalPicturesDir, "image.jpg");
                    cameraImageFilePath = pictureFile.getAbsolutePath();
                    System.out.println("cameraImageFilePath-----："+cameraImageFilePath);
                    Uri cameraUri = Uri.fromFile(pictureFile);

                    openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,cameraUri);
                    startActivityForResult(openCameraIntent,ACTION_IMAGE_CAPTURE_CODE);
                    break;
                }
                case R.id.bt_espresso_test_internet_load:
                {
                    //模拟加载网络数据
                    //最好采用手动设置检查，而不是自动检测
//                    //耗时操作开始，设置空闲状态为false，阻塞测试线程
//                    mIdlingResource.setIdleState(false);
                    internetTextView.setText("网络信息加载中");
                    try {
                        //模拟网络加载了2秒
                        Thread.sleep(1500);
                        internetTextView.setText("网络信息加载成功");
//                        //耗时操作结束，设置空闲状态为true，放开测试线程
//                        mIdlingResource.setIdleState(true);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                }
            }
        }
    }

    //拨打电话的结果
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
            case CONTACTS_REQUEST_CODE:
            {
                if(resultCode == RESULT_OK){
                    if(data != null)
                    {
                        //获取得到的phone
                        phoneNumber = data.getStringExtra("phoneNumber");
                        System.out.println("Uri-----------phoneNumber:"+phoneNumber);
                    }
                }
                break;
            }
            case ACTION_IMAGE_CAPTURE_CODE:
            {
                if(resultCode == RESULT_OK){
                    if(data != null)
                    {
                        // 获取输入流
//                        FileInputStream fileInputStream = null;
//                        try {
//                            fileInputStream = new FileInputStream(cameraImageFilePath);
//                            // 把流解析成bitmap,此时就得到了清晰的原图
//                            Bitmap imageBitmap = BitmapFactory.decodeStream(fileInputStream);
//                            //imageView显示
//                            imageView.setImageBitmap(imageBitmap);
//                        } catch (FileNotFoundException e) {
//                            throw new RuntimeException(e);
//                        }

                        //Espresso测试是接收自己设定的imageBitmap
                        // 如果data不为null，直接从data中获取照片
                        Bundle extras = data.getExtras();
                        if (extras != null) {
                            Bitmap bitmap = (Bitmap) extras.get("data");
                            imageView.setImageBitmap(bitmap); // 显示照片
                        }
                    }

                }
            }
        }

    }

    //试了一下，去除@VisibleForTesting这个注解，不妨碍Test调用，就把他当成测试标志吧
    @VisibleForTesting
    public IdlingResource getIdlingResource() {

        if(mIdlingResource == null)
            //模拟测试网络加载数据（3秒后自动验证）
            mIdlingResource = new IdlingDelayResources(3000);

        return mIdlingResource;
    }
}