package cn.flyrise.feep.meeting7.protocol;

import cn.flyrise.feep.core.network.request.RequestContent;

/**
 * @author ZYP
 * @since 2018-06-26 15:07
 */
public class MeetingRoomListRequest extends RequestContent {

	@Override public String getNameSpace() {
		return "MeetingRequest";
	}

	public String method;
	public String time;
	public String page;
	public String size;

	public static MeetingRoomListRequest newInstance(String time, int page) {
		MeetingRoomListRequest request = new MeetingRoomListRequest();
		request.time = time;
		request.page = page + "";
		request.method = "meetingRoomList";
		request.size = "20";
		return request;
	}
}
