package com.example.chenshuang.csvideo;

import android.animation.TypeEvaluator;

/**
 * 返回当前动画的值
 */
public class ValueTypeEvaluator implements TypeEvaluator {
    @Override
    public Object evaluate(float fraction, Object startValue, Object endValue) {
        Value start = (Value) startValue;
        Value end = (Value) endValue;
        Value result = new Value();
        result.value1 = start.value1 + fraction * (end.value1 - start.value1);
        result.value2 = start.value2 + fraction * (end.value2 - start.value2);
        return result;
    }
}
