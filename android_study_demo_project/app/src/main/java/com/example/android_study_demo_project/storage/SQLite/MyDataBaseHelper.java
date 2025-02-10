package com.example.android_study_demo_project.storage.SQLite;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MyDataBaseHelper extends SQLiteOpenHelper {

    private Context myContext;
    //创建表语句
    public static final String CREATE_BOOK_TABLE = "Create table Book("+
                                              "id integer primary key autoincrement,"+
                                              "author text,"+
                                              "price real,"+
                                              "pages integer,"+
                                              "name text)";
    //用于更新用
    public static final String CREATE_CATEGORY_TABLE = "Create table Category("+
            "id integer primary key autoincrement,"+
            "category_name text,"+
            "category_code integer)";

    //对外提供的单例模式
    private static SQLiteOpenHelper instance;
    public static synchronized SQLiteOpenHelper getInstance(Context context){
        if(instance == null)
        {
            instance = new MyDataBaseHelper(context,
                    "BookStore.dp", null, 1);
        }
        return instance;
    }

    public MyDataBaseHelper(@Nullable Context context, @Nullable String name,
                            @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        myContext = context;
    }

    public MyDataBaseHelper(@Nullable Context context, @Nullable String name,
                            @Nullable SQLiteDatabase.CursorFactory factory, int version,
                            @Nullable DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
        myContext = context;
    }

    public MyDataBaseHelper(@Nullable Context context, @Nullable String name,
                            int version, @NonNull SQLiteDatabase.OpenParams openParams) {
        super(context, name, version, openParams);
        myContext = context;
    }

    //没有数据库时执行onCreate
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_BOOK_TABLE);
        Toast.makeText(myContext,"创建数据库成功",Toast.LENGTH_SHORT).show();
    }

    //数据库version升级时执行onUpgrade
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("drop table if exists Category");
        sqLiteDatabase.execSQL(CREATE_CATEGORY_TABLE);
        Toast.makeText(myContext,"更新数据库版本成功",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        super.onDowngrade(db, oldVersion, newVersion);
        //不注释掉降数据库version new时会报错
    }
}
