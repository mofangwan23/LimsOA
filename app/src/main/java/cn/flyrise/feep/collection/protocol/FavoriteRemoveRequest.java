package cn.flyrise.feep.collection.protocol;

import cn.flyrise.feep.core.network.request.RequestContent;
import com.google.gson.annotations.SerializedName;

/**
 * @author ZYP
 * @since 2018-05-23 09:52
 */
public class FavoriteRemoveRequest extends RequestContent {

	@Override public String getNameSpace() {
		return "CollectRequest";
	}

	public String method;
	public String favoriteId;
	@SerializedName("id")
	public String businessId;
	public String type;

	public static FavoriteRemoveRequest newInstance(String favoriteId,String businessId, String type) {
		FavoriteRemoveRequest request = new FavoriteRemoveRequest();
		request.method = "CollectCancel";
		request.favoriteId = favoriteId;
		request.businessId = businessId;
		request.type = type;
		return request;
	}

}
