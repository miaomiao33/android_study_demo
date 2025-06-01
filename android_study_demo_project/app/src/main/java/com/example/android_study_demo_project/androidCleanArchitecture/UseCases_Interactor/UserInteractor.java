package com.example.android_study_demo_project.androidCleanArchitecture.UseCases_Interactor;

import com.example.android_study_demo_project.androidCleanArchitecture.Entities.User;
import com.example.android_study_demo_project.androidCleanArchitecture.Gateway.StorageAddCallBack;
import com.example.android_study_demo_project.androidCleanArchitecture.Gateway.UserGateway;

import java.util.List;

//UseCases层（Interactor层）
//交互器层实现了具体的业务逻辑，并与Gateway和Presenter进行交互。
public class UserInteractor implements GetUserUseCase {

    private final UserGateway userGateway;

    public UserInteractor(UserGateway userGateway) {
        this.userGateway = userGateway;
    }

    @Override
    public List<User> getUsers() {
        //获取user
        return userGateway.fetchUsers();
    }

    @Override
    public void addUser(User user,AddUserCallBack callBack) {
        //添加user
        userGateway.saveUser(user, new StorageAddCallBack() {
            @Override
            public void storageAddSuccess() {
                //添加成功
                callBack.addSuccess();
            }

            @Override
            public void storageAddError() {

            }
        });
    }
}
