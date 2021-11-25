package com.example.meet.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.meet.R;

/**
 * 拖动图片验证View
 */
public class TouchPictureView extends View {

    //背景bitmap和画笔
    private Bitmap backgroundBitmap;
    private Paint backgroundPaint;
    //空白块bitmap和画笔
    private Bitmap nullBitmap;
    private Paint nullPaint;
    //移动方块
    private Bitmap moveBitmap;
    private Paint movePaint;
    //view的宽高
    private int mWidth;
    private int mHeight;

    //方块大小
    private int CARD_SIZE = 350;
    //方块坐标
    private int LINE_W, LINE_H = 0;

    //移动方块横坐标
    private int moveX = 200;
    //误差值
    private int errorValues = 10;

    private OnViewResultListener onViewResultListener;

    public void setOnViewResultListener(OnViewResultListener onViewResultListener) {
        this.onViewResultListener = onViewResultListener;
    }

    public TouchPictureView(Context context) {
        super(context);
        init();
    }

    public TouchPictureView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TouchPictureView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        backgroundPaint = new Paint();
        nullPaint = new Paint();
        movePaint = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //进行绘制
        drawBackground(canvas);
        drawNullCard(canvas);
        drawMoveCard(canvas);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //获取view的宽高
        mWidth = w;
        mHeight = h;
    }

    /**
     * 绘制背景图片
     */
    private void drawBackground(Canvas canvas) {
        //从资源文件中获取图片
        Bitmap mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img_bg);
        //创建一个空的bitmap  宽高等于view的宽高
        backgroundBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        //将图片绘制到空的bitmap
        Canvas backgroundCanvas = new Canvas(backgroundBitmap);
        backgroundCanvas.drawBitmap
                (mBitmap, null, new Rect(0, 0, mWidth, mHeight), backgroundPaint);
        //将backgroundBitmap绘制到view上
        canvas.drawBitmap(mBitmap, null, new Rect(0, 0, mWidth, mHeight), backgroundPaint);
    }

    /**
     * 绘制空白区域
     */
    private void drawNullCard(Canvas canvas) {
        //1.获取图片
        nullBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img_null_card);
        //2.计算值(空白块的坐标)
        CARD_SIZE = nullBitmap.getWidth();
        LINE_W = mWidth / 3 * 2;
        LINE_H = mHeight / 2 - (CARD_SIZE / 2);
        //3.绘制空白块
        canvas.drawBitmap(nullBitmap, LINE_W, LINE_H, nullPaint);
    }

    /**
     * 绘制移动的滑块区域，空白块位置截取图片
     *
     * @param canvas
     */
    private void drawMoveCard(Canvas canvas) {
        //1.截取空白块位置的图片
        moveBitmap = Bitmap.createBitmap(backgroundBitmap, LINE_W, LINE_H, CARD_SIZE, CARD_SIZE);
        //2.绘制bitmap
        canvas.drawBitmap(moveBitmap, moveX, LINE_H, movePaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //如果点击的不是移动方块，则不可以滑动
                break;
            case MotionEvent.ACTION_MOVE:
                //防止越界
                if (event.getX() > 0 && event.getX() < (mWidth - CARD_SIZE)) {
                    moveX = (int) event.getX();
                    invalidate();//实时刷新
                }
                break;
            case MotionEvent.ACTION_UP:
                if (moveX > (LINE_W - errorValues) && moveX < (LINE_W + errorValues)) {
                    if(onViewResultListener != null){
                        onViewResultListener.onResult();
                    }
                }
                invalidate();//实时刷新
                break;
        }
        return true;
    }

    public interface OnViewResultListener {
        void onResult();
    }
}
