package cn.flyrise.android.protocol.entity;

import java.util.List;

import cn.flyrise.feep.core.network.entry.AttachmentBean;
import cn.flyrise.android.protocol.model.MeetingAttendUser;
import cn.flyrise.android.protocol.model.Reply;
import cn.flyrise.feep.core.network.request.ResponseContent;

/**
 * @author CWW
 * @version 1.0 <br/>
 * 创建时间: 2013-3-16 上午10:54:41 <br/>
 * 类说明 :
 */
public class MeetingInfoResponse extends ResponseContent {

	private String id;
	private String title;
	private String sendUserID;
	private String sendUser;
	private String sendUserImageHref;
	private String sendTime;
	private String content;
	private String attachmentGUID;
	private String meeting_join_id;
	private String meetingStatus;
	private String meetingPeopleNumber;
	private String meetingAttendNumber;
	private String meetingNotAttendNumber;
	private String meetingConsiderNumber;
	private String meetingNotDealNumber;
	private String master;
	private String masterID;

	// 会议附件
	private List<AttachmentBean> attachments;
	// 回复
	private List<Reply> replies;
	// 会议参与人员信息
	private List<MeetingAttendUser> meetingAttendUsers;

	//2018-4-10 update  增加扫码签到人员数
	private String meetingSignNumber;


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSendUserID() {
		return sendUserID;
	}

	public void setSendUserID(String sendUserID) {
		this.sendUserID = sendUserID;
	}

	public String getSendUser() {
		return sendUser;
	}

	public void setSendUser(String sendUser) {
		this.sendUser = sendUser;
	}

	public String getSendUserImageHref() {
		return sendUserImageHref;
	}

	public void setSendUserImageHref(String sendUserImageHref) {
		this.sendUserImageHref = sendUserImageHref;
	}

	public String getSendTime() {
		return sendTime;
	}

	public void setSendTime(String sendTime) {
		this.sendTime = sendTime;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getAttachmentGUID() {
		return attachmentGUID;
	}

	public void setAttachmentGUID(String attachmentGUID) {
		this.attachmentGUID = attachmentGUID;
	}

	public String getMeeting_join_id() {
		return meeting_join_id;
	}

	public void setMeeting_join_id(String meeting_join_id) {
		this.meeting_join_id = meeting_join_id;
	}

	public String getMeetingStatus() {
		return meetingStatus;
	}

	public void setMeetingStatus(String meetingStatus) {
		this.meetingStatus = meetingStatus;
	}

	public String getMeetingPeopleNumber() {
		return meetingPeopleNumber;
	}

	public void setMeetingPeopleNumber(String meetingPeopleNumber) {
		this.meetingPeopleNumber = meetingPeopleNumber;
	}

	public String getMeetingAttendNumber() {
		return meetingAttendNumber;
	}

	public void setMeetingAttendNumber(String meetingAttendNumber) {
		this.meetingAttendNumber = meetingAttendNumber;
	}

	public String getMeetingNotAttendNumber() {
		return meetingNotAttendNumber;
	}

	public void setMeetingNotAttendNumber(String meetingNotAttendNumber) {
		this.meetingNotAttendNumber = meetingNotAttendNumber;
	}

	public String getMeetingConsiderNumber() {
		return meetingConsiderNumber;
	}

	public void setMeetingConsiderNumber(String meetingConsiderNumber) {
		this.meetingConsiderNumber = meetingConsiderNumber;
	}

	public String getMeetingNotDealNumber() {
		return meetingNotDealNumber;
	}

	public void setMeetingNotDealNumber(String meetingNotDealNumber) {
		this.meetingNotDealNumber = meetingNotDealNumber;
	}

	public String getMaster() {
		return master;
	}

	public void setMaster(String master) {
		this.master = master;
	}

	public String getMasterID() {
		return masterID;
	}

	public void setMasterID(String masterID) {
		this.masterID = masterID;
	}

	public List<AttachmentBean> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<AttachmentBean> attachments) {
		this.attachments = attachments;
	}

	public List<Reply> getReplies() {
		return replies;
	}

	public void setReplies(List<Reply> replies) {
		this.replies = replies;
	}

	public List<MeetingAttendUser> getMeetingAttendUsers() {
		return meetingAttendUsers;
	}

	public void setMeetingAttendUsers(List<MeetingAttendUser> meetingAttendUsers) {
		this.meetingAttendUsers = meetingAttendUsers;
	}


	public String getMeetingSignNumber() {
		return meetingSignNumber;
	}
}
