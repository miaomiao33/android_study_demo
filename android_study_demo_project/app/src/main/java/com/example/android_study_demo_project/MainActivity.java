package com.example.android_study_demo_project;

import android.Manifest;
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

import com.example.android_study_demo_project.intent.IntentMainActivity;
import com.example.android_study_demo_project.storage.StorageActivity;

//default activity
public class MainActivity extends AppCompatActivity {

    private  final String TAG = MainActivity.class.getSimpleName();

    //权限数组（申请定位）
    private String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS};

    //返回code
    private static final int OPEN_SET_REQUEST_CODE = 100;
    //调用此方法判断是否拥有权限
    private void initPermissions() {
        if (lacksPermission(permissions)) {//判断是否拥有权限
            //请求权限，第二参数权限String数据，第三个参数是请求码便于在onRequestPermissionsResult 方法中根据code进行判断
            ActivityCompat.requestPermissions(this, permissions, OPEN_SET_REQUEST_CODE);

        } else {
            //拥有权限执行操作
        }
    }

    //如果返回true表示缺少权限
        public boolean lacksPermission(String[] permissions) {
        for (String permission : permissions) {
            //判断是否缺少权限，true=缺少权限
            if(ContextCompat.checkSelfPermission(getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED){
                return true;
            }
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){//响应Code
            case OPEN_SET_REQUEST_CODE:
                if (grantResults.length > 0) {
                    for(int i = 0; i < grantResults.length; i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(getApplicationContext(),"未拥有相应权限"+i+"___"+grantResults[i],Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    //拥有权限执行操作
                } else {
                    Toast.makeText(getApplicationContext(),"未拥有相应权限3",Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private Button storageButton;
    private Button intentButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG,TAG+"--->onCreate");
        initPermissions();
        //跳转到存储
        storageButton = (Button)findViewById(R.id.bt_Storage);
        intentButton = (Button)findViewById(R.id.bt_Intent);
        storageButton.setOnClickListener(new myClick());
        intentButton.setOnClickListener(new myClick());
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

    public void gotoStorage()
    {
        Intent intent = new Intent(MainActivity.this, StorageActivity.class);
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
                default:
                    break;
            }
        }
    }
}
