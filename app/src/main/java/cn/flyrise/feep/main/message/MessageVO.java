package cn.flyrise.feep.main.message;

import android.support.annotation.Keep;

import cn.flyrise.feep.core.common.utils.CommonUtil;

@Keep
public class MessageVO {
    private String title;
    private String sendTime;
    private String badge;
    private String category;
    private String action;
    private String content;
    private String messageID;
    private String businessID;
    private String sender;
    private String type;

    private String readed;
    private String pcType;
    private String typeDcrp;
    private String url;
    private String userName;
    private String userIcon;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserIcon() {
        return userIcon;
    }

    public void setUserIcon(String userIcon) {
        this.userIcon = userIcon;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getReaded() {
        return readed;
    }

    public void setReaded() {
        this.readed = "true";
    }

    private String getPcType() {
        return pcType;
    }

    public void setPcType(String pcType) {
        this.pcType = pcType;
    }

    public String getTypeDcrp() {
        return typeDcrp;
    }

    public void setTypeDcrp(String typeDcrp) {
        this.typeDcrp = typeDcrp;
    }

    public int getRequestType() {
        return CommonUtil.parseInt(type);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public String getBusinessID() {
        return businessID;
    }

    public void setBusinessID(String businessID) {
        this.businessID = businessID;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getContent() {
        return content;
    }

    public void setContent() {
        this.content = "";
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSendTime() {
        return sendTime;
    }

    public void setSendTime(String sendTime) {
        this.sendTime = sendTime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBadge() {
        return badge;
    }

    public void setBadge() {
        this.badge = "0";
    }

    @Override
    public String toString() {
        return "--title:" + getTitle() + "--sendTime:" + getRequestType() + "--sendTime:" + getSendTime()
                + "--category:" + getCategory() + "--action:" + getAction()
                + "--content:" + getContent() + "--messageID:" + getMessageID() + "--businessID:" + getBusinessID() + "--sender:" + getSender()
                + "--readed:" + getReaded() + "--pcType:" + getPcType() + "--typeDcrp:" + getTypeDcrp();
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MessageVO messageVO = (MessageVO) o;

        if (!title.equals(messageVO.title)) return false;
        if (!category.equals(messageVO.category)) return false;
        if (!messageID.equals(messageVO.messageID)) return false;
        return businessID.equals(messageVO.businessID);

    }

    @Override public int hashCode() {
        int result = title.hashCode();
        result = 31 * result + category.hashCode();
        result = 31 * result + messageID.hashCode();
        result = 31 * result + businessID.hashCode();
        return result;
    }
}
