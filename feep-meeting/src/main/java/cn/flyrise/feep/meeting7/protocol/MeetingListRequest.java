package cn.flyrise.feep.meeting7.protocol;

import cn.flyrise.feep.core.network.request.RequestContent;

/**
 * @author ZYP
 * @since 2018-06-22 09:27
 */
public class MeetingListRequest extends RequestContent {

	@Override public String getNameSpace() {
		return "MeetingRequest";
	}

	/**
	 * "method": "meetingList",
	 * "page":1,//第几页
	 * "size":5,//每页多少条
	 * "meetingType": "1",// 1：全部，2：我参与，3：我发起，4：未办理
	 * "isOver": "false"  // 是否往上翻 true为是,false为否
	 **/

	public String method;
	public int size;
	public int page;
	public boolean isOver;
	public String meetingType;

	public MeetingListRequest() {
		this.method = "meetingList";
		this.size = 20;
	}

}
