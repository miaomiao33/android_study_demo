package com.example.android_study_demo_project;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class RXJavaMainActivity extends AppCompatActivity {

    //打印日志
    private final String TAG = RXJavaMainActivity.class.getSimpleName();
    //弹出加载框弹窗（正在加载中。。。）
    private ProgressDialog progressDialog;
    //网络图片的链接地址
    private final static String PATH = "https://ts1.cn.mm.bing.net/th?id=ORMS.e455dc1e29141e9cc7beb5f5d00a0089&pid=Wdp&w=612&h=304&qlt=90&c=1&rs=1&dpr=1&p=0";

    //显示图片
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_r_x_java_main);

        imageView = findViewById(R.id.image_view1);
    }

    /**
     * 图片显示功能
     * @param view
     */

    public void showImageAction(View view) {
        //使用RXJava显示图片
        /**
         * 采用传统的方式完成此项功能， 每位开发人员的思维不同实现不同
         * A：线程池
         * B：new Thread + Handler
         * C: XXX
         * .....
         */

        /**
         * RX思维
         * 起点 和 终点
         * RXJava RXJS等RX框架把所有函数称为操作符是因为我们函数要去操作从起点到终点
         */

        //TODO 第二步
        //起点
        Observable.just(PATH)

                //TODO 第三步
                //需求 图片下载需求 PATH-----> Bitmap
                .map(new Function<String, Bitmap>() {

                    @NonNull
                    @Override
                    public Bitmap apply(String path) throws Throwable {
                        try {
                            Thread.sleep(2000);//睡眠2秒

                            URL url = new URL(path);
                            HttpURLConnection httpURLConnection =  (HttpURLConnection)url.openConnection();
                            httpURLConnection.setReadTimeout(5000);//链接超时时长 5s
                            int responseCode = httpURLConnection.getResponseCode();//开始请求
                            //拿到服务器响应 200成功 404有问题。。。。
                            if(responseCode == HttpURLConnection.HTTP_OK)
                            {
                                InputStream inputStream = httpURLConnection.getInputStream();
                                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                                return bitmap;//返回给下一级
                            }

                        }catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                        return null;
                    }
                })

                //给上面的需求请求分配异步线程
                .subscribeOn(Schedulers.io())
                //终点分配Android主线程
                .observeOn(AndroidSchedulers.mainThread())

               //TODO 导火线 点燃 开始执行第一步
              //关联 观察者模式 关联 起点 和 终点   == 订阅
                .subscribe(
                        //终点
                new Observer<Bitmap>() {

                    //TODO 第一步
                    //订阅成功
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        //加载框
                        progressDialog = new ProgressDialog(RXJavaMainActivity.this);
                        progressDialog.setTitle("RX 加载中。。。");
                        progressDialog.show();

                    }

                    //TODO 第四步
                   // 上一层给的响应（上面需求给的bitmap）
                    @Override
                    public void onNext(@NonNull Bitmap bitmap) {
                        imageView.setImageBitmap(bitmap);//显示图片

                    }

                    //链条发生了异常
                    @Override
                    public void onError(@NonNull Throwable e) {

                    }

                    //TODO 第五步 整个链路完成
                    //完成
                    @Override
                    public void onComplete() {
                        //隐藏加载框
                        if(progressDialog != null)
                        {
                            progressDialog.dismiss();
                        }

                    }
                });


    }
}
