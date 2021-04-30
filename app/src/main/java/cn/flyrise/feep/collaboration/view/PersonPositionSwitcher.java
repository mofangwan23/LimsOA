/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2012-6-25
 */

package cn.flyrise.feep.collaboration.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.flyrise.feep.R;

/**
 * <b>类功能描述：</b><div style="margin-left:40px;margin-top:-10px"> 流程图 [人员] [岗位] 切换 </div>
 * @author <a href="mailto:184618345@qq.com">017</a>
 * @version 1.0 </p> 修改时间：</br> 修改备注：</br>
 */
public class PersonPositionSwitcher extends RelativeLayout {
    private final TextView leftText, rightText;
    private final LayoutParams lpL, lpR;
    private int padding = 0;

    private OnBoxClickListener onBoxClickListener;
    private boolean value = true;

    public PersonPositionSwitcher(Context context) {
        this(context, null);
    }

    public PersonPositionSwitcher(Context context, AttributeSet attrs) {
        super(context, attrs);
        padding = (int) getResources().getDimension(R.dimen.mdp_4);
        this.setBackgroundResource(R.color.list_item_time_color);
        this.setPadding(padding, 0, padding + 2, 0);
        leftText = new TextView(context);
        rightText = new TextView(context);
        leftText.setText(getResources().getString(R.string.flow_person));
        rightText.setText(getResources().getString(R.string.flow_position));
        turn(value);
        lpL = new LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        lpR = new LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        lpL.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        lpL.addRule(RelativeLayout.CENTER_VERTICAL);
        lpL.leftMargin = (int) getResources().getDimension(R.dimen.mdp_10);
        lpR.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        lpR.addRule(RelativeLayout.CENTER_VERTICAL);
        lpR.rightMargin = (int) getResources().getDimension(R.dimen.mdp_10);
        this.addView(leftText, lpL);
        this.addView(rightText, lpR);

        leftText.setOnClickListener(v -> {
            if (!value) {
                value = true;
                PersonPositionSwitcher.this.onClick(v, value);
                turn(value);
            }
        });
        rightText.setOnClickListener(v -> {
            if (value) {
                value = false;
                PersonPositionSwitcher.this.onClick(v, value);
                turn(value);
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        turn(value);
        super.onWindowFocusChanged(hasWindowFocus);
    }

    /*--改变状态--*/
    private void turn(boolean value) {
        if (value) {
            leftText.setTextColor(getResources().getColor(R.color.workflow_textview));
            rightText.setTextColor(getResources().getColor(R.color.all_background_color));
        }
        else {
            leftText.setTextColor(getResources().getColor(R.color.all_background_color));
            rightText.setTextColor(getResources().getColor(R.color.workflow_textview));
        }

    }

    public interface OnBoxClickListener {
        /**
         * 人员岗位选择器
         * @param b true-人员,flase-岗位
         */
        void setOnBoxClickListener(boolean b);
    }

    public void setOnBoxClickListener(OnBoxClickListener c) {
        this.onBoxClickListener = c;
    }

    /**
     * @param v 这个choosebox控件
     * @param b true代表左边,false代表右边
     */
    private void onClick(View v, boolean b) {
        if (onBoxClickListener != null) {
            onBoxClickListener.setOnBoxClickListener(b);
        }
    }

    /**
     * 你懂得~~
     * @param left
     * @param right
     */
    public void setText(String left, String right) {
        leftText.setText(left);
        rightText.setText(right);
    }

    /**
     * 你懂得~~
     * @param size
     */
    public void setTextSize(float size) {
        leftText.setTextSize(size);
        rightText.setTextSize(size);
    }

    public int getPadding() {
        return padding;
    }

    /**
     * 设置你需要的边距,可能按钮过大距离就太小
     * @param padding
     */
    public void setPadding(int padding) {
        this.padding = padding;
    }

    public boolean getValue() {
        return value;
    }

    /**
     * 设置显示的值
     * @param value
     */
    public void setValue(boolean value) {
        turn(value);
        this.value = value;
    }

    @Override
    public void setClickable(boolean clickable) {
        super.setClickable(clickable);
        leftText.setClickable(clickable);
        rightText.setClickable(clickable);
    }
}
