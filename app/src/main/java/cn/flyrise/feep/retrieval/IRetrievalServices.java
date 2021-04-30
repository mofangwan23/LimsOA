package cn.flyrise.feep.retrieval;

import android.content.Context;
import android.text.Spannable;
import cn.flyrise.feep.retrieval.vo.RetrievalResults;
import cn.flyrise.feep.retrieval.vo.RetrievalType;
import java.util.List;
import rx.Observable;

/**
 * @author ZYP
 * @since 2018-04-28 13:44
 * 统一数据检索接口
 */
public interface IRetrievalServices {

	Observable<RetrievalResults> execute(List<RetrievalType> retrievalTypes, String keyword);

	Spannable formatTextFromEmoticon(Context context, String text);

	void setContext(Context context);

}
