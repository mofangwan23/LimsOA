/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2012-9-24 上午11:25:16
 */

package cn.flyrise.android.shared.utility.datepicker;

import android.app.Service;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kankan.wheel.widget.OnWheelChangedListener;
import com.kankan.wheel.widget.WheelView;
import com.kankan.wheel.widget.adapters.NumericWheelAdapter;
import com.kankan.wheel.widget.adapters.WheelViewAdapter;

import java.text.DecimalFormat;
import java.util.Calendar;

import cn.flyrise.android.shared.utility.FEPopupWindow;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.common.utils.CommonUtil;

/**
 * 类功能描述：时间选择器</br>
 * @author 钟永健
 * @version 1.0</br> 修改时间：2012-9-24</br> 修改备注：</br>
 */
public class FEDatePicker extends FEPopupWindow {
    private WheelView yearWV;

    private WheelView monthWV;

    private WheelView dayWV;

    private WheelView hoursWV;

    private WheelView minuteWV;

    private Calendar calendar;

    private int currentYearIndex;

    private int currentYear;

    private int currentMonth;

    private static int START_YEAR = 1990, END_YEAR = 2100;

    private TextView titleTV;

    private Button titleSureBnt;
    private Button titleEmptyBnt;

    private final ContentView contentView;

    private final int contentViewHight;

    private final int contentViewWidth;

    public OnDataPickerButtonClickListener buttonClickListener;

    public FEDatePicker(Context context) {
        super(context);
        contentView = new ContentView(context);
        setContentView(contentView);
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        final int[] viewSpecs = measureViewSpecs(contentView);
        contentViewWidth = viewSpecs[0];
        contentViewHight = viewSpecs[1];
    }

    /**
     * 显示时间控件
     */
    @Override
    public void show() {
        super.show();
        contentView.refreshView();
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
            refreshView();
            setListener();
        }

