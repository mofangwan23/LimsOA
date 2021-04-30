package cn.flyrise.android.protocol.entity;

import cn.flyrise.feep.core.network.request.ResponseContent;

/**
 * @author klc
 * @version 会议签到
 */
public class MeetingSignInResponse extends ResponseContent {


	public MeetingSign data;


	public class MeetingSign {

		public String title;
		public String startTime;
		public String endTime;
		public String meetingPlace;
		public String meetingMaster;
		public String signType;//会议签到类型，为1时“已签退”，其他“已签到”
		public String signTime;//会议签到时间

	}

}
