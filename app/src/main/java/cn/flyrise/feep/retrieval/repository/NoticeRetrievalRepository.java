package cn.flyrise.feep.retrieval.repository;

import static cn.flyrise.feep.retrieval.bean.Retrieval.VIEW_TYPE_CONTENT;
import static cn.flyrise.feep.retrieval.vo.RetrievalType.TYPE_NOTICE;

import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.retrieval.bean.NoticeRetrieval;
import cn.flyrise.feep.retrieval.bean.Retrieval;
import cn.flyrise.feep.retrieval.protocol.DRNotice;
import cn.flyrise.feep.retrieval.protocol.NoticeRetrievalResponse;
import cn.flyrise.feep.retrieval.protocol.RetrievalSearchRequest;
import cn.flyrise.feep.retrieval.vo.RetrievalResults;
import java.util.ArrayList;
import java.util.List;
import rx.Subscriber;

/**
 * @author ZYP
 * @since 2018-05-09 16:26
 */
public class NoticeRetrievalRepository extends RetrievalRepository {

	@Override public void search(Subscriber<? super RetrievalResults> subscriber, String keyword) {
		this.mKeyword = keyword;
		RetrievalSearchRequest request = RetrievalSearchRequest.searchNotice(keyword);

		FEHttpClient.getInstance().post(request, new ResponseCallback<NoticeRetrievalResponse>() {
			@Override public void onCompleted(NoticeRetrievalResponse response) {
				List<NoticeRetrieval> newsRetrievals = null;
				if (response != null && response.data != null && CommonUtil.nonEmptyList(response.data.results)) {
					newsRetrievals = new ArrayList<>();
					newsRetrievals.add((NoticeRetrieval) header("公告"));

					for (DRNotice notice : response.data.results) {
						newsRetrievals.add(createRetrieval(notice));
					}

					if (response.data.maxCount >= 3) {
						newsRetrievals.add((NoticeRetrieval) footer("更多公告"));
					}
				}

				subscriber.onNext(new RetrievalResults.Builder()
						.retrievalType(getType())
						.retrievals(newsRetrievals)
						.create());
			}

			@Override public void onFailure(RepositoryException repositoryException) {
				FELog.e("Retrieval notice failed, Error: " + repositoryException.exception().getMessage());
				subscriber.onNext(emptyResult());
			}
		});
	}

	private NoticeRetrieval createRetrieval(DRNotice notice) {
		NoticeRetrieval retrieval = new NoticeRetrieval();
		retrieval.viewType = VIEW_TYPE_CONTENT;
		retrieval.retrievalType = getType();
		retrieval.content = fontDeepen(notice.title, mKeyword);
		String sendTime = notice.sendTime.split(" ")[0];
		retrieval.extra = fontDeepen(notice.category + " " + sendTime, mKeyword);

		retrieval.businessId = notice.id;
		retrieval.userId = notice.userId;
		retrieval.username = notice.userName;
		return retrieval;
	}


	@Override protected int getType() {
		return TYPE_NOTICE;
	}

	@Override protected Retrieval newRetrieval() {
		return new NoticeRetrieval();
	}
}
