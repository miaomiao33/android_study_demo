package com.example.android_study_demo_project.opencv.liveStreamingPush;

import static com.huawei.hms.framework.common.ContextCompat.startService;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.android_study_demo_project.opencv.recordVideo.VideoCodec;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;

/***
 * 控制直播过程
 * 不断从queue中取出RTMPPackage并发送
 */
public class ScreenLive extends Thread {

    static {
        System.loadLibrary("librtmpLib");
    }

    private String url;
    private MediaProjectionManager mediaProjectionManager;
    private boolean isLiving ;
    private LinkedBlockingQueue<RTMPPackage> queue = new LinkedBlockingQueue<>();
    private MediaProjection mediaProjection;

    private final String TAG = ScreenLive.class.getSimpleName();
    private LiveStreamingMainActivity.LiveStreamingActivityCallBack stopCallBack;

    //初始化
    public void startInitLive(MediaProjectionManager manager, String url,
                              LiveStreamingMainActivity.LiveStreamingActivityCallBack callBack)
    {
        this.url = url;
        this.mediaProjectionManager = manager;
        this.stopCallBack = callBack;
    }

    public void stopLive()
    {
        //添加一个空包最为结束信号
        addPackage(RTMPPackage.EMPTY_PACKAGE);
        isLiving = false;
    }

    //投屏请求结果处理
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data,
                                 LiveTaskManager.TaskBinder taskBinder){
        //用户授权
        if(requestCode == 100 && resultCode == Activity.RESULT_OK)
        {
            //获得截屏器
            assert data != null;
            Log.i("onActivityResult","stopService onActivityResult");
            mediaProjection = mediaProjectionManager.getMediaProjection(resultCode,data);
            //执行当前程序
            taskBinder.execute(this);
        }
    }

    //将要发送的包放在当前的queue中
    public void addPackage(RTMPPackage rtmpPackage)
    {
        if(!isLiving)
        {
            return;
        }
        queue.add(rtmpPackage);
    }

    @Override
    public void run() {
        //连接服务器并发送数据
        //1.连接服务器
        if(!connect(url))
        {
            Log.e(TAG,"连接服务器失败");
            //回调释放Service
            stopCallBack.callBack();
            return;
        }else{
            Log.i(TAG,"stopService 连接服务器成功");
        }

        isLiving = true;
        //开始编解码
        VideoCodecLiveStream videoCodec = new VideoCodecLiveStream(this);
        videoCodec.startLive(mediaProjection);

        AudioCodec audioCodec = new AudioCodec(this);
        audioCodec.startLive();

        boolean isSend = true;
        while (isLiving && isSend)
        {
            RTMPPackage rtmpPackage = null;
            try {
                //如果队列积压超过200包，持续丢弃旧包，直到数量≤200
                checkDrop();
                rtmpPackage = queue.take();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            if(rtmpPackage == null)
            {
                break;
            }

            //判断是不是最后传进来的空包
            if(rtmpPackage.getBuffer() != null && rtmpPackage.getBuffer().length != 0)
            {
                //调用native进行发送数据
                isSend = sendData(rtmpPackage.getBuffer(),
                        rtmpPackage.getBuffer().length,
                        rtmpPackage.getType(),rtmpPackage.getTms()
                );
            }else {
                //得到最后一个空包，停止发送
                isSend = false;
                Log.i(TAG,"stopService 最后一个空包");
            }
        }

        //发送失败或者是发送完毕
        //停止编解码
        isLiving = false;
        videoCodec.stopLive();
        audioCodec.stopLive();
        queue.clear();
        disConnect();
        Log.i(TAG,"stopService 断开连接");
        //回调释放Service
        stopCallBack.callBack();
    }

    private void checkDrop() throws InterruptedException {
        //如果队列积压超过200包，持续丢弃旧包，直到数量≤200
        while (queue.size() > 200){
            // 阻塞队列，无帧就卡住，不空轮询耗CPU
            queue.take();
        }
    }

    //链接服务
    private native boolean connect(String url);

    //结束链接

    private native void disConnect();

    //发送数据
    private native boolean sendData(byte[] data,int len,int type,long tms);
}
