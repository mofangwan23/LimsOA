package cn.flyrise.feep.retrieval.dispatcher;

import static cn.flyrise.feep.core.common.X.RequestType.Meeting;

import android.content.Context;
import android.text.TextUtils;
import cn.flyrise.feep.retrieval.bean.Retrieval;
import cn.flyrise.feep.retrieval.bean.ScheduleRetrieval;
import cn.squirtlez.frouter.FRouter;

/**
 * @author ZYP
 * @since 2018-05-07 17:02
 */
public class ScheduleDispatcher implements RetrievalDispatcher {

	@Override public void jumpToSearchPage(Context context, String keyword) {
		FRouter.build(context, "/schedule/search")
				.withString("keyword", keyword)
				.go();
	}

	@Override public void jumpToDetailPage(Context context, Retrieval retrieval) {
		ScheduleRetrieval scheduleRetrieval = (ScheduleRetrieval) retrieval;
		if (TextUtils.isEmpty(scheduleRetrieval.meetingId)) {
			FRouter.build(context, "/schedule/detail")
					.withString("EXTRA_SCHEDULE_ID", scheduleRetrieval.scheduleId)
					.withString("EXTRA_EVENT_SOURCE", scheduleRetrieval.eventSource)
					.withString("EXTRA_EVENT_SOURCE_ID", scheduleRetrieval.eventSourceId)
					.go();
			return;
		}

		FRouter.build(context, "/particular/detail")
				.withInt("extra_particular_type", 3)
				.withString("extra_business_id", scheduleRetrieval.meetingId)
				.withSerializable("extra_request_type", Meeting)
				.go();

	}
}
