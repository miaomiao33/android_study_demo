package com.example.android_study_demo_project.opencv.ffmpegUsage.livePush.CameraView;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.concurrent.BrokenBarrierException;
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
public class BaseVideoRecorder {
    private WeakReference<BaseVideoRecorder> mVideoRecorderWr = new WeakReference<>(this);
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
    private AudioEncoderThread mAudioThread;
    private MediaMuxer mMediaMuxer;
    private MediaCodec mVideoCodec;
    private MediaCodec mAudioCodec;
    private CyclicBarrier mStartCb = new CyclicBarrier(2);
    private CyclicBarrier mDestroyCb = new CyclicBarrier(2);
    private RecordVideoDarrenPlayer mMusicPlayer;

    public void setRenderer(GLSurfaceView.Renderer renderer) {
        this.mRenderer = renderer;
        mRendererThread = new VideoRenderThread(mVideoRecorderWr);
    }

    public BaseVideoRecorder(Context context, EGLContext eglContext) {
        this.mContext = context;
        //和外面共享的 EGLContext
        this.mEglContext = eglContext;
        mMusicPlayer = new RecordVideoDarrenPlayer();
        //异步准备（带队列）
        mMusicPlayer.setOnPreparedListener(new MediaPreparedListener() {
            @Override
            public void onPrepared() {
                Log.i("TAG","准备完毕");
                //播放背景音乐
                mMusicPlayer.playOpenSLESWithQueue();
                start();
            }
        });
        //在音乐播放器准备好了进行的回调
        mMusicPlayer.setInfoListener(mMediaInfoListener);
    }

