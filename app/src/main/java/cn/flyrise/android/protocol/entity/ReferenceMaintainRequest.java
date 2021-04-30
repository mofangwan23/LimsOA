package cn.flyrise.android.protocol.entity;

import cn.flyrise.feep.core.network.request.RequestContent;

public class ReferenceMaintainRequest extends RequestContent {
    public static final String NAMESPACE = "ReferenceMaintainRequest";

    private String id;
    private String requestType;
    private String referenceType;
    private String value;

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

    public String getReferenceType () {
        return referenceType;
    }

    public void setReferenceType (String referenceType) {
        this.referenceType = referenceType;
    }

    public String getValue () {
        return value;
    }

    public void setValue (String value) {
        this.value = value;
    }

    @Override
    public String getNameSpace () {
        return NAMESPACE;
    }

}
