package com.example.meet.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

/**
 * 自定义头部拉伸的view
 */
public class HeadZoomScrollView extends ScrollView {

    //头部view
    private View mScrollView;
    private int mWidth;
    private int mHeight;
    //是否在滑动
    private boolean isScrolling;
    //第一次按下的坐标
    private float firstPosition;
    //滑动系数
    private float mScrollRate = 0.3f;
    //回弹系数
    private float mReplyRate = 0.5f;

    public HeadZoomScrollView(Context context) {
        super(context);
    }

    public HeadZoomScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HeadZoomScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 布局文件被加载完成后回调，一般用于做初始化的工作
     * getChildAt--->返回指定索引位置的view
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildAt(0) != null) {
            ViewGroup viewGroup = (ViewGroup) getChildAt(0);
            if (viewGroup.getChildAt(0) != null) {
                mScrollView = viewGroup.getChildAt(0);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        /**
         * 获取view的宽高
         * getMeasuredWidth：获取View的实际内容的宽度
         * getWidth：获取view布局的宽度
         */
        if (mWidth <= 0 || mHeight <= 0) {
            mWidth = mScrollView.getMeasuredWidth();
            mHeight = mScrollView.getMeasuredHeight();
        }
        switch (ev.getAction()) {
            case MotionEvent.ACTION_MOVE:
                if (!isScrolling) {
                    /**
                     * 没有滑动
                     * getScrollY：上下滑动
                     * getScrollX：左右滑动
                     */
                    if (getScrollY() == 0) {
                        //第一次滑动，记录当前位置
                        firstPosition = ev.getY();
                    } else {
                        break;
                    }
                }
                //计算缩放值(当前位置 - 缩放的位置)*缩放系数
                int distance = (int) ((ev.getY() - firstPosition) * mScrollRate);
                if (distance < 0) {
                    break;
                }
                isScrolling = true;
                setZoomView(distance);
                break;
            case MotionEvent.ACTION_UP:
                recoverZoomView();
                break;
        }
        return true;
    }

    /**
     * 手指抬起时，恢复原样
     */
    private void recoverZoomView() {
        //获取缩放值
        int distance=mScrollView.getMeasuredWidth()-mWidth;
        //使用属性动画复原
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(distance, 0)
                .setDuration((long) (distance * mReplyRate));
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                setZoomView((Float) valueAnimator.getAnimatedValue());
            }
        });
        //启动动画
        valueAnimator.start();
    }

    /**
     * 缩放view
     *
     * @param zoom
     */
    private void setZoomView(float zoom) {
        if (mWidth <= 0 || mHeight <= 0) {
            return;
        }
        ViewGroup.LayoutParams layoutParams = mScrollView.getLayoutParams();
        //缩放后的宽
        layoutParams.width = (int) (mWidth + zoom);
        // 现在的宽/原本的宽 得到 缩放比例 * 原本的高 得到缩放的高
        layoutParams.height = (int) (mHeight * ((mWidth + zoom) / mWidth));
        //设置间距
        //公式：- (lp.width - mWidth) / 2
        ((MarginLayoutParams) layoutParams).
                setMargins(-(layoutParams.width - mWidth) / 2, 0, 0, 0);
        mScrollView.setLayoutParams(layoutParams);
    }
}
