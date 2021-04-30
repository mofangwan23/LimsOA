package cn.flyrise.feep.meeting7.protocol;

import cn.flyrise.feep.core.network.request.RequestContent;

/**
 * @author ZYP
 * @since 2018-06-27 15:43
 */
public class MeetingUsageRequest extends RequestContent {

	@Override public String getNameSpace() {
		return "MeetingRequest";
	}

	public String method;
	public String id;
	public int type;

	public String time;

	public String year;
	public String month;
	public String num;

	/**
	 * 请求会议室详情
	 */
	public static MeetingUsageRequest requestDetail(String roomId) {
		MeetingUsageRequest request = new MeetingUsageRequest();
		request.method = "meetingRoomDetail";
		request.id = roomId;
		request.type = 0;
		return request;
	}

	/**
	 * 请求会议室当天占用情况
	 * @param dateTime yyyy-MM-dd
	 */
	public static MeetingUsageRequest requestDailyUsage(String roomId, String dateTime) {
		MeetingUsageRequest request = new MeetingUsageRequest();
		request.method = "meetingRoomDetail";
		request.id = roomId;
		request.type = 1;
		request.time = dateTime;
		return request;
	}

	/**
	 * 请求会议室的跨天占用情况
	 */
	public static MeetingUsageRequest requestAcrossDayUsage(String roomId, String year, String month) {
		MeetingUsageRequest request = new MeetingUsageRequest();
		request.method = "meetingRoomDetail";
		request.id = roomId;
		request.type = 2;
		request.year = year;
		request.month = month;
		request.num = "6";
		return request;
	}

}
