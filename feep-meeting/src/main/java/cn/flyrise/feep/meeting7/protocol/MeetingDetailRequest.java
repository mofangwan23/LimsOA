package cn.flyrise.feep.meeting7.protocol;

import cn.flyrise.feep.core.network.request.RequestContent;

/**
 * @author ZYP
 * @since 2018-06-22 09:32
 */
public class MeetingDetailRequest extends RequestContent {

	@Override public String getNameSpace() {
		return "MeetingRequest";
	}

	public String method;
	public String meetingId;
	public String type;

	public MeetingDetailRequest() {
		this.method = "meetingDetail";
	}


}
