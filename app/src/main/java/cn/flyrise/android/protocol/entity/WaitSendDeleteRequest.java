/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2013-1-4 ����04:42:03
 */
package cn.flyrise.android.protocol.entity;

import com.google.gson.annotations.SerializedName;

import cn.flyrise.feep.core.network.request.RequestContent;

public class WaitSendDeleteRequest extends RequestContent {

    public static final String NAMESPACE = "RemoteRequest";

    private String obj;
    private String method;
    private String count;
    @SerializedName("param1")
    private String ids;

    public WaitSendDeleteRequest(String ids) {
        this.obj = "collaborationService";
        this.method = "deleteWaitSendForMobile";
        this.count = "1";
        this.ids = ids;
    }

    @Override
    public String getNameSpace() {
        return NAMESPACE;
    }

}
