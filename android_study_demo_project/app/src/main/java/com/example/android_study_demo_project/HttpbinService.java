package com.example.android_study_demo_project;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface HttpbinService {

    //https://www.httpbin.org/post?username=name
    //@Field("username") String name表示在url后面拼接上的参数
    @POST("post")//注解此方法为post方法
    @FormUrlEncoded//@Multipart 和 @PartMap 注解配合 Map<String, RequestBody> 参数是在多文件上传时用的，
    // @FormUrlEncoded，其对应的参数注解可以是 @FieldMap 或者 @Field
    Call<ResponseBody> post(@Field("username") String name, @Field("password") String psw);

    @GET("get")
    Call<ResponseBody> get(@Query("username") String name,@Query("password") String psw);
}
