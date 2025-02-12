package com.example.android_study_demo_project.storage.ROOM;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

//ROOM插件的DAO层
//对表进行增删改查
@Dao
public interface StudentDao {

    //增
    @Insert
    void insertStudents(Student ... student);

    //删(条件删除)
    @Delete
    void deleteStudents(Student ... student);
    //删（删除所有）
    @Query("DELETE FROM Student")
    void deleteAllStudents();

    //改
    @Update
    void updateStudents(Student ... student);

    //查（查询所有）
    @Query("SELECT * FROM Student ORDER BY ID DESC")//倒叙
    List<Student> getALLStudents();

    //查（有条件）
    @Query("SELECT * FROM Student WHERE name = :studentName")
    List<Student> getStudents(String studentName);
}
