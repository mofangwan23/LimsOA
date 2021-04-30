package cn.flyrise.feep.retrieval;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.SparseArray;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.retrieval.repository.RetrievalRepository;
import cn.flyrise.feep.retrieval.vo.RetrievalResults;
import cn.flyrise.feep.retrieval.vo.RetrievalType;
import com.hyphenate.easeui.utils.EaseSmileUtils;
import java.util.List;
import rx.Observable;

/**
 * @author ZYP
 * @since 2018-04-28 17:29
 */
public class FeepRetrievalService implements IRetrievalServices {

	private String mKeyword;
	private final SparseArray<RetrievalRepository> mRepositories;
	private Context mContext;

	public FeepRetrievalService() {
		mRepositories = new SparseArray<>(9);
	}

	// 搜索何种类型
	@Override public Observable<RetrievalResults> execute(List<RetrievalType> retrievalTypes, String keyword) {
		this.mKeyword = keyword;
		return Observable.create(f -> {
			for (RetrievalType retrievalType : retrievalTypes) {
				int type = retrievalType.getRetrievalType();
				RetrievalRepository repository = mRepositories.get(type);
				if (repository != null) {
					repository.mContext = this.mContext;
					repository.search(f, mKeyword);
					continue;
				}

				repository = RetrievalRepository.newRepository(type);
				if (repository != null) {
					repository.mContext = this.mContext;
					mRepositories.put(type, repository);
					repository.search(f, mKeyword);
					continue;
				}

				FELog.e("Error retrieval type, create repository failed by using this type ${" + type + "}");
			}
		});
	}

	@Override public Spannable formatTextFromEmoticon(Context context, String text) {
		SpannableString spannable = new SpannableString(text);
		int index = text.indexOf(mKeyword);
		if (index >= 0) {
			ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor("#00ADEF"));
			spannable.setSpan(colorSpan, index, index + mKeyword.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
		}

		EaseSmileUtils.addSmiles(context, spannable, false);
		return spannable;
	}

	@Override public void setContext(Context context) {
		this.mContext = context;
	}
}
