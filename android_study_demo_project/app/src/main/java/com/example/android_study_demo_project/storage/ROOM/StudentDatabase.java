package com.example.android_study_demo_project.storage.ROOM;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Student.class},version = 1,exportSchema = false)
public abstract class StudentDatabase extends RoomDatabase {

    //用户只操作dao层，要暴露dao层
    public abstract StudentDao getStudentDao();

    //单例模式返回DB
    private static StudentDatabase instance;
    static synchronized StudentDatabase getInstance(Context context)
    {
        if(instance == null)
        {
            instance = Room.databaseBuilder
                    (context.getApplicationContext(),StudentDatabase.class,"student_database")
                    //默认为异步线程

                    //强制开启主线程也可以操作数据库，慎用
//                    .allowMainThreadQueries()
                    .build();
        }

        return instance;
    }
}
