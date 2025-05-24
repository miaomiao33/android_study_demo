package com.example.android_study_demo_project;

import static junit.framework.TestCase.assertEquals;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {
    private Context mTargetContext;

    @Before
    public void setUp() throws Exception {
        //启动测试前的一些初始化
        mTargetContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    @Test
    public void onCreate()
    {
        try {
            //获取配置的meta-data元素据
            ApplicationInfo applicationInfo = mTargetContext.getPackageManager().getApplicationInfo(
                    mTargetContext.getPackageName(),
                    PackageManager.GET_META_DATA);
            Bundle metaData = applicationInfo.metaData;
            String data = metaData.getString("com.example.android_study_demo_project.TEST");

            System.out.println("data:"+data);
            assertEquals("1234567890", data);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void versiontest()
    {
//        android:versionCode=”2”
//        android:versionName=”1.1”
//        versionCode是给设备程序识别版本(升级)用的，必须是一个interger值，整数，代表app更新过多少次
//        versionName是给用户看的，可以写1.1.1 , 1.1.2的形式

        try {

            PackageInfo packageInfo = mTargetContext.getPackageManager().getPackageInfo(
                    mTargetContext.getPackageName(),
                    0);
            int versionCode = packageInfo.versionCode;
            String versionName = packageInfo.versionName;
            System.out.println("versionName:"+versionName);
            System.out.println("versionCode:"+ versionCode);
            Assert.assertNotNull(versionName);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}