package com.example.android_study_demo_project.frameLayoutUsage;

import static androidx.core.content.ContextCompat.getMainExecutor;
import static androidx.core.content.ContextCompat.getSystemService;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.example.android_study_demo_project.R;
import java.util.ArrayList;
import java.util.List;

/***
 * 图片像鼠标一样跟踪的视图
 */
public class MouseTrackingView extends View {
    public float bitMapX;
    public float bitMapY;
    Paint paint;

    //根据图片生成位图对象
    private List<Bitmap> frameBitmapList; // 帧图片列表
    private int currentFrameIndex = 0; // 当前帧索引
    private Handler handler;
    private static final int MY_FRAME_DURATION = 200; // 每帧持续时间（毫秒）



    public MouseTrackingView(Context context,int parentWidth,int parentHeight) {
        super(context);
        init(parentWidth,parentHeight);
    }

    void init(int parentWidth,int parentHeight)
    {
        //根据图片生成位图对象
        frameBitmapList = new ArrayList<>();
        frameBitmapList.add(BitmapFactory.decodeResource(this.getResources(), R.mipmap.s_1));
        frameBitmapList.add(BitmapFactory.decodeResource(this.getResources(), R.mipmap.s_2));
        frameBitmapList.add(BitmapFactory.decodeResource(this.getResources(), R.mipmap.s_3));
        frameBitmapList.add(BitmapFactory.decodeResource(this.getResources(), R.mipmap.s_4));
        if(parentWidth > 0 && parentHeight > 0)
        {
            bitMapX = (float) (parentWidth - frameBitmapList.get(currentFrameIndex).getWidth()) / 2;
            bitMapY = (float) (parentHeight - frameBitmapList.get(currentFrameIndex).getHeight()) / 2;
        }

        //创建,并且实例化Paint的对象
        paint = new Paint();
        handler = new Handler(Looper.getMainLooper());
        startAnimation();
    }

    //显示每帧动画
    private final Runnable frameRunnable = new Runnable() {
        @Override
        public void run() {
            currentFrameIndex = (currentFrameIndex + 1) % frameBitmapList.size();
            invalidate(); // 重绘 View
            handler.postDelayed(this, MY_FRAME_DURATION);
        }
    };
    private void startAnimation(){
        handler.postDelayed(frameRunnable,MY_FRAME_DURATION);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //绘制图片
        Bitmap bitmap = frameBitmapList.get(currentFrameIndex);
        canvas.drawBitmap(bitmap,bitMapX,bitMapY,paint);
        //判断图片是否回收,木有回收的话强制收回图片
        if(bitmap.isRecycled())
        {
            bitmap.recycle();
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_UP:
                Log.i("TrackingView","TrackingView-ACTION_UP");
                break;
            case MotionEvent.ACTION_DOWN:
                Log.i("TrackingView","TrackingView-ACTION_DOWN");
                break;
            case MotionEvent.ACTION_MOVE:
                Log.i("TrackingView","TrackingView-ACTION_MOVE");
                performClick();
                //减是因为坐标是左上角
                bitMapX = event.getX() - (float) frameBitmapList.get(currentFrameIndex).getWidth() /2;
                bitMapY = event.getY() - (float) frameBitmapList.get(currentFrameIndex).getHeight() /2;
                invalidate();//调用重绘方法
                break;
        }
//        return super.onTouchEvent(event);
        return true;//返回true才能触发其他ACTION
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }
}
