package cn.flyrise.android.protocol.entity;

import cn.flyrise.feep.core.network.request.RequestContent;

public class UserDetailsRequest extends RequestContent {

	private static final String NAMESPACE = "UserDetailsRequest";

	public UserDetailsRequest() {}

	public UserDetailsRequest(String userId) {
		this.userId = userId;
	}

	@Override
	public String getNameSpace() {
		return NAMESPACE;
	}

	public String userId;//用户Id用来查询停用（离职）人员数据
}
