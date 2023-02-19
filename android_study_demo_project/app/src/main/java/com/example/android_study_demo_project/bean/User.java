package com.example.android_study_demo_project.bean;

/**
 * Copyright 2023 json.cn
 */

/**
 * Auto-generated: 2023-02-19 18:19:58
 *
 * @author json.cn (i@json.cn)
 * @website http://www.json.cn/java2pojo/
 */
//序列化和反序列化bean类
public class User {

    private String userName;
    private String password;
    private int age;
    private boolean isStudent;
    private Job job;

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
}