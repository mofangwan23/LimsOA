/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2013-2-25 ����3:51:57
 */
package cn.flyrise.android.protocol.entity;

import cn.flyrise.feep.core.network.request.RequestContent;

public class UserInfoModifyRequest extends RequestContent {
    public static final String NAMESPACE = "UserInfoModifyRequest";

    private String requestType;
    private String text;
    private String guid = "";

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    @Override
    public String getNameSpace() {
        return NAMESPACE;
    }
}
