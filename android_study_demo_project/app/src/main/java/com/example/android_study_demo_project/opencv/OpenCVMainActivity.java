package com.example.android_study_demo_project.opencv;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.android_study_demo_project.MainActivity;
import com.example.android_study_demo_project.R;

import org.opencv.android.OpenCVLoader;

public class OpenCVMainActivity extends AppCompatActivity {
    private  final String TAG = OpenCVMainActivity.class.getSimpleName();
    private Button getVersionButton;

    private OpenCVJNIJavaCallC openCVJNIJavaCallC = new OpenCVJNIJavaCallC();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_cv_main);
        getVersionButton = (Button) findViewById(R.id.bt_get_opencv_version_from_native);

        OpenCVClick click = new OpenCVClick();
        getVersionButton.setOnClickListener(click);

        //OpenCV验证
        if(OpenCVLoader.initDebug())
        {
            Log.i(TAG,"OpenCV 加载成功"+OpenCVLoader.OPENCV_VERSION);
        }else
        {
            Log.i(TAG,"OpenCV 加载失败");
        }
    }

    /**
     * 获取 OpenCV的版本
     */
    void getOpenCVVersion()
    {
        String version = openCVJNIJavaCallC.getOpenCVInfoFromJNI();
        TextView textView = (TextView) findViewById(R.id.tv_opencv_version);
        textView.setText("opencv version:"+version);
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
            }
        }
    }
}