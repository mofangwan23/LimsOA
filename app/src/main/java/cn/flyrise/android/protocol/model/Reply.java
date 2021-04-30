package cn.flyrise.android.protocol.model;

import cn.flyrise.feep.core.network.entry.AttachmentBean;
import java.util.ArrayList;
import java.util.List;

public class Reply {
    private String id;
    private String nodeName;
    private String content;
    private String sendTime;
    private String sendUser;
    private String sendUserID;
    private String sendUserImageHref;
    private String sendUserImg;                                      // 回复的图像，修改于2015/04/21 by 罗展健
    private String attitude;
    private String isHidden;
    private String isTemporary;
    private String hasAttachment;
    private String hasReply;
    private String tips;
    private List<AttachmentBean> attachments = new ArrayList<> ();
    private List<Reply> subReplies = new ArrayList<> ();

    private String writtenGUID;
    private List<AttachmentBean> writtenContentHref = new ArrayList<> ();

    private String collaborationID;

    public String getId () {
        return id;
    }

    public void setId (String id) {
        this.id = id;
    }

    public String getNodeName () {
        return nodeName;
    }

    public void setNodeName (String nodeName) {
        this.nodeName = nodeName;
    }

    public String getContent () {
        return content;
    }

    public void setContent (String content) {
        this.content = content;
    }

    public String getSendTime () {
        return sendTime;
    }

    public void setSendTime (String sendTime) {
        this.sendTime = sendTime;
    }

    public String getSendUser () {
        return sendUser;
    }

    public void setSendUser (String sendUser) {
        this.sendUser = sendUser;
    }

    public String getSendUserID () {
        return sendUserID;
    }

    public void setSendUserID (String sendUserID) {
        this.sendUserID = sendUserID;
    }

    public String getSendUserImageHref () {
        return sendUserImageHref;
    }

    public void setSendUserImageHref (String sendUserImageHref) {
        this.sendUserImageHref = sendUserImageHref;
    }

    public String getAttitude () {
        return attitude;
    }

    public void setAttitude (String attitude) {
        this.attitude = attitude;
    }

    public String getIsHidden () {
        return isHidden;
    }

    public void setIsHidden (String isHidden) {
        this.isHidden = isHidden;
    }

    public String getIsTemporary () {
        return isTemporary;
    }

    public void setIsTemporary (String isTemporary) {
        this.isTemporary = isTemporary;
    }

    public String getHasAttachment () {
        return hasAttachment;
    }

    public void setHasAttachment (String hasAttachment) {
        this.hasAttachment = hasAttachment;
    }

    public String getHasReply () {
        return hasReply;
    }

    public void setHasReply (String hasReply) {
        this.hasReply = hasReply;
    }

    public String getTips () {
        return tips;
    }

    public void setTips (String tips) {
        this.tips = tips;
    }

    public List<AttachmentBean> getAttachments () {
        return attachments;
    }

    public void setAttachments (List<AttachmentBean> attachments) {
        this.attachments = attachments;
    }

    public List<Reply> getSubReplies () {
        return subReplies;
    }

    public void setSubReplies (List<Reply> subReplies) {
        this.subReplies = subReplies;
    }

    public String getCollaborationID () {
        return collaborationID;
    }

    public void setCollaborationID (String collaborationID) {
        this.collaborationID = collaborationID;
    }

    // public String getWrittenContentHref() {
    // return writtenContentHref;
    // }
    // public void setWrittenContentHref(String writtenContentHref) {
    // this.writtenContentHref = writtenContentHref;
    // }
    public String getWrittenGUID () {
        return writtenGUID;
    }

    public void setWrittenGUID (String writtenGUID) {
        this.writtenGUID = writtenGUID;
    }

    public List<AttachmentBean> getWrittenContentHref () {
        return writtenContentHref;
    }

    public void setWrittenContentHref (List<AttachmentBean> writtenContentHref) {
        this.writtenContentHref = writtenContentHref;
    }

    public String getSendUserImg () {
        return sendUserImg;
    }

    public void setSendUserImg (String sendUserImg) {
        this.sendUserImg = sendUserImg;
    }

}
