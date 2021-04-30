/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2012-6-12 上午9:52:53
 */

package cn.flyrise.feep.core.base.component;

import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.DevicesUtil;
import cn.flyrise.feep.core.dialog.FELoadingDialog;
import java.util.List;

import cn.flyrise.feep.core.R;
import cn.flyrise.feep.core.base.views.PullAndLoadMoreRecyclerView;
import cn.flyrise.feep.core.base.views.adapter.FEListAdapter;


/**
 * 搜索列表的父类
 * author klc
 */
public abstract class FESearchListActivity<T> extends BaseActivity implements FEListContract.View<T> {

	protected ImageView iv_delete;
	protected EditText et_Search;
	protected TextView btnSearchCancel;
	protected PullAndLoadMoreRecyclerView listView;
	protected View iv_empty;
	protected FELoadingDialog mLoadingDialog;

	protected String searchKey;
	private FEListAdapter<T> mAdapter;
	private FEListContract.Presenter mPresenter;

	protected final Handler myHandler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.what == 10012) {
				InputMethodManager inputManager = (InputMethodManager) et_Search.getContext().getSystemService(INPUT_METHOD_SERVICE);
				inputManager.showSoftInput(et_Search, 0);
			}
			else if (msg.what == 10013) {
				InputMethodManager inputManager = (InputMethodManager) et_Search.getContext().getSystemService(INPUT_METHOD_SERVICE);
				inputManager.hideSoftInputFromWindow(et_Search.getWindowToken(), 0); //强制隐藏键盘
			}
		}
	};

	protected final Runnable searchRunnable = new Runnable() {
		@Override
		public void run() {
			if (!TextUtils.isEmpty(searchKey)) {
				searchData(searchKey);
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.core_search_layout);
	}

	@Override
	public void bindView() {
		super.bindView();
		iv_delete = (ImageView) this.findViewById(R.id.ivDeleteText);
		et_Search = (EditText) this.findViewById(R.id.etSearch);
		btnSearchCancel = (TextView) this.findViewById(R.id.btnSearchCancle);
		listView = (PullAndLoadMoreRecyclerView) this.findViewById(R.id.form_search_listview);
		iv_empty =  this.findViewById(R.id.error_layout);
	}

	@Override
	public void bindData() {
		searchKey = "";
		myHandler.sendEmptyMessageDelayed(10012, 390);
	}

	@Override
	public void bindListener() {
		listView.setRefreshListener(() -> {
			if (TextUtils.isEmpty(searchKey)) {
				listView.setRefreshing(false);
			}
			else {
				searchData(searchKey);
			}
		});
		listView.setLoadMoreListener(() -> mPresenter.loadMoreData());
		iv_delete.setOnClickListener(v -> et_Search.setText(""));
		btnSearchCancel.setOnClickListener(v -> finish());
		listView.getLoadMoreRecyclerView().setOnTouchListener(onTouchListener);
		et_Search.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				searchKey = et_Search.getText().toString().trim();
				if (!TextUtils.isEmpty(searchKey)) {
					myHandler.removeCallbacks(searchRunnable);
					myHandler.postDelayed(searchRunnable, 500);
				}
				else {
					inputTextEmpty();
				}
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			public void afterTextChanged(Editable s) {
				if (s.length() == 0) {
					iv_delete.setVisibility(View.GONE);
					iv_empty.setVisibility(View.GONE);
					mAdapter.setDataList(null);
					listView.removeFootView();
				}
				else {
					iv_delete.setVisibility(View.VISIBLE);
				}
			}
		});
	}

	private View.OnTouchListener onTouchListener = (v, event) -> {
		if (event.getAction() == MotionEvent.ACTION_MOVE) {
			myHandler.sendEmptyMessage(10013);
		}
		return false;
	};

	public void setPresenter(FEListContract.Presenter mPresenter) {
		this.mPresenter = mPresenter;
	}

	public void setAdapter(FEListAdapter<T> mAdapter) {
		this.mAdapter = mAdapter;
		this.listView.setAdapter(mAdapter);
	}

	public void searchData(String searchKey) {
		mPresenter.refreshListData(searchKey);
	}


	public void showRefreshLoading(boolean show) {
		if (show) {
			listView.setRefreshing(true);
		}
		else {
			myHandler.postDelayed(() -> listView.setRefreshing(false), 500);
		}
	}

	@Override
	public void refreshListData(List<T> dataList) {
		if (!TextUtils.isEmpty(et_Search.getText().toString())) {
			mAdapter.setDataList(dataList);
			setEmptyView();
		}
		showRefreshLoading(false);
	}

	@Override
	public void loadMoreListData(List<T> dataList) {
		mAdapter.addDataList(dataList);
		setCanPullUp(mPresenter.hasMoreData());
	}

	@Override public void refreshListFail() {
		listView.setRefreshing(false);
		FEToast.showMessage("获取内容失败");
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
		if (mAdapter.getDataSourceCount() == 0) {
			iv_empty.setVisibility(View.VISIBLE);
		}
		else {
			iv_empty.setVisibility(View.GONE);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		DevicesUtil.tryCloseKeyboard(this);
	}

	@Override
	public void finish() {
		super.finish();
		intiFinishAnimation();
	}

	private void intiFinishAnimation() {
		//完善关闭动画
		TypedArray activityStyle = getTheme().obtainStyledAttributes(new int[]{android.R.attr.windowAnimationStyle});
		int windowAnimationStyleResId = activityStyle.getResourceId(0, 0);
		activityStyle.recycle();
		activityStyle = getTheme().obtainStyledAttributes(windowAnimationStyleResId,
				new int[]{android.R.attr.activityCloseEnterAnimation, android.R.attr.activityCloseExitAnimation});
		int activityCloseEnterAnimation = activityStyle.getResourceId(0, 0);
		int activityCloseExitAnimation = activityStyle.getResourceId(1, 0);
		activityStyle.recycle();
		overridePendingTransition(activityCloseEnterAnimation, activityCloseExitAnimation);
		//end
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
		}
		return super.onKeyDown(keyCode, event);
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

	public void inputTextEmpty() {

	}

	@Override protected int statusBarColor() {
		return Color.WHITE;
	}
}
