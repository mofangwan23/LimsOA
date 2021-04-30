package cn.flyrise.android.protocol.entity;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import cn.flyrise.android.protocol.model.MailAttachment;
import cn.flyrise.feep.core.network.request.ResponseContent;

/**
 * @author ZYP
 * @since 2016/7/14 14:35
 */
public class EmailDetailsResponse extends ResponseContent {

    /**
     * 邮件标题
     */
    public String title;

    /**
     * 邮件发送人 Id
     */
    public String sendUserId;

    /**
     * 邮件发送人
     */
    @SerializedName("mailform") public String mailFrom;

    /**
     * 发送时间
     */
    public String sendTime;

    /**
     * 邮件接收人列表
     */
    public String tto;

    /**
     * 接收人 UserId
     */
    public String ttoUserId;

    /**
     * 抄送人列表
     */
    @SerializedName("mailnamecc") public String cc;

    /**
     * 抄送人 Id
     */
    @SerializedName("cc") public String ccUserId;


    /**
     * 邮件正文
     */
    public String context;

    /**
     * 附件列表
     */
    @SerializedName("MailFileList") public List<MailAttachment> mailAttachments;

    public boolean isEmailEmpty() {
        // 当发送人，标题，收件人为空时，判断该邮件已被删除。
        return TextUtils.isEmpty(sendUserId) && TextUtils.isEmpty(title) && TextUtils.isEmpty(tto);
    }


}
