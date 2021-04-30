/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2012-10-31 上午11:26:40
 */
package cn.flyrise.feep.form.been;

import android.support.annotation.Keep;

import java.util.List;

import cn.flyrise.android.protocol.model.FormTypeItem;

/**
 * 类功能描述：</br>
 * 
 * @author 钟永健
 * @version 1.0</br> 修改时间：2012-10-31</br> 修改备注：</br>
 */
@Keep
public class FormListItemInfo {
    private String                 formListId;
    private String                 searchKey;
    private FormTypeItem           formTypeItem;
    private FormListItemInfo       parentItem;
    private List<FormListItemInfo> childFormTypeItems;
    private int                    totalNums;
    private int                    page = 1;

    public String getFormListId() {
        return formListId;
    }

    public void setFormListId(String formListId) {
        this.formListId = formListId;
    }

    public String getSearchKey() {
        return searchKey;
    }

    public void setSearchKey(String searchKey) {
        this.searchKey = searchKey;
    }

    public FormTypeItem getFormTypeItem() {
        return formTypeItem;
    }

    public void setFormTypeItem(FormTypeItem formTypeItem) {
        this.formTypeItem = formTypeItem;
    }

    public FormListItemInfo getParentItem() {
        return parentItem;
    }

    public void setParentItem(FormListItemInfo parentItem) {
        this.parentItem = parentItem;
    }

    public List<FormListItemInfo> getChildFormTypeItems() {
        return childFormTypeItems;
    }

    public void setChildFormTypeItems(List<FormListItemInfo> childFormTypeItems) {
        this.childFormTypeItems = childFormTypeItems;
    }

    public int getTotalNums() {
        return totalNums;
    }

    public void setTotalNums(int totalNums) {
        this.totalNums = totalNums;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

}
