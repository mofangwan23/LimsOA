package cn.flyrise.android.protocol.entity;

import cn.flyrise.feep.core.network.request.RequestContent;

/**
 * @author ZYP
 * @since 2018-03-23 15:45
 *
 * 请求他妈某个常用组下的通讯录人员
 */
public class DefineUserGroupRequest extends RequestContent {

	@Override public String getNameSpace() {
		return "DefineUserGroupRequest";
	}

	public String method;
	public String groupId;

	// 请求常用组列表
	public static DefineUserGroupRequest requestGroup() {
		DefineUserGroupRequest request = new DefineUserGroupRequest();
		request.method = "acquireDGroups";
		return request;
	}

	// 请求常用组用户
	public static DefineUserGroupRequest requestUsersInGroup(String groupId) {
		DefineUserGroupRequest request = new DefineUserGroupRequest();
		request.method = "acquireDUsers";
		request.groupId = groupId;
		return request;
	}
}
