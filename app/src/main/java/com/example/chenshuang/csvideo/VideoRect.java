package com.example.chenshuang.csvideo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * video视频录制中,点击出现的小方块
 */

public class VideoRect extends View {

    public Paint mPaint;
    public int width;
    public int height;

    public VideoRect(Context context) {
        super(context);
        initData();
    }

    public VideoRect(Context context, AttributeSet attrs) {
        super(context, attrs);
        initData();
    }

    public VideoRect(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initData();
    }

    private void initData() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStrokeWidth(2);
        mPaint.setColor(Color.GREEN);
        mPaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        this.width = getMeasuredWidth();
        this.height = getMeasuredHeight();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawRect(10, 10, getMeasuredWidth()-10, getMeasuredHeight()-10, mPaint);
        for (int i = 0; i < 4; i++) {
            canvas.drawLine(width/2,height/2-width/2+10,width/2,height/2-width/2+40,mPaint);
            canvas.rotate(90,width/2,height/2);
        }
    }
}
