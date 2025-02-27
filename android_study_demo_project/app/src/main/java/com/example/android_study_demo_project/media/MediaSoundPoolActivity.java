package com.example.android_study_demo_project.media;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android_study_demo_project.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//soundPool的使用以及录制音频
public class MediaSoundPoolActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    MediaSoundPoolAdapter mediaSoundPoolAdapter;
    SoundPool soundPool;
    Map<String,Sound> modelMap;
    Button optionButton;
    AudioRecord audioRecord;
    List<Sound> soundModelList = new ArrayList<>();
    MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_sound_pool);
//        floatingActionButton = (FloatingActionButton)findViewById(R.id.fab_sound_add);
//        floatingActionButton.setOnClickListener(view -> {
//            //点击了添加按钮
//            addSound();
//        });
        optionButton = (Button)findViewById(R.id.bt_sound_pool_option);
        optionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //点击了添加按钮
                addSound(optionButton.getText().toString());
            }
        });

        //RecyclerView的初始化
        recyclerView = findViewById(R.id.rv_sound_pool);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MediaSoundPoolActivity.this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);

        //初始化sound pool
        initSoundPool();
        modelMap = new LinkedHashMap<>();//数据
        mediaPlayer = new MediaPlayer();//用于播放wav文件
        //初始化音频数据
        getModelList();

        mediaSoundPoolAdapter = new MediaSoundPoolAdapter(
                MediaSoundPoolActivity.this,
                recyclerView,//用于获取点击的view位置
                soundModelList);
        //暴露出来的点击事件
        mediaSoundPoolAdapter.setOnItemClickListener(new itemClick());
        recyclerView.setAdapter(mediaSoundPoolAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        soundPool.autoPause();
        if(!modelMap.isEmpty())
        {
            modelMap.forEach((key,valueModel)->{
                if(valueModel.getSoundId() != -1)
                {
                    soundPool.unload(valueModel.getSoundId());
                }
            });
        }

        soundPool.release();
    }

    //初始化sound pool
    void initSoundPool() {
        if (soundPool == null) {
            // 5.0 及 之后
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                AudioAttributes audioAttributes = null;
                audioAttributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build();

                soundPool = new SoundPool.Builder()
                        .setMaxStreams(6)//最多6个音频源
                        .setAudioAttributes(audioAttributes)
                        .build();
            } else { // 5.0 以前
                soundPool = new SoundPool(6,
                        AudioManager.STREAM_MUSIC,//流类型
                        0);//采样率转换器质量,目前没有什么作用,默认填充0
            }
        }
    }

    //初始化音频数据
    void getModelList()
    {
        Map tempMap = new LinkedHashMap();
        //根据load获取到的id为soundId
        //wav文件的路径放在系统的音频目录下
        String wavFilePath = getExternalFilesDir(Environment.DIRECTORY_PODCASTS).getAbsolutePath();
        //获取系统的音频目录下所有文件
        File rootFile = new File(wavFilePath);
        File[] tempList = rootFile.listFiles();
        for (File file:tempList)
        {
            if(file.isFile())
            {
                String fileName = file.getName();
                if(fileName.endsWith(".wav")||fileName.endsWith(".mp3"))
                {
                    if(!modelMap.containsKey(fileName))
                    {
                        //没有该文件，新加的
                        tempMap.put(fileName,new Sound(fileName,-1,file.getAbsolutePath()));
                    }else
                    {
                        //有该文件
                        tempMap.put(fileName,modelMap.get(fileName));
                    }
                }
            }
        }

        modelMap.clear();
        modelMap.putAll(tempMap);
        soundModelList.clear();
        modelMap.forEach((key,valueModel)->{
            soundModelList.add(valueModel);
        });
    }

    //点击了哪个item
    class itemClick implements MediaSoundPoolAdapter.OnItemClickListener{

        @Override
        public void onItemClick(int position, Sound model) {
            if(model.name.endsWith(".wav"))
            {
                mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(model.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mediaPlayer.prepareAsync();

                //异步准备
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        //异步准备完毕
                        mediaPlayer.start();
                    }
                });
                //播放完毕
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        //播放完毕释放
                        mediaPlayer.release();
                    }
                });

            }else
            {
                if(model.getSoundId() == -1)
                {
                    //没有load过
                    Toast.makeText(MediaSoundPoolActivity.this,"正在加载",Toast.LENGTH_SHORT).show();
                    int soundId = soundPool.load(model.absolutePath,1);
                    model.setSoundId(soundId);
                    soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                        @Override
                        public void onLoadComplete(SoundPool soundPool, int i, int i1) {
                            //加载完
                            if(soundPool != null)
                            {
                                Toast.makeText(MediaSoundPoolActivity.this,"加载完毕",Toast.LENGTH_SHORT).show();
                                soundPool.play(model.getSoundId(),
                                        0.5f,0.5f,//左右声道音量
                                        1,//优先权
                                        0,//循环
                                        1.0f);//速率
                            }
                        }
                    });
                }else
                {

                    soundPool.play(model.getSoundId(),
                            0.5f,0.5f,//左右声道音量
                            1,//优先权
                            0,//循环
                            1.0f);//速率
                }
            }
        }
    }

    //录音线程
    Thread recordingAudioThread = null;

    //点击了添加按钮
    void addSound(String text)
    {
        if(TextUtils.equals(text,"开始录制"))
        {
            //读取音频的准备
            optionButton.setText("结束录制");
            //获得缓冲区字节大小
            int bufferSizeInBytes = AudioRecord.getMinBufferSize(
                    //采样率
                    //44100是目前的标准，但是某些设备仍然支持22050，16000，11025，
                    //采用频率一般分为22.05KHz，44.1KHz，48KHz三个等级
                        44100,
                    //音频通道 双声道
                    AudioFormat.CHANNEL_IN_STEREO,
                    //PCM编码
                    AudioFormat.ENCODING_PCM_16BIT
                    );
            audioRecord = new AudioRecord(
                    //音频源：音频输入-麦克风
                    MediaRecorder.AudioSource.MIC,
                    //采样率
                    44100,
                    //音频通道 双声道，（单声道CHANNEL_IN_MONO声音会变成童音）
                    AudioFormat.CHANNEL_IN_STEREO,
                    //PCM编码
                    AudioFormat.ENCODING_PCM_16BIT,
                    //缓冲区大小
                    bufferSizeInBytes
                    );
            //开始录音
            audioRecord.startRecording();

            //临时的cache文件
            String audioCacheFilePath = getExternalFilesDir(Environment.DIRECTORY_MUSIC).getAbsolutePath()
                    +"audio_cache.pcm";
            //创建数据流，一边录制，将缓存导入数据流
            recordingAudioThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    File file = new File(audioCacheFilePath);
                    Log.i("audio cache file path:",audioCacheFilePath);
                    //以防文件重复
                    if(!file.exists())
                    {
                        try {
                            file.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    FileOutputStream outputStream = null;
                    try {
                        outputStream = new FileOutputStream(file);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        Log.e("SoundPool","临时文件未找到");
                    }

                    byte[] data = new byte[bufferSizeInBytes];
                    List<byte[]> dataCache = new ArrayList<>();//采用二级缓存
                    int readResultCode;
                    int cachePosition = 0;//在缓存中的位置
                    if(outputStream != null)
                    {
                        //当前录音没有被中断
                        while (TextUtils.equals(optionButton.getText(),"结束录制")&&!recordingAudioThread.isInterrupted())
                        {
                            readResultCode = audioRecord.read(data,0,bufferSizeInBytes);

                            if(readResultCode != AudioRecord.ERROR_INVALID_OPERATION)
                            {
                                dataCache.add(data);
                                cachePosition++;
                                try {
                                    outputStream.write(dataCache.get(cachePosition-1));
                                    Log.i("audioTest","写入录音数据->"+readResultCode);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                        }
                    }

                    //关闭数据流
                    try {
                        outputStream.flush();
                        outputStream.close();
                        dataCache.clear();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            recordingAudioThread.start();
        }else
        {
            //结束录制
            //释放所有
            optionButton.setText("开始录制");
            //必须先打断，不然线程中可能会用到空的audioRecord
            recordingAudioThread.isInterrupted();
            if(audioRecord != null)
            {
                audioRecord.stop();
                audioRecord.release();
                audioRecord = null;
                recordingAudioThread = null;

                saveTheData();
            }

        }
    }

    //保存文件
    void saveTheData()
    {
        //wav文件的路径放在系统的音频目录下
        String wavFilePath = this.getExternalFilesDir(Environment.DIRECTORY_PODCASTS) +
                "/wav_" + System.currentTimeMillis() + ".wav";
        PcmToWavUtil ptwUtil = new PcmToWavUtil();
        //临时的音频cache文件
        String audioCacheFilePath = getExternalFilesDir(Environment.DIRECTORY_MUSIC).getAbsolutePath()+"audio_cache.pcm";
        ptwUtil.pcmToWav(audioCacheFilePath,wavFilePath,true);
        //刷新
        getModelList();
        mediaSoundPoolAdapter.notifyDataSetChanged();
    }

}
