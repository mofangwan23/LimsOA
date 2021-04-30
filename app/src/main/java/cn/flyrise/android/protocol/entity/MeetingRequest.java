package cn.flyrise.android.protocol.entity;

import cn.flyrise.feep.core.network.request.RequestContent;

/**
 * @author CWW
 * @version 1.0 <br/>
 *          创建时间: 2013-3-16 上午10:44:17 <br/>
 *          类说明 :
 */
public class MeetingRequest extends RequestContent {
    public static final String NAMESPACE = "MeetingRequest";
    private String msgId = "";
    private String meetingId;
    private String requestType;

    public String getMsgId () {
        return msgId;
    }

    public void setMsgId (String msgId) {
        if (msgId != null) {
            this.msgId = msgId;
        }
    }

    @Override
    public String getNameSpace () {
        return NAMESPACE;
    }

    public String getMeetingId () {
        return meetingId;
    }

    public void setMeetingId (String meetingId) {
        this.meetingId = meetingId;
    }

    public String getRequestType () {
        return requestType;
    }

    public void setRequestType (String requestType) {
        this.requestType = requestType;
    }

    public static String getNamespace () {
        return NAMESPACE;
    }

}
