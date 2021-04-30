package cn.flyrise.android.protocol.entity.workplan;

import cn.flyrise.feep.core.network.entry.AttachmentBean;
import cn.flyrise.feep.core.network.request.ResponseContent;
import cn.flyrise.feep.workplan7.model.NewWorkPlanRequest;
import java.util.List;

/**
 * author : klc
 * data on 2018/5/7 10:27
 * Msg : 工作计划暂存详情
 */
public class WorkPlanWaitSendDetailResponse extends ResponseContent {

	public WPTemporaryDetail data;

	public class WPTemporaryDetail extends NewWorkPlanRequest {
		public List<AttachmentBean> attachmentList;
	}
}
