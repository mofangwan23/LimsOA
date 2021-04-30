package cn.flyrise.android.protocol.entity.workplan;

import cn.flyrise.feep.core.network.request.RequestContent;

/**
 * author : klc
 * data on 2018/5/7 10:27
 * Msg : 工作计划暂列表情请求
 */
public class WorkPlanWaitSendDelRequest extends RequestContent {

	private final String nameSpace = "WorkPlanRequest";
	private String method = "delete";

	private String id;

	public WorkPlanWaitSendDelRequest(String id) {
		this.id = id;
	}

	@Override public String getNameSpace() {
		return nameSpace;
	}
}
