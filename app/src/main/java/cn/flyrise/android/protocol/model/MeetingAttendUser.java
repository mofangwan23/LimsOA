package cn.flyrise.android.protocol.model;

/**
 * @author CWW
 * @version 1.0 <br/>
 *          创建时间: 2013-3-16 上午11:14:24 <br/>
 *          类说明 :
 */
public class MeetingAttendUser {
    private String meetingAttendUser;
    private String meetingAttendStatus;
    private String meetingAttendUserID;

    public String getMeetingAttendUser () {
        return meetingAttendUser;
    }

    public void setMeetingAttendUser (String meetingAttendUser) {
        this.meetingAttendUser = meetingAttendUser;
    }

    public String getMeetingAttendStatus () {
        return meetingAttendStatus;
    }

    public void setMeetingAttendStatus (String meetingAttendStatus) {
        this.meetingAttendStatus = meetingAttendStatus;
    }

    public String getMeetingAttendUserID () {
        return meetingAttendUserID;
    }

    public void setMeetingAttendUserID (String meetingAttendUserID) {
        this.meetingAttendUserID = meetingAttendUserID;
    }

}
