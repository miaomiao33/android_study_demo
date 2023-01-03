package com.example.android_study_demo_project;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
/*
cookie的缓存和获取并使用
 */

public class CookieUnitTest {
    Map<String,List<Cookie>> cookies = new HashMap<>();
    @Test
    public void CookieTest()
    {

        //addInterceptor先于addNetworkInterceptor执行，哪怕是addNetworkInterceptor放在前面
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .cookieJar(new CookieJar() {
                    @Override
                    public void saveFromResponse(@NotNull HttpUrl httpUrl, @NotNull List<Cookie> list) {
                        cookies.put(httpUrl.host(),list);
                    }

                    @NotNull
                    @Override
                    public List<Cookie> loadForRequest(@NotNull HttpUrl httpUrl) {

                        List<Cookie> cookieslist  =  cookies.get(httpUrl.host());//如果访问的是玩android api网站则返回cookie
                        return cookieslist == null ? new ArrayList<Cookie>():cookieslist;
                    }
                })
                .build();
        FormBody formBody = new FormBody.Builder()
                .add("username","fengfeng123")
                .add("password","123123").build();
        Request request = new Request.Builder()
                .url("https://www.wanandroid.com/user/login")
                .post(formBody)
                .build();
        Call call = okHttpClient.newCall(request);

        try {
            Response response =  call.execute();
            System.out.println(response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }

        //获取收藏的文章
         request = new Request.Builder()
                .url("https://www.wanandroid.com/lg/collect/list/0/json")
                .build();
         call = okHttpClient.newCall(request);

        try {
            Response response =  call.execute();
            System.out.println(response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
