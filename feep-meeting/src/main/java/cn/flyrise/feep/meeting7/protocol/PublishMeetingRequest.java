package cn.flyrise.feep.meeting7.protocol;

import cn.flyrise.feep.core.network.request.RequestContent;

public class PublishMeetingRequest extends RequestContent {

	@Override public String getNameSpace() {
		return "MeetingRequest";
	}

	public String id;               // 会议 id
	public String flag;             // 0 暂存 1 发起
	public String method;           // meetingAdd
	public String topics;           // 标题
	public String content;          // 会议简介
	public String roomId;           // 会议室id(自定义的传 空字符串)
	public String address;          // 会议室地址
	public String compere;          // 主持人  可以为空
	public String recordMan;        // 记录人  可以为空

	public String attendee;         // 参与人  userId
	public String meetingType;      // 会议类型
	public String remindTime;       // 提醒时间
	public String attachments;      // 附件id

	public String startDate;        // 开始时间 2017-12-28 00:00:00
	public String endDate;          // 结束时间 2017-12-28 00:00:00
	public String res;              // 需要的资源

	public PublishMeetingRequest(String requestType) {
		this.method = requestType;
	}

}