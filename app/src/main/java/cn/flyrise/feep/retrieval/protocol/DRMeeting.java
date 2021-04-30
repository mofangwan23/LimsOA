package cn.flyrise.feep.retrieval.protocol;

import com.google.gson.annotations.SerializedName;

/**
 * @author ZYP
 * @since 2018-05-09 20:40
 */
public class DRMeeting {

	/**
	 * "id": "6158",
	 * "title": "测试参会",
	 * "sendUser": "罗伟豪",
	 * "status": "0",
	 * "sendTime": "2016-04-25 13:40:37",
	 * "sendUserId":""
	 * "START_DATE": "2016-04-30 14:00:00.0"
	 */

	public String id;
	public String title;
	@SerializedName("sendUserId") public String userId;
	@SerializedName("sendUser") public String username;
	public String sendTime;
}
