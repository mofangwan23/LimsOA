//
// feep
//
// Created by ZhongYJ on 2012-02-13.
// Copyright 2011 flyrise. All rights reserved.
//
package cn.flyrise.feep.form.widget;

import cn.flyrise.feep.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageButton;

public class SwitchStateButton extends FrameLayout {
    private OnEventClickListener mEventClickListener;
    private ImageButton          mAddBnt;

    public SwitchStateButton(Context context) {
        super(context);
    }

    public SwitchStateButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        final ScaleAnimation addEnter = new ScaleAnimation (0, 1, 0, 1);
        final ScaleAnimation addExit = new ScaleAnimation (1, 0, 1, 0);
        addEnter.setDuration(200);
        addExit.setDuration(200);
        mAddBnt = new ImageButton(context);
        mAddBnt.setBackgroundResource(R.drawable.action_add_fe);
        addView(mAddBnt);
        mAddBnt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                SwitchStateButton.this.onButtonClick(v);
            }
        });
    }

    private void onButtonClick(View v) {
        if (mEventClickListener != null) {
            mEventClickListener.onButtonClickListener(v);
        }
    }

    public interface OnEventClickListener {
        void onButtonClickListener(View v);
    }

    /**
     * 监听按钮
     * 
     * @param event
     *            监听按钮事件
     * */
    public void setOnEventClickListener(OnEventClickListener event) {
        mEventClickListener = event;
    }

    /**
     * 改变按钮状态
     * 
     * @param state
     *            true为加,false为减
     * */
    public void changeBntState(boolean state) {
        if (state) {
            mAddBnt.setBackgroundResource(R.drawable.action_add_fe);
        } else {
            mAddBnt.setBackgroundResource(R.drawable.icon_wrong);
        }
    }
}
