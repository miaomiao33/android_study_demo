package com.example.android_study_demo_project;

import com.example.android_study_demo_project.bean.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.junit.Test;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
Map和set的序列化和反序列化
 */
public class MapAndSetUnitTest {

    @Test
    public void MapTest()
    {
        Map<String, User> map1 = new HashMap();
        map1.put("1",new User("xiaohong","123",17,true));
        map1.put("2",new User("xiaoming","123",16,true));
        map1.put("3",null);
        map1.put(null,null);//null并不会被序列化

        //Gson提供的Gson对象
        Gson gson = new Gson();
        //序列化
        String json = gson.toJson(map1);
        System.out.println("序列化"+json);
        //反序列化
        Type type = new TypeToken<Map<String,User>>(){}.getType();
        Map<String,User> map2 = gson.fromJson(json,type);
        System.out.println(map2.get(null));
        System.out.println(map1.get("1"));
    }

    @Test
    public void SetTest()
    {
        Set<User> set1 = new HashSet<>();
        set1.add(new User("xiaohong","123",17,true));
        set1.add(new User("xiaoming","123",16,true));
        set1.add(null);

        //Gson提供的Gson对象
        Gson gson = new Gson();
        //序列化
        String json = gson.toJson(set1);
        System.out.println("序列化"+json);
//        //反序列化1
//        Type type = new TypeToken<List<User>>(){}.getType();
//        List<User> ste2 = gson.fromJson(json,type);
//        System.out.println(ste2.get(0));
//        System.out.println(ste2.get(1));
//        System.out.println(ste2.get(2));
        //反序列化2
        Type type = new TypeToken<List<User>>(){}.getType();
        Set<User> set2 = gson.fromJson(json,type);
        Iterator<User> iterator = set2.iterator();
        while (iterator.hasNext())
        {
            User next = iterator.next();
            System.out.println("反序列化"+next.toString());
        }
    }

    @Test
    public void NullTest()
    {
        //job为null，序列化时就不会序列化job
        User u1 = new User("xiaohong","123",17,true);

        //Gson提供的Gson对象
        Gson gson = new Gson();
        //序列化
        String json = gson.toJson(u1);
        System.out.println("序列化"+json);
        //反序列化
        User u2 = gson.fromJson(json, User.class);
        System.out.println("反序列化"+u2.toString());
    }

    @Test
    public void ExposeTest()
    {
        //job为null，序列化时就不会序列化job
        User u1 = new User("xiaohong","123",17,true);
        u1.setTest1(1);
        u1.setTest2(2);

        //Gson提供的Gson对象,控制序列化和反序列化的名称需要使用GsonBuilder
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        //序列化
        String json = gson.toJson(u1);
        System.out.println("序列化"+json);
        //反序列化
        User u2 = gson.fromJson(json, User.class);
        System.out.println("反序列化"+u2.toString());
    }
}
