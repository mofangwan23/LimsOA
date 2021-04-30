/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2012-9-26 上午11:05:20
 */
package cn.flyrise.android.shared.utility.picker;

import java.util.List;

import android.content.Context;

import com.kankan.wheel.widget.adapters.AbstractWheelTextAdapter;

/**
 * 类功能描述：</br>
 *
 * @author 钟永健
 * @version 1.0</br> 修改时间：2012-9-26</br> 修改备注：</br>
 */
public class FEPickerAdaper extends AbstractWheelTextAdapter {

    private List<String> pickerTexts;
    /**
     * 默认可见的item个数
     */
    private final int visibleItemNums = 5;

    public FEPickerAdaper (Context context, List<String> pickerTexts) {
        super (context);
        this.pickerTexts = pickerTexts;
    }

    @Override
    public int getItemsCount () {
        if (pickerTexts != null && pickerTexts.size () != 0) {
            return pickerTexts.size ();
        }
        return visibleItemNums;
    }

    @Override
    protected CharSequence getItemText (int index) {
        if (index >= 0 && index < getItemsCount ()) {
            if (pickerTexts != null && pickerTexts.size () > index) {
                return pickerTexts.get (index);
            }
        }
        return null;
    }

    /**
     * 刷新适配器
     */
    public void refreshAdapter (List<String> pickerTexts) {
        this.pickerTexts = pickerTexts;
        notifyDataChangedEvent ();
    }

}
