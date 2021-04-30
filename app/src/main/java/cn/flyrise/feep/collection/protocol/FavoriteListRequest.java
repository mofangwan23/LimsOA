package cn.flyrise.feep.collection.protocol;

import cn.flyrise.feep.core.network.request.RequestContent;

/**
 * @author ZYP
 * @since 2018-05-23 09:42
 */
public class FavoriteListRequest extends RequestContent {

	@Override public String getNameSpace() {
		return "CollectRequest";
	}

	/**
	 * "method":"CollectList",
	 * "page":"1",
	 * "size":"10",
	 * "favoriteId":"10"
	 **/

	public String method;
	public String page;
	public String size;
	public String favoriteId;

	public static FavoriteListRequest create(int page, String favoriteId) {
		FavoriteListRequest request = new FavoriteListRequest();
		request.method = "CollectList";
		request.favoriteId = favoriteId;
		request.page = page + "";
		request.size = "20";
		return request;
	}

}
