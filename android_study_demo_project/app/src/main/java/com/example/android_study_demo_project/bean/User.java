package com.example.android_study_demo_project.bean;

/**
 * Copyright 2023 json.cn
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Auto-generated: 2023-02-19 18:19:58
 *
 * @author json.cn (i@json.cn)
 * @website http://www.json.cn/java2pojo/
 */
//序列化和反序列化bean类
public class User {
    @Expose
    private String userName;
    @Expose
    private String password;
    @Expose
    private int age;
    @Expose
    private boolean isStudent;
    @Expose
    private Job job;

    @Expose
    //无法以class命名的字段，处理一些如class，public这种特殊字段
    @SerializedName("class")
    private int cls;

    //serialize:参与序列化，deserialize：参与反序列化
    @Expose(serialize = false,deserialize = false)
    private int test1;

    //transient不参与序列化和反序列化
    private transient int test2;

    public User(String userName, String password, int age, boolean isStudent) {
        this.userName = userName;
        this.password = password;
        this.age = age;
        this.isStudent = isStudent;
    }

    public User(String userName, String password, int age, boolean isStudent,Job job) {
        this.userName = userName;
        this.password = password;
        this.age = age;
        this.isStudent = isStudent;
        this.job = job;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
    public String getUserName() {
        return userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    public String getPassword() {
        return password;
    }

    public void setAge(int age) {
        this.age = age;
    }
    public int getAge() {
        return age;
    }

    public void setIsStudent(boolean isStudent) {
        this.isStudent = isStudent;
    }
    public boolean getIsStudent() {
        return isStudent;
    }

    public void setJob(Job job) {
        this.job = job;
    }
    public Job getJob() {
        return job;
    }


    public void setTest1(int a) {
        this.test1 = a;
    }

    public void setTest2(int b) {
        this.test2 = b;
    }

    @Override
    public String toString() {
        return "User{" +
                "userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", age=" + age +
                ", isStudent=" + isStudent +
                ", job=" + job +
                ", cls=" + cls +
                ", test1=" + test1 +
                ", test2=" + test2 +
                '}';
    }
}