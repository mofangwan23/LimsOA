package cn.flyrise.feep.retrieval.repository;

import static cn.flyrise.feep.retrieval.bean.Retrieval.VIEW_TYPE_CONTENT;
import static cn.flyrise.feep.retrieval.vo.RetrievalType.TYPE_GROUP;

import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.retrieval.bean.GroupRetrieval;
import cn.flyrise.feep.retrieval.bean.Retrieval;
import cn.flyrise.feep.retrieval.vo.RetrievalResults;
import com.hyphenate.chatui.retrieval.GroupInfo;
import com.hyphenate.chatui.retrieval.GroupRepository;
import com.hyphenate.chatui.utils.IMHuanXinHelper;
import java.util.ArrayList;
import java.util.List;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author ZYP
 * @since 2018-05-09 16:25
 * 群组检索
 */
public class GroupRetrievalRepository extends RetrievalRepository {

	private GroupRepository mGroupRepository;

	@Override public void search(Subscriber<? super RetrievalResults> subscriber, String keyword) {
		if (!IMHuanXinHelper.getInstance().isImLogin()) {
			FELog.w("Hyphenate hasn't login.");
			subscriber.onNext(emptyResult());
			return;
		}

		if (mGroupRepository == null) {
			mGroupRepository = new GroupRepository();
		}

		this.mKeyword = keyword;
		this.mGroupRepository.queryGroupInfo(keyword, 3)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(groups -> {
					List<GroupRetrieval> groupRetrievals = null;
					if (CommonUtil.nonEmptyList(groups)) {
						groupRetrievals = new ArrayList<>();
						groupRetrievals.add((GroupRetrieval) header("群聊"));

						for (GroupInfo groupInfo : groups) {
							groupRetrievals.add(createRetrievalGroup(groupInfo));
						}

						if (groupRetrievals.size() >= 3) {
							groupRetrievals.add((GroupRetrieval) footer("更多群聊"));
						}
					}

					subscriber.onNext(new RetrievalResults.Builder()
							.retrievalType(getType())
							.retrievals(groupRetrievals)
							.create());
				}, exception -> {
					FELog.e("Group information retrieval failed. Error: " + exception.getMessage());
					subscriber.onNext(emptyResult());
				});
	}

	private GroupRetrieval createRetrievalGroup(GroupInfo groupInfo) {
		GroupRetrieval groupRetrieval = new GroupRetrieval();
		groupRetrieval.viewType = VIEW_TYPE_CONTENT;
		groupRetrieval.retrievalType = TYPE_GROUP;

		groupRetrieval.content = fontDeepen(groupInfo.conversationName, mKeyword);
		groupRetrieval.extra = fontDeepen(groupInfo.content, mKeyword);
		groupRetrieval.keyword = mKeyword;

		groupRetrieval.conversationId = groupInfo.conversationId;
		groupRetrieval.imageRes = groupInfo.imageRes;
		return groupRetrieval;
	}

	@Override protected int getType() {
		return TYPE_GROUP;
	}

	@Override protected Retrieval newRetrieval() {
		return new GroupRetrieval();
	}
}
