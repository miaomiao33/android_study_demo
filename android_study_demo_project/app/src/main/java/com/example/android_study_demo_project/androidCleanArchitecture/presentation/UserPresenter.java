package com.example.android_study_demo_project.androidCleanArchitecture.presentation;


import android.content.Context;
import android.widget.Toast;

import com.example.android_study_demo_project.androidCleanArchitecture.CompositionRoot.UserListViewAdapter;
import com.example.android_study_demo_project.androidCleanArchitecture.Entities.User;
import com.example.android_study_demo_project.androidCleanArchitecture.UseCases_Interactor.AddUserCallBack;
import com.example.android_study_demo_project.androidCleanArchitecture.UseCases_Interactor.GetUserUseCase;

//presenter处理UI事件，如单击事件，通常包含内层Interactor的回调方法。
public class UserPresenter {
    private final UserListViewAdapter listViewAdapter;
    private final GetUserUseCase userInteractor;//用于存储数据

    public UserPresenter(UserListViewAdapter listViewAdapter, GetUserUseCase userInteractor) {
        this.listViewAdapter = listViewAdapter;
        this.userInteractor = userInteractor;
    }

    public void onAddUserButtonClicked(User user, Context context) {

            userInteractor.addUser(user, new AddUserCallBack() {
                @Override
                public void addSuccess() {
                    //业务执行成功
                    //刷新listView
                    if(listViewAdapter !=  null) {
                        listViewAdapter.notifyDataSetChanged();
                    }else
                    {
                        Toast.makeText(context,"listView为空，刷新失败",Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void addError() {

                }
            });
    }
}