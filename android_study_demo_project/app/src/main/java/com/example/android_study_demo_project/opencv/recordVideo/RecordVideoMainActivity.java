package com.example.android_study_demo_project.opencv.recordVideo;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.android_study_demo_project.R;

public class RecordVideoMainActivity extends AppCompatActivity implements Camera.PreviewCallback, View.OnClickListener {
    private CameraHelper mCameraHelper;
    private VideoCodec videoCodec;
    private Button startButton;
    private String TAG = RecordVideoMainActivity.class.getSimpleName();
    private TextureView textureView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_video_main);
        mCameraHelper = new CameraHelper();
        mCameraHelper.setPreviewCallback(this);

        textureView = findViewById(R.id.texture_view_living_stream);
        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int width, int height) {
                mCameraHelper.startPreview(surfaceTexture);
            }

            @Override
            public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surfaceTexture, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surfaceTexture) {
                mCameraHelper.stopPreview();
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surfaceTexture) {

            }
        });

        videoCodec = new VideoCodec();
        startButton = (Button)findViewById(R.id.bt_start_living_stream);
        startButton.setOnClickListener(this);
    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        videoCodec.queueEncode(bytes,mCameraHelper);
    }

    @Override
    public void onClick(View view) {
        Button button = (Button) view;
        if(videoCodec.isRecording())
        {
            button.setText("开始录制");
            videoCodec.stopRecording();

        }else {
            Log.i(TAG,"startPreview1"+
                    getExternalFilesDir(Environment.DIRECTORY_MOVIES).getPath()+"/a.mp4");
            button.setText("停止录制");
            videoCodec.startRecording(
                            getExternalFilesDir(Environment.DIRECTORY_MOVIES).getPath()+"/a.mp4",
                    mCameraHelper.getYWidth(),
                    mCameraHelper.getYHeight(),
                    90);
        }
    }
}