/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2013-2-25 ����11:48:20
 */
package cn.flyrise.android.protocol.entity;

import java.util.List;

import cn.flyrise.android.protocol.model.AddressBookItem;
import cn.flyrise.feep.core.network.request.ResponseContent;

public class AddressBookResponse extends ResponseContent {

    private String requestType;
    private String totalNums;
    private String currentDeptID;
    private String currentDeptName;
    private String currentDeptType;
    private List<AddressBookItem> items;

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

    public String getCurrentDeptID () {
        return currentDeptID;
    }

    public void setCurrentDeptID (String currentDeptID) {
        this.currentDeptID = currentDeptID;
    }

    public String getCurrentDeptName () {
        return currentDeptName;
    }

    public void setCurrentDeptName (String currentDeptName) {
        this.currentDeptName = currentDeptName;
    }

    public String getCurrentDeptType () {
        return currentDeptType;
    }

    public void setCurrentDeptType (String currentDeptType) {
        this.currentDeptType = currentDeptType;
    }

    public List<AddressBookItem> getItems () {
        return items;
    }

    public void setItems (List<AddressBookItem> items) {
        this.items = items;
    }
}
