package cn.flyrise.feep.retrieval.repository;

import static cn.flyrise.feep.retrieval.bean.Retrieval.VIEW_TYPE_CONTENT;
import static cn.flyrise.feep.retrieval.vo.RetrievalType.TYPE_MEETING;

import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.retrieval.bean.MeetingRetrieval;
import cn.flyrise.feep.retrieval.bean.Retrieval;
import cn.flyrise.feep.retrieval.protocol.DRMeeting;
import cn.flyrise.feep.retrieval.protocol.MeetingRetrievalResponse;
import cn.flyrise.feep.retrieval.protocol.RetrievalSearchRequest;
import cn.flyrise.feep.retrieval.vo.RetrievalResults;
import java.util.ArrayList;
import java.util.List;
import rx.Subscriber;

/**
 * @author ZYP
 * @since 2018-05-10 18:45
 */
public class MeetingRetrievalRepository extends RetrievalRepository {

	@Override public void search(Subscriber<? super RetrievalResults> subscriber, String keyword) {
		this.mKeyword = keyword;

		RetrievalSearchRequest request = RetrievalSearchRequest.searchMeeting(keyword);
		FEHttpClient.getInstance().post(request, new ResponseCallback<MeetingRetrievalResponse>() {
			@Override public void onCompleted(MeetingRetrievalResponse response) {
				List<MeetingRetrieval> meetingRetrievals = null;
				if (response != null && response.data != null && CommonUtil.nonEmptyList(response.data.results)) {
					meetingRetrievals = new ArrayList<>();
					meetingRetrievals.add((MeetingRetrieval) header("会议"));

					for (DRMeeting meeting : response.data.results) {
						meetingRetrievals.add(createRetrieval(meeting));
					}

					if (response.data.maxCount >= 3) {
						meetingRetrievals.add((MeetingRetrieval) footer("更多会议"));
					}
				}

				subscriber.onNext(new RetrievalResults.Builder()
						.retrievalType(getType())
						.retrievals(meetingRetrievals)
						.create());
			}

			@Override public void onFailure(RepositoryException repositoryException) {
				FELog.i("Retrieval meeting failed. Error: " + repositoryException.exception());
				subscriber.onNext(emptyResult());
			}
		});
	}

	private MeetingRetrieval createRetrieval(DRMeeting meeting) {
		MeetingRetrieval retrieval = new MeetingRetrieval();
		retrieval.viewType = VIEW_TYPE_CONTENT;
		retrieval.retrievalType = getType();
		retrieval.content = fontDeepen(meeting.title, mKeyword);
		retrieval.extra = fontDeepen("来自 " + meeting.username, mKeyword);

		retrieval.businessId = meeting.id;
		retrieval.userId = meeting.userId;
		retrieval.username = meeting.username;
		return retrieval;
	}

	@Override protected int getType() {
		return TYPE_MEETING;
	}

	@Override protected Retrieval newRetrieval() {
		return new MeetingRetrieval();
	}
}
