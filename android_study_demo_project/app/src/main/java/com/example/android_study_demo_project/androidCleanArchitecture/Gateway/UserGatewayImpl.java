package com.example.android_study_demo_project.androidCleanArchitecture.Gateway;


import com.example.android_study_demo_project.androidCleanArchitecture.Entities.User;

import java.util.ArrayList;
import java.util.List;

//Gateway层
//网关层定义了数据访问接口，并由具体的数据提供者（如数据库或网络API）实现。
public class UserGatewayImpl implements UserGateway {
    private final List<User> users = new ArrayList<>();

    @Override
    public List<User> fetchUsers() {
        return users;
    }

    @Override
    public void saveUser(User user,StorageAddCallBack callBack) {
        users.add(user);
        //模拟添加数据库成功
        callBack.storageAddSuccess();
    }
}