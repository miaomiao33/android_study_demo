package com.example.android_study_demo_project.opencv.ffmpegUsage.livePush.livePushMain;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.MediaRecorder;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Surface;

import com.example.android_study_demo_project.opencv.ffmpegUsage.livePush.CameraView.EglHelper;
import com.example.android_study_demo_project.opencv.ffmpegUsage.livePush.CameraView.MediaInfoListener;
import com.example.android_study_demo_project.opencv.ffmpegUsage.livePush.CameraView.MediaPreparedListener;
import com.example.android_study_demo_project.opencv.ffmpegUsage.livePush.CameraView.RecordVideoDarrenPlayer;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.concurrent.CyclicBarrier;

import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.opengles.GL10;

/**
 * 视频、音频是两个独立线程、两个独立 MediaCodec，互不阻塞,两个编码器各自独立编码
 *
 * 音频和视频能复合为一个文件：两个共用了一个 MediaMuxer
 * 靠 `MediaMuxer`（MP4 复用器），先创建两条独立轨道（video 轨道 + audio 轨道），视频 H264 编码包、音频 AAC
 * 编码包分别交给 muxer，muxer 自动把两路流封装到同一个 mp4 文件。
 */
public class BaseVideoPush {
    private LivePush mLivePush ;
    private WeakReference<BaseVideoPush> mVideoRecorderWr = new WeakReference<>(this);
    /**
     * 硬编码 MediaCode 的 surface
     */
    //`MediaCodec.createInputSurface()` 返回的 InputSurface，不是 MediaCodec 主动去 “拉取、读取”Surface 数据

    //在 EGL 环境里，把 OpenGL 画面渲染到这个 Surface 上，底层 BufferQueue 自动传递图形 buffer 给编码器，
    // MediaCodec 再从自己的输入 buffer 队列拿到图形帧做编码
    private Surface mSurface;
    /**
     * 相机共享的 EGL 上下文
     */
    private EGLContext mEglContext;
    private Context mContext;
    private GLSurfaceView.Renderer mRenderer;
    private VideoRenderThread mRendererThread;
    private VideoEncoderThread mVideoThread;
    private AudioEncoderThread mAudioEncoderThread;
    private AudioRecorderThread mAudioRecorderThread;
    private MediaCodec mVideoCodec;
    private MediaCodec mAudioCodec;
    private static final int AUDIO_SAMPLE_RATE = 44100;
    private static final int AUDIO_CHANNELS = 2;

    public void setRenderer(GLSurfaceView.Renderer renderer) {
        this.mRenderer = renderer;
        mRendererThread = new VideoRenderThread(mVideoRecorderWr);
    }

    public BaseVideoPush(Context context, EGLContext eglContext) {
        this.mContext = context;
        //和外面共享的 EGLContext
        this.mEglContext = eglContext;
    }

    private void start() {
        mRendererThread.start();;
        mVideoThread.start();
        mAudioEncoderThread.start();
        mAudioRecorderThread.start();
    }

    public void startPush()
    {
        mLivePush.initConnect();
    }
    public void stopPush()
    {
        mRendererThread.requestExit();
        mVideoThread.requestExit();
        mAudioEncoderThread.requestExit();
        mAudioRecorderThread.requestExit();
        mLivePush.stop();
    }

    /**
     * 初始化参数
     * @param videoWidth 录制宽度
     * @param videoHeight 录制高度
     */
    public void initVideo(String liveUrl, int videoWidth, int videoHeight)
    {
        try {
            mLivePush = new LivePush(liveUrl);

            //这里是 LivePush 的连接
            mLivePush.setConnectListener(new LivePush.ConnectListener() {
                @Override
                public void connectError(int errorCode, String errorMsg) {
                    //Log.e("TAG", "connectError:  errorCode:"+ errorCode);
                    //Log.e("TAG", "connectError:  errorMsg:"+ errorMsg);

                    // LivePush 连接失败再去回调 BaseVideoPush 的 mConnectListener
                    if(mConnectListener != null)
                    {
                        mConnectListener.connectError(errorCode,errorMsg);
                    }
                }

                @Override
                public void connectSuccess() {
                    //Log.e("TAG", "connectSuccess: 可以推流了");
                    start();
                    // LivePush 连接成功再去回调 BaseVideoPush 的 mConnectListener
                    if(mConnectListener != null)
                    {
                        mConnectListener.connectSuccess();
                    }
                }
            });

            mRendererThread.setSize(videoWidth,videoHeight);
            initVideoCodec(videoWidth,videoHeight);
            initAudioCodec(AUDIO_SAMPLE_RATE,AUDIO_CHANNELS);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化视频的 MediaCodec
     * @param width
     * @param height
     */
    private void initVideoCodec(int width, int height) throws IOException {
        //记录编码参数，输出AVC，AVC是H264，HEVC是H265
        MediaFormat videoFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC,width,height);
        //设置颜色格式
        videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        //码率
        videoFormat.setInteger(MediaFormat.KEY_BIT_RATE,width * height * 4);
        //帧率 fps
        videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE,24);
        //关键帧间隔，单位秒
        videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL,1);

