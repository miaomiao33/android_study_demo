package com.example.android_study_demo_project.opencv.liveStreamingPush;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.android_study_demo_project.R;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/***
 * 后台启动的service
 * 负责启动手机录屏、推送数据等
 */
public class LiveTaskManager extends Service {
    private static volatile LiveTaskManager instance;
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();//当前可运行cpu数
    private static final int CORE_POOL_SIZE = Math.max(2, Math.min(CPU_COUNT - 1, 4));//核心线程数
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;//线程池最大线程数
    private static final int KEEP_ALIVE_SECONDS = 30;//空闲线程等待任务的最长时间30s
    private static final BlockingQueue<Runnable> sPoolWorkQueue =
            new LinkedBlockingQueue<Runnable>(5);//线程队列
    private static ThreadPoolExecutor THREAD_POOL_EXECUTOR;
    private final TaskBinder binder = new TaskBinder();

    // 通知ID与渠道ID
    private static final int NOTIFY_ID = 9998;
    private static final String CHANNEL_ID = "LIVE_TASK_CHANNEL";
    private String TAG = LiveTaskManager.class.getSimpleName();
    private boolean isServiceDestroyed = false;

    //static: 类加载的时候只执行一次
    static {
        initThreadPool();
    }

    private static void initThreadPool()
    {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_SECONDS, TimeUnit.SECONDS,
                sPoolWorkQueue);
        //允许核心线程超时，低加载时减少核心线程数
        threadPoolExecutor.allowCoreThreadTimeOut(true);
        THREAD_POOL_EXECUTOR = threadPoolExecutor;
    }

//    //防止外部直接实例化
//    private LiveTaskManager() {
//    }

    public static class TaskBinder extends Binder {
        public LiveTaskManager getService() {
            return getInstance();
        }
        //启动当前发送过来的线程
        public void execute(Runnable task) {
            if(THREAD_POOL_EXECUTOR == null)
            {
                initThreadPool();
            }
            THREAD_POOL_EXECUTOR.execute(task);
        }
    }


    public static LiveTaskManager getInstance()
    {
        if(instance == null)
        {
            //锁住类
            synchronized (LiveTaskManager.class){
                if(instance == null)
                {
                    instance = new LiveTaskManager();
                }
            }
        }

        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        // 初始化单线程池
//        taskExecutor = Executors.newSingleThreadExecutor();
        // 开启mediaProjection类型前台服务（解决录屏SecurityException）
        startLiveForeground();
    }

    /**
     * 启动录屏专用前台服务（适配Android10+ MediaProjection要求）
     */
    private void startLiveForeground() {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "直播录屏任务服务",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("用于后台执行屏幕录制推流任务");
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("直播任务运行中")
                .setContentText("录屏/推流后台任务正在执行")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setOngoing(true);

        Notification notification = notifyBuilder.build();

        // API29+ 传入mediaProjection类型，修复录屏权限异常
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFY_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION);
        } else {
            startForeground(NOTIFY_ID, notification);
        }
    }

    /**
     * 关闭任务线程池，释放资源
     */
    private void shutdownTaskExecutor() {
        Log.i(TAG, "stopService shutdownTaskExecutor");
        if (THREAD_POOL_EXECUTOR != null)
        {

            THREAD_POOL_EXECUTOR.shutdownNow();
            THREAD_POOL_EXECUTOR = null;
        }
    }
    // 停止前台并关闭服务
    public void stopForegroundService() {
        if (isServiceDestroyed) {
            return;
        }
        // 1. 移除前台通知，取消前台状态
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(true);
        }
        // 2. 停止自身服务
        stopSelf(NOTIFY_ID);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isServiceDestroyed = true;
        // 停止任务、关闭前台通知
        shutdownTaskExecutor();
    }
}
