package com.example.android_study_demo_project;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UploadFileUnitTest {

    @Test
   public void UploadFileUnitTest(){
        OkHttpClient okHttpClient = new OkHttpClient();
        File file1 = new File("C:\\Users\\machenike\\Desktop\\文件\\学习\\1.txt");
        File file2 = new File("C:\\Users\\machenike\\Desktop\\文件\\学习\\2.txt");

      MultipartBody multipartBody=  new MultipartBody.Builder()
                .addFormDataPart("file1",file1.getName(), RequestBody.create(file1, MediaType.parse("text/plain")))//file1S是和服务器协商好的key
                .addFormDataPart("file2",file2.getName(),RequestBody.create(file1, MediaType.parse("text/plain")))
        .build();

      Request request = new Request.Builder().url("https://www.httpbin.org/post").post(multipartBody).build();
      Call call = okHttpClient.newCall(request);
        try {
            Response response = call.execute();
            System.out.println(response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void jsonTest(){
        OkHttpClient okHttpClient = new OkHttpClient();
        RequestBody requestBody =
                RequestBody.create("{\"a\":1,\"b\":2}",MediaType.parse("application/json"));
        Request request = new Request.Builder().url("https://www.httpbin.org/post").post(requestBody).build();
        Call call = okHttpClient.newCall(request);
        try {
            Response response = call.execute();
            System.out.println(response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
