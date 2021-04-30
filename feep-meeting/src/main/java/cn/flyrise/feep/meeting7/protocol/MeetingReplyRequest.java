package cn.flyrise.feep.meeting7.protocol;

import cn.flyrise.feep.core.network.request.RequestContent;

/**
 * @author ZYP
 * @since 2018-07-03 09:06
 */
public class MeetingReplyRequest extends RequestContent {

	public static final String NAMESPACE = "MeetingRequest";
	public String id;
	public String meetingId;
	public String requestType;            // 会议处理请求类型为10
	public String meetingContent;
	public String meetingStatus;
	public String meetingAnnex;
	public String client = "Android";

	public MeetingReplyRequest() {
		this.requestType = "10";
	}

	@Override public String getNameSpace() {
		return NAMESPACE;
	}
}
