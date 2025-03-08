package com.example.android_study_demo_project.internetImageUsage.storage;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class InternetImageDataBaseHelper extends SQLiteOpenHelper {
    private Context context;
    private static String TableName = "InternetImageTable";

    public static String getTableName() {
        return TableName;
    }

    //图像表
    public static final String CREATE_IMAGE_TABLE = "Create table "+TableName+"("
            +"id integer primary key autoincrement,"
            +"url text)";
    //单例模式
    private static InternetImageDataBaseHelper instance;
    public static synchronized InternetImageDataBaseHelper getInstance(Context contextData){
        if(instance == null)
        {
            instance = new InternetImageDataBaseHelper(contextData,"InternetImage.dp",
                    null,1);
        }
        return instance;
    }

    public InternetImageDataBaseHelper(@Nullable Context context, @Nullable String name,
                                       @Nullable SQLiteDatabase.CursorFactory factory,
                                       int version) {
        super(context, name, factory, version);
        this.context = context;
    }

    public InternetImageDataBaseHelper(@Nullable Context context, @Nullable String name,
                                       @Nullable SQLiteDatabase.CursorFactory factory,
                                       int version, @Nullable DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
        this.context = context;
    }

    public InternetImageDataBaseHelper(@Nullable Context context, @Nullable String name,
                                       int version, @NonNull SQLiteDatabase.OpenParams openParams) {
        super(context, name, version, openParams);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        //没有数据库时执行
        sqLiteDatabase.execSQL(CREATE_IMAGE_TABLE);
        Toast.makeText(this.context,"初始化数据库成功",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        //数据库版本更新时
    }
}
