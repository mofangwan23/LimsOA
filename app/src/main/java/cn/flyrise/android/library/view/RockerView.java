/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2013-1-8 上午9:32:10
 */

package cn.flyrise.android.library.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import cn.flyrise.feep.R;

/**
 * 类功能描述：摇杆</br>
 * @author 钟永健
 * @version 1.0</br> 修改时间：2013-1-8</br> 修改备注：</br>
 */
public class RockerView extends RelativeLayout {
    private Drawable circleDrawable;

    private Drawable ballDrawable;

    private ImageView circleIV;

    private BallView ballIV;

    private int x;

    private int y;

    private int ballSize;

    private int circleSize;
    private onShakingListener shakingListener;

    private Handler handler;

    private Runnable shakingRunnable;
    /**
     * 单位时间
     */
    private final int haploidTime = 24;
    /**
     * 在单位时间内移动的距离
     */
    private int perMoveDistance = 10;

    private boolean isDown = false;

    public RockerView(Context context) {
        this(context, null);
    }

    public RockerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initResourcesIfNecessary();
        initViews();
    }

    private void initViews() {
        LayoutParams params = new LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        circleIV = new ImageView(getContext());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            circleIV.setBackground(circleDrawable);
        }
        else {
            circleIV.setBackgroundDrawable(circleDrawable);
        }
        params.setMargins(ballSize, ballSize, ballSize, ballSize);
        addView(circleIV, params);

        params = new LayoutParams(circleSize + ballSize, circleSize + ballSize);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        ballIV = new BallView(getContext());
        addView(ballIV, params);
    }

    /**
     * 初始化资源
     */
    private void initResourcesIfNecessary() {
        if (circleDrawable == null) {
            circleDrawable = getContext().getResources().getDrawable(R.drawable.btn_directiion_bg_fe);
        }
        if (ballDrawable == null) {
            ballDrawable = getContext().getResources().getDrawable(R.drawable.btn_directiion_big_fe);
        }
        ballSize = ballDrawable.getIntrinsicWidth();
        circleSize = circleDrawable.getIntrinsicWidth();
    }

    class BallView extends View {

        private boolean isInCenter;

        public BallView(Context context) {
            super(context);
            isInCenter = true;
            handler = new Handler();
            shakingRunnable = new Runnable() {
                @Override
                public void run() {
                    if (shakingListener != null) {
                        final int x_value = x - getWidth() / 2;
                        final int y_value = y - getHeight() / 2;
                        final double distanceZ = Math.sqrt(x_value * x_value + y_value * y_value);
                        final double scale = (perMoveDistance / distanceZ);
                        shakingListener.onGyroScrolling(scale * x_value, scale * y_value);
                        handler.postDelayed(shakingRunnable, haploidTime);
                    }
                }
            };
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            x = (int) event.getX();
            y = (int) event.getY();
            ballIV.setInBorder();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (!isTouchBall()) {
                        ballIV.setCenter();
                        return false;
                    }
                    isDown = true;
                    break;
                case MotionEvent.ACTION_MOVE:
                    ballIV.invalidate();
                    if (isDown) {
                        handler.postDelayed(shakingRunnable, haploidTime);
                        isDown = false;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    ballIV.setCenter();
                    handler.removeCallbacks(shakingRunnable);
                    break;
            }
            return true;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (isInCenter) {
                x = getWidth() / 2;
                y = getHeight() / 2;
                isInCenter = false;
            }
            final int halfBallSize = ballSize / 2;
            final Rect bounds = new Rect(x - halfBallSize, y - halfBallSize, x + halfBallSize, y + halfBallSize);
            ballDrawable.setBounds(bounds);
            ballDrawable.draw(canvas);
        }

        /**
         * 设置球在边界内
         */
        public void setInBorder() {
            if ((calculationDistance(x, y)) > (circleSize / 2)) {
                final double angle = calculationAngle(x, y);
                final int distanceX = (int) (Math.cos(angle) * circleSize / 2);
                final int distanceY = (int) (Math.sin(angle) * circleSize / 2);
                final int circleX = getWidth() / 2;
                final int circleY = getHeight() / 2;
                if (x >= circleX && y <= circleY) {// 坐标系的第一象限
                    x = circleX + distanceX;
                    y = circleY - distanceY;
                }
                else if (x <= circleX && y <= circleY) {// 坐标系的第二象限
                    x = circleX - distanceX;
                    y = circleY - distanceY;
                }
                else if (x <= circleX && y >= circleY) {// 坐标系的第三象限
                    x = circleX - distanceX;
                    y = circleY + distanceY;
                }
                else if (x >= circleX && y >= circleY) {// 坐标系的第四象限
                    x = circleX + distanceX;
                    y = circleY + distanceY;
                }
            }
        }

        /**
         * 设置球体在控件的中部
         */
        public void setCenter() {
            isInCenter = true;
            invalidate();
        }

        /**
         * 通过当前移动点和控件中心点计算角度
         */
        private double calculationAngle(int pointX, int pointY) {
            final int x = Math.abs(getWidth() / 2 - pointX);
            final int y = Math.abs(getHeight() / 2 - pointY);
            final double z = Math.sqrt(x * x + y * y);
            // int angle = Math.round((float)(Math.asin(y / z) / Math.PI * 180));// 最终角度
            return Math.asin(y / z);
        }

        /**
         * 计算当前移动点到控件中心的距离
         */
        private int calculationDistance(int pointX, int pointY) {
            final int x = Math.abs(getWidth() / 2 - pointX);
            final int y = Math.abs(getHeight() / 2 - pointY);
            return (int) Math.sqrt(x * x + y * y);
        }

        /**
         * 判断点击范围是否在小球内
         */
        private boolean isTouchBall() {
            final int circleX = getWidth() / 2;
            final int circleY = getHeight() / 2;
            final int halfBallSize = ballSize / 2;
            return !((circleX - halfBallSize) > x || (circleY - halfBallSize) > y || (circleX + halfBallSize) < x || (circleY + halfBallSize) < y);
        }
    }

    /**
     * 设置移动速度，默认是10（也就是每次移动的距离，24毫秒移动一次）
     * @param speed 每次移动的距离
     */
    public void setMoveSpeed(int speed) {
        this.perMoveDistance = speed;
    }

    /**
     * 设置摇杆摇动监听器
     */
    public void setOnShakingListener(onShakingListener shakingListener) {
        this.shakingListener = shakingListener;
    }

    public interface onShakingListener {
        /**
         * 摇杆在摇动过程中
         */
        void onGyroScrolling(double distanceX, double distanceY);
    }

}
