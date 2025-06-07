package com.example.android_study_demo_project.dataBindingUsage;

import androidx.databinding.ObservableField;
import androidx.databinding.ObservableLong;
//单向关联，ObservableField注解使用
public class ObservablePerson {
    //该类的属性用ObservableField封装，调用set方法即可实现页面控件绑定的数据自动刷新
    public ObservableField<String> name = new ObservableField<>();
    public ObservableField<Integer> age = new ObservableField<>();
    public ObservableLong id = new ObservableLong();


    public ObservablePerson(String name, int age,long id) {
        this.name.set(name);
        this.age.set(age);
        this.id.set(id);
    }
}
