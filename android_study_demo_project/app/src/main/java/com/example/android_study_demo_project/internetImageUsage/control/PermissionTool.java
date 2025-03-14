package com.example.android_study_demo_project.internetImageUsage.control;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionTool {
    private final int TAKE_PHOTO_PERMISSION_REQUEST_CODE = 0;  //拍照的权限处理返回码
    private final int WRITE_SDCARD_PERMISSION_REQUEST_CODE = 1; // 读储存卡内容的权限处理返回码
    private final int REQUEST_CODE_FROM_PHOTO = 2; //相册选取返回的requestCode
    private final int REQUEST_CODE_FROM_CAMERA = 1;//拍照返回的requestCode

    //单例模式
    private static PermissionTool instance;
    public static synchronized PermissionTool getInstance(){
        if(instance == null)
        {
            instance = new PermissionTool();
        }
        return instance;
    }


    String[] permissions = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    //使用相机拍摄功能的权限检查并设置
    public void checkPermission(Activity activity, Context context,
                                PermissionResultCallBackController permissionResultCallBackController) {
        List<String> permissionList = new ArrayList<>();
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(context, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(permissions[i]);
            }
        }
        if (permissionList.size() <= 0) {
            //说明权限都已经通过
            permissionResultCallBackController.checkPermissionCallBack();
        } else {
            //对存在的未允许的权限进行申请
            ActivityCompat.requestPermissions(activity, permissions, TAKE_PHOTO_PERMISSION_REQUEST_CODE);
        }
    }

    public interface PermissionResultCallBackController{
        void checkPermissionCallBack();
    }

//    //手动打开设置应用权限
//    private void permissionDialog(Context context) {
//        AlertDialog alertDialog;
//            alertDialog = new AlertDialog.Builder(context)
//                    .setTitle("提示信息")
//                    .setMessage("当前应用缺少必要权限，该拍摄功能暂时无法使用。如若需要，请单击【设置】按钮前往设置中心进行权限授权。")
//                    .setPositiveButton("设置", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            alertDialog.cancel();
//                            Uri packageURI = Uri.parse("package:" + getPackageName());
//                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
//                            startActivity(intent);
//                        }
//                    })
//                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            alertDialog.cancel();
//                        }
//                    })
//                    .create();
//
//        alertDialog.show();
//    }

    /**
     * todo  uri 转 file
     * @param uri
     * @return
     */
    public String UriToFile(Uri uri,Context context) {
        String[] filePc = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, filePc, null, null, null);
        cursor.moveToFirst();
        Log.i(((Activity)context).getLocalClassName(), "UriToFile: 22"+cursor);
        int col = cursor.getColumnIndex(filePc[0]);
        String pic = cursor.getString(col);
        cursor.close();
        return pic;
    }

    /**
     * todo 压缩图片
     * @param origin
     * @param ratio
     * @return
     */
    public Bitmap scaleBitmap(Bitmap origin, float ratio) {
        if (origin == null) {
            return null;
        }
        int width = origin.getWidth();
        int height = origin.getHeight();
        Matrix matrix = new Matrix();
        matrix.preScale(ratio, ratio);
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        return newBM;
    }

    /**
     * todo 自定义方法，遮罩层
     * @param f
     */
    public void setAlpha(float f,Context context) {
        Window window = ((Activity) context).getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.alpha = f;
        window.setAttributes(lp);
    }
}
