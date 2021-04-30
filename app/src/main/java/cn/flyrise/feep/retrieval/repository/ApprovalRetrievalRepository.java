package cn.flyrise.feep.retrieval.repository;

import static cn.flyrise.feep.retrieval.vo.RetrievalType.TYPE_APPROVAL;

import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.retrieval.bean.ApprovalRetrieval;
import cn.flyrise.feep.retrieval.bean.Retrieval;
import cn.flyrise.feep.retrieval.protocol.ApprovalRetrievalResponse;
import cn.flyrise.feep.retrieval.protocol.DRApproval;
import cn.flyrise.feep.retrieval.protocol.RetrievalSearchRequest;
import cn.flyrise.feep.retrieval.vo.RetrievalResults;
import java.util.ArrayList;
import java.util.List;
import rx.Subscriber;

/**
 * @author ZYP
 * @since 2018-05-09 16:29
 * 审批信息的检索
 */
public class ApprovalRetrievalRepository extends RetrievalRepository {

	@Override public void search(Subscriber<? super RetrievalResults> subscriber, String keyword) {
		this.mKeyword = keyword;
		RetrievalSearchRequest request = RetrievalSearchRequest.searchTodo(keyword);
		FEHttpClient.getInstance().post(request, new ResponseCallback<ApprovalRetrievalResponse>() {
			@Override public void onCompleted(ApprovalRetrievalResponse response) {
				List<ApprovalRetrieval> approvalRetrievals = null;
				if (response != null && response.data != null && CommonUtil.nonEmptyList(response.data.results)) {
					approvalRetrievals = new ArrayList<>();
					approvalRetrievals.add((ApprovalRetrieval) header("审批"));

					for (DRApproval approval : response.data.results) {
						approvalRetrievals.add(createRetrieval(approval));
					}

					if (response.data.maxCount >= 3) {
						approvalRetrievals.add((ApprovalRetrieval) footer("更多审批"));
					}
				}

				subscriber.onNext(new RetrievalResults.Builder()
						.retrievalType(getType())
						.retrievals(approvalRetrievals)
						.create());
			}

			@Override public void onFailure(RepositoryException repositoryException) {
				FELog.e("Retrieval approval failed. Error: " + repositoryException.exception().getMessage());
				subscriber.onNext(emptyResult());
			}
		});
	}

	private ApprovalRetrieval createRetrieval(DRApproval approval) {
		ApprovalRetrieval retrieval = new ApprovalRetrieval();
		retrieval.viewType = Retrieval.VIEW_TYPE_CONTENT;
		retrieval.retrievalType = getType();
		retrieval.content = fontDeepen(approval.title, mKeyword);
		retrieval.extra = approval.important + " " + approval.sendTime;

		retrieval.businessId = approval.id;
		retrieval.userId = approval.userId;
		retrieval.username = approval.username;
		retrieval.type = approval.type;
		return retrieval;
	}

	@Override protected int getType() {
		return TYPE_APPROVAL;
	}

	@Override protected Retrieval newRetrieval() {
		return new ApprovalRetrieval();
	}
}
