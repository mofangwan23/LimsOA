package cn.flyrise.android.protocol.entity;

import cn.flyrise.feep.core.network.request.RequestContent;

public class EmailSendDoRequest extends RequestContent {
    public static final String NAMESPACE = "EmailSendDoRequest";

    public static final String OPERATOR_GET = "getGUID";
    /**
     * 发送邮件
     */
    public static final String OPERATOR_SEND = "send";

    /**
     * 保存草稿
     */
    public static final String OPERATOR_SAVE = "save";

    /**
     * 发送草稿
     */
    public static final String OPERATOR_DRAFT = "draft";

    public String operator;
    public String mailname; // 发送人的userId   ，
    public String to;       // 收件人列表
    public String cc;       // 抄送人列表
    public String bcc;      // 密送人列表
    public String title;    // 邮件标题
    public String content;  // 邮件内容
    public String sa01;     // 邮件上面过程中获取的FE_GUID
    public String mailid;   // 回复邮件时，原邮件ID.

    public String to1;  // 外部邮箱收件人
    public String to2;  // 内部人员 名称

    @Override public String getNameSpace() {
        return NAMESPACE;
    }

}
