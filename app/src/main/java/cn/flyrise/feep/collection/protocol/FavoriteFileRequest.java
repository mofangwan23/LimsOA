package cn.flyrise.feep.collection.protocol;

import cn.flyrise.feep.core.network.request.RequestContent;

/**
 * @author 社会主义接班人
 * @since 2018-08-03 11:46
 */
public class FavoriteFileRequest extends RequestContent {

	@Override public String getNameSpace() {
		return "AppsRequest";
	}

	public String method;
	public String id;

	public static FavoriteFileRequest O_O(String id) {
		FavoriteFileRequest request = new FavoriteFileRequest();
		request.method = "getFileDetail";
		request.id = id;
		return request;
	}
}
