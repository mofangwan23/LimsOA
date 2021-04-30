package cn.flyrise.feep.retrieval;

import android.content.Context;
import cn.flyrise.feep.retrieval.bean.Retrieval;
import cn.flyrise.feep.retrieval.vo.RetrievalType;
import java.util.List;

/**
 * @author ZYP
 * @since 2018-05-10 14:14
 */
public interface DataRetrievalContract {

	byte STATE_RESET = 1;          // 重置回默认布局
	byte STATE_EMPTY = 2;          // 没数据
	byte STATE_SUCCESS = 3;        // 显示数据

	interface IView {

		void onSearchTypeSuccess(List<RetrievalType> retrievalTypes);

		void onSearchTypeFailure();

		void onRetrievalDataArrival(List<? extends Retrieval> retrievals);

		void onRetrievalServicePrepared(IRetrievalServices retrievalServices);

		void updateContentLayout(byte state);

		Context getContext();

	}

	interface IPresenter {

		void start();

		void executeQuery(String keyword);

	}

}
