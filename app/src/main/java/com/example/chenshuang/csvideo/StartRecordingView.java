package com.example.chenshuang.csvideo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;


/**
 * 该控件为自定义的控件,实现了原生没有的功能,主要是配合录制视频的按钮
 */

public class StartRecordingView extends View {

    public Paint mPaint1;

    public Paint mPaint2;

    public Paint mPaint3;

    public Handler handler;

    public ValueAnimator animator;

    public ValueAnimator animator2;

    public ValueAnimator animator3;

    public float circleLength1;

    public float circleLength2;

    public float sweepAngle;

    //标识手指是否抬起
    public boolean isUp = true;

    public boolean enable;

    //动画开关
    public int flag = 0;

    //已接口回调的形式通知外部进行相应的操作
    public interface Recording {

        void start();

        void stop();

        void cancle();

        void takePhoto();

    }

    public Recording recording;

    public void setRecording(Recording recording) {
        this.recording = recording;
    }

    public StartRecordingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        intData();
    }

    public StartRecordingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        intData();
    }

    public StartRecordingView(Context context) {
        super(context);
        intData();
    }

    private void intData() {
        circleLength1 = dip2px(40);
        circleLength2 = (dip2px(40) - 30);
        handler = new Handler();
        mPaint1 = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint3 = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint1.setColor(0xFFDDDDDD);
        mPaint2.setColor(0xFFFFFFFF);
        mPaint3.setStrokeWidth(10);
        mPaint3.setColor(0xFF64C166);
        mPaint3.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawCircle(getMeasuredWidth() / 2, getMeasuredHeight() / 2, circleLength1, mPaint1);
        canvas.drawCircle(getMeasuredWidth() / 2, getMeasuredHeight() / 2, circleLength2, mPaint2);
        if (flag == 1) {
            //FIXME: android4.x设备没有此api
            canvas.drawArc(10, 10, getMeasuredWidth() - 10, getMeasuredHeight() - 10, -90, sweepAngle, false, mPaint3);
        }

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(isEnable()){
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    isUp = false;
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (!isUp) {
                                startAnimator(1);
                            }
                        }
                    }, 500);
                    break;

                case MotionEvent.ACTION_UP:
                    isUp = true;
                    flag = 0;
                    startAnimator(2);
                    break;
            }
        }
        return true;
    }

    /**
     * 开始动画
     * animatorType = 1 down
     * animatorType = 2 up
     */
    public void startAnimator(int animatorType) {
        if (animatorType == 1) {
            if (animator == null) {
                Value startValue = new Value();
                Value endValue = new Value();
                startValue.value1 = dip2px(40);
                startValue.value2 = dip2px(40) - 30;
                endValue.value1 = dip2px(60);
                endValue.value2 = (dip2px(40) - 30) * 0.7f;
                animator = ValueAnimator.ofObject(new ValueTypeEvaluator(), startValue, endValue);
                animator.setDuration(250);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        Value value = (Value) animation.getAnimatedValue();
                        circleLength1 = value.value1;
                        circleLength2 = value.value2;
                        invalidate();
                    }
                });
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (!isUp) {
                            flag = 1;
                            startProgressAnimator();
                        }
                    }
                });
            }
            animator.start();
        } else if (animatorType == 2) {
            if (animator != null && animator.isRunning()) {
                animator.cancel();
            }
            if (animator3 != null && animator3.isRunning()) {
                animator3.cancel();
            }
            if (circleLength1 != dip2px(40)) {
                Value startValue = new Value();
                Value endValue = new Value();
                startValue.value1 = circleLength1;
                startValue.value2 = circleLength2;
                endValue.value1 = dip2px(40);
                endValue.value2 = dip2px(40) - 30;
                animator2 = ValueAnimator.ofObject(new ValueTypeEvaluator(), startValue, endValue);
                animator2.setDuration(250);
                animator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        Value value = (Value) animation.getAnimatedValue();
                        circleLength1 = value.value1;
                        circleLength2 = value.value2;
                        invalidate();
                    }
                });
                animator2.start();
            }else{
                recording.takePhoto();
            }
        }
    }

    private void startProgressAnimator() {
        if (animator3 == null) {
            animator3 = ValueAnimator.ofFloat(0, 360);
            animator3.setInterpolator(new LinearInterpolator());
            animator3.setDuration(10000);
            animator3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    sweepAngle = (float) animation.getAnimatedValue();
                    invalidate();
                }
            });
            animator3.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (recording != null) {
                        if (!isUp) {
                            isUp = true;
                            flag = 0;
                            startAnimator(2);
                        }
                        recording.stop();
                    }
                }
            });
        }
        if (recording != null) {
            recording.start();
            animator3.start();
        }

    }

    public void setEnable(boolean enable){
        this.enable = enable;
    }

    public boolean isEnable(){
        return enable;
    }

    /**
     * dp转换成px
     */
    public float dip2px(float dp) {
        float i = this.getResources().getDisplayMetrics().density;
        return dp * i + 0.5f;
    }

}
