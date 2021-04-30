package cn.flyrise.android.protocol.entity;

import cn.flyrise.feep.core.network.request.RequestContent;

public class EmailDeleteRequest extends RequestContent {
    public static final String NAMESPACE = "EmailDeleteRequest";
    private String id;
    private String requestType;

    @Override
    public String getNameSpace () {
        return NAMESPACE;
    }

    public String getId () {
        return id;
    }

    public void setId (String id) {
        this.id = id;
    }

    public String getRequestType () {
        return requestType;
    }

    public void setRequestType (String requestType) {
        this.requestType = requestType;
    }

}
