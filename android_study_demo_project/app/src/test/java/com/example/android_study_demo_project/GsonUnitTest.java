package com.example.android_study_demo_project;

import com.example.android_study_demo_project.bean.BaseResponse;
import com.example.android_study_demo_project.gsonInter.WanAndroidService;
import com.example.android_study_demo_project.gsonInter.WanAndroidService2;
import com.google.gson.Gson;

import org.junit.Test;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GsonUnitTest {
    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://www.wanandroid.com/")
            .build();
    WanAndroidService wanAndroidService = retrofit.create(WanAndroidService.class);

    @Test
    public void logionTest() throws IOException {
        Call<ResponseBody> call = wanAndroidService.login("fengfeng123","123123");
        Response<ResponseBody> response = call.execute();
        String body = response.body().string();
        System.out.println(body);
        //手动进行数据转换
        BaseResponse baseBean = new Gson().fromJson(body,BaseResponse.class);
        System.out.println(baseBean.getData());
    }

    Retrofit retrofit2 = new Retrofit.Builder()
            .baseUrl("https://www.wanandroid.com/")
            .addConverterFactory(GsonConverterFactory.create())//添加转换器
            .build();
    WanAndroidService2 wanAndroidService2 = retrofit2.create(WanAndroidService2.class);

    @Test
    public void logionConvertTest() throws IOException {
        Call<BaseResponse> call = wanAndroidService2.login("fengfeng123","123123");
        Response<BaseResponse> response = call.execute();
        //自动进行转换
        BaseResponse body = response.body();
        System.out.println(body.getData());

    }
}
