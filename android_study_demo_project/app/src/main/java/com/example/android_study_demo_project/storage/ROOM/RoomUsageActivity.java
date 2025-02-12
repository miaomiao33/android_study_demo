package com.example.android_study_demo_project.storage.ROOM;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.android_study_demo_project.R;

public class RoomUsageActivity extends AppCompatActivity {

    private DBEngine dbEngine;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_usage);
        dbEngine = new DBEngine(RoomUsageActivity.this);
    }

    /**
     * 插入
     * @param view
     */
    public void  insertAction(View view)
    {
        Student student1 = new Student("张三",20);
        Student student2 = new Student("李四",19);
        Student student3 = new Student("王五",18);
        dbEngine.insertStudents(student1,student2,student3);
    }

    /**
     * 删除 id为3的
     * @param view
     */
    public void  deleteAction(View view)
    {
        Student student = new Student(null,0);
        student.setId(3);
        dbEngine.deleteStudents(student);
    }
    /**
     * 删除所有
     * @param view
     */
    public void  deleteAllAction(View view)
    {
        dbEngine.deleteAllStudents();
    }
    /**
     * 修改 id为3的修改为李元霸
     * @param view
     */
    public void  updateAction(View view)
    {
        Student student = new Student("李元霸",40);
        student.setId(3);
        dbEngine.updateStudents(student);
    }
    /**
     * 查询 name=张三，李四
     * @param view
     */
    public void  queryAction(View view)
    {
        dbEngine.queryStudents("张三","李四");
    }
    /**
     * 查询
     * @param view
     */
    public void  queryAllAction(View view)
    {
        dbEngine.queryAllStudents();
    }
}
