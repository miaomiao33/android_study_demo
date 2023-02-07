package com.example.android_study_demo_project.gsonInter;

import com.example.android_study_demo_project.bean.BaseResponse;

import io.reactivex.rxjava3.core.Flowable;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface WanAndroidService2 {

    @POST("user/login")
    @FormUrlEncoded
    Call<BaseResponse> login(@Field("username") String username, @Field("password") String password);

    @POST("user/login")
    @FormUrlEncoded
    Flowable<BaseResponse> login2(@Field("username") String username, @Field("password") String password);

    @GET("lg/collect/list/{pageNum}/json")
    Flowable<ResponseBody> getArticle(@Path("pageNum") int pageNum);
}
