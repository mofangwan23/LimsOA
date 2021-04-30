/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2012-9-26 上午10:30:52
 */
package cn.flyrise.android.shared.utility.picker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kankan.wheel.widget.OnWheelChangedListener;
import com.kankan.wheel.widget.WheelView;
import com.kankan.wheel.widget.adapters.WheelViewAdapter;

import java.util.List;

import cn.flyrise.android.shared.utility.FEPopupWindow;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.common.utils.PixelUtil;

/**
 * 类功能描述：</br>
 *
 * @author 钟永健
 * @version 1.0</br> 修改时间：2012-9-26</br> 修改备注：</br>
 */
public class FEPicker extends FEPopupWindow {
    private WheelView WV;

    private TextView titleTV;

    private Button titleSureBnt;
    private Button titleEmptyBnt;

    private final ContentView contentView;

    private List<String> pickerTexts;

    private OnWheelChangedListener wheelChangedListener;

    private final int contentViewHight;

    private final int contentViewWidth;

    public OnPickerButtonClickListener buttonClickListener;

    public FEPicker(Context context) {
        super(context);
        contentView = new ContentView(context);
        setContentView(contentView);
        setWidth(android.view.ViewGroup.LayoutParams.FILL_PARENT);
        setHeight(android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        final int[] viewSpecs = measureViewSpecs(contentView);
        contentViewWidth = viewSpecs[0];
        contentViewHight = viewSpecs[1];
    }

    /**
     * 带数据的显示
     */
    public void showWithData(List<String> pickerTexts) {
        showWithData(pickerTexts, null);
    }

    /**
     * 带数据的显示
     */
    public void showWithData(List<String> pickerTexts, String currentText) {
        show();
        contentView.refreshView(pickerTexts, currentText);
        this.pickerTexts = pickerTexts;
    }

    @Override
    public int getContentViewHight() {
        return contentViewHight;
    }

    @Override
    public int getContentViewWidth() {
        return contentViewWidth;
    }

    public class ContentView extends LinearLayout {

        public ContentView(Context context) {
            super(context);
            intView(context);
            setViewVisibility();
            setListener();
        }

        /**
         * 获取布局文件中的view
         */
        private void intView(Context context) {
            final View view = LayoutInflater.from(context).inflate(R.layout.datepicker_view, null);
            titleTV = (TextView) view.findViewById(R.id.picker_title_tv);
            WV = (WheelView) view.findViewById(R.id.picker_wheelview01);
            // 确定按钮
            titleSureBnt = (Button) view.findViewById(R.id.picker_sure_bnt);
            titleEmptyBnt = (Button) view.findViewById(R.id.picker_reset_bnt);
            addView(view, android.view.ViewGroup.LayoutParams.FILL_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
            setPadding(0, 0, 0, PixelUtil.dipToPx(5));
        }

        /**
         * 设置view显示或隐藏
         */
        private void setViewVisibility() {
            WV.setVisibility(View.VISIBLE);
        }

        /**
         * 刷新view
         */
        public void refreshView(List<String> pickerTexts, String currentText) {
            int index = 0;
            if (pickerTexts != null) {
                final int size = pickerTexts.size();
                for (int i = 0; i < size; i++) {
                    final String text = pickerTexts.get(i);
                    if (currentText != null && currentText.equals(text)) {
                        index = i;
                    }
                }
                setWheelView(WV, new FEPickerAdaper(getContext(), pickerTexts), index);
                titleTV.setText(currentText);
            }

        }

        /**
         * 设置监听器
         */
        private void setListener() {
            WV.addChangingListener(wheelListener);

            // 确定按钮的点击事件监听
            titleSureBnt.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (buttonClickListener != null) {
                        buttonClickListener.OnButtonClick(v, getCurrentText());
                    }
                }
            });
            // 确定按钮的点击事件监听
            titleEmptyBnt.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (buttonClickListener != null) {
                        buttonClickListener.OnButtonClick(v, "");
                    }
                }
            });
        }

        /**
         * 对WheelView做相关操作
         */
        private void setWheelView(WheelView wheelView, WheelViewAdapter adapter, int currentItem) {
            wheelView.setViewAdapter(adapter);// 设置显示数据适配器
            wheelView.setCyclic(false);// 可循环滚动
            wheelView.setCurrentItem(currentItem);// 初始化时显示的数据
        }

        private final OnWheelChangedListener wheelListener = new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                final FEPickerAdaper adapter = (FEPickerAdaper) wheel.getViewAdapter();
                switch (wheel.getId()) {
                    case R.id.picker_wheelview01:
                        titleTV.setText(adapter.getItemText(newValue));
                        if (wheelChangedListener != null) {
                            wheelChangedListener.onChanged(wheel, oldValue, newValue);
                        }
                        break;
                    default:
                        break;
                }
            }
        };

    }

    /**
     * 设置item改变监听事件
     */
    public void setOnChangingListener(OnWheelChangedListener wheelChangedListener) {
        this.wheelChangedListener = wheelChangedListener;
    }

    /**
     * 获取当前选中文本
     */
    public String getCurrentText() {
        final int index = WV.getCurrentItem();
        if (pickerTexts != null && index < pickerTexts.size()) {
            return pickerTexts.get(index);
        }
        return null;
    }

    /**
     * 设置是否循环滚动
     */
    public void setCyclic(boolean isCyclic) {
        WV.setCyclic(isCyclic);
    }

    /**
     * 设置点击确定和清空按钮的监听事件
     */
    public void setOnPickerButtonClickListener(OnPickerButtonClickListener listener) {
        this.buttonClickListener = listener;
    }

    public interface OnPickerButtonClickListener {
        /**
         * 点击了确定或清空按钮
         */
        void OnButtonClick(View view, String data);
    }
}
