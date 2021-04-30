package cn.flyrise.feep.retrieval.dispatcher;

import android.content.Context;
import cn.flyrise.feep.retrieval.bean.NewsRetrieval;
import cn.flyrise.feep.retrieval.bean.Retrieval;
import cn.squirtlez.frouter.FRouter;

/**
 * @author ZYP
 * @since 2018-05-03 11:39
 */
public class NewsDispatcher implements RetrievalDispatcher {

	private final static String REQUEST_NAME = "新闻";
	private final static int REQUEST_TYPE = 5;

	@Override public void jumpToSearchPage(Context context, String keyword) {
		FRouter.build(context, "/message/search")
				.withString("request_NAME", REQUEST_NAME)
				.withInt("request_type", REQUEST_TYPE)
				.withString("keyword", keyword)
				.go();
	}

	@Override public void jumpToDetailPage(Context context, Retrieval retrieval) {
		NewsRetrieval newsRetrieval = (NewsRetrieval) retrieval;
		FRouter.build(context, "/particular/detail")
				.withInt("extra_particular_type", 1)
				.withString("extra_business_id", newsRetrieval.businessId)
				.withSerializable("extra_request_type", newsRetrieval.getRequestType())
				.go();
	}
}
