package cn.flyrise.feep.robot.bean;

import cn.flyrise.feep.core.network.request.RequestContent;

/**
 * 新建：陈冕;
 * 日期： 2018-8-28-10:30.
 */
public class RobotEmailNumberRequest extends RequestContent {

	public static final String NAMESPACE = "EmailNumberRequest";

	@Override public String getNameSpace() {
		return NAMESPACE;
	}

	public String typeid;

	public String mailname;

	public RobotEmailNumberRequest() { }

	public RobotEmailNumberRequest(String mailname) {
		this("", mailname);
	}

	public RobotEmailNumberRequest(String typeId, String mailname) {
		this.typeid = typeId;
		this.mailname = mailname;
	}

}