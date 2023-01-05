package com.example.android_study_demo_project;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;

public class OkHttpMainActivity extends AppCompatActivity {

    private OkHttpClient okHttpClient;
    String url = "https://www.httpbin.org/get?a=1&b=2";
    String postUrl = "https://www.httpbin.org/post";
    private Retrofit retrofit;
    private HttpbinService httpbinService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ok_http_main);
         okHttpClient = new OkHttpClient();
         //使用retrofit
         retrofit = new Retrofit.Builder().baseUrl("https://www.httpbin.org").build();
         httpbinService = retrofit.create(HttpbinService.class);
    }

    public void getSync(View view) {

        new Thread(){
            @Override
            public void run() {
                Request request = new Request.Builder().url(url).build();
                //准备好请求的call对象
                Call call = okHttpClient.newCall(request);

                try {
                    Response response =  call.execute();
                    Log.i("getSync","getSync"+response.body().string());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void getAsync(View view) {
        Request request = new Request.Builder().url(url).build();
        //准备好请求的call对象
        Call call = okHttpClient.newCall(request);

        //异步请求
        //无需创建线程，内部会自动创建
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                //请求结束
                if(response.isSuccessful())
                {
                    Log.i("getASync","getASync"+response.body().string());
                }

            }
        });
    }

    public void postSync(View view) {
        new Thread(){
            @Override
            public void run() {
                FormBody formBody =  new FormBody.Builder().add("a","1").add("b","2").build();
                Request request = new Request.Builder().url(postUrl).post(formBody).build();
                //准备好请求的call对象
                Call call = okHttpClient.newCall(request);
                try {
                    Response response =  call.execute();
                    Log.i("postSync","postSync"+response.body().string());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void postAsync(View view) {
        //使用okhttp
//        FormBody formBody =  new FormBody.Builder().add("a","1").add("b","2").build();
//        Request request = new Request.Builder().url(postUrl).post(formBody).build();
//        //准备好请求的call对象
//        Call call = okHttpClient.newCall(request);
//
//        call.enqueue(new Callback() {
//            @Override
//            public void onFailure(@NotNull Call call, @NotNull IOException e) {
//
//            }
//
//            @Override
//            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
//                Log.i("postASync","postASync"+response.body().string());
//            }
//        });
        Log.i("postASync","postASync"+"1123123123123123123123");
        //使用retrofit
        retrofit2.Call<ResponseBody> call = httpbinService.post("fengfeng123", "123456");
        call.enqueue(new retrofit2.Callback<ResponseBody>() {
            @Override
            public void onResponse(retrofit2.Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                //成功
                try {
                    String responseData = response.body().string();
                    Log.i("postASync","postASync"+responseData);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ResponseBody> call, Throwable t) {
                //失败
            }
        });
    }
}
