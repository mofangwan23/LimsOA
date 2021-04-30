package cn.flyrise.feep.knowledge.contract;

import android.content.Context;

import android.content.Intent;
import java.util.List;

import cn.flyrise.feep.knowledge.model.SearchFile;

/**
 * Created by KLC on 2016/12/6.
 */

public interface SearchListContract {

	interface View extends ListContract.View<SearchFile> {

		void showProgress(int text, int progress);

		void openFile(Intent intent);
	}

	interface Presenter {

		void refreshListData(String key);

		void loadMore();

		boolean hasMoreData();

		void opeFile(Context context, SearchFile searchFile);

		void cancelSearch();
	}

	interface LoadListCallback {

		void loadListDataSuccess(List<SearchFile> dataList, int totalPage);

		void loadListDataError();
	}

}