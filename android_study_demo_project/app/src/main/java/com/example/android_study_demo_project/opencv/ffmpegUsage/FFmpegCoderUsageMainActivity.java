package com.example.android_study_demo_project.opencv.ffmpegUsage;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android_study_demo_project.R;

public class FFmpegCoderUsageMainActivity extends AppCompatActivity {

    private EditText mInput;
    private Button mPlayer;
    private Button mPause;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ffmpeg_coder_usage_main);
        mInput = (EditText) findViewById(R.id.et_ffmpeg_coder_input);
//        mInput.setText("https://ra01.sycdn.kuwo.cn/resource/n3/32/56/3260586875.mp3");
        //这里的url要去网站实时拿取，记得先刷新
        mInput.setText("http://kw-lw.kuwo.cn/4e3a0bf7603c73244c86f15c78050046/6a7c71ff/resource/30106/trackmedia/M500002cCQTw28Wdyu.mp3");
        mPlayer = (Button)findViewById(R.id.bt_ffmpeg_coder_play);
        mPause = (Button)findViewById(R.id.bt_ffmpeg_coder_pause);
        FFmpegCoderCLick coderCLick = new FFmpegCoderCLick();
        mPlayer.setOnClickListener(coderCLick);
        mPause.setOnClickListener(coderCLick);
    }

    void playAudio()
    {
        FFmpegCoderNativePlayer.play(mInput.getText().toString().trim());
    }

    void pauseAudio()
    {
        FFmpegCoderNativePlayer.stop();
    }

    public class FFmpegCoderCLick implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.bt_ffmpeg_coder_play:
                    playAudio();
                    break;
                case R.id.bt_ffmpeg_coder_pause:
                    pauseAudio();
                    break;
            }
        }
    }
}