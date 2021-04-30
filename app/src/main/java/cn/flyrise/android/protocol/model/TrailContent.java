/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2013-2-25 ����5:00:15
 */
package cn.flyrise.android.protocol.model;

public class TrailContent {
    public String sendUser;
    public String sendTime;
    public String content;

    public String getSendUser () {
        return sendUser;
    }

    public void setSendUser (String sendUser) {
        this.sendUser = sendUser;
    }

    public String getSendTime () {
        return sendTime;
    }

    public void setSendTime (String sendTime) {
        this.sendTime = sendTime;
    }

    public String getContent () {
        return content;
    }

    public void setContent (String content) {
        this.content = content;
    }

}
