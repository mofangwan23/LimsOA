/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2012-12-30 ����10:35:46
 */
package cn.flyrise.android.protocol.entity;

import java.util.List;

import cn.flyrise.android.protocol.model.ReferenceItem;
import cn.flyrise.feep.core.network.request.ResponseContent;

public class ReferenceItemsResponse extends ResponseContent {

    private String requestType;
    private List<ReferenceItem> items;

    public String getRequestType () {
        return requestType;
    }

    public void setRequestType (String requestType) {
        this.requestType = requestType;
    }

    public List<ReferenceItem> getItems () {
        return items;
    }

    public void setItems (List<ReferenceItem> items) {
        this.items = items;
    }
}
