package cn.flyrise.feep.meeting7.protocol;

import cn.flyrise.feep.core.network.request.RequestContent;

public class MeetingPromptRequest extends RequestContent {

	@Override public String getNameSpace() {
		return "MeetingRequest";
	}

	public String method;
	public String id;

	public static MeetingPromptRequest newInstance(String meetingId) {
		MeetingPromptRequest request = new MeetingPromptRequest();
		request.id = meetingId;
		request.method = "meetingPressMethod";
		return request;
	}
}
