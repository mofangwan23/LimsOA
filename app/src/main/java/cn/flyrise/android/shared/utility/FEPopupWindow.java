/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2012-9-26 上午10:03:21
 */
package cn.flyrise.android.shared.utility;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.PopupWindow;

/**
 * 类功能描述：加上了动画和点击外部消失的PopupWindow</br>
 *
 * @author 钟永健
 * @version 1.0</br> 修改时间：2012-9-26</br> 修改备注：</br>
 */
public class FEPopupWindow extends PopupWindow {
    private TranslateAnimation enterAnima;

    private TranslateAnimation exitAnima;

    private final Handler handler;

    /**
     * 标示正在enter动画中
     */
    private boolean isEntering;
    /**
     * 动画延续时间
     */
    private final long animTime = 200;
    /**
     * 标示控件尚未完全消失
     */
    private boolean isVisible;

    private final View showParentView;

    private OnActionChangeListener actionChangeLisenter;

    public FEPopupWindow (Context context) {
        super (context);
        setBackgroundDrawable (new BitmapDrawable ());// 响应点击popupwindow外关闭菜单和返回键必须加上这句
        setOutsideTouchable (true);// 当点击菜单外时使气泡框消失
        setFocusable (true);// 如果不加这个，childView不会响应ItemClick
        setTouchable (true);
        handler = new Handler ();
        showParentView = new View (context);
        initAnima ();
    }

    /**
     * 显示
     */
    public void show () {
        if (!isShowing ()) {
            getContentView ().startAnimation (enterAnima);
            showAtLocation (showParentView, Gravity.BOTTOM, 0, 0);
            isVisible = true;
        }
        if (actionChangeLisenter != null) {
            actionChangeLisenter.show (this, getContentView ());
        }
    }

    /**
     * 初始化动画
     */
    private void initAnima () {
        enterAnima = new TranslateAnimation (Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 1, Animation.RELATIVE_TO_SELF, 0);
        enterAnima.setDuration (animTime);

        exitAnima = new TranslateAnimation (Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 1);
        exitAnima.setInterpolator (new AccelerateInterpolator ());
        exitAnima.setDuration (animTime);

        enterAnima.setAnimationListener (new AnimationListener () {

            @Override
            public void onAnimationStart (Animation animation) {
                isEntering = true;
            }

            @Override
            public void onAnimationRepeat (Animation animation) {
            }

            @Override
            public void onAnimationEnd (Animation animation) {
                isEntering = false;
            }
        });
    }

    @Override
    public void dismiss () {
        if (isVisible && !isEntering && isShowing ()) {
            getContentView ().startAnimation (exitAnima);
            handler.postDelayed (new Runnable () {// 动画之后再消失
                @Override
                public void run () {
                    FEPopupWindow.super.dismiss ();
                    if (actionChangeLisenter != null) {
                        actionChangeLisenter.dismiss (FEPopupWindow.this, getContentView ());
                    }
                }
            }, animTime);
            isVisible = false;// 已经触发了一次dismiss(),在其消失前不让dismiss()再次触发
        }
    }

    /**
     * 计算控件的宽高
     *
     * @param view 需要计算的view
     * @return 宽:int[0],高:int[1]
     */
    public int[] measureViewSpecs (View view) {
        final int[] specs = new int[2];
        final int spec = View.MeasureSpec.makeMeasureSpec (0, View.MeasureSpec.UNSPECIFIED);
        view.measure (spec, spec);
        final int width = view.getMeasuredWidth ();
        final int height = view.getMeasuredHeight ();
        specs[0] = width;
        specs[1] = height;
        return specs;
    }

    /**
     * 获取控件的高度
     */
    public int getContentViewHight () {
        return 0;
    }

    /**
     * 获取控件的宽度
     */
    public int getContentViewWidth () {
        return 0;
    }

    /**
     * 获取当前选中的数据
     */
    public void getData () {

    }

    /**
     * 设置控件显示或消失的监听事件
     */
    public void setOnActionChangeLisenter (OnActionChangeListener listener) {
        this.actionChangeLisenter = listener;
    }

    public interface OnActionChangeListener {
        /**
         * 显示
         */
        void show(FEPopupWindow pw, View contentView);

        /**
         * 消失
         */
        void dismiss(FEPopupWindow pw, View contentView);
    }
}
