package cn.flyrise.android.protocol.entity;

import com.google.gson.annotations.SerializedName;

import cn.flyrise.feep.core.network.request.RequestContent;

/**
 * @author ZYP
 * @since 2016/7/14 14:34
 */
public class EmailDetailsRequest extends RequestContent {

    public static final String NAMESPACE = "EmailDetailsRequest";

    @Override public String getNameSpace() {
        return NAMESPACE;
    }

    @SerializedName("boxname") public String boxName;

    @SerializedName("mailid") public String mailId;

    @SerializedName("mailname") public String mailName;

    public String typeTrash;

    public EmailDetailsRequest() { }

    public EmailDetailsRequest(String boxName, String mailId) {
        this.boxName = boxName;
        this.mailId = mailId;
    }




}
