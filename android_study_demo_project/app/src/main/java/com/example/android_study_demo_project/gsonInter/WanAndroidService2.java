package com.example.android_study_demo_project.gsonInter;

import com.example.android_study_demo_project.bean.BaseResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface WanAndroidService2 {

    @POST("user/login")
    @FormUrlEncoded
    Call<BaseResponse> login(@Field("username") String username, @Field("password") String password);
}