        //创建编码器
        mVideoCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);

        mVideoCodec.configure(videoFormat,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);
        mSurface = mVideoCodec.createInputSurface();//下面创建EGL上下文会使用

        //开启一个编码采集 InputSurface 上的数据，合成视频
        mVideoThread = new VideoEncoderThread(mVideoRecorderWr);

    }

    /**
     * 初始化音频的 MediaCodec
     * @param sampleRate
     * @param channels
     */
    private void initAudioCodec(int sampleRate, int channels) throws IOException {
        MediaFormat audioFormat = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC,sampleRate,channels);
        audioFormat.setInteger(MediaFormat.KEY_BIT_RATE,96000);
        audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE,MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        audioFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE,sampleRate * channels * 2);//一通道是16位，即2字节

        //创建编码器
        mAudioCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC);

        mAudioCodec.configure(audioFormat,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);

        mAudioRecorderThread = new AudioRecorderThread(mVideoRecorderWr);
        //开启一个编码采集音乐播放器回调的 PCM 数据，合成视频
        mAudioEncoderThread = new AudioEncoderThread(mVideoRecorderWr);
    }

    /**
     * 视频的渲染线程
     */
    private static final class VideoRenderThread extends Thread{
        private WeakReference<BaseVideoPush> mVideoRecorderWr;
        private volatile boolean mShouldExit = false;
        private EglHelper mEglHelper;
        private boolean mHasCreatedContext = false;
        private boolean mHasSurfaceCreated = false;
        private boolean mHasSurfaceChanged = false;
        private int mWidth;
        private int mHeight;
        private MediaCodec mVideoCodec;

        public VideoRenderThread(WeakReference<BaseVideoPush> videoRecorderWr) {
            this.mVideoRecorderWr = videoRecorderWr;
            mEglHelper = new EglHelper();
            mVideoCodec = videoRecorderWr.get().mVideoCodec;
        }


        @Override
        public void run() {
            try {
                while (true){
                    if(mShouldExit)
                    {
                        return;
                    }

                    BaseVideoPush videoRecorder = mVideoRecorderWr.get();
                    if(videoRecorder == null)
                    {
                        return;
                    }

                    //创建 EGL 上下文
                    if(!mHasCreatedContext)
                    {
                        mEglHelper.init(videoRecorder.mSurface,videoRecorder.mEglContext);
                        mHasCreatedContext = true;
                    }

                    //回调 Render
                    GL10 gl = (GL10) mEglHelper.getEglContext().getGL();
                    if(!mHasSurfaceCreated)
                    {
                        videoRecorder.mRenderer.onSurfaceCreated(gl,mEglHelper.getEglConfig());
                        mHasSurfaceCreated = true;
                    }

                    if(!mHasSurfaceChanged)
                    {
                        videoRecorder.mRenderer.onSurfaceChanged(gl,mWidth,mHeight);
                        mHasSurfaceChanged = true;
                    }

                    //gl绘制画面（画在EGL后台缓冲）
                    videoRecorder.mRenderer.onDrawFrame(gl);

                    //更新到 Surface 中
                    //把当前 EGL 后台渲染缓冲区（back buffer）提交给 Surface（BufferQueue）
                    mEglHelper.swapBuffers();

                    //60 fps
                    Thread.sleep(16);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                onDestroy();
            }
        }

        private void onDestroy() {
            mEglHelper.release();
        }

        public void setSize(int width, int height) {
            this.mWidth = width;
            this.mHeight = height;
        }

        private void requestExit()
        {
            mShouldExit = true;
        }
    }
    private RecordListener mRecordListener;

    public void setOnRecordListener(RecordListener recordListener) {
        this.mRecordListener = recordListener;
    }

    public interface RecordListener {
        void onTime(long times);
    }

    /**
     * 视频的编码线程
     */
    private static final class VideoEncoderThread extends Thread{

        private WeakReference<BaseVideoPush> mVideoRecorderWr;

        private volatile boolean mShouldExit = false;

        private MediaCodec mVideoCodec;
        private MediaCodec.BufferInfo mBufferInfo;
        private long mVideoPts = 0;
        private byte[] mVideoSps;
        private byte[] mVideoPps;


        public VideoEncoderThread(WeakReference<BaseVideoPush> videoRecorderWr) {
            this.mVideoRecorderWr = videoRecorderWr;
            mVideoCodec = videoRecorderWr.get().mVideoCodec;
            mBufferInfo = new MediaCodec.BufferInfo();
        }


        @Override
        public void run() {
            try {
                mVideoCodec.start();
                while (true) {
                    if (mShouldExit) {
                        return;
                    }

                    BaseVideoPush videoRecorder = mVideoRecorderWr.get();
                    if (videoRecorder == null) {
                        return;
                    }

                    //代码先不写，先测试，从 surface 上获取数据，编码成 h264，通过 MediaMuxer 合成 MP4
                    int outputBufferIndex = mVideoCodec.dequeueOutputBuffer(mBufferInfo, 0);
                    if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        //拿到视频的轨道
                        //获取 SPS 和 PPS
                        Log.i("TAG","获取 SPS 和 PPS");
                        ByteBuffer byteBuffer = mVideoCodec.getOutputFormat().getByteBuffer("csd-0");
                        //`byteBuffer.remaining()`：返回 Buffer 当前位置到 limit 之间剩余可读字节数,limit：可读区域末尾
                        mVideoSps = new byte[byteBuffer.remaining()];
                        //从 buffer 当前 position 位置，读取 `data.length` 字节，写入 data 数组；
                        //读完后 buffer 的 position 自动向后移动 data.length
                        byteBuffer.get(mVideoSps,0,mVideoSps.length);
                        Log.i("SPS",byteToHexString(mVideoSps));

                        byteBuffer = mVideoCodec.getOutputFormat().getByteBuffer("csd-1");
                        mVideoPps = new byte[byteBuffer.remaining()];
                        byteBuffer.get(mVideoPps,0,mVideoPps.length);
                        Log.i("PPS",byteToHexString(mVideoPps));
                    } else {
                        while (outputBufferIndex >= 0) {
                            //获取数据
                            ByteBuffer outBuffer = mVideoCodec.getOutputBuffers()[outputBufferIndex];
                            outBuffer.position(mBufferInfo.offset);
                            //限制这一段数据取出
                            outBuffer.limit(mBufferInfo.offset + mBufferInfo.size);

                            //修改pts
                            if (mVideoPts == 0) {
                                mVideoPts = mBufferInfo.presentationTimeUs;
                            }
                            mBufferInfo.presentationTimeUs -= mVideoPts;

                            //在关键帧之前先把 SPS 和 PPS 推到流媒体服务器
                            if(mBufferInfo.flags == MediaCodec.BUFFER_FLAG_KEY_FRAME)
                            {
                                mVideoRecorderWr.get().mLivePush.pushSPSPps(mVideoSps,mVideoSps.length,
                                        mVideoPps,mVideoPps.length);
                            }

                            //再把每一帧推送上去
                            byte[] data = new byte[outBuffer.remaining()];
                            outBuffer.get(data,0,data.length);
//                            Log.i("TAG",byteToHexString(data));
                            mVideoRecorderWr.get().mLivePush.pushVideo(data,data.length,
                                    mBufferInfo.flags == MediaCodec.BUFFER_FLAG_KEY_FRAME);


                            //回调当前录制时间
                            if (videoRecorder.mRecordListener != null) {
                                videoRecorder.mRecordListener.onTime(mBufferInfo.presentationTimeUs / 1000);
                            }

                            mVideoCodec.releaseOutputBuffer(outputBufferIndex, false);
                            //取数据
                            outputBufferIndex = mVideoCodec.dequeueOutputBuffer(mBufferInfo, 0);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                onDestroy();
            }

        }

        private void onDestroy() {
            try {
                if(mVideoCodec != null)
                {
                    mVideoCodec.stop();
                    mVideoCodec.release();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void requestExit() {
            mShouldExit = true;
        }

    }

    /**
     * 音频的编码线程
     */
    private static final class AudioEncoderThread extends Thread{
        private WeakReference<BaseVideoPush> mVideoRecorderWr;

        private volatile boolean mShouldExit = false;

        private MediaCodec mAudioCodec;
        private MediaCodec.BufferInfo mBufferInfo;
        private long mAudioPts = 0;


        public AudioEncoderThread(WeakReference<BaseVideoPush> videoRecorderWr) {
            this.mVideoRecorderWr = videoRecorderWr;
            mAudioCodec = videoRecorderWr.get().mAudioCodec;
            mBufferInfo = new MediaCodec.BufferInfo();
        }


        @Override
        public void run() {
            try {
                //开启 AudioCodec
                mAudioCodec.start();

                while (true) {
                    if (mShouldExit) {
                        return;
                    }

                    BaseVideoPush videoRecorder = mVideoRecorderWr.get();
                    if (videoRecorder == null) {
                        return;
                    }

                    //获取音频数据，音频数据来源：麦克风 pcm 数据
                    int outputBufferIndex = mAudioCodec.dequeueOutputBuffer(mBufferInfo, 0);
                        while (outputBufferIndex >= 0) {
                            //获取数据
                            ByteBuffer outBuffer = mAudioCodec.getOutputBuffers()[outputBufferIndex];
                            outBuffer.position(mBufferInfo.offset);
                            //限制这一段数据取出
                            outBuffer.limit(mBufferInfo.offset + mBufferInfo.size);

                            //修改pts
                            if (mAudioPts == 0) {
                                mAudioPts = mBufferInfo.presentationTimeUs;
                            }
                            mBufferInfo.presentationTimeUs -= mAudioPts;

                            //写入数据
                            //打印一下音频的AAC数据
                            byte[] data = new byte[outBuffer.remaining()];
                            outBuffer.get(data,0,data.length);
//                            Log.i("TAG",byteToHexString(data));
                            mVideoRecorderWr.get().mLivePush.pushAudio(data,data.length);

                            //回调当前录制时间
                            if (videoRecorder.mRecordListener != null) {
                                videoRecorder.mRecordListener.onTime(mBufferInfo.presentationTimeUs / 1000);
                            }

                            mAudioCodec.releaseOutputBuffer(outputBufferIndex, false);
                            //取数据
                            outputBufferIndex = mAudioCodec.dequeueOutputBuffer(mBufferInfo, 0);
                        }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                onDestroy();
            }

        }

        private void onDestroy() {
            try {
                if(mAudioCodec != null)
                {
                    mAudioCodec.stop();
                    mAudioCodec.release();
                }
            }catch (Exception e){
                e.printStackTrace();
            }

        }

        private void requestExit() {
            mShouldExit = true;
        }

    }

    /**
     * 音频的采集线程
     */
    private static final class AudioRecorderThread extends Thread{
        private volatile boolean mShouldExit = false;
        private MediaCodec mAudioCodec;
        private long mAudioPts = 0;
        private AudioRecord mAudioRecord;
        //PCM 数据
        private byte[] mAudioData;
        private int mMinBufferSize;


        public AudioRecorderThread(WeakReference<BaseVideoPush> videoRecorderWr) {

            mAudioCodec = videoRecorderWr.get().mAudioCodec;

            mMinBufferSize = AudioRecord.getMinBufferSize(
                    BaseVideoPush.AUDIO_SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_STEREO,
                    AudioFormat.ENCODING_PCM_16BIT);

            mAudioRecord = new AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    BaseVideoPush.AUDIO_SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_STEREO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    mMinBufferSize);
            mAudioData = new byte[mMinBufferSize];
        }


        @Override
        public void run() {
            try {
                //开启录制
                mAudioRecord.startRecording();
                while (true) {
                    if (mShouldExit) {
                        return;
                    }

                    //不断读取麦克风的 PCM 数据
                    mAudioRecord.read(mAudioData,0,mMinBufferSize);

                    //把数据写入到 mAudioCodec 的 InputBuffer
                    int inputBufferIndex = mAudioCodec.dequeueInputBuffer(0);
                    if(inputBufferIndex >= 0)
                    {
                        ByteBuffer byteBuffer = mAudioCodec.getInputBuffers()[inputBufferIndex];
                        byteBuffer.clear();//清空然后写入
                        byteBuffer.put(mAudioData);
                        //pts 44100 * 2 * 2(一秒总字节)，每次累加的是【微秒】
                        mAudioPts += (long) (mMinBufferSize * 1.0f /
                                (BaseVideoPush.AUDIO_SAMPLE_RATE * BaseVideoPush.AUDIO_CHANNELS * 2) * 1000000L);
                        //size 22050 * 2 * 2(本次回调拿到的 PCM 字节总数)
                        mAudioCodec.queueInputBuffer(inputBufferIndex,0,mMinBufferSize,mAudioPts,0);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                onDestroy();
            }

        }

        private void onDestroy() {
            try {
                if(mAudioCodec != null)
                {
                    mAudioCodec.stop();
                    mAudioCodec.release();
                }
            }catch (Exception e){
                e.printStackTrace();
            }

        }

        private void requestExit() {
            mShouldExit = true;
        }

    }

    /**
     * byte数组转十六进制字符串（大写，空格分隔，方便看SPS/PPS）
     * @param data 字节数组
     * @return hex字符串
     */
    public static String byteToHexString(byte[] data) {
        if (data == null || data.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (byte b : data) {
            // &0xFF 转无符号，再转两位十六进制
            String hex = Integer.toHexString(b & 0xFF);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
            sb.append(" ");
            i++;
            if(i > 20)
            {
                break;
            }
        }
        // 去掉末尾多余空格
        return sb.toString().trim().toUpperCase();
    }

    private LivePush.ConnectListener mConnectListener;

    public void setConnectListener(LivePush.ConnectListener connectListener) {
        this.mConnectListener = connectListener;
    }
}
