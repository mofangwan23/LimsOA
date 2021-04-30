package cn.flyrise.android.protocol.entity;

import cn.flyrise.feep.core.network.request.RequestContent;

public class EmailReplyRequest extends RequestContent {
    public static final String NAMESPACE = "EmailReplyRequest";

    public static final String B_REPLY_ALL = "4";   // 回复全部
    public static final String B_REPLY = "3";       // 回复
    public static final String B_DRAFT = "2";       // 草稿
    public static final String B_TRANSMIT = "1";    // 转发

    @Override
    public String getNameSpace() {
        return NAMESPACE;
    }

    public String boxname;
    public String mailID;
    public String mailname;

    /**
     * 转发：bTransmit = 1;
     * 草稿：bTransmit = 2;
     * 回复：bTransmit = 3;
     */
    public String bTransmit;

    public EmailReplyRequest() { }

    public EmailReplyRequest(String boxname, String mailId) {
        this.boxname = boxname;
        this.mailID = mailId;
    }

}
