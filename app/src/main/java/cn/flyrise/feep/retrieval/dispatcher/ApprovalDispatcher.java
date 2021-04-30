package cn.flyrise.feep.retrieval.dispatcher;

import android.content.Context;
import cn.flyrise.feep.retrieval.bean.ApprovalRetrieval;
import cn.flyrise.feep.retrieval.bean.Retrieval;
import cn.squirtlez.frouter.FRouter;

/**
 * @author ZYP
 * @since 2018-05-07 17:01
 */
public class ApprovalDispatcher implements RetrievalDispatcher {

	private final static int REQUEST_TYPE = 0;

	@Override public void jumpToSearchPage(Context context, String keyword) {
		FRouter.build(context, "/collaboration/search")
				.withInt("request_type", REQUEST_TYPE)
				.withString("keyword", keyword)
				.go();
	}

	@Override public void jumpToDetailPage(Context context, Retrieval retrieval) {
		ApprovalRetrieval approval = (ApprovalRetrieval) retrieval;
		FRouter.build(context, "/particular/detail")
				.withInt("extra_particular_type", 4)
				.withString("extra_business_id", approval.businessId)
				.withSerializable("extra_request_type", approval.getRequestType())
				.go();
	}
}
