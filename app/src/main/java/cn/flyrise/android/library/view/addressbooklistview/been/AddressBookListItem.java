/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2012-9-17 下午3:55:30
 */
package cn.flyrise.android.library.view.addressbooklistview.been;

import java.io.Serializable;
import java.util.ArrayList;

import cn.flyrise.android.protocol.model.AddressBookItem;

/**
 * 类功能描述：</br>
 *
 * @author 钟永健
 * @version 1.0</br> 修改时间：2012-9-17</br> 修改备注：</br>
 */
public class AddressBookListItem implements Serializable {
    private static final long serialVersionUID = 1L;
    private String itemID;
    private String itemName;
    private int dataPage;
    private int totalNums;
    private AddressBookItem addressBookItem;
    private ArrayList<AddressBookListItem> listDatas;
    private AddressBookListItem childItem;
    private AddressBookListItem parentItem;

    public String getItemID () {
        return itemID;
    }

    public void setItemID (String itemID) {
        this.itemID = itemID;
    }

    public String getItemName () {
        return itemName;
    }

    public void setItemName (String itemName) {
        this.itemName = itemName;
    }

    public int getDataPage () {
        return dataPage;
    }

    public void setDataPage (int dataPage) {
        this.dataPage = dataPage;
    }

    public int getTotalNums () {
        return totalNums;
    }

    public void setTotalNums (int totalNums) {
        this.totalNums = totalNums;
    }

    public AddressBookItem getAddressBookItem () {
        return addressBookItem;
    }

    public void setAddressBookItem (AddressBookItem addressBookItem) {
        this.addressBookItem = addressBookItem;
    }

    public ArrayList<AddressBookListItem> getListDatas () {
        return listDatas;
    }

    public void setListDatas (ArrayList<AddressBookListItem> listDatas) {
        this.listDatas = listDatas;
    }

    public AddressBookListItem getChildItem () {
        return childItem;
    }

    public void setChildItem (AddressBookListItem childItem) {
        this.childItem = childItem;
    }

    public AddressBookListItem getParentItem () {
        return parentItem;
    }

    public void setParentItem (AddressBookListItem parentItem) {
        this.parentItem = parentItem;
    }

    public void clearListDatas() {
        if (this.listDatas == null) {
            return;
        }
        listDatas.clear();
    }
}
