//
// DeleteButton.java
// feep
//
// Created by lin yiqi on 2012-2-10.
// Copyright 2012 flyrise. All rights reserved.
//

package cn.flyrise.android.library.view;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.flyrise.feep.R;
import cn.flyrise.feep.core.common.utils.PixelUtil;

/**
 * 自定义的删除按钮,带确认功能的哦~
 * @author <a href="mailto:184618345@qq.com">017</a>
 */
public class DeleteButton extends RelativeLayout {
    private static final int duration = 250;
    private final RotateAnimation animation1;
    private final RotateAnimation animation2;
    private final AlphaAnimation animation3;
    private final AlphaAnimation animation4;
    private final ImageView bottom;                 // top;
    private final TextView top;
    private int delay = 2000;
    private final Handler handler;
    private OnConfirmClickListener onConfirmClickListener;

    public DeleteButton(Context context) {
        this(context, null);
    }

    public DeleteButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        handler = new Handler();
        /*--初始化要用的动画--*/
        animation1 = new RotateAnimation(0, -90, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation1.setDuration(duration);
        animation2 = new RotateAnimation(-90, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation2.setDuration(duration);
        animation3 = new AlphaAnimation(0, 1);
        animation3.setDuration(duration / 2);
        animation4 = new AlphaAnimation(1, 0);
        animation4.setDuration(duration / 2);
        /*--End--*/
        bottom = new ImageView(context);
        top = new TextView(context);
        top.setText(getResources().getString(R.string.button_delete));
        top.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        top.setTextColor(0xffffffff);
        top.setGravity(Gravity.CENTER);
        top.setPadding(0, 0, 0, 0);
        bottom.setImageResource(R.drawable.icon_wrong);
        LayoutParams rl = new LayoutParams(PixelUtil.dipToPx(28), PixelUtil.dipToPx(28));
        rl.addRule(RelativeLayout.CENTER_VERTICAL);
        top.setBackgroundResource(R.drawable.action_delete_definite_fe);
        this.addView(bottom, rl);
        this.addView(top);
        top.setVisibility(GONE);
        bottom.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                top.setHeight(bottom.getHeight());
                final AnimationSet as = new AnimationSet(true);
                as.addAnimation(animation1);
                as.addAnimation(animation4);
                bottom.startAnimation(as);
                handler.postDelayed(showDeleteCallback, duration);
                handler.postDelayed(recoverCallback, delay + duration);
            }
        });
        top.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                DeleteButton.this.onClick(v);
            }
        });
    }

    private void onClick(View v) {
        if (onConfirmClickListener != null) {
            onConfirmClickListener.setOnConfirmClickListener(v);
        }
    }

    /*--下面加功能哦~--*/
    public interface OnConfirmClickListener {
        void setOnConfirmClickListener(View v);
    }

    public void setOnConfirmClickListener(OnConfirmClickListener c) {
        this.onConfirmClickListener = c;
    }

    public void recoverButton() {
        handler.postDelayed(showTobCallback, duration * 9 / 10);
    }

    public void recoverButtonImmediately() {
        bottom.clearAnimation();
        bottom.setVisibility(VISIBLE);
        top.clearAnimation();
        top.clearAnimation();
        top.setVisibility(GONE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            top.setBackground(null);
        }
        else {
            top.setBackgroundDrawable(null);
        }
    }

    public int getDelayToRecover() {
        return delay;
    }

    public void setDelayToRecover(int delay) {
        this.delay = delay;
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        handler.removeCallbacks(recoverCallback);
        handler.removeCallbacks(showDeleteCallback);
        handler.removeCallbacks(showTobCallback);
        if (visibility == VISIBLE) {
            bottom.setVisibility(VISIBLE);
            top.setVisibility(GONE);
        }
    }

    Runnable recoverCallback = new Runnable() {
        @Override
        public void run() {
            recoverButton();
        }
    };
    Runnable showDeleteCallback = new Runnable() {
        @Override
        public void run() {
            bottom.setVisibility(GONE);
            top.setBackgroundResource(R.drawable.action_delete_definite_fe);
            top.startAnimation(animation3);
            top.setVisibility(VISIBLE);
        }
    };
    Runnable showTobCallback = new Runnable() {
        @Override
        public void run() {
            if (bottom.getVisibility() == VISIBLE) {
                return;
            }
            final AnimationSet as = new AnimationSet(true);
            as.addAnimation(animation2);
            as.addAnimation(animation3);
            bottom.startAnimation(as);
            bottom.setVisibility(VISIBLE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                top.setBackground(null);
            }
            else {
                top.setBackgroundDrawable(null);
            }
            top.setVisibility(GONE);
            top.startAnimation(animation4);
        }
    };
}
