/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2012-6-18 下午7:48:19
 */
package cn.flyrise.feep.commonality.bean;

import android.support.annotation.Keep;

import java.util.ArrayList;
import java.util.List;

/**
 * 类功能描述：ViewPager列表的信息</br>
 * 
 * @author 钟永健
 * @version 1.0</br> 修改时间：2012-6-18</br> 修改备注：</br>
 */
@Keep
public class FEListInfo {
    /** 数据总数量 */
    private int              totalNums;
    private String           searchKey;
    private int  requestType;
    private List<FEListItem> listItems;

    /** 获得列表数据总数量 */
    public int getTotalNums() {
        return totalNums;
    }

    /** 设置列表数据总数量 */
    public void setTotalNums(int totalNums) {
        this.totalNums = totalNums;
    }

    public String getSearchKey() {
        return searchKey;
    }

    public void setSearchKey(String searchKey) {
        this.searchKey = searchKey;
    }

    public int getRequestType() {
        return requestType;
    }

    public void setRequestType(int requestType) {
        this.requestType = requestType;
    }

    public List<FEListItem> getListItems() {
        return listItems;
    }

    public void setListItems(List<FEListItem> listItems) {
        this.listItems = listItems;
    }

    public void addAllListItem(List<FEListItem> collection) {
        if (this.listItems == null) {
            this.listItems = new ArrayList<> ();
        }
        this.listItems.addAll(collection);
    }

    public void addAllListItem(int location, List<FEListItem> collection) {
        if (this.listItems == null) {
            listItems = new ArrayList<> ();
        }
        listItems.addAll(location, collection);
    }

    public void clearListDatas() {
        if (this.listItems == null) {
            return;
        }
        listItems.clear();
    }
}
