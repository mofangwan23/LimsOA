package cn.flyrise.android.protocol.model;

import com.google.gson.annotations.SerializedName;

/**
 * @author ZYP
 * @since 2016/7/14 14:39
 */
public class MailAttachment {

    /**
     * 附件 Id
     */
    @SerializedName("accid") public String accId;

    /**
     * 附件名称
     */
    @SerializedName("filename") public String fileName;

    /**
     * 附件唯一标识
     */
    public String attachPK;


}
