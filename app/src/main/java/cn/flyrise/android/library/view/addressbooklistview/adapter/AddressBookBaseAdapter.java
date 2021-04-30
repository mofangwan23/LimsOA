/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2012-9-18 上午10:36:53
 */
package cn.flyrise.android.library.view.addressbooklistview.adapter;

import java.util.ArrayList;

import android.widget.BaseAdapter;

import cn.flyrise.android.library.view.addressbooklistview.been.AddressBookListItem;

/**
 * 类功能描述：</br>
 *
 * @author 钟永健
 * @version 1.0</br> 修改时间：2012-9-18</br> 修改备注：</br>
 */
public abstract class AddressBookBaseAdapter extends BaseAdapter {
    /**
     * 刷新适配器
     */
    public abstract void refreshAdapter (ArrayList<AddressBookListItem> listDatas);

    @Override
    public abstract AddressBookListItem getItem (int position);
}
