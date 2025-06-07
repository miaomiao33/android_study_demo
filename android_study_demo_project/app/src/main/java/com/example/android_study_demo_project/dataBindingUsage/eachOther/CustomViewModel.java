package com.example.android_study_demo_project.dataBindingUsage.eachOther;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
//自定义双向绑定的MutableLiveData
public class CustomViewModel extends ViewModel {
    MutableLiveData<String> mutableLiveData;
    public MutableLiveData<String> getMutableLiveData() {
        return mutableLiveData;
    }

    public void setMutableLiveData(MutableLiveData<String> mutableLiveData) {
        this.mutableLiveData = mutableLiveData;
    }
}
