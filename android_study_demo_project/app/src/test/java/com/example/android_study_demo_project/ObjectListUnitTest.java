package com.example.android_study_demo_project;

import com.example.android_study_demo_project.bean.Job;
import com.example.android_study_demo_project.bean.User;
import com.google.gson.Gson;

import org.junit.Test;

/*
序列化和反序列化
 */
public class ObjectListUnitTest {

    @Test
    public void testObject()
    {
        //Java对象
        User u1 = new User("fengfeng","123",18,false);
        //Gson提供的Gson对象
        Gson gson = new Gson();

        //序列化
        String json = gson.toJson(u1);
        System.out.println("序列化："+json);

        //反序列化
        User u2 = gson.fromJson(json,User.class);
        System.out.println("反序列化："+u2.getUserName()+"_"+u2.getPassword());

    }

    @Test
    public void testNestedObject()
    {
        //Java对象
        User u1 = new User("fengfeng","123",18,false);
        Job job = new Job("工人",8000);
        u1.setJob(job);
        //Gson提供的Gson对象
        Gson gson = new Gson();

        //序列化
        String json = gson.toJson(u1);
        System.out.println("序列化："+json);

        //反序列化
        User u2 = gson.fromJson(json,User.class);
        System.out.println("反序列化："+u2.getUserName()+"_"+u2.getPassword()+"_"+u2.getJob().toString());

    }
}
