package cn.flyrise.feep.collection.bean;

import com.google.gson.annotations.SerializedName;

/**
 * @author ZYP
 * @since 2018-05-23 09:46
 */
public class Favorite {

	/**
	 * "favoriteName":"审批",
	 * "id":"12",
	 * "pubDate":"2018-05-22 16:36:22",
	 * "title":"《出差成都费用报销详情汇报》",
	 * "type":"1",
	 * "userId":"119540",
	 * "userName":"单位调查问卷管理员1"
	 */

	public String id;       // 业务 ID
	public String title;
	public String type;
	public String userId;
	public String userName;
	public boolean isChoice;
	@SerializedName("pubDate") public String publishTime;
	@SerializedName("filetype") public String fileType;
	@SerializedName("filesize") public String fileSize;
}
