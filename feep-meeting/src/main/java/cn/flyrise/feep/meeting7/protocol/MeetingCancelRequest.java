package cn.flyrise.feep.meeting7.protocol;

import cn.flyrise.feep.core.network.request.RequestContent;
import cn.flyrise.feep.core.network.request.ResponseContent;

/**
 * @author 社会主义接班人
 * @since 2018-08-01 09:49
 */
public class MeetingCancelRequest extends RequestContent {

	@Override public String getNameSpace() {
		return "MeetingRequest";
	}

	public String method;
	public String id;
	public String content;

	public static MeetingCancelRequest newInstance(String meetingId, String content) {
		MeetingCancelRequest request = new MeetingCancelRequest();
		request.id = meetingId;
		request.content = content;
		request.method = "meetingCancel";
		return request;
	}

}
