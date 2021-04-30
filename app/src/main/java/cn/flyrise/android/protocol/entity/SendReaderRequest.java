/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2013-1-4 ����04:42:03
 */
package cn.flyrise.android.protocol.entity;

import com.google.gson.annotations.SerializedName;

import cn.flyrise.feep.core.network.request.RequestContent;

public class SendReaderRequest extends RequestContent {

    public static final String NAMESPACE = "RemoteRequest";

    private String obj;
    private String method;
    private String count;
    @SerializedName("param1")
    private String figureId;  ////传阅人ID
    @SerializedName("param2")
    private String inforId; //当前流程id
    @SerializedName("param3")
    private String mind; //回复
    @SerializedName("param4")
    private String wait;//传阅等待状态 "0" 或 "1"
    @SerializedName("param5")
    private String msgFlag;//手机短信
    @SerializedName("param6")
    private String emailFlag;//邮件提醒

    public SendReaderRequest(String figureId, String inforId, String mind, String msgFlag, String emailFlag) {
        this.obj = "taskService";
        this.method = "SendReaderForMobile";
        this.count = "6";
        this.figureId = figureId;
        this.inforId = inforId;
        this.mind = mind;
        this.wait = "0";
        this.msgFlag = msgFlag;
        this.emailFlag = emailFlag;
    }

    @Override
    public String getNameSpace() {
        return NAMESPACE;
    }

}
