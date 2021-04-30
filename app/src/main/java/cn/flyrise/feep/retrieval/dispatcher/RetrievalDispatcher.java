package cn.flyrise.feep.retrieval.dispatcher;

import android.content.Context;
import cn.flyrise.feep.retrieval.bean.Retrieval;

/**
 * @author ZYP
 * @since 2018-05-03 11:37
 */
public interface RetrievalDispatcher {

	void jumpToSearchPage(Context context, String keyword);

	void jumpToDetailPage(Context context, Retrieval retrieval);

}
