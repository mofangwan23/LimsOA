/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2013-1-4 ����04:42:03
 */
package cn.flyrise.android.protocol.entity;


import cn.flyrise.feep.core.network.request.RequestContent;

public class MatterFolderTreeRequest extends RequestContent {

    public static final String NAMESPACE = "RemoteRequest";

    private String obj;
    private String method;
    private String count;

    @Override
    public String getNameSpace() {
        return NAMESPACE;
    }

    public MatterFolderTreeRequest() {
        obj = "relevanceService";
        method = "getGroupFolderTree";
        count = "0";
    }
}
