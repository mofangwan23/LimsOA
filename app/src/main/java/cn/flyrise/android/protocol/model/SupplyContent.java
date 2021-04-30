/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2013-2-25 ����4:33:11
 */
package cn.flyrise.android.protocol.model;

import cn.flyrise.feep.core.network.entry.AttachmentBean;
import java.util.ArrayList;
import java.util.List;

public class SupplyContent {

    private String sendUser;
    private String sendTime;
    private String content;
    private List<AttachmentBean> attachments = new ArrayList<> ();

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

    public List<AttachmentBean> getAttachments () {
        return attachments;
    }

    public void setAttachments (List<AttachmentBean> attachments) {
        this.attachments = attachments;
    }

}
