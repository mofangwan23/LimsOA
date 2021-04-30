package cn.flyrise.android.protocol.entity;

import cn.flyrise.feep.core.network.request.RequestContent;

/**
 * @author CWW
 * @version 1.0 <br/>
 *          创建时间: 2013-3-18 下午3:46:41 <br/>
 *          类说明 :
 */
public class MeetingReplyRequest extends RequestContent {
    public static final String NAMESPACE = "MeetingRequest";
    private String id;
    private String meetingId;
    private String requestType = "10";            // 会议处理请求类型为10
    private String meetingContent;
    private String meetingStatus;
    private String meetingAnnex;

    @Override
    public String getNameSpace () {
        return NAMESPACE;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getMeetingContent () {
        return meetingContent;
    }

    public void setMeetingContent (String meetingContent) {
        this.meetingContent = meetingContent;
    }

    public String getMeetingStatus () {
        return meetingStatus;
    }

    public void setMeetingStatus (String meetingStatus) {
        this.meetingStatus = meetingStatus;
    }

    public String getMeetingAnnex () {
        return meetingAnnex;
    }

    public void setMeetingAnnex (String meetingAnnex) {
        this.meetingAnnex = meetingAnnex;
    }

    public static String getNamespace () {
        return NAMESPACE;
    }

}
