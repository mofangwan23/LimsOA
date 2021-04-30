package cn.flyrise.feep.meeting7.protocol;

import cn.flyrise.feep.core.network.request.RequestContent;

public class MeetingTypeRequest extends RequestContent {

	@Override public String getNameSpace() {
		return "MeetingRequest";
	}

	public String method;

	public MeetingTypeRequest() {
		this.method = "meetingType";
	}
}
