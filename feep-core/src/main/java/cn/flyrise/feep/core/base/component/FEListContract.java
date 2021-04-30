package cn.flyrise.feep.core.base.component;

import java.util.List;

/**
 * Created by KLC on 2016/12/28.
 */

public class FEListContract {

	public interface View<T> {

		void refreshListData(List<T> dataList);

		void refreshListFail();

		void loadMoreListData(List<T> dataList);

		void loadMoreListFail();

		void setCanPullUp(boolean hasMore);

		void setEmptyView();

		void showLoading(boolean show);

	}

	public interface Presenter {

		void onStart();

		void refreshListData();

		void refreshListData(String searchKey);

		void loadMoreData();

		boolean hasMoreData();
	}

	public interface LoadListCallback<T> {

		void loadListDataSuccess(List<T> dataList, int totalPage);

		void loadListDataError();
	}
}
