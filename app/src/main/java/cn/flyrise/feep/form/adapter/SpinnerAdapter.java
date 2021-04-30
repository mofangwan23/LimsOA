/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2012-8-15 下午5:15:54
 */

package cn.flyrise.feep.form.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.flyrise.android.protocol.model.FormNodeItem;
import cn.flyrise.android.protocol.model.ReferenceItem;
import cn.flyrise.feep.R;
import cn.flyrise.feep.form.been.FormNodeToSubNode;

/**
 * 类功能描述：</br>
 *
 * @author 钟永健
 * @version 1.0</br> 修改时间：2012-8-15</br> 修改备注：</br>
 */
public class SpinnerAdapter extends BaseAdapter {
    private final Context context;

    private List<String> data = new ArrayList<>();

    private Object objectData = new Object();

    private int selectedPosition = 0;

    /**
     * isSendDo 判断是送办还是退回，送办-true；退回-false
     */
    private boolean isSendDo = true;

    public SpinnerAdapter(Context context, List<String> data, boolean isSendDo) {
        this.context = context;
        this.data = data;
        this.isSendDo = isSendDo;
    }

    /**
     * 获取item对应的数据
     */
    @SuppressWarnings("unchecked")
    public Object getDataItem(int position) {
        final ArrayList<Object> datas = (ArrayList<Object>) objectData;
        return datas.get(position);
    }

    @Override
    public int getCount() {
        return data == null ? 0 : data.size();
    }

    /**
     * 获取item对应的数据
     */
    public String getItem(int position) {
        String itemName = "";
        if (data != null) {
            itemName = data.get(position);
        }
        return itemName;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View view = LayoutInflater.from(context).inflate(R.layout.spiner_dialog_item, null);
        TextView tv = (TextView) view.findViewById(R.id.text);
        tv.setBackgroundResource(R.drawable.form_dispose_listview_item_selecter);
        tv.setText(data.get(position));
        if (selectedPosition == position) {// 如果item被选中就改变其颜色
            tv.setTextColor(context.getResources().getColor(R.color.text_checked_color));
        }
        return view;
    }

    /**
     * 设置当前选中的position
     */
    public void setSelectedItemPosition(int position) {
        selectedPosition = position;
    }

//    @Override
//    public IView getDropDownView(int position, IView convertView, android.view.ViewGroup parent) {
//        final IView view = (IView) LayoutInflater.from(context).inflate(R.layout.spiner_dialog_item, null);
//        TextView tv = (TextView) view.findViewById(R.id.text);
//        tv.setBackgroundResource(R.drawable.form_dispose_listview_item_selecter);
//        tv.setText(record.get(position));
//        if (selectedPosition == position) {// 如果item被选中就改变其颜色
//            tv.setTextColor(context.getResources().getColor(R.color.text_checked_color));
//        }
//        return tv;
//    }

    /**
     * 刷新数据
     *
     * @param refrshData 数据;
     */
    @SuppressWarnings("unchecked")
    public void refreshData(Object refrshData) {
        objectData = refrshData;
        if (refrshData != null) {
            final ArrayList<Object> datas = (ArrayList<Object>) refrshData;
            for (final Object obj : datas) {
                if (obj != null) {
                    String name = null;
                    if (isSendDo) {
                        name = ((ReferenceItem) obj).getValue();
                    } else {
                        final FormNodeItem nodeItem = ((FormNodeToSubNode) obj).getFormNodeItem();
                        if (nodeItem != null) {
                            name = nodeItem.getName();
                        }
                    }
                    if (!TextUtils.isEmpty(name)) {//防止ArrayAdapter崩溃
                        data.add(name);
                    }
                }
            }
        }
        notifyDataSetChanged();
    }

}
