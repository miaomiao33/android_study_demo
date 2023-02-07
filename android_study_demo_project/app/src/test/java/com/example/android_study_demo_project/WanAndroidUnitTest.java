package com.example.android_study_demo_project;

import com.example.android_study_demo_project.bean.BaseResponse;
import com.example.android_study_demo_project.gsonInter.WanAndroidService2;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.reactivestreams.Publisher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class WanAndroidUnitTest {
    Map<String,List<Cookie>> cookies = new HashMap<>();
    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://www.wanandroid.com")
            .callFactory(new OkHttpClient
                    .Builder()
                    .cookieJar(new CookieJar() {
                        @Override
                        public void saveFromResponse(@NotNull HttpUrl httpUrl, @NotNull List<Cookie> list) {
                            cookies.put(httpUrl.host(),list);
                        }

                        @NotNull
                        @Override
                        public List<Cookie> loadForRequest(@NotNull HttpUrl httpUrl) {
                            List<Cookie> cookieslist  =  WanAndroidUnitTest.this.cookies.get(httpUrl.host());//如果访问的是玩android api网站则返回cookie
                            return cookieslist == null ? new ArrayList<Cookie>():cookieslist;
                        }
                    }).build()
            )
            .addConverterFactory(GsonConverterFactory.create())//添加转换器
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())//添加适配器
            .build();
    WanAndroidService2 wanAndroidService = retrofit.create(WanAndroidService2.class);

    @Test
    public void rxjavaTest(){
        wanAndroidService.login2("fengfeng123","123123")
                .flatMap(new Function<BaseResponse, Publisher<ResponseBody>>() {
                    //再次发起一个请求
                    @Override
                    public Publisher<ResponseBody> apply(BaseResponse baseResponse) throws Throwable {
                        return wanAndroidService.getArticle(0);
                    }
                })
        .observeOn(Schedulers.io())//观察者
        .subscribeOn(Schedulers.newThread())
//        .subscribeOn(AndroidSchedulers.mainThread())//Android里面用这个切换到主线程中
        .subscribe(new Consumer<ResponseBody>() {
            //被观察者login2的请求后getArticle的结果
            @Override
            public void accept(ResponseBody responseBody) throws Throwable {
                System.out.println("123123123123123");
                System.out.println(responseBody.string());
            }
        });

        //防止login2执行完了就退出了
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
