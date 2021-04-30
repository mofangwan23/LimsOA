/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2013-2-25 ����11:52:19
 */
package cn.flyrise.android.protocol.entity;

import cn.flyrise.feep.core.network.request.RequestContent;

public class LogoutRequest extends RequestContent {
    public static final String NAMESPACE = "LogoutRequest";
    private String pushOff = "1";//"1":清楚服务端推送数据

    public LogoutRequest(String pushOff) {
        this.pushOff = pushOff;
    }

    public LogoutRequest() {
    }

    @Override
    public String getNameSpace () {
        return NAMESPACE;
    }

    public String getPushOff () {
        return pushOff;
    }

    public void setPushOff (String pushOff) {
        this.pushOff = pushOff;
    }

}
