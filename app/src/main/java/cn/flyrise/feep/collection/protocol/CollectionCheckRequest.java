package cn.flyrise.feep.collection.protocol;

import cn.flyrise.feep.core.network.request.RequestContent;

/**
 * @author 社会主义接班人
 * @since 2018-08-23 17:04
 */
public class CollectionCheckRequest extends RequestContent {

	@Override public String getNameSpace() {
		return "CollectRequest";
	}

	public String method;
	public String id;
	public String type;

	public static CollectionCheckRequest request(String id, String type) {
		CollectionCheckRequest request = new CollectionCheckRequest();
		request.method = "CollectCheck";
		request.id = id;
		request.type = type;
		return request;
	}

}
