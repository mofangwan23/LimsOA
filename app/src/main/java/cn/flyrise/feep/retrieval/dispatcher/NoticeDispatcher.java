package cn.flyrise.feep.retrieval.dispatcher;

import android.content.Context;
import cn.flyrise.feep.retrieval.bean.NoticeRetrieval;
import cn.flyrise.feep.retrieval.bean.Retrieval;
import cn.squirtlez.frouter.FRouter;

/**
 * @author ZYP
 * @since 2018-05-10 19:59
 */
public class NoticeDispatcher implements RetrievalDispatcher {

	private final static String REQUEST_NAME = "公告";
	private final static int REQUEST_TYPE = 6;

	@Override public void jumpToSearchPage(Context context, String keyword) {
		FRouter.build(context, "/message/search")
				.withString("request_NAME", REQUEST_NAME)
				.withInt("request_type", REQUEST_TYPE)
				.withString("keyword", keyword)
				.go();
	}

	@Override public void jumpToDetailPage(Context context, Retrieval retrieval) {
		NoticeRetrieval noticeRetrieval = (NoticeRetrieval) retrieval;
		FRouter.build(context, "/particular/detail")
				.withInt("extra_particular_type", 2)
				.withString("extra_business_id", noticeRetrieval.businessId)
				.withSerializable("extra_request_type", noticeRetrieval.getRequestType())
				.go();
	}
}
