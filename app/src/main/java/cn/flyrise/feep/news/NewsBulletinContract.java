package cn.flyrise.feep.news;

import java.util.List;

import cn.flyrise.feep.commonality.bean.FEListItem;
import cn.flyrise.feep.core.network.RepositoryException;

/**
 * @author ZYP
 * @since 2016-11-09 15:09
 */
public interface NewsBulletinContract {

	interface IPresenter {

		void start();

		void request(int pageNumber);

		void refresh();

		int getCurrentPage();

		void setCurrentPage(int currentPage);

		boolean hasMoreData();

		void onDestroy();

	}

	interface IView {

		void showLoading();

		void fetchDataSuccess(List<FEListItem> feListItems, boolean hasMoreData);

		void fetchDataError(RepositoryException repositoryException);
	}
}
