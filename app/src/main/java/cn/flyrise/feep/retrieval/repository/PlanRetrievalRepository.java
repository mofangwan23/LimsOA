package cn.flyrise.feep.retrieval.repository;

import static cn.flyrise.feep.retrieval.vo.RetrievalType.TYPE_PLAN;

import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.retrieval.bean.PlanRetrieval;
import cn.flyrise.feep.retrieval.bean.Retrieval;
import cn.flyrise.feep.retrieval.protocol.DRPlan;
import cn.flyrise.feep.retrieval.protocol.PlanRetrievalResponse;
import cn.flyrise.feep.retrieval.protocol.RetrievalSearchRequest;
import cn.flyrise.feep.retrieval.vo.RetrievalResults;
import java.util.ArrayList;
import java.util.List;
import rx.Subscriber;

/**
 * @author ZYP
 * @since 2018-05-09 16:28
 */
public class PlanRetrievalRepository extends RetrievalRepository {

	@Override public void search(Subscriber<? super RetrievalResults> subscriber, String keyword) {
		this.mKeyword = keyword;
		RetrievalSearchRequest request = RetrievalSearchRequest.searchWorkPlan(keyword);

		FEHttpClient.getInstance().post(request, new ResponseCallback<PlanRetrievalResponse>() {
			@Override public void onCompleted(PlanRetrievalResponse response) {
				List<PlanRetrieval> planRetrievals = null;
				if (response != null && response.data != null && CommonUtil.nonEmptyList(response.data.results)) {
					planRetrievals = new ArrayList<>(5);
					planRetrievals.add((PlanRetrieval) header("计划"));

					for (DRPlan plan : response.data.results) {
						planRetrievals.add(createRetrieval(plan));
					}

					if (response.data.maxCount >= 3) {
						planRetrievals.add((PlanRetrieval) footer("更多计划"));
					}
				}

				subscriber.onNext(new RetrievalResults.Builder()
						.retrievalType(getType())
						.retrievals(planRetrievals)
						.create());
			}

			@Override public void onFailure(RepositoryException repositoryException) {
				FELog.e("Retrieval plan failed. Error: " + repositoryException.exception().getMessage());
				subscriber.onNext(emptyResult());
			}
		});
	}

	private PlanRetrieval createRetrieval(DRPlan plan) {
		PlanRetrieval retrieval = new PlanRetrieval();
		retrieval.viewType = Retrieval.VIEW_TYPE_CONTENT;
		retrieval.retrievalType = getType();
		retrieval.content = fontDeepen(plan.title, mKeyword);
		retrieval.extra = plan.content;

		retrieval.businessId = plan.id;
		retrieval.userId = plan.userId;
		retrieval.username = plan.username;
		return retrieval;
	}

	@Override protected int getType() {
		return TYPE_PLAN;
	}

	@Override protected Retrieval newRetrieval() {
		return new PlanRetrieval();
	}
}
