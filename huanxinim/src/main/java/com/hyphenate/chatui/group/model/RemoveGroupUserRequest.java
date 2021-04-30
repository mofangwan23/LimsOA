package com.hyphenate.chatui.group.model;

import cn.flyrise.feep.core.network.request.RequestContent;

/**
 * Created by klc on 2017/3/23.
 */

public class RemoveGroupUserRequest extends RequestContent {

    public static final String NAMESPACE = "RemoteRequest";

    private String obj = "easemobGroupService";
    private String method = "delMember";
    private String count = "2";
    private String[] param1;  //userId的数组
    private String param2;  //群Id


    @Override
    public String getNameSpace() {
        return NAMESPACE;
    }

    public RemoveGroupUserRequest(String[] userIds, String groupId) {
        this.param1 = userIds;
        this.param2 = groupId;
    }
}
