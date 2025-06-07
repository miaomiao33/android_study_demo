package com.example.android_study_demo_project.dataBindingUsage;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.example.android_study_demo_project.BR;
//单向关联，注解使用
public class Person extends BaseObservable {
    public int age;
    public String name;
    public String id;

    public Person(int age, String name, String id) {
        this.age = age;
        this.name = name;
        this.id = id;
    }

    @Bindable
    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
        //只会通知修改age
        notifyPropertyChanged(BR.age);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        //更新所有字段
        notifyChange();
    }

    @Bindable//get注释后id不会自动刷新
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