    private MediaInfoListener mMediaInfoListener = new MediaInfoListener() {
        private long mAudioPts = 0;
        private int mSampleRate = 0;
        private int mChannels = 0;
        @Override
        public void musicInfo(int sampleRate, int channels) {
            //获取了音频信息
            try {
                this.mSampleRate = sampleRate;
                this.mChannels = channels;
                initAudioCodec(sampleRate,channels);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void callbackPcm(byte[] pcmData, int size) {
            //native 端音乐播放器获取到的 pcm 数据
            //把数据写入到 mAudioCodec 的 InputBuffer
            int inputBufferIndex = mAudioCodec.dequeueInputBuffer(0);
            if(inputBufferIndex >= 0)
            {
                ByteBuffer byteBuffer = mAudioCodec.getInputBuffers()[inputBufferIndex];
                byteBuffer.clear();//清空然后写入
                byteBuffer.put(pcmData);
                //pts 44100 * 2 * 2(一秒总字节)，每次累加的是【微秒】
//                mAudioPts += size * 1.0f / (1.0f/mSampleRate * mChannels * 2);
                mAudioPts += size * 1.0f / (mSampleRate * mChannels * 2) * 1000000L;
                //size 22050 * 2 * 2(本次回调拿到的 PCM 字节总数)
                mAudioCodec.queueInputBuffer(inputBufferIndex,0,size,mAudioPts,0);
            }
        }
    };

    public void start()
    {
        mRendererThread.start();
        mVideoThread.start();
        mAudioThread.start();
    }

    public void startRecord()
    {
        //准备完了直接就进行回调然后进行播放
        mMusicPlayer.prepareAsyncWithQueue();
    }
    public void stopRecord()
    {
        mMusicPlayer.stop();
        mRendererThread.requestExit();
        mVideoThread.requestExit();
        mAudioThread.requestExit();
    }

    /**
     * 初始化参数
     * @param audioPath 背景音乐地址
     * @param outPath 输出文件路径
     * @param videoWidth 录制宽度
     * @param videoHeight 录制高度
     */
    public void initVideo(String audioPath, String outPath, int videoWidth, int videoHeight)
    {
        try {
            mRendererThread.setSize(videoWidth,videoHeight);
            mMediaMuxer = new MediaMuxer(outPath,MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            mMusicPlayer.setDataSource(audioPath);
            initVideoCodec(videoWidth,videoHeight);
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
        //开启一个编码采集音乐播放器回调的 PCM 数据，合成视频
        mAudioThread = new AudioEncoderThread(mVideoRecorderWr);

        //开启 AudioCodec
        mAudioCodec.start();
    }

    /**
     * 视频的渲染线程
     */
    private static final class VideoRenderThread extends Thread{
        private WeakReference<BaseVideoRecorder> mVideoRecorderWr;
        private volatile boolean mShouldExit = false;
        private EglHelper mEglHelper;
        private boolean mHasCreatedContext = false;
        private boolean mHasSurfaceCreated = false;
        private boolean mHasSurfaceChanged = false;
        private int mWidth;
        private int mHeight;
        private MediaCodec mVideoCodec;

        public VideoRenderThread(WeakReference<BaseVideoRecorder> videoRecorderWr) {
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

                    BaseVideoRecorder videoRecorder = mVideoRecorderWr.get();
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
        private final MediaMuxer mMediaMuxer;
        private WeakReference<BaseVideoRecorder> mVideoRecorderWr;

        private volatile boolean mShouldExit = false;

        private MediaCodec mVideoCodec;
        private MediaCodec.BufferInfo mBufferInfo;
        private int mVideoTrackIndex = -1;
        private long mVideoPts = 0;
        private CyclicBarrier mStartCb;
        private CyclicBarrier mDestroyCb;


        public VideoEncoderThread(WeakReference<BaseVideoRecorder> videoRecorderWr) {
            this.mVideoRecorderWr = videoRecorderWr;
            mVideoCodec = videoRecorderWr.get().mVideoCodec;
            mMediaMuxer = videoRecorderWr.get().mMediaMuxer;
            mBufferInfo = new MediaCodec.BufferInfo();
            mStartCb = videoRecorderWr.get().mStartCb;
            mDestroyCb = videoRecorderWr.get().mDestroyCb;
        }


        @Override
        public void run() {
            try {
                mVideoCodec.start();
                while (true) {
                    if (mShouldExit) {
                        return;
                    }

                    BaseVideoRecorder videoRecorder = mVideoRecorderWr.get();
                    if (videoRecorder == null) {
                        return;
                    }

                    //代码先不写，先测试，从 surface 上获取数据，编码成 h264，通过 MediaMuxer 合成 MP4
                    int outputBufferIndex = mVideoCodec.dequeueOutputBuffer(mBufferInfo, 0);
                    if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        //拿到视频的轨道
                        mVideoTrackIndex = mMediaMuxer.addTrack(mVideoCodec.getOutputFormat());
                        //开启混合
                        mMediaMuxer.start();
                        mStartCb.await();//等待Audio执行到等待的地方
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

                            //写入数据
                            mMediaMuxer.writeSampleData(mVideoTrackIndex, outBuffer, mBufferInfo);

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
                mDestroyCb.await();
                if(mMediaMuxer != null)
                {
                    mMediaMuxer.stop();
                    mMediaMuxer.release();
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
        private final MediaMuxer mMediaMuxer;
        private WeakReference<BaseVideoRecorder> mVideoRecorderWr;

        private volatile boolean mShouldExit = false;

        private MediaCodec mAudioCodec;
        private MediaCodec.BufferInfo mBufferInfo;
        private int mAudioTrackIndex = -1;
        private long mAudioPts = 0;
        private final CyclicBarrier mStartCb;
        private final CyclicBarrier mDestroyCb;


        public AudioEncoderThread(WeakReference<BaseVideoRecorder> videoRecorderWr) {
            this.mVideoRecorderWr = videoRecorderWr;
            mAudioCodec = videoRecorderWr.get().mAudioCodec;
            mMediaMuxer = videoRecorderWr.get().mMediaMuxer;
            mBufferInfo = new MediaCodec.BufferInfo();
            mStartCb = videoRecorderWr.get().mStartCb;
            mDestroyCb = videoRecorderWr.get().mDestroyCb;
        }


        @Override
        public void run() {
            try {
                while (true) {
                    if (mShouldExit) {
                        return;
                    }

                    BaseVideoRecorder videoRecorder = mVideoRecorderWr.get();
                    if (videoRecorder == null) {
                        return;
                    }

                    //获取音频数据，音频数据来源：音乐播放器来，pcm 数据
                    int outputBufferIndex = mAudioCodec.dequeueOutputBuffer(mBufferInfo, 0);
                    if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        //拿到音频的轨道
                        mAudioTrackIndex = mMediaMuxer.addTrack(mAudioCodec.getOutputFormat());
                        //开启混合
                        //mMediaMuxer.start();
                        //mMediaMuxer在Video中开启一次即可
                        mStartCb.await();//等待Video执行过mMediaMuxer.start();
                    } else {
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
                            mMediaMuxer.writeSampleData(mAudioTrackIndex, outBuffer, mBufferInfo);

                            //回调当前录制时间
                            if (videoRecorder.mRecordListener != null) {
                                videoRecorder.mRecordListener.onTime(mBufferInfo.presentationTimeUs / 1000);
                            }

                            mAudioCodec.releaseOutputBuffer(outputBufferIndex, false);
                            //取数据
                            outputBufferIndex = mAudioCodec.dequeueOutputBuffer(mBufferInfo, 0);
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
                if(mAudioCodec != null)
                {
                    mAudioCodec.stop();
                    mAudioCodec.release();
                }
                mDestroyCb.await();
                //mMediaMuxer 在 Video 中进行释放
            }catch (Exception e){
                e.printStackTrace();
            }

        }

        private void requestExit() {
            mShouldExit = true;
        }

    }
}
