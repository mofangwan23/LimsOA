/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2013-2-25 ����3:51:57
 */
package cn.flyrise.android.protocol.entity;

import cn.flyrise.feep.core.network.request.RequestContent;

public class WaitingSendListRequest extends RequestContent {

    public static final String NAMESPACE = "RemoteRequest";

    public String count = "0";
    public String method = "getTemporaryList";
    public String obj = "cflowworkService";

    public WaitingSendListRequest() {
    }

    @Override
    public String getNameSpace() {
        return NAMESPACE;
    }
}
