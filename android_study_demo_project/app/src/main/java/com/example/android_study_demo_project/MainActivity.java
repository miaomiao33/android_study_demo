package com.example.android_study_demo_project;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.android_study_demo_project.androidCleanArchitecture.CompositionRoot.CleanArchitectureActivity;
import com.example.android_study_demo_project.dataBindingUsage.DataBindingActivity;
import com.example.android_study_demo_project.espressoUsage.EspressoUsageActivity;
import com.example.android_study_demo_project.fragmentUsage.FragmentUsageActivity;
import com.example.android_study_demo_project.frameLayoutUsage.FrameLayoutMainActivity;
import com.example.android_study_demo_project.intent.IntentMainActivity;
import com.example.android_study_demo_project.internetImageUsage.InternetImageUsageMainActivity;
import com.example.android_study_demo_project.media.MediaMainActivity;
import com.example.android_study_demo_project.storage.StorageActivity;

import cn.jiguang.api.utils.JCollectionAuth;
import cn.jiguang.joperate.api.JOperateInterface;
import cn.jpush.android.api.JPushInterface;

//default activity
public class MainActivity extends AppCompatActivity {

    private  final String TAG = MainActivity.class.getSimpleName();

    //需要的权限数组（申请定位）
    private String[] permissions = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,//访问设备精确位置信息,GPS或其他基于网络的位置信息
            Manifest.permission.ACCESS_COARSE_LOCATION,//允许应用访问设备的大概位置信息，精度较低，通过Wi-Fi或移动网络基站获取
            Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS};//普通权限，仅需在 AndroidManifest.xml 中声明即可使用，允许应用通过ILocationManager接口发送额外的命令给位置提供程序

    //申请定位的Code
    private static final int REQUEST_LOCATION_CODE = 100;
    //调用此方法判断是否拥有权限
    private void initPermissions() {
        //判断是否拥有权限
        if (!hasPermission(permissions)) {
            //没有则申请权限
            // permissions：权限数组
            // REQUEST_LOCATION_CODE：onRequestPermissionsResult中对申请结果进行处理
            ActivityCompat.requestPermissions(this, permissions, REQUEST_LOCATION_CODE);
        } else {
            //拥有定位权限执行操作
            Log.i(TAG,"已获得定位权限");
        }
    }

    //判断permissions权限数组中的权限是否都有
        public boolean hasPermission(String[] permissions) {
        for (String permission : permissions) {
            //判断是否缺少权限
            //已授予返回 PackageManager.PERMISSION_GRANTED；
            //未授予返回 PackageManager.PERMISSION_DENIED
            if(ContextCompat.checkSelfPermission(getApplicationContext(), permission)
                    != PackageManager.PERMISSION_GRANTED){
                Log.i(TAG,"requestPermissions: "+permission);
                return false;
            }
        }
        return true;
    }

    /**
     * todo 对用户权限授予结果处理
     * @param requestCode 权限要求码，即我们申请权限时传入的常量 如： REQUEST_LOCATION_CODE
     * @param permissions  保存权限名称的 String 数组，可以同时申请一个以上的权限
     * @param grantResults 每一个申请的权限的用户处理结果数组(是否授权)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //权限申请的结果
        switch (requestCode){
            //定位权限申请
            case REQUEST_LOCATION_CODE:
            {
                if (grantResults.length > 0) {
                    for(int i = 0; i < grantResults.length; i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(getApplicationContext(),"未拥有相应权限："
                                    +permissions[i],Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    //拥有定位权限执行操作
                    Log.i(TAG,"已获得定位权限");
                } else {
                    Toast.makeText(getApplicationContext(),"grantResults为空",Toast.LENGTH_SHORT).show();
                }
            }
                break;
        }
    }

    //极光初始化信息
    void jiguangInit(){
        JPushInterface.setDebugMode(true);//设置为debug模式，正式环境需要删除
        Context context = getApplicationContext();

        // 调整点一：初始化代码前增加setAuth调用
        boolean isPrivacyReady = true; // app根据是否已弹窗获取隐私授权来赋值
        if(!isPrivacyReady){
            JCollectionAuth.setAuth(context, false); // 后续的初始化与启用推送服务过程将被拦截，即不会开启推送业务
        }
        JPushInterface.init(context);


        // 调整点二：App用户同意了隐私政策授权，并且开发者确定要开启推送服务后调用
        JCollectionAuth.setAuth(context, true); //如初始化被拦截过，将重试初始化过程
        //打印出来用于发送到特定设备
        String registrationID = JPushInterface.getRegistrationID(context);
        Log.i("JPushInterface","jiguang Registration ID:   "+registrationID);
    }

    private Button storageButton;
    private Button intentButton;
    private Button fragmentWithBroadcastButton;
    private Button mediaUsageButton;
    private Button internetImageUsageButton;
    private Button espressoButton;
    private Button cleanArchitectureButton;

    private Button dataBindingButton;

    private Button trackingViewUsageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG,TAG+"--->onCreate");
        initPermissions();
        //跳转到存储
        storageButton = (Button)findViewById(R.id.bt_Storage);
        intentButton = (Button)findViewById(R.id.bt_Intent);
        fragmentWithBroadcastButton = (Button)findViewById(R.id.bt_fragment_with_Broadcast);
        mediaUsageButton = (Button)findViewById(R.id.bt_media_usage);
        internetImageUsageButton = (Button)findViewById(R.id.bt_internet_image_usage);
        espressoButton = (Button) findViewById(R.id.bt_espresso_usage);
        cleanArchitectureButton = (Button) findViewById(R.id.bt_clean_architecture);
        dataBindingButton = (Button) findViewById(R.id.bt_dataBinding_usage);
        trackingViewUsageButton = (Button) findViewById(R.id.bt_tracking_view_usage);


        storageButton.setOnClickListener(new myClick());
        intentButton.setOnClickListener(new myClick());
        fragmentWithBroadcastButton.setOnClickListener(new myClick());
        mediaUsageButton.setOnClickListener(new myClick());
        internetImageUsageButton.setOnClickListener(new myClick());
        espressoButton.setOnClickListener(new myClick());
        cleanArchitectureButton.setOnClickListener(new myClick());
        dataBindingButton.setOnClickListener(new myClick());
        trackingViewUsageButton.setOnClickListener(new myClick());

        //极光配置信息初始化
        jiguangInit();
    }



    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG,TAG+"--->onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,TAG+"--->onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG,TAG+"--->onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG,TAG+"--->onStop");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG,TAG+"--->onRestart");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG,TAG+"--->onDestroy");
    }

    public void onclickCallBack(View view) {

        startActivity(new Intent(this,MainActivity2.class));
    }

    public void onclickCallBackService(View view) {
        startActivity(new Intent(this,MainActivityForService.class));
    }

    public void onclickCallBackMap(View view) {
        //高德
        startActivity(new Intent(this,MainActivity_map.class));
    }

    public void onclickCallBackGlide(View view) {
        //glide的使用
        startActivity(new Intent(MainActivity.this,GlideMainActivity.class));
    }

    public void onclickCallBackOkHttp(View view) {
        startActivity(new Intent(MainActivity.this,OkHttpMainActivity.class));
    }

    public void gotoRxJava(View view) {
        //跳转到rxJava的使用
        startActivity(new Intent(MainActivity.this,RXJavaMainActivity.class));
    }

    //跳转到Intent的使用
    public void gotoIntent()
    {
        Intent intent = new Intent(MainActivity.this, IntentMainActivity.class);
        //传递数据方式1
//        intent.putExtra("data","跳转数据");
//        intent.putExtra("number",1);
        //传递数据方式2
        Bundle bundle = new Bundle();
        bundle.putString("data","跳转数据");
        bundle.putInt("number",1);
        intent.putExtras(bundle);

        startActivity(intent);
    }

    //跳转到SQLite等存储的使用
    public void gotoStorage()
    {
        Intent intent = new Intent(MainActivity.this, StorageActivity.class);
        startActivity(intent);
    }

    //跳转到fragment和broadcast的使用
    public void gotoFragmentWithBroadcast()
    {
        Intent intent = new Intent(MainActivity.this, FragmentUsageActivity.class);
        startActivity(intent);
    }

    //跳转到流媒体开发
    public void gotoMediaUsage()
    {
        Intent intent = new Intent(MainActivity.this, MediaMainActivity.class);
        startActivity(intent);
    }

    //跳转到网络图片开发
    public void gotoInternetImageUsage()
    {
        Intent intent = new Intent(MainActivity.this, InternetImageUsageMainActivity.class);
        startActivity(intent);
    }

    //跳转到espresso测试框架使用
    public void gotoEspressoUsage()
    {
        Intent intent = new Intent(MainActivity.this, EspressoUsageActivity.class);
        startActivity(intent);
    }

    //跳转到clean框架使用
    public void gotoCleanUsage()
    {
        Intent intent = new Intent(MainActivity.this, CleanArchitectureActivity.class);
        startActivity(intent);
    }

    //跳转到DataBinding使用
    public void gotoDataBindingUsage()
    {
        Intent intent = new Intent(MainActivity.this, DataBindingActivity.class);
        startActivity(intent);
    }

    //跳转到自定义跟踪视图使用
    public void gotoTrackingViewUsage()
    {
        Intent intent = new Intent(MainActivity.this, FrameLayoutMainActivity.class);
        startActivity(intent);
    }

    //集中处理按钮方法
    private class myClick implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId())
            {
                case R.id.bt_Storage:
                    gotoStorage();
                    break;
                case R.id.bt_Intent:
                    gotoIntent();
                    break;
                case R.id.bt_fragment_with_Broadcast:
                    gotoFragmentWithBroadcast();
                    break;
                case R.id.bt_media_usage:
                    gotoMediaUsage();
                    break;
                case R.id.bt_internet_image_usage:
                    gotoInternetImageUsage();
                    break;
                case R.id.bt_espresso_usage:
                    gotoEspressoUsage();
                    break;
                case R.id.bt_clean_architecture:
                    gotoCleanUsage();
                    break;
                case R.id.bt_dataBinding_usage:
                    gotoDataBindingUsage();
                    break;
                case R.id.bt_tracking_view_usage:
                    gotoTrackingViewUsage();
                    break;
                default:
                    break;
            }
        }
    }
}
