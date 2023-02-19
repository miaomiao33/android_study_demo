package com.example.android_study_demo_project;

import com.example.android_study_demo_project.getfile.GetFileService;

import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Function;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/*
文件的上传和下载操作
 */
public class GetFileUnitTest {
    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://www.httpbin.org/")
            .addConverterFactory(GsonConverterFactory.create())//添加转换器
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())//添加适配器
            .build();
    GetFileService getFileService = retrofit.create(GetFileService.class);

    @Test
    //上传文件
    public void uploadFileTest()
    {
        File file1 = new File("C:\\Users\\machenike\\Desktop\\文件\\学习\\1.txt");

        MultipartBody.Part part = MultipartBody.Part.createFormData(
                "file1","1.txt",RequestBody.create(file1,MediaType.parse("text/plain"))
        );


        Call<ResponseBody> call = getFileService.upload(part);
        try {
            Response<ResponseBody> response = call.execute();
            System.out.println(response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void downloadTest() throws IOException {
        Response<ResponseBody> response = getFileService
                .download("http://ppt.1ppt.com/uploads/soft/2103/1-210325100G7.zip")
                .execute();
        //先判断文件在不，不在就创建一个文件
        File file = new File("C:\\Users\\machenike\\Desktop\\文件\\学习\\a.zip");
        if(!file.exists()){
            //先得到文件的上级目录，并创建上级目录，在创建文件
            file.getParentFile().mkdir();
            try {
                //创建文件
                file.createNewFile();
                if(response.isSuccessful())
                {
                    InputStream inputStream = response.body().byteStream();
                    FileOutputStream fos = new FileOutputStream("C:\\Users\\machenike\\Desktop\\文件\\学习\\a.zip");
                    int len;//每次读到了多长
                    byte[] buffer = new byte[4096];
                    while((len = inputStream.read(buffer)) != -1)
                    {
                        fos.write(buffer,0,len);
                    }
                    fos.close();
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Test
    public void downloadRxJavaTest()
    {
        getFileService.downloadRxJava("http://ppt.1ppt.com/uploads/soft/2103/1-210325100G7.zip")
                .map(new Function<ResponseBody, File>() {
                    @Override
                    public File apply(ResponseBody responseBody) throws Throwable {
                        //先判断文件在不，不在就创建一个文件
                        File file = new File("C:\\Users\\machenike\\Desktop\\文件\\学习\\b.zip");
                        if(!file.exists()){
                            //先得到文件的上级目录，并创建上级目录，在创建文件
                            file.getParentFile().mkdir();
                            try {
                                //创建文件
                                file.createNewFile();
                                InputStream inputStream = responseBody.byteStream();
                                FileOutputStream fos = new
                                        FileOutputStream(file);
                                int len;//每次读到了多长
                                byte[] buffer = new byte[4096];
                                while((len = inputStream.read(buffer)) != -1)
                                {
                                    fos.write(buffer,0,len);
                                }
                                fos.close();
                                inputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        return file;
                    }
                }).subscribe(new Consumer<File>() {
            @Override
            public void accept(File file) throws Throwable {
                //下载完毕
            }
        });

        //防止执行完了就退出了
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

