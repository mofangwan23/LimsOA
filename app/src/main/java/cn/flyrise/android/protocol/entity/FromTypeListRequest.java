/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2013-3-12 下午5:29:54
 */

package cn.flyrise.android.protocol.entity;

import cn.flyrise.feep.core.network.request.RequestContent;

/**
 * 类功能描述：</br>
 *
 * @author 钟永健
 * @version 1.0</br> 修改时间：2013-3-12</br> 修改备注：</br>
 */
public class FromTypeListRequest extends RequestContent {
    public static final String NAMESPACE = "NewFormTypeRequest";

    private String formListId;

    private String page;

    private String perPageNums;

    private String searchKey;

    @Override
    public String getNameSpace () {
        return NAMESPACE;
    }

    public String getFormListId () {
        return formListId;
    }

    public void setFormListId (String formListId) {
        this.formListId = formListId;
    }

    public String getPage () {
        return page;
    }

    public void setPage (String page) {
        this.page = page;
    }

    public String getPerPageNums () {
        return perPageNums;
    }

    public void setPerPageNums (String perPageNums) {
        this.perPageNums = perPageNums;
    }

    public String getSearchKey () {
        return searchKey;
    }

    public void setSearchKey (String searchKey) {
        this.searchKey = searchKey;
    }

}
