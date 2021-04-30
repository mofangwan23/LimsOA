package cn.flyrise.android.protocol.entity;

import cn.flyrise.feep.core.network.request.RequestContent;

public class EmailNumberRequest extends RequestContent {

    public static final String NAMESPACE = "EmailNumberRequest";

    @Override public String getNameSpace() {
        return NAMESPACE;
    }

    public String typeid;

    public String mailname;

    public EmailNumberRequest() { }

    public EmailNumberRequest(String mailname) {
        this("", mailname);
    }

    public EmailNumberRequest(String typeId, String mailname) {
        this.typeid = typeId;
        this.mailname = mailname;
    }

}
