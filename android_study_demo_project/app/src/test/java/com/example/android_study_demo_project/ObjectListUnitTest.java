package com.example.android_study_demo_project;

import com.example.android_study_demo_project.bean.Job;
import com.example.android_study_demo_project.bean.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.junit.Test;

import java.lang.reflect.Type;
import java.util.List;

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

    @Test
    //list的序列化和反序列化
    public void testArray()
    {
        User[] users1 = new User[3];

        //Java对象
        users1[0] = new User("fengfeng1","123",18,false);
        users1[1] = new User("fengfeng2","1234",18,true);
        users1[2] = null;

        //Gson提供的对象
        Gson gson = new Gson();
        //序列化
        String json = gson.toJson(users1);
        System.out.println("序列化"+json);

        //反序列化
        User[] users2 = gson.fromJson(json,User[].class);
        System.out.println("序列化0"+users2[0]);
        System.out.println("序列化1"+users2[1]);
        System.out.println("序列化2"+users2[2]);
    }

    //list的序列化和反序列化
    @Test
    public void testListObject()
    {
        User[] users1 = new User[3];

        //Java对象
        users1[0] = new User("fengfeng1","123",18,false);
        users1[1] = new User("fengfeng2","1234",18,true);
        users1[2] = null;

        //Gson提供的对象
        Gson gson = new Gson();
        //序列化
        String json = gson.toJson(users1);
        System.out.println("序列化"+json);

        //反序列化
        Type type = new TypeToken<List<User>>(){}.getType();
//        List<User> list2 = gson.fromJson(json,List.class);
// 此list为伪泛型，里面的<User>会被泛型擦除，所以gson泛型类型是什么，不知道转换为什么类，直接就塞到了linkedtreeMap里面了
        List<User> list2 = gson.fromJson(json,type);
        System.out.println("序列化0"+list2.get(0).getUserName());
        System.out.println("序列化1"+list2.get(1));
        System.out.println("序列化2"+list2.get(2));
    }
}
