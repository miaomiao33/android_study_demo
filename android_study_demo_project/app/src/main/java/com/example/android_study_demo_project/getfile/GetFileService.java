package com.example.android_study_demo_project.getfile;
/*
文件下载和上传操作
 */

import io.reactivex.rxjava3.core.Flowable;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

public interface GetFileService {
    @POST("post")
    @Multipart
    Call<ResponseBody> upload(@Part MultipartBody.Part file);//表明我们要上传的是MultipartBody.Part中的文件

    @Streaming
    @GET
    Call<ResponseBody> download(@Url String url);

    @Streaming//下载的文件很大要使用这个注释
    @GET
    Flowable<ResponseBody> downloadRxJava(@Url String url);
}
