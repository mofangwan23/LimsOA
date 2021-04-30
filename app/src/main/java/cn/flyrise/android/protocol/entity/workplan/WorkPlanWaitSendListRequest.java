package cn.flyrise.android.protocol.entity.workplan;

import cn.flyrise.feep.core.network.request.RequestContent;

/**
 * author : klc
 * data on 2018/5/7 10:27
 * Msg : 工作计划暂列表情请求
 */
public class WorkPlanWaitSendListRequest extends RequestContent {

	private final String nameSpace = "WorkPlanRequest";
	private String method = "tempList";
	private String page;
	private String pageSize;

	public WorkPlanWaitSendListRequest(String page, String pageSize) {
		this.page = page;
		this.pageSize = pageSize;
	}

	@Override public String getNameSpace() {
		return nameSpace;
	}
}
