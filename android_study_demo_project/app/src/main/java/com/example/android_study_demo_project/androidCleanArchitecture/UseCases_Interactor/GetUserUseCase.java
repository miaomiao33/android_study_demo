package com.example.android_study_demo_project.androidCleanArchitecture.UseCases_Interactor;

import com.example.android_study_demo_project.androidCleanArchitecture.Entities.User;

import java.util.List;

//UseCases层（Interactor层）
//交互器层实现了具体的业务逻辑，并与Gateway和Presenter进行交互。
public interface GetUserUseCase {
    List<User> getUsers();
    void addUser(User user,AddUserCallBack callBack);
}

