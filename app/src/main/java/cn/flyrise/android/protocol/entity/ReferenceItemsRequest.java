/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2013-2-25 ����11:48:20
 */
package cn.flyrise.android.protocol.entity;

import cn.flyrise.feep.core.network.request.RequestContent;

public class ReferenceItemsRequest extends RequestContent {
    public static final String TYPE_URGENCY = "0";
    public static final String TYPE_COMMON_WORDS = "1";

    public static final String NAMESPACE = "ReferenceItemsRequest";

    private String requestType;

    @Override
    public String getNameSpace () {
        return NAMESPACE;
    }

    public String getRequestType () {
        return requestType;
    }

    public void setRequestType (String requestType) {
        this.requestType = requestType;
    }

}
