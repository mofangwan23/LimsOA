package cn.flyrise.feep.retrieval.protocol;

import com.google.gson.annotations.SerializedName;

/**
 * @author ZYP
 * @since 2018-05-09 18:04
 */
public class DRNews {

	/**
	 * "id": "149",
	 * "content": "2016年7月30日18时，中央气象台",
	 * "badge": "227",
	 * "category": "OA新闻",
	 * "title": "中央气象台7月30日发布台风蓝色预警",
	 * "sendUser": "陈亮",
	 * "sendUserId":"6138",
	 * "sendTime": "2017-01-05 14:55:57"
	 **/

	public String id;
	public String title;
	public String content;
	@SerializedName("sendUserId") public String userId;
	@SerializedName("sendUser") public String userName;
	public String sendTime;
	public String category;

}
