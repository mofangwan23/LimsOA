package cn.flyrise.feep.retrieval.dispatcher;

import android.content.Context;
import cn.flyrise.feep.retrieval.bean.ContactRetrieval;
import cn.flyrise.feep.retrieval.bean.Retrieval;
import cn.squirtlez.frouter.FRouter;

/**
 * @author ZYP
 * @since 2018-05-03 11:38
 */
public class ContactDispatcher implements RetrievalDispatcher {

	@Override public void jumpToSearchPage(Context context, String keyword) {
		FRouter.build(context, "/contact/search/network")
				.withString("keyword", keyword)
				.go();
	}

	@Override public void jumpToDetailPage(Context context, Retrieval retrieval) {
		ContactRetrieval contact = (ContactRetrieval) retrieval;
		FRouter.build(context, "/addressBook/detail")
				.withString("user_id", contact.userId)
				.withString("department_id", contact.deptId)
				.go();
	}
}
