package com.example.android_study_demo_project.opencv;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.android_study_demo_project.R;
import com.example.android_study_demo_project.opencv.recordVideo.RecordVideoMainActivity;
import com.googlecode.tesseract.android.TessBaseAPI;

import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.Objects;

public class OpenCVMainActivity extends AppCompatActivity {
    private  final String TAG = OpenCVMainActivity.class.getSimpleName();
    private final int DEFAULT_ID_CARD_WIDTH = 640;
    private final int DEFAULT_ID_CARD_HEIGHT = 480;
    private Button getVersionButton;
    private Button getImgFromPhotoAlbumButton;
    private Button searchImgIdButton;
    private Button recognizeTextFromImgButton;
    private Button gotoLivingStreamButton;
    private TextView getInfoFromImgTextView;//显示图片获取的信息
    private ImageView idCardimageView;


    private OpenCVJNIJavaCallC openCVJNIJavaCallC = new OpenCVJNIJavaCallC();
    private final int REQUEST_CODE_GET_IMG_FROM_PHOTO_ALBUM = 100;
    private Bitmap idCardCodeImg;//获取到的身份证号码截图
    private Bitmap fullIDCardImage;//身份证全图
    private static TessBaseAPI tessBaseAPI;//图片识别
    private static final String LANGUAGE = "num";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_cv_main);
        //初始化组件信息
        initView();

        //OpenCV验证
        if(OpenCVLoader.initDebug())
        {
            Log.i(TAG,"OpenCV 加载成功"+OpenCVLoader.OPENCV_VERSION);
        }else
        {
            Log.i(TAG,"OpenCV 加载失败");
        }

        //初始化ORC识别信息
        initTess();
    }

    //初始化组件信息
    void initView()
    {
        getInfoFromImgTextView = (TextView) findViewById(R.id.tv_get_info_from_img);
        idCardimageView = (ImageView) findViewById(R.id.iv_opencv_Id_card);
        getVersionButton = (Button) findViewById(R.id.bt_get_opencv_version_from_native);
        getImgFromPhotoAlbumButton = (Button) findViewById(R.id.bt_get_img_from_photo_album);
        searchImgIdButton = (Button) findViewById(R.id.bt_get_id_card_id_from_img);
        recognizeTextFromImgButton = (Button) findViewById(R.id.bt_recognize_text_from_img);
        gotoLivingStreamButton = (Button) findViewById(R.id.bt_goto_living_stream);

        OpenCVClick click = new OpenCVClick();
        getVersionButton.setOnClickListener(click);
        getImgFromPhotoAlbumButton.setOnClickListener(click);
        searchImgIdButton.setOnClickListener(click);
        recognizeTextFromImgButton.setOnClickListener(click);
        gotoLivingStreamButton.setOnClickListener(click);
    }

    //初始化 OCR 识别， Tess-two
    private void initTess() {
        tessBaseAPI= new TessBaseAPI();
        new InitTessTask(this).execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode)
        {
            case REQUEST_CODE_GET_IMG_FROM_PHOTO_ALBUM:
                if(data != null)
                {
                    getPhotoAlbumResult(data.getData());
                }
                break;
        }

    }

    //获得图片选择结果
    private void getPhotoAlbumResult(Uri uri)
    {
        String imagePath = null;
        if(uri != null)
        {
            if(Objects.equals(uri.getScheme(), "file"))
            {
                imagePath = uri.getPath();
                Log.i(TAG,"uri path:"+imagePath);
            }else if(Objects.equals(uri.getScheme(), "content"))
            {
                Log.i(TAG,"content uri 获得图片");
                String[] filePathColumns = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(uri, filePathColumns,
                        null, null, null);
                if(cursor != null)
                {
                    if(cursor.moveToFirst())
                    {
                        int columnIndex = cursor.getColumnIndex(filePathColumns[0]);
                        imagePath = cursor.getString(columnIndex);
                    }
                    cursor.close();
                }
            }
        }
        if(!TextUtils.isEmpty(imagePath))
        {
            //Bitmap资源回收
            if(fullIDCardImage != null && !fullIDCardImage.isRecycled())
            {
                fullIDCardImage.recycle();
                fullIDCardImage = null;
                System.gc();
            }
            fullIDCardImage = toBitmap(imagePath,DEFAULT_ID_CARD_WIDTH,DEFAULT_ID_CARD_HEIGHT);
            if(fullIDCardImage == null)
            {
                getInfoFromImgTextView.setText("选择图片错误");
            }else {
                getInfoFromImgTextView.setText(String.format("选择了图片：%s",imagePath));

                idCardimageView.setImageBitmap(fullIDCardImage);
            }
        }
    }
    //文件路径转bitmap，并按照比例缩小
    public Bitmap toBitmap(String filePath, int width, int height)
    {
        if(TextUtils.isEmpty(filePath))
            return null;
        Bitmap bitmap;
        //只解码图像的尺寸（宽高）
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;// 只解码图像的边界，不加载图像到内存
        BitmapFactory.decodeFile(filePath,options);
        int srcWidth = options.outWidth, srcHeight = options.outHeight;
        //获取采样率,小于等于1时，图像高、宽不变，大于1时，图像高、宽分别以2的inSampleSize次方分之一缩小
        int scale = 1;

        if (srcWidth > height && srcWidth > width) {
            scale  = srcWidth / width;
        } else if(srcWidth <height  && srcHeight >height  ){
            scale  = srcHeight / height ;
        }

        if(scale <= 0){
            scale = 1;
        }

        options.inJustDecodeBounds = false;
        options.inSampleSize = scale;

        return BitmapFactory.decodeFile(filePath, options);
    }

    /**
     * 获取 OpenCV的版本
     */
    void getOpenCVVersion()
    {
        String version = openCVJNIJavaCallC.getOpenCVInfoFromJNI();
        TextView textView = (TextView) findViewById(R.id.tv_opencv_version);
        textView.setText("opencv version:" + version);
    }
    /**
     * 从相册获取图片
     */
    void getImgFromPhotoAlbum()
    {
        Intent intent;
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
        {
            intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }
        intent.setType("image/*");
        //使用选取器并自定义标题
        startActivityForResult(
                Intent.createChooser(intent,"选择身份证图片"),
                REQUEST_CODE_GET_IMG_FROM_PHOTO_ALBUM);
    }

    /**
     * 从身份证全图中获取身份证号码截图
     */
    void getIdCardIDFromImg()
    {
        getInfoFromImgTextView.setText("获取到的身份证号码截图");
        idCardCodeImg = null;
        //NDK 获取图片(身份证号码截图)
        Bitmap bitmapResult = OpenCVJNIJavaCallC.getIDCardImage(fullIDCardImage,Bitmap.Config.ARGB_8888);
        //Bitmap资源回收
        if(fullIDCardImage != null && !fullIDCardImage.isRecycled())
        {
            fullIDCardImage.recycle();
            fullIDCardImage= null;
            System.gc();
        }
        idCardCodeImg = bitmapResult;
        idCardimageView.setImageBitmap(idCardCodeImg);
    }
    /**
     *  OCR 识别身份证号码截图获取到身份证号码
     */
    void recognizeTextFromImg()
    {
        tessBaseAPI.setImage(idCardCodeImg);
        getInfoFromImgTextView.setText("身份证号码：" + tessBaseAPI.getUTF8Text());
        tessBaseAPI.clear();
    }
    /**
     *  进入直播页面
     */
    void gotoLivingStream()
    {
        Intent intent = new Intent(this, RecordVideoMainActivity.class);
        startActivity(intent);
    }

    class OpenCVClick implements View.OnClickListener
    {

        @Override
        public void onClick(View view) {
            switch (view.getId())
            {
                case R.id.bt_get_opencv_version_from_native:
                    getOpenCVVersion();
                    break;
                case R.id.bt_get_img_from_photo_album:
                    getImgFromPhotoAlbum();
                    break;
                case R.id.bt_get_id_card_id_from_img:
                    getIdCardIDFromImg();
                    break;
                case R.id.bt_recognize_text_from_img:
                    recognizeTextFromImg();
                    break;
                case R.id.bt_goto_living_stream:
                    gotoLivingStream();
                    break;
            }
        }
    }

    private static class InitTessTask extends AsyncTask<Void,Void,Boolean>{
        private WeakReference<OpenCVMainActivity> activityReference;

        InitTessTask(OpenCVMainActivity context){
            this.activityReference = new WeakReference<>(context);
        }
        @Override
        protected Boolean doInBackground(Void... parameters) {
            OpenCVMainActivity openCVMainActivity = activityReference.get();
            if(openCVMainActivity == null || openCVMainActivity.isFinishing())
            {
                return false;
            }
            try {
                //直接就是从main下开始的，使用getAssets，故前面不用加/assets/
                InputStream inputStream = openCVMainActivity.getAssets().open("tessdata/"+LANGUAGE+".traineddata");
                String assetFilePath = Environment.
                        getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getPath()
                        + "/tessdata/"+LANGUAGE+".traineddata";
                File assetFile = new File(assetFilePath);
                if(!assetFile.exists())
                {
                    assetFile.getParentFile().mkdirs();
                    FileOutputStream fos = new FileOutputStream(assetFile);
                    byte[] buffer = new byte[2048];
                    int len;
                    while ((len = inputStream.read(buffer)) != -1){
                        fos.write(buffer,0,len);
                    }
                    fos.close();
                }
                inputStream.close();
                // init 传入的 datapath 必须是包含 tessdata 的目录
                return tessBaseAPI.init(Environment.
                        getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getPath(),LANGUAGE);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}