package cn.flyrise.feep.retrieval.protocol;

import com.google.gson.annotations.SerializedName;

/**
 * @author ZYP
 * @since 2018-05-09 17:41
 */
public class DRApproval {

	/**
	 * "id": "1166",
	 * "title": "《testtest》",
	 * "sendUserId": "5577",
	 * "sendUser": "李金明",
	 * "sendTime": "2016-05-04 11:56:26",
	 * "important": "平件",
	 * "type": "4"
	 **/

	public String id;
	public String title;    // 这玩意是 title 还是 content ?
	@SerializedName("sendUserId") public String userId;
	@SerializedName("sendUser") public String username;
	public String sendTime;
	public String important;
	public String type;

}
