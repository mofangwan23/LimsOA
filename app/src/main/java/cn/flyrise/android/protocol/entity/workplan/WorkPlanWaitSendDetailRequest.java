package cn.flyrise.android.protocol.entity.workplan;

import cn.flyrise.feep.core.network.request.RequestContent;

/**
 * author : klc
 * data on 2018/5/7 10:27
 * Msg : 工作计划暂存详情请求
 */
public class WorkPlanWaitSendDetailRequest extends RequestContent {

	private final String nameSpace = "WorkPlanRequest";
	private String method = "getTemporaryDetail";
	private String workPlanId;

	public WorkPlanWaitSendDetailRequest(String workPlanId) {
		this.workPlanId = workPlanId;
	}

	@Override public String getNameSpace() {
		return nameSpace;
	}
}