        /**
         * 获取布局文件中的view
         */
        private void intView(Context context) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
            final View view = inflater.inflate(R.layout.datepicker_view, null);
            titleTV = (TextView) view.findViewById(R.id.picker_title_tv);
            yearWV = (WheelView) view.findViewById(R.id.picker_wheelview01);
            monthWV = (WheelView) view.findViewById(R.id.picker_wheelview02);
            dayWV = (WheelView) view.findViewById(R.id.picker_wheelview03);
            hoursWV = (WheelView) view.findViewById(R.id.picker_wheelview04);
            minuteWV = (WheelView) view.findViewById(R.id.picker_wheelview05);
            // 确定按钮
            titleSureBnt = (Button) view.findViewById(R.id.picker_sure_bnt);
            titleEmptyBnt = (Button) view.findViewById(R.id.picker_reset_bnt);
            addView(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        /**
         * 设置view显示或隐藏
         */
        private void setViewVisibility() {
            yearWV.setVisibility(View.GONE);
            monthWV.setVisibility(View.VISIBLE);
            dayWV.setVisibility(View.VISIBLE);
            hoursWV.setVisibility(View.VISIBLE);
            minuteWV.setVisibility(View.VISIBLE);
        }

        /**
         * 刷新view
         */
        public void refreshView() {
            calendar = Calendar.getInstance();
            currentYear = calendar.get(Calendar.YEAR);
            currentMonth = calendar.get(Calendar.MONTH);
            final int day = calendar.get(Calendar.DATE);
            final int hour = calendar.get(Calendar.HOUR_OF_DAY);
            final int minute = calendar.get(Calendar.MINUTE);

            currentYearIndex = currentYear - START_YEAR;
            // “年”WheelView控件
            setWheelView(yearWV, new NumericWheelAdapter(getContext(), START_YEAR, END_YEAR), CommonUtil.getString(R.string.util_year), currentYearIndex);
            // “月”WheelView控件
            setWheelView(monthWV, new NumericWheelAdapter(getContext(), 1, 12, "%02d"), CommonUtil.getString(R.string.util_month), currentMonth);

            // “日”WheelView控件
            setWheelView(dayWV, new DayWeekWheelAdapter(getContext(), currentYear, currentMonth), null, day - 1);

            // “时”WheelView控件
            setWheelView(hoursWV, new NumericWheelAdapter(getContext(), 0, 23), CommonUtil.getString(R.string.util_hour), hour);

            // “分”WheelView控件
            setWheelView(minuteWV, new NumericWheelAdapter(getContext(), 0, 59, "%02d"), CommonUtil.getString(R.string.util_minute), minute);

            titleTV.setText(getDate());

        }

        /**
         * 设置监听器
         */
        private void setListener() {
            yearWV.addChangingListener(wheelListener);
            monthWV.addChangingListener(wheelListener);
            dayWV.addChangingListener(wheelListener);
            hoursWV.addChangingListener(wheelListener);
            minuteWV.addChangingListener(wheelListener);

            // 确定按钮的点击事件监听
            titleSureBnt.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (buttonClickListener != null) {
                        buttonClickListener.OnButtonClick(v, getDate());
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
        private void setWheelView(WheelView wheelView, WheelViewAdapter adapter, String lable, int currentItem) {
            wheelView.setViewAdapter(adapter);// 设置显示数据适配器
            wheelView.setCyclic(true);// 可循环滚动
            // wheelView.setLabel(lable);// 添加文字
            wheelView.setCurrentItem(currentItem);// 初始化时显示的数据
        }

        private final OnWheelChangedListener wheelListener = new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                switch (wheel.getId()) {
                    case R.id.picker_wheelview01:
                        currentYear = newValue + START_YEAR;
                        dayWV.setViewAdapter(new DayWeekWheelAdapter(getContext(), currentYear, currentMonth));
                        break;
                    case R.id.picker_wheelview02:
                        currentMonth = newValue;
                        // 判断改变月前后来判断年是否变化
                        if (oldValue < newValue && oldValue == 0 && newValue == 11) {
                            final int downindex = --currentYearIndex;
                            yearWV.setCurrentItem(downindex);
                        }
                        else if (oldValue > newValue && oldValue == 11 && newValue == 0) {
                            final int upindex = ++currentYearIndex;
                            yearWV.setCurrentItem(upindex);
                        }
                        dayWV.setViewAdapter(new DayWeekWheelAdapter(getContext(), currentYear, currentMonth));
                        break;
                    // case R.id.picker_wheelview03:
                    // DayWeekWheelAdapter adapter =
                    // (DayWeekWheelAdapter)wheel.getViewAdapter();
                    // int maxDay = 30;
                    // if (adapter != null) {
                    // maxDay = adapter.getMaxValue();
                    // if (oldValue < newValue && newValue == maxDay - 1 &&
                    // oldValue == 0) {
                    // monthWV.setCurrentItem(--currentMonth);
                    //
                    // } else if (oldValue > newValue && newValue == 0
                    // && oldValue == maxDay - 1) {
                    // monthWV.setCurrentItem(++currentMonth);
                    //
                    // }
                    // }
                    // break;

                    default:
                        break;
                }
                titleTV.setText(getDate());
            }
        };

    }

    /**
     * 获取当前选中的时间</br>格式如：2012-09-24 13:26
     */
    public String getDate() {
        final String parten = "00";
        final DecimalFormat decimal = new DecimalFormat(parten);
        final StringBuilder sb = new StringBuilder();
        sb.append(yearWV.getCurrentItem() + START_YEAR + "-");
        sb.append(decimal.format((monthWV.getCurrentItem() + 1)) + "-");
        sb.append(decimal.format(getDay()) + " ");
        sb.append(decimal.format(hoursWV.getCurrentItem()) + ":");
        sb.append(decimal.format(minuteWV.getCurrentItem()));
        return sb.toString();
    }

    private int getDay() {
        final DayWeekWheelAdapter adapter = (DayWeekWheelAdapter) dayWV.getViewAdapter();
        final int itemsCount = adapter.getItemsCount();
        int day = dayWV.getCurrentItem() + 1;
        if (itemsCount < day) {
            day = day - itemsCount;
        }
        return day;
    }

    /**
     * 设置点击确定和清空按钮的监听事件
     */
    public void setOnDatePickerButtonClickListener(OnDataPickerButtonClickListener listener) {
        this.buttonClickListener = listener;
    }

    public interface OnDataPickerButtonClickListener {
        /**
         * 点击了确定或清空按钮
         */
        void OnButtonClick(View view, String data);
    }
}
