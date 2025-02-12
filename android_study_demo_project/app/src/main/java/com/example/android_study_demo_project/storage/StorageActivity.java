package com.example.android_study_demo_project.storage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.android_study_demo_project.R;
import com.example.android_study_demo_project.storage.ROOM.RoomUsageActivity;

public class StorageActivity extends AppCompatActivity {

    Button fileStorageButton;//跳转到文件存储
    Button sharePreferenceButton;//sharePreference的使用
    Button SQLiteButton;//SQLite的使用
    Button RoomButton;//SQLite的使用
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_storage);
        fileStorageButton = (Button) findViewById(R.id.bt_FileStorage);
        sharePreferenceButton = (Button) findViewById(R.id.bt_sharePreference);
        SQLiteButton = (Button) findViewById(R.id.bt_SQLite);
        RoomButton = (Button) findViewById(R.id.bt_ROOM);

        fileStorageButton.setOnClickListener(new storageClick());
        sharePreferenceButton.setOnClickListener(new storageClick());
        SQLiteButton.setOnClickListener(new storageClick());
        RoomButton.setOnClickListener(new storageClick());
    }

    public class storageClick implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId())
            {
                case R.id.bt_FileStorage:
                    gotoFileStorage();
                    break;
                case R.id.bt_sharePreference:
                    gotoSharePreference();
                    break;
                case R.id.bt_SQLite:
                    gotoSQLite();
                    break;
                case R.id.bt_ROOM:
                    gotoRoom();
                    break;
            }
        }

    }

    //跳转到文件使用
    private void gotoFileStorage()
    {
        //隐式跳转使用
        Intent intent = new Intent("com.example.android_study_demo_project.ACTION_START");
        intent.addCategory("com.example.android_study_demo_project.MY_CATEGORY");
        startActivity(intent);
    }
    //跳转到SharePreference使用
    private void gotoSharePreference()
    {
        Intent intent = new Intent(StorageActivity.this,SharePreferenceActivity.class);
        startActivity(intent);
    }
    //跳转到SQLite的使用
    private void gotoSQLite()
    {
        Intent intent = new Intent(StorageActivity.this,SQLiteUsageActivity.class);
        startActivity(intent);
    }
    //跳转到ROOM插件的使用
    private void gotoRoom()
    {
        Intent intent = new Intent(StorageActivity.this, RoomUsageActivity.class);
        startActivity(intent);
    }
}
