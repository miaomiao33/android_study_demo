package com.example.android_study_demo_project.androidCleanArchitecture.Gateway;

import com.example.android_study_demo_project.androidCleanArchitecture.Entities.User;

import java.util.List;

//Gateway层
//网关层定义了数据访问接口，并由具体的数据提供者（如数据库或网络API）实现。
public interface UserGateway {
    List<User> fetchUsers();
    void saveUser(User user,StorageAddCallBack callBack);
}
