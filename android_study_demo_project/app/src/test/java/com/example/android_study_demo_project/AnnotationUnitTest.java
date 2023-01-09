package com.example.android_study_demo_project;

import org.junit.Test;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.Retrofit;
/*
Retrofit的一些注解的使用
 */

public class AnnotationUnitTest {
    Retrofit retrofit = new Retrofit.Builder().baseUrl("https://www.httpbin.org").build();
    HttpbinService httpbinService = retrofit.create(HttpbinService.class);

    @Test
    public void bodyTest()
    {
        FormBody formBody =  new FormBody.Builder()
                .add("a","1").add("b","2").build();
        try {
            Response<ResponseBody> response = httpbinService.postBody(formBody).execute();
            System.out.println(response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void pathTest()
    {
        //使用path将post传给id写入到后面
        //https://www.httpbin.org/post
        try {
            Response<ResponseBody> response = httpbinService.postInPath("post","android","lance","123").execute();
            System.out.println(response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void HeadersTest()
    {
        //多个请求头
        //https://www.httpbin.org/post
        try {
            Response<ResponseBody> response = httpbinService.postWithHeaders().execute();
            System.out.println(response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void UrlTest()
    {
        //多个请求头
        //https://www.httpbin.org/post
        try {
            Response<ResponseBody> response = httpbinService.postUrl("https://www.httpbin.org/post").execute();
            System.out.println(response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
