package com.example.android_study_demo_project;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.content.ContextCompat;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;

//模拟点击权限弹窗上的允许按钮，要在会触发权限申请的页面和操作后再去调用
public class PermissionGranter {
    private static final int PERMISSIONS_DIALOG_DELAY = 3000;//3秒
    private static final int GRANT_BUTTON_INDEX = 1;

    public static void allowPermissionsIfNeeded(String permissionNeeded) {

        try {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                    && !hasNeededPermission(permissionNeeded))
            {
                //要求
                sleep(PERMISSIONS_DIALOG_DELAY);
                UiDevice device = UiDevice.getInstance(getInstrumentation());
                // 使用 UiSelector 查找权限允许
                UiSelector uiSelector = new UiSelector()
                        .clickable(true)// 设置可点击
                        .checkable(false)// 设置不可选中
                        .index(GRANT_BUTTON_INDEX);// 指定索引,UI 元素在其父容器中的位置，从 0 开始计数
                UiObject allowPermissions = device.findObject(uiSelector);
                if(allowPermissions.exists()&&allowPermissions.isEnabled())
                {
                    allowPermissions.click();// 如果元素存在且可用，则点击它
                }
            }
        } catch (UiObjectNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    //是否已经拥有必要的权限
    private static boolean hasNeededPermission(String permissionNeeded)
    {
        Context context = getInstrumentation().getTargetContext();
        int permissionStatus = ContextCompat.checkSelfPermission(context,permissionNeeded);
        return permissionStatus == PackageManager.PERMISSION_GRANTED;
    }

    //模拟请求权限
    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException("Cannot execute Thread.sleep()");
        }
    }
}
