/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2013-1-4 ����04:42:03
 */
package cn.flyrise.android.protocol.entity;

import com.google.gson.annotations.SerializedName;

import cn.flyrise.feep.core.network.request.RequestContent;

public class CollaborationTransmitRequest extends RequestContent {

    public static final String NAMESPACE = "RemoteRequest";

    private String obj;
    private String method;
    private String count;
    @SerializedName("param1")
    private String collaborationId;
    @SerializedName("param2")
    private String opinion;
    @SerializedName("param3")
    private String transmitReplay;

    public CollaborationTransmitRequest(String collaborationId, String opinion, boolean transmitReplay) {
        this.collaborationId = collaborationId;
        this.count = "3";
        this.method = "changeFlowworkForMobile";
        this.obj = "cflowworkService";
        this.collaborationId = collaborationId;
        this.opinion = opinion;
        this.transmitReplay = transmitReplay ? "1" : "0";
    }

    @Override
    public String getNameSpace() {
        return NAMESPACE;
    }


}
