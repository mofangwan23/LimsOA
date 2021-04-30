package cn.flyrise.feep.retrieval.repository;

import static cn.flyrise.feep.retrieval.vo.RetrievalType.TYPE_SCHEDULE;

import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.retrieval.bean.Retrieval;
import cn.flyrise.feep.retrieval.bean.ScheduleRetrieval;
import cn.flyrise.feep.retrieval.protocol.DRSchedule;
import cn.flyrise.feep.retrieval.protocol.RetrievalSearchRequest;
import cn.flyrise.feep.retrieval.protocol.ScheduleRetrievalResponse;
import cn.flyrise.feep.retrieval.vo.RetrievalResults;
import java.util.ArrayList;
import java.util.List;
import rx.Subscriber;

/**
 * @author ZYP
 * @since 2018-05-09 16:27
 */
public class ScheduleRetrievalRepository extends RetrievalRepository {

	@Override public void search(Subscriber<? super RetrievalResults> subscriber, String keyword) {
		this.mKeyword = keyword;

		RetrievalSearchRequest request = RetrievalSearchRequest.searchAgenda(keyword);
		FEHttpClient.getInstance().post(request, new ResponseCallback<ScheduleRetrievalResponse>() {
			@Override public void onCompleted(ScheduleRetrievalResponse response) {
				List<ScheduleRetrieval> scheduleRetrievals = null;
				if (response != null && response.data != null && CommonUtil.nonEmptyList(response.data.results)) {
					scheduleRetrievals = new ArrayList<>();
					scheduleRetrievals.add((ScheduleRetrieval) header("日程"));

					for (DRSchedule schedule : response.data.results) {
						scheduleRetrievals.add(createRetrieval(schedule));
					}

					if (response.data.maxCount >= 3) {
						scheduleRetrievals.add((ScheduleRetrieval) footer("更多日程"));
					}
				}

				subscriber.onNext(new RetrievalResults.Builder()
						.retrievalType(getType())
						.retrievals(scheduleRetrievals)
						.create());
			}

			@Override public void onFailure(RepositoryException repositoryException) {
				FELog.e("Retrieval schedule failed. Error: " + repositoryException.exception().getMessage());
				subscriber.onNext(emptyResult());
			}
		});
	}

	private ScheduleRetrieval createRetrieval(DRSchedule schedule) {
		ScheduleRetrieval retrieval = new ScheduleRetrieval();
		retrieval.viewType = Retrieval.VIEW_TYPE_CONTENT;
		retrieval.retrievalType = getType();
		retrieval.content = fontDeepen(schedule.title, mKeyword);
		retrieval.extra = fontDeepen(schedule.content, mKeyword);

		retrieval.userId = schedule.userId;
		retrieval.scheduleId = schedule.id;
		retrieval.meetingId = schedule.meetingId;
		retrieval.eventSource = schedule.eventSource;
		retrieval.eventSourceId = schedule.eventSourceId;

		return retrieval;
	}

	@Override protected int getType() {
		return TYPE_SCHEDULE;
	}

	@Override protected Retrieval newRetrieval() {
		return new ScheduleRetrieval();
	}
}
