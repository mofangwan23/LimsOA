/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2012-6-25 下午1:33:05
 */

package cn.flyrise.feep.commonality.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import cn.flyrise.feep.R;

/**
 * 类功能描述：附件选择checkbox</br>
 * 
 * @author 钟永健
 * @version 1.0</br> 修改时间：2012-6-25</br> 修改备注：</br>
 */
public class ThreeStateCheckBox extends FrameLayout {

    private ImageView            noCheck;

    private ImageView            allChecked;

    private ImageView            partCheck;

    private onCheckStateListener stateListener;

    private int                  checkStateType        = 0;

    public static final int      NO_CHECK_STATE_TYPE   = 0;

    public static final int      ALL_CHECK_STATE_TYPE  = 1;

    public static final int      PART_CHECK_STATE_TYPE = 2;

    public ThreeStateCheckBox(Context context) {
        this(context, null);
    }

    public ThreeStateCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    /**
     * 初始化
     * 
     * @param context
     */
    private void init(Context context) {
        final LayoutParams params = new LayoutParams(android.view.ViewGroup.LayoutParams.FILL_PARENT, android.view.ViewGroup.LayoutParams.FILL_PARENT);
        noCheck = new ImageView(context);
        noCheck.setImageResource(R.drawable.node_add_icon);
        allChecked = new ImageView(context);
        allChecked.setImageResource(R.drawable.node_current_icon);
        partCheck = new ImageView(context);
        partCheck.setImageResource(R.drawable.login_checkbox_part_fe);
        addView(noCheck, params);
        addView(allChecked, params);
        addView(partCheck, params);
        setCheckStateType(NO_CHECK_STATE_TYPE);
        setListener();
    }

    /**
     * 设置监听器
     */
    private void setListener() {
        noCheck.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                state(v, ALL_CHECK_STATE_TYPE);
            }
        });
        allChecked.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                state(v, NO_CHECK_STATE_TYPE);
            }
        });
        partCheck.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                state(v, NO_CHECK_STATE_TYPE);
            }
        });

    }

    /**
     * 状态
     */
    private void state(View v, int state) {
        if (stateListener != null) {
            stateListener.onCheckState(v, state);
        }
    }

    /**
     * 设置选择状态监听器
     * 
     * @param listener
     *            状态监听接口
     */
    public void setOnCheckStateListener(onCheckStateListener listener) {
        this.stateListener = listener;
    }

    public interface onCheckStateListener {
        /**
         * @param v
         *            点击对应的view
         * @param state
         *            选择状态：0-未选中；1-全选中；2部分选中
         */
        void onCheckState(View v, int state);
    }

    /**
     * 设置选择类型
     * 
     * @param type
     *            选择类型：未选中-0(AttachmentCheckBox.NO_CHECK_STATE_TYPE); 全选-1(AttachmentCheckBox.ALL_CHECK_STATE_TYPE); 部分选中-2(AttachmentCheckBox.NO_CHECK_STATE_TYPE)
     */
    public void setCheckStateType(int type) {
        switch (type) {
            case NO_CHECK_STATE_TYPE:
                noCheck.setVisibility(View.VISIBLE);
                allChecked.setVisibility(View.GONE);
                partCheck.setVisibility(View.GONE);
                break;
            case ALL_CHECK_STATE_TYPE:
                noCheck.setVisibility(View.GONE);
                allChecked.setVisibility(View.VISIBLE);
                partCheck.setVisibility(View.GONE);
                break;
            case PART_CHECK_STATE_TYPE:
                noCheck.setVisibility(View.GONE);
                allChecked.setVisibility(View.GONE);
                partCheck.setVisibility(View.VISIBLE);
                break;

            default:
                break;
        }
        this.checkStateType = type;
    }

    /**
     * 获取当前选择状态
     * 
     * @return 选择状态：0-未选中；1-全选中；2部分选中
     */
    public int getCheckStateType() {
        return checkStateType;
    }

}
