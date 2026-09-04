package com.example.android_study_demo_project.opencv.ffmpegUsage.livingStream;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

public class AudioPush extends BasePush{
    private AudioInfo mAudioInfo;
    private AudioRecord audioRecord;
    private boolean isPushing = false;
    private int minBufferSize;
    private FFmpegLiveStreamNativePlayer nativePlayer;

    public AudioPush(AudioInfo mAudioInfo, FFmpegLiveStreamNativePlayer nativePlayer) {
        this.mAudioInfo = mAudioInfo;
        this.nativePlayer = nativePlayer;

        int channelConfig = mAudioInfo.getChannel() == 1 ?
                AudioFormat.CHANNEL_IN_MONO : AudioFormat.CHANNEL_IN_STEREO;
        //最小缓冲区大小
        minBufferSize = AudioRecord.getMinBufferSize(mAudioInfo.getSampleRateInHz(),
                channelConfig, AudioFormat.ENCODING_PCM_16BIT);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                mAudioInfo.getSampleRateInHz(),
                channelConfig,
                AudioFormat.ENCODING_PCM_16BIT, minBufferSize);
    }


    @Override
    public void startPush() {
//        //- 参数 1：`44100` → **采样率 sampleRate：44100 Hz**
//        //- 参数 2：`2` → **声道数 channels：2 = 立体声 (Stereo)**
        nativePlayer.setAudioOptions(mAudioInfo.getSampleRateInHz(),mAudioInfo.getChannel());
        isPushing = true;
        //启动一个录音子线程
        new Thread(new AudioRecordTask()).start();
    }

    @Override
    public void stopPush() {
        isPushing = false;
        audioRecord.stop();
    }

    @Override
    public void release() {
        if(audioRecord != null)
        {
            audioRecord.release();
            audioRecord = null;
        }
    }

    class AudioRecordTask implements Runnable{
        @Override
        public void run() {
            //开始录音
            audioRecord.startRecording();
            while (isPushing)
            {
                //通过 AudioRecord 不断读取音频数据
                byte[] buffer = new byte[minBufferSize];
                int len = audioRecord.read(buffer, 0, buffer.length);
                if(len > 0)
                {
                    //传给 Native 代码，进行音频编码
                    nativePlayer.sendAudioPacket(buffer, len);
                }
            }
        }
    }
}
