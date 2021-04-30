/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2012-6-12 上午9:52:53
 */

package cn.flyrise.feep.core.base.component;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import cn.flyrise.feep.core.R;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.base.views.PullAndLoadMoreRecyclerView;
import cn.flyrise.feep.core.base.views.adapter.FEListAdapter;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.dialog.FELoadingDialog;
import java.util.List;

/**
 * 类功能描述：</br> 列表activity的父类
 * @author update BY klc
 * @version 2.0
 */
public abstract class FEListActivity<T> extends BaseActivity implements FEListContract.View<T> {

	protected View layoutSearch;
	protected TextView tv_search;
	protected PullAndLoadMoreRecyclerView listView;
	protected FELoadingDialog mLoadingDialog;
	protected View emptyView;
	protected FEToolbar mToolBar;

	private Handler mHandler;
	private FEListContract.Presenter mPresenter;
	private FEListAdapter<T> mListAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.core_activity_fe_list);
	}

	@Override
	protected void toolBar(FEToolbar toolbar) {
		this.mToolBar = toolbar;
	}

	@Override
	public void bindView() {
		layoutSearch = findViewById(R.id.fe_list_searchBar);
		tv_search = this.findViewById(R.id.search_et);
		listView = findViewById(R.id.fe_list_listview);
		emptyView = findViewById(R.id.fe_list_empty);
		tv_search.setHint(CommonUtil.getString(R.string.core_lbl_txt_search));
		emptyView.setVisibility(View.GONE);
	}

	@Override
	public void bindData() {
		this.mHandler = new Handler();
	}

	public void startLoadData() {
		this.mHandler.postDelayed(() -> {
			listView.setRefreshing(true);
			mPresenter.refreshListData();
		}, 200);
	}

	@Override
	public void bindListener() {
		listView.setRefreshListener(this::listDownRefresh);
		listView.setLoadMoreListener(this::listUpRefresh);
	}

	public void setAdapter(FEListAdapter<T> mListAdapter) {
		this.mListAdapter = mListAdapter;
		this.listView.setAdapter(mListAdapter);
	}

	public void setPresenter(FEListContract.Presenter mPresenter) {
		this.mPresenter = mPresenter;
	}

	public void listUpRefresh() {
		this.mPresenter.loadMoreData();
	}

	public void listDownRefresh() {
		this.mPresenter.refreshListData();
	}

	@Override
	public void refreshListData(List<T> dataList) {
		this.mListAdapter.setDataList(dataList);
		mHandler.postDelayed(() -> showRefreshLoading(false), 200);
		setEmptyView();
		setCanPullUp(mPresenter.hasMoreData());
	}

	@Override public void refreshListFail() {
		listView.setRefreshing(false);
		setEmptyView();
		FEToast.showMessage("获取内容失败");
		setCanPullUp(mPresenter.hasMoreData());
	}

	public void showRefreshLoading(boolean show) {
		if (show) {
			listView.setRefreshing(true);
		}
		else {
			listView.setRefreshing(false);
		}
	}

	@Override
	public void loadMoreListData(List<T> dataList) {
		mListAdapter.addDataList(dataList);
		setCanPullUp(mPresenter.hasMoreData());
	}

	@Override
	public void loadMoreListFail() {
		listView.scrollLastItem2Bottom();
	}

	@Override
	public void setCanPullUp(boolean hasMore) {
		if (hasMore) {
			listView.addFootView();
		}
		else {
			listView.removeFootView();
		}
	}

	@Override
	public void setEmptyView() {
		if (mListAdapter.getDataSourceCount() == 0) {
			emptyView.setVisibility(View.VISIBLE);
		}
		else {
			emptyView.setVisibility(View.GONE);
		}
	}

	@Override
	public void showLoading(boolean show) {
		if (show) {
			if (mLoadingDialog == null) {
				mLoadingDialog = new FELoadingDialog.Builder(this)
						.setCancelable(true)
						.create();
				mLoadingDialog.show();
			}
		}
		else {
			if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
				mLoadingDialog.hide();
			}
			mLoadingDialog = null;
		}
	}
}
