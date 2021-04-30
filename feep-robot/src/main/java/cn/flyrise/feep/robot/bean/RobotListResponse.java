package cn.flyrise.feep.robot.bean;

import cn.flyrise.feep.core.network.request.ResponseContent;

/**
 * 新建：陈冕;
 * 日期： 2018-8-28-10:54.
 */
public class RobotListResponse extends ResponseContent {

	public String requestType;
	public String totalNums = "0";
	public RobotListTable table;
}
