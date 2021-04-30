/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2012-12-30 ����10:35:46
 */
package cn.flyrise.android.protocol.entity;

import cn.flyrise.android.protocol.model.ListTable;
import cn.flyrise.feep.core.network.request.ResponseContent;

public class ListResponse extends ResponseContent {
    private String requestType;
    private String totalNums = "0";
    private ListTable table;

    public String getRequestType () {
        return requestType;
    }

    public void setRequestType (String requestType) {
        this.requestType = requestType;
    }

    public String getTotalNums () {
        return totalNums;
    }

    public void setTotalNums (String totalNums) {
        this.totalNums = totalNums;
    }

    public ListTable getTable () {
        return table;
    }

    public void setTable (ListTable table) {
        this.table = table;
    }
}
