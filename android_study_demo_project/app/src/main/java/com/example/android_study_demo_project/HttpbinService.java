package com.example.android_study_demo_project;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface HttpbinService {

    //https://www.httpbin.org/post?username=name
    //@Field("username") String name表示在url后面拼接上的参数
    @POST("post")//注解此方法为post方法
    @FormUrlEncoded//@Multipart 以表单的形式上传
    // @FormUrlEncoded，其对应的参数注解可以是 @FieldMap 或者 @Field
    Call<ResponseBody> post(@Field("username") String name, @Field("password") String psw);

    //https://www.httpbin.org/get?username=name
    @GET("get")//get其实就是路径
    Call<ResponseBody> get(@Query("username") String name,
                           @Query("password") String psw);
//                           @QueryMap Map<String,String> params);//@QueryMap 就是以map作为参数

    @HTTP(method = "POST",path = "get",hasBody = true)
    Call<ResponseBody> http(@Query("username") String name,@Query("password") String psw);


    @POST("post")
    Call<ResponseBody> postBody(@Body RequestBody body);//这儿的body需要自己创建一个RequestBody，上面都会自己创建

    @POST("{id}")
    @FormUrlEncoded
    Call<ResponseBody> postInPath(@Path("id") String  path,
                                  @Header("OS") String os,//在header中添加
                                  @Field("username") String name, @Field("password") String psw);
    @Headers({"os:Android","version:1.0",})
    @POST("post")
    Call<ResponseBody> postWithHeaders();

    @POST
    Call<ResponseBody> postUrl(@Url String url);
}
