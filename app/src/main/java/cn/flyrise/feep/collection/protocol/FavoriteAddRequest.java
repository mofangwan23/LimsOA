package cn.flyrise.feep.collection.protocol;

import cn.flyrise.feep.core.network.request.RequestContent;
import com.google.gson.annotations.SerializedName;

/**
 * @author ZYP
 * @since 2018-05-23 09:52
 */
public class FavoriteAddRequest extends RequestContent {

	@Override public String getNameSpace() {
		return "CollectRequest";
	}

	/**
	 * "favoriteId":"10",
	 * "title":"标题",
	 * "type":"processed",
	 * "userId":"119540",
	 * "initTime":"2018-05-06 10:00:00",
	 * "id":"22"
	 */

	// processed已办 sended已发 news新闻 notice公告
	public String method;
	public String favoriteId;
	@SerializedName("id") public String businessId;
	public String type;
	public String title;
	public String userId;
	@SerializedName("initTime") public String sendTime;

	public static FavoriteAddRequest newInstance(String favoriteId,
			String businessId, String type, String title, String userId, String sendTime) {
		FavoriteAddRequest request = new FavoriteAddRequest();
		request.method = "CollectAdd";
		request.favoriteId = favoriteId;
		request.businessId = businessId;
		request.type = type;
		request.title = title;
		request.userId = userId;
		request.sendTime = sendTime;
		return request;
	}

}
