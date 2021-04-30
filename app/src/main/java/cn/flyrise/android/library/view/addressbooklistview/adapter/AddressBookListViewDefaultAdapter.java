/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2012-9-18 上午10:44:12
 */

package cn.flyrise.android.library.view.addressbooklistview.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import cn.flyrise.android.library.view.addressbooklistview.been.AddressBookListItem;
import cn.flyrise.android.protocol.model.AddressBookItem;
import cn.flyrise.feep.R;

/**
 * 类功能描述：通讯录列表控件的默认适配器</br>
 *
 * @author 钟永健
 * @version 1.0</br> 修改时间：2012-9-18</br> 修改备注：</br>
 */
public class AddressBookListViewDefaultAdapter extends AddressBookBaseAdapter {

    private final Context context;

    private ArrayList<AddressBookListItem> listDatas = new ArrayList<> ();

    class ViewHolder {
        TextView nameTV;
        ImageView addIconIV;
    }

    public AddressBookListViewDefaultAdapter (Context context) {
        this.context = context;
    }

    @Override
    public int getCount () {
        if (listDatas == null) {
            return 0;
        }
        return listDatas.size ();
    }

    @Override
    public AddressBookListItem getItem (int position) {
        return listDatas.get (position);
    }

    @Override
    public long getItemId (int position) {
        return position;
    }

    @Override
    public View getView (final int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder ();
            convertView = LayoutInflater.from (context).inflate (R.layout.addressbooklistview_item, null);
            holder.nameTV = (TextView) convertView.findViewById (R.id.addressbooklistview_item_name);
            holder.addIconIV = (ImageView) convertView.findViewById (R.id.addressbooklistview_item_add_icon);
            convertView.setTag (holder);
        } else {
            holder = (ViewHolder) convertView.getTag ();
        }
        final AddressBookItem item = listDatas.get (position).getAddressBookItem ();
        if (item != null) {
            holder.nameTV.setText (item.getName ());
        }
        convertView.setBackgroundResource (R.drawable.listview_item_bg);
        return convertView;
    }

    @Override
    public void refreshAdapter (ArrayList<AddressBookListItem> listDatas) {
        this.listDatas = listDatas;
        notifyDataSetChanged ();
    }

}
