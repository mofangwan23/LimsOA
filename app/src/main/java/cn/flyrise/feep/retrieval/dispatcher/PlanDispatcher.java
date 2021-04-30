package cn.flyrise.feep.retrieval.dispatcher;

import android.content.Context;
import cn.flyrise.feep.core.function.FunctionManager;
import cn.flyrise.feep.retrieval.bean.PlanRetrieval;
import cn.flyrise.feep.retrieval.bean.Retrieval;
import cn.flyrise.feep.utils.Patches;
import cn.squirtlez.frouter.FRouter;

/**
 * @author ZYP
 * @since 2018-05-07 17:02
 */
public class PlanDispatcher implements RetrievalDispatcher {

	@Override public void jumpToSearchPage(Context context, String keyword) {
		FRouter.build(context, "/plan/search")
				.withString("keyword", keyword)
				.go();
	}

	@Override public void jumpToDetailPage(Context context, Retrieval retrieval) {
		PlanRetrieval planRetrieval = (PlanRetrieval) retrieval;
		if (FunctionManager.hasPatch(Patches.PATCH_PLAN)) {
			FRouter.build(context, "/plan/detail")
					.withString("EXTRA_BUSINESSID", planRetrieval.businessId)
					.go();
		}
		else {
			FRouter.build(context, "/particular/detail")
					.withInt("extra_particular_type", 5)
					.withString("extra_business_id", planRetrieval.businessId)
					.withString("extra_related_user_id", planRetrieval.userId)
					.go();
		}
	}
}
