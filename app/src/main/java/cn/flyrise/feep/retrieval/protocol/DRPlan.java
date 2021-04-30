package cn.flyrise.feep.retrieval.protocol;

import com.google.gson.annotations.SerializedName;

/**
 * @author ZYP
 * @since 2018-05-09 17:55
 */
public class DRPlan {

	/**
	 * "id": "989",
	 * "title": "李金明发起2018-05-06—2018-05-12的计划",
	 * "sendUser": "李金明",
	 * "sendTime": "2018-05-08 17:11:02",
	 * "sectionName": "2018年5月第2周",
	 * "status": "1",
	 * "UserId": "5577"
	 */

	public String id;
	public String title;
	@SerializedName("sectionName") public String content;
	@SerializedName("UserId") public String userId;
	@SerializedName("sendUser") public String username;

}
