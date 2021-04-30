package cn.flyrise.feep.core.network.request;

import com.google.gson.annotations.SerializedName;

/**
 * @author ZYP
 * @since 2017-02-13 16:11
 */
public class RemoteRequest extends RequestContent {

	public static final String METHOD_GET_COMMON_PERSONS = "getCommonPersons";              // 获取常用联系人
	public static final String METHOD_GET_TAG_PERSONS = "getTagPersons";                    // 获取我的关注
	public static final String METHOD_GET_LINK_INFO = "getLinkInfo";                        // 获取联系人详情
	public static final String OBJ = "dbAddressBookUtil";

	public String count;
	public String method;
	public String obj;

	@SerializedName("param1")
	public String userId;

	@Override public String getNameSpace() {
		return "RemoteRequest";
	}

	public static RemoteRequest buildRequest(String method) {
		RemoteRequest request = new RemoteRequest();
		request.method = method;
		request.count = "0";
		request.obj = OBJ;
		return request;
	}

	public static RemoteRequest buildRequest(String method, String version) {
		RemoteRequest request = new RemoteRequest();
		request.method = method;
		request.count = "1";
		request.obj = OBJ;
		request.userId = version;
		return request;
	}

	public static RemoteRequest buildUserDetailInfoRequest(String userId) {
		RemoteRequest request = new RemoteRequest();
		request.method = METHOD_GET_LINK_INFO;
		request.count = "1";
		request.obj = OBJ;
		request.userId = userId;
		return request;
	}


}
