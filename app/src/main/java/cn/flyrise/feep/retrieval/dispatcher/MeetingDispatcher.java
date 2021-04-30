package cn.flyrise.feep.retrieval.dispatcher;

import android.content.Context;
import cn.flyrise.feep.core.function.FunctionManager;
import cn.flyrise.feep.retrieval.bean.MeetingRetrieval;
import cn.flyrise.feep.retrieval.bean.Retrieval;
import cn.flyrise.feep.utils.Patches;
import cn.squirtlez.frouter.FRouter;

/**
 * @author ZYP
 * @since 2018-05-10 20:02
 */
public class MeetingDispatcher implements RetrievalDispatcher {

	@Override public void jumpToSearchPage(Context context, String keyword) {
		FRouter.build(context, "/meeting/search")
				.withString("keyword", keyword)
				.go();
	}

	@Override public void jumpToDetailPage(Context context, Retrieval retrieval) {
		MeetingRetrieval meetingRetrieval = (MeetingRetrieval) retrieval;
		if (FunctionManager.hasPatch(Patches.PATCH_MEETING_MANAGER)) {
			FRouter.build(context, "/meeting/detail")
					.withString("meetingId", meetingRetrieval.businessId)
					.go();
		}
		else {
			FRouter.build(context, "/particular/detail")
					.withInt("extra_particular_type", 3)
					.withString("extra_business_id", meetingRetrieval.businessId)
					.withSerializable("extra_request_type", meetingRetrieval.getRequestType())
					.go();
		}
	}
}
