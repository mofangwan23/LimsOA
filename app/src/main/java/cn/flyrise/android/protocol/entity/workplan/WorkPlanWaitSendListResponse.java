package cn.flyrise.android.protocol.entity.workplan;

import cn.flyrise.feep.core.network.request.ResponseContent;
import cn.flyrise.feep.workplan7.model.WorkPlanWaitSend;
import java.util.List;

/**
 * author : klc
 * data on 2018/5/7 10:27
 * Msg : 工作计划暂存详情
 */
public class WorkPlanWaitSendListResponse extends ResponseContent {

	public WorkPlanWaitSendListData data;

	public class WorkPlanWaitSendListData {

		public int totalNums;
		public int totalPage;
		public List<WorkPlanWaitSend> rows;
	}
}
