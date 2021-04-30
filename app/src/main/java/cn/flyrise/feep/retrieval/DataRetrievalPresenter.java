package cn.flyrise.feep.retrieval;

import static cn.flyrise.feep.retrieval.DataRetrievalContract.STATE_EMPTY;
import static cn.flyrise.feep.retrieval.DataRetrievalContract.STATE_SUCCESS;

import android.text.TextUtils;
import android.util.SparseArray;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.retrieval.bean.Retrieval;
import cn.flyrise.feep.retrieval.vo.RetrievalResults;
import cn.flyrise.feep.retrieval.vo.RetrievalType;
import java.util.ArrayList;
import java.util.List;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author ZYP
 * @since 2018-05-10 14:22
 */
public class DataRetrievalPresenter implements DataRetrievalContract.IPresenter {

	private final DataRetrievalContract.IView mDataRetrievalView;
	private final DataRetrievalRepository mDataRetrievalRepository;
	private final SparseArray<RetrievalResults> mRetrievalResults;
	private final List<Retrieval> mDisplayRetrievals;

	public DataRetrievalPresenter(DataRetrievalContract.IView view, DataRetrievalRepository repository) {
		this.mDataRetrievalView = view;
		this.mDataRetrievalRepository = repository;
		this.mRetrievalResults = new SparseArray<>(9);
		this.mDisplayRetrievals = new ArrayList<>(24);
	}

	@Override public void start() {
		this.mDataRetrievalRepository.obtainSearchTypes()
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(retrievalTypes -> {
					if (CommonUtil.isEmptyList(retrievalTypes)) {
						FELog.w("Empty retrieval search type get. ");
						mDataRetrievalView.onSearchTypeFailure();
						return;
					}
					mDataRetrievalView.onSearchTypeSuccess(retrievalTypes);
				}, exception -> {
					FELog.e("Exception in obtain retrieval search type. Error: " + exception.getMessage());
					exception.printStackTrace();
					mDataRetrievalView.onSearchTypeFailure();
				});
		this.mDataRetrievalView.onRetrievalServicePrepared(mDataRetrievalRepository.getRetrievalService());
	}

	@Override public void executeQuery(String input) {
		if (TextUtils.isEmpty(input)) return;
		input = input.trim();

		// 两次判断非空，防止输入一堆空格
		if (TextUtils.isEmpty(input)) return;

		final String keyword = input;
		this.mRetrievalResults.clear();
		this.mDataRetrievalRepository.execute(mDataRetrievalView.getContext(),keyword)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(retrievalResults -> {
					int retrievalType = retrievalResults.retrievalType;
					mRetrievalResults.put(retrievalType, retrievalResults);

					if (isEmptyResult()) {
						mDataRetrievalView.updateContentLayout(STATE_EMPTY);
						return;
					}

					resortRetrievalResults();
					if (CommonUtil.isEmptyList(mDisplayRetrievals)) {
						mDataRetrievalView.updateContentLayout(STATE_EMPTY);
					}
					else {
						mDataRetrievalView.updateContentLayout(STATE_SUCCESS);
						mDataRetrievalView.onRetrievalDataArrival(mDisplayRetrievals);
					}
				}, exception -> {
					FELog.e("Exception in execute query by using this keyword ${"
							+ keyword + "}. Error: " + exception.getMessage());
					exception.printStackTrace();
				});
	}

	private boolean isEmptyResult() {
		List<RetrievalType> retrievalTypes = mDataRetrievalRepository.getRetrievalTypes();
		if (mRetrievalResults.size() != retrievalTypes.size()) return false;    // 数据没有加载完
		for (int i = 0, n = mRetrievalResults.size(); i < n; i++) {
			int key = mRetrievalResults.keyAt(i);
			RetrievalResults results = mRetrievalResults.get(key);
			if (results.resultCode > 0) {
				return false;
			}
		}

		return true;
	}

	// 根据服务端的搜索类型，对搜索结果重新排序
	private void resortRetrievalResults() {
		mDisplayRetrievals.clear();
		if (mRetrievalResults.size() == 0) return;

		List<RetrievalType> retrievalTypes = mDataRetrievalRepository.getRetrievalTypes();
		for (RetrievalType retrievalType : retrievalTypes) {
			RetrievalResults results = mRetrievalResults.get(retrievalType.getRetrievalType());
			if (results == null) continue;
			if (CommonUtil.isEmptyList(results.retrievals)) continue;

			mDisplayRetrievals.addAll(results.retrievals);
		}
	}

}
