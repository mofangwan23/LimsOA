package cn.flyrise.feep.retrieval.protocol;

import com.google.gson.annotations.SerializedName;

/**
 * @author ZYP
 * @since 2018-05-09 18:04
 */
public class DRNotice {

	/**
	 * "id": "303",
	 * "content": "飞企互联07月31日考勤公",
	 * "badge": "6",
	 * "category": "人事",
	 * "title": "飞企互联07月31日考勤公告",
	 * "sendUser": "刘艳芳",
	 * "sendUserId":"6138",
	 * "sendTime": "2017-07-31 13:42:50"
	 **/

	public String id;
	public String title;
	public String content;
	@SerializedName("sendUserId") public String userId;
	@SerializedName("sendUser") public String userName;
	public String sendTime;
	public String category;

}
