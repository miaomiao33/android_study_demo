package com.example.android_study_demo_project.gsonInter;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface WanAndroidService {

    @POST("user/login")
    @FormUrlEncoded
    Call<ResponseBody> login(@Field("username") String username, @Field("password") String password);
}
