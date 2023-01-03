package com.example.android_study_demo_project;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/*
Description 拦截器的使用
 */

public class InterceptorUnitTest {
    @Test
    public void InterceptorTest()
    {
        //addInterceptor先于addNetworkInterceptor执行，哪怕是addNetworkInterceptor放在前面
     OkHttpClient okHttpClient = new OkHttpClient.Builder().addNetworkInterceptor(new Interceptor() {
         @NotNull
         @Override
         public Response intercept(@NotNull Chain chain) throws IOException {
             System.out.println("version"+chain.request().header("version"));
             return chain.proceed(chain.request());
         }
     })
             .addInterceptor(new Interceptor() {
            @NotNull
            @Override
            public Response intercept(@NotNull Chain chain) throws IOException {
                //前置处理
                Request request = chain.request().newBuilder()
                        .addHeader("os","android")
                        .addHeader("version","1.0")
                        .build();
                Response response =  chain.proceed(request);
                //后置处理
                return response;
               // return  chain.proceed(chain.request());//什么处理都不做
            }
        }).build();
        Request request = new Request.Builder().url("https://www.httpbin.org/get?a=1&b=2").build();
        Call call = okHttpClient.newCall(request);

        try {
            Response response =  call.execute();
            System.out.println(response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
