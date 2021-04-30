package cn.flyrise.feep.robot.bean;

import cn.flyrise.feep.core.network.request.RequestContent;

/**
 * 新建：陈冕;
 * 日期： 2018-8-28-10:49.
 */
public class RobotListRequest extends RequestContent {

	public static final String NAMESPACE = "ListRequest";

	@Override
	public String getNameSpace() {
		return NAMESPACE;
	}

	public String requestType;
	public String page;
	public String perPageNums;
	public String orderBy;
	public String orderType;
	public String searchKey;
	public String id;
	public String userId;                    // 新增在v6.03 2015-3-27

	public int sumId;//月汇总详情类型0或不填返回所有记录，102 休息天数,103 迟到,104 早退,105 缺卡,106 旷工,107 外勤,108 未签到

}
