package com.example.android_study_demo_project.opencv.ffmpegUsage.livePush.CameraView;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.android_study_demo_project.R;

public class RecordProgressButton extends View {

    public RecordProgressButton(Context context) {
        this(context,null);
    }

    public RecordProgressButton(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public RecordProgressButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mInnerRadius = dip2px(INNER_RADIUS_MAX);
        mOuterRadius = dip2px(OUTER_RADIUS_Mix);

        mPaint = new Paint();
        //抗锯齿
        mPaint.setAntiAlias(true);
        //色彩抖动,用噪点模拟中间色，减少渐变断层（色带）
        mPaint.setDither(true);
    }

    private final float INNER_RADIUS_MIN = 15;
    private final float INNER_RADIUS_MAX = 25;
    private final float OUTER_RADIUS_Mix = 35;
    private final float OUTER_RADIUS_MAX = 55;

    private float mInnerRadius = 0;
    private float mOuterRadius = 0;

    private long mCurrentProgress = 0;
    private int mMaxProgress = 60000;
    private boolean mIsRecording = false;
    private Paint mPaint;

    /**
     * dp → px（像素）
     * @param dip
     * @return
     */
    private int dip2px(float dip)
    {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,dip,
                getResources().getDisplayMetrics());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setColor(Color.parseColor("#dfdfdf"));
        canvas.drawCircle(getMeasuredWidth()/2,getMeasuredWidth()/2,mOuterRadius,mPaint);

        mPaint.setColor(ContextCompat.getColor(getContext(), R.color.white));
        canvas.drawCircle(getMeasuredWidth()/2,getMeasuredWidth()/2,mInnerRadius,mPaint);

        if(mIsRecording && mCurrentProgress > 0)
        {
            //画进度
            float top = getMeasuredHeight() / 2 - mOuterRadius;
            float bottom = top + mOuterRadius * 2;
            float left = getMeasuredWidth() / 2 - mOuterRadius;
            float right = left + mOuterRadius * 2;
            //外接矩形，决定圆的位置和大小
            RectF rectF = new RectF(left,top,right,bottom);

            //顶部开始
            int startAngle = -90;
            //扫过角度
            int sweepAngle = (int) (mCurrentProgress * 360f / mMaxProgress);

            //描边模式，只画轮廓，不填充内部，
            // `FILL`：填充（默认，drawArc 是实心扇形）、`STROKE`：描边、`FILL_AND_STROKE`：填充 + 描边
            mPaint.setStyle(Paint.Style.STROKE);
            //描边线宽为 4 px
            mPaint.setStrokeWidth(dip2px(4));
            //圆角端头
            mPaint.setStrokeCap(Paint.Cap.ROUND);

            // 背景环
            mPaint.setColor(ContextCompat.getColor(getContext(), R.color.gray));
            canvas.drawArc(rectF, 0, 360, false, mPaint);

            // 进度环
            mPaint.setColor(Color.parseColor("#62c554"));
            //`useCenter = false`：只画圆弧，不连接圆心，true 是扇形
            canvas.drawArc(rectF,startAngle,sweepAngle,false,mPaint);

        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                figureDownAnim();
                break;
            case MotionEvent.ACTION_UP:
                onEnd();
                break;
        }

        return true;
    }

    /**
     * 按下的动画
     */
    private void figureDownAnim()
    {
        if(mRecorderListener != null)
        {
            mRecorderListener.onStart();
        }

        //外圆放大，内圆缩小
        ValueAnimator innerAnimator = ObjectAnimator.ofFloat(mInnerRadius,dip2px(INNER_RADIUS_MIN));
        ValueAnimator outerAnimator = ObjectAnimator.ofFloat(mOuterRadius,dip2px(OUTER_RADIUS_MAX));
        innerAnimator.setDuration(200);
        outerAnimator.setDuration(200);

        innerAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mInnerRadius = (float)  valueAnimator.getAnimatedValue();
                postInvalidate();
            }
        });

        outerAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mOuterRadius = (float) valueAnimator.getAnimatedValue();
                postInvalidate();
            }
        });

        innerAnimator.start();
        outerAnimator.start();

        outerAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mIsRecording = true;
                mInnerRadius = dip2px(INNER_RADIUS_MIN);
                mOuterRadius = dip2px(OUTER_RADIUS_MAX);
            }
        });
    }

    /**
     * 抬起动画
     */
    private void figureUpAnim()
    {
        //外圆缩小，内圆变大
        ValueAnimator innerAnimator = ObjectAnimator.ofFloat(mInnerRadius,dip2px(INNER_RADIUS_MAX));
        ValueAnimator outerAnimator = ObjectAnimator.ofFloat(mOuterRadius,dip2px(OUTER_RADIUS_Mix));
        innerAnimator.setDuration(200);
        outerAnimator.setDuration(200);

        innerAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mInnerRadius = (float) valueAnimator.getAnimatedValue();
            }
        });

        outerAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mOuterRadius = (float) valueAnimator.getAnimatedValue();
            }
        });

        innerAnimator.start();
        outerAnimator.start();

        outerAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mInnerRadius = dip2px(INNER_RADIUS_MAX);
                mOuterRadius = dip2px(OUTER_RADIUS_Mix);
            }
        });
    }

    public interface RecorderListener{
        void onStart();
        void onEnd();
    }
    private RecorderListener mRecorderListener;

    public void setOnRecorderListener(RecorderListener mRecorderListener) {
        this.mRecorderListener = mRecorderListener;
    }

    public void setMaxProgress(int mMaxProgress) {
        this.mMaxProgress = mMaxProgress;
    }

    public void setCurrentProgress(long currentProgress) {
        this.mCurrentProgress = currentProgress;
        postInvalidate();
        if(mCurrentProgress >= mMaxProgress)
        {
            onEnd();
        }
    }

    private void onEnd()
    {
        mCurrentProgress = 0;
        mIsRecording = false;
        post(new Runnable() {
            @Override
            public void run() {
                figureUpAnim();
            }
        });
        if(mRecorderListener != null)
        {
            mRecorderListener.onEnd();
        }
        postInvalidate();
    }
}
