/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2013-3-13 上午11:03:22
 */

package cn.flyrise.android.protocol.entity;

import java.util.List;

import cn.flyrise.android.protocol.model.FormTypeItem;
import cn.flyrise.feep.core.network.request.ResponseContent;

/**
 * 类功能描述：</br>
 *
 * @author 钟永健
 * @version 1.0</br> 修改时间：2013-3-13</br> 修改备注：</br>
 */
public class FormTypeResponse extends ResponseContent {
    private String totalNums;
    private List<FormTypeItem> formTypeItems;

    public String getTotalNums () {
        return totalNums;
    }

    public void setTotalNums (String totalNums) {
        this.totalNums = totalNums;
    }

    public List<FormTypeItem> getFormTypeItems () {
        return formTypeItems;
    }

    public void setFormTypeItems (List<FormTypeItem> formTypeItems) {
        this.formTypeItems = formTypeItems;
    }

}
