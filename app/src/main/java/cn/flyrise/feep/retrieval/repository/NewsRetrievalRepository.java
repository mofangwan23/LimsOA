package cn.flyrise.feep.retrieval.repository;

import static cn.flyrise.feep.retrieval.bean.Retrieval.VIEW_TYPE_CONTENT;
import static cn.flyrise.feep.retrieval.vo.RetrievalType.TYPE_NEWS;

import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.retrieval.bean.NewsRetrieval;
import cn.flyrise.feep.retrieval.bean.Retrieval;
import cn.flyrise.feep.retrieval.protocol.DRNews;
import cn.flyrise.feep.retrieval.protocol.NewsRetrievalResponse;
import cn.flyrise.feep.retrieval.protocol.RetrievalSearchRequest;
import cn.flyrise.feep.retrieval.vo.RetrievalResults;
import java.util.ArrayList;
import java.util.List;
import rx.Subscriber;

/**
 * @author ZYP
 * @since 2018-05-09 16:19
 */
public class NewsRetrievalRepository extends RetrievalRepository {

	@Override public void search(Subscriber<? super RetrievalResults> subscriber, String keyword) {
		this.mKeyword = keyword;
		RetrievalSearchRequest request = RetrievalSearchRequest.searchNews(keyword);
		FEHttpClient.getInstance().post(request, new ResponseCallback<NewsRetrievalResponse>() {
			@Override public void onCompleted(NewsRetrievalResponse response) {
				List<NewsRetrieval> newsRetrievals = null;
				if (response != null && response.data != null && CommonUtil.nonEmptyList(response.data.results)) {
					newsRetrievals = new ArrayList<>();
					newsRetrievals.add((NewsRetrieval) header("新闻"));

					for (DRNews news : response.data.results) {
						newsRetrievals.add(createRetrieval(news));
					}

					if (response.data.maxCount >= 3) {
						newsRetrievals.add((NewsRetrieval) footer("更多新闻"));
					}
				}

				subscriber.onNext(new RetrievalResults.Builder()
						.retrievalType(getType())
						.retrievals(newsRetrievals)
						.create());
			}

			@Override public void onFailure(RepositoryException repositoryException) {
				FELog.e("Retrieval news failed, Error: " + repositoryException.exception().getMessage());
				subscriber.onNext(emptyResult());
			}
		});
	}

	private NewsRetrieval createRetrieval(DRNews news) {
		NewsRetrieval retrieval = new NewsRetrieval();
		retrieval.viewType = VIEW_TYPE_CONTENT;
		retrieval.retrievalType = getType();
		retrieval.content = fontDeepen(news.title, mKeyword);
		String sendTime = news.sendTime.split(" ")[0];
		retrieval.extra = fontDeepen(news.category + " " + sendTime, mKeyword);

		retrieval.businessId = news.id;
		retrieval.userId = news.userId;
		retrieval.username = news.userName;
		return retrieval;
	}

	@Override protected int getType() {
		return TYPE_NEWS;
	}

	@Override protected Retrieval newRetrieval() {
		return new NewsRetrieval();
	}
}
