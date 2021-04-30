/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2013-2-25 ����3:51:57
 */
package cn.flyrise.android.protocol.entity;

import com.google.gson.annotations.SerializedName;

import cn.flyrise.feep.core.network.request.RequestContent;

public class WaitingSendDetailRequest extends RequestContent {

    public static final String NAMESPACE = "RemoteRequest";

    @SerializedName("param1")
    private String detailId;
    public String count = "1";
    public String method = "getTemporaryCollaborative";
    public String obj = "cflowworkService";

    public WaitingSendDetailRequest(String detailId) {
        this.detailId = detailId;
    }

    @Override
    public String getNameSpace() {
        return NAMESPACE;
    }
}
