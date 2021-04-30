package cn.flyrise.feep.location.views;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.flyrise.feep.R;
import cn.flyrise.feep.commonality.view.SwipeLayout;
import cn.flyrise.feep.core.base.component.FESearchListActivity;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.DevicesUtil;
import cn.flyrise.feep.location.adapter.LocationSaveAdapter;
import cn.flyrise.feep.location.adapter.NewLocationRecylerAdapter;
import cn.flyrise.feep.location.bean.LocationSaveItem;
import cn.flyrise.feep.location.bean.SignPoiItem;
import cn.flyrise.feep.location.presenter.LocationSearchSignPresenter;
import cn.flyrise.feep.location.util.GpsStateUtils;
import cn.flyrise.feep.location.util.LocationSearchRecordSaveUtil;
import cn.squirtlez.frouter.annotations.Route;
import java.util.List;

/**
 * 新建：陈冕;
 * 日期： 2018-3-22-15:53.
 * 只负责搜索、选择签到地点
 */
@Route("/location/search")
public class SignInSearchActivity extends FESearchListActivity<SignPoiItem> implements GpsStateUtils.GpsStateListener {

	private LocationSearchSignPresenter mPresenter;
	private RecyclerView mSaveListView;//签到历史记录
	private TextView mTvClearHint;
	private LinearLayout mHistoryLayout;
	public LocationSaveAdapter mLocationSaveAdapter;//历史记录适配器
	public NewLocationRecylerAdapter mAdapter;

	private GpsStateUtils mGpsStateUtils;
	private boolean isReStart = false;//是否为重新打开

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.location_search_sign_layout);
		mGpsStateUtils = new GpsStateUtils(this);

		mPresenter.initPresenter(this);
		mSaveListView = this.findViewById(R.id.location_save_data);
		mTvClearHint = this.findViewById(R.id.clicke_empty_hint);
		mHistoryLayout = this.findViewById(R.id.historyLayout);
		showSaveLocation(true);

		LinearLayoutManager mLayout = new LinearLayoutManager(this);
		mLayout.setAutoMeasureEnabled(true);
		mSaveListView.setLayoutManager(mLayout);

		mGpsStateUtils.requsetGpsIsState();
		if (!mGpsStateUtils.gpsIsOpen()) {
			mGpsStateUtils.openGPSSetting(getString(R.string.lbl_text_open_gps));
		}
		mTvClearHint.setOnClickListener(v -> {
			LocationSearchRecordSaveUtil.clear();
			showSaveLocation(true);
		});
		myHandler.postDelayed(()->DevicesUtil.showKeyboard(et_Search), 100);
	}


	@Override
	public void bindData() {
		super.bindData();
		mPresenter = new LocationSearchSignPresenter();
		setPresenter(mPresenter);
		mAdapter = new NewLocationRecylerAdapter();
		mAdapter.setLocationType(LocationSearchSignPresenter.TYPE);
		setPresenter(mPresenter);
		setAdapter(mAdapter);
		listView.setAdapter(mAdapter);
	}

	@Override
	public void bindListener() {
		super.bindListener();
		listView.setOnScrollStateTouchListener(() -> {
			mAdapter.closeAllSwipeView();
		});

		mAdapter.setOnItemClickListener(new NewLocationRecylerAdapter.OnItemClickListener() {
			@Override
			public void onSignInClick(SwipeLayout swipeLayout, LocationSaveItem saveItem, int position) {
				mPresenter.signSelectedPoiItem(saveItem);
			}

			@Override
			public void onFrontViewClick() {
			}

			@Override
			public void onSignWorkingClick() {

			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == mPresenter.REQUEST_SIGN_IN && resultCode == Activity.RESULT_OK && data != null) {
			String text = data.getStringExtra("title");
			et_Search.setText(text);
			isReStart = false;
			et_Search.setSelection(TextUtils.isEmpty(text) ? 0 : text.length());
		}
	}

	@Override
	public void inputTextEmpty() {
		super.inputTextEmpty();
		showLocationSaveListView(!isSaveEmpty());
		setCanPullUp(false);
	}

	public void unlockAdapter() {
		mAdapter.setCleanAllSignIcon();
		mAdapter.notifyDataSetChanged();
	}

	public void setLocationAdapterEnabled(boolean isEnabled) {
		mAdapter.setEnabled(isEnabled);
	}

	public void setSearchKey(String searchKey) {
		if (mAdapter != null) mAdapter.setSearchKey(searchKey);
	}

	public void setSwipeRefreshEnabled(boolean isEnabled) {
		listView.getRefreshLayout().setEnabled(isEnabled);
	}

	public void showSaveLocation(boolean isShow) {//显示或隐藏历史记录
		List<LocationSaveItem> items = LocationSearchRecordSaveUtil.getSavePoiItems();
		mLocationSaveAdapter = new LocationSaveAdapter(this);
		if (CommonUtil.isEmptyList(items)) {
			showLocationSaveListView(false);
			return;
		}
		mLocationSaveAdapter.setLocationSave(items);
		if (isShow) showLocationSaveListView(true);
		mSaveListView.setAdapter(mLocationSaveAdapter);
		mLocationSaveAdapter.setOnItemClickListener(this::clickLocationSaveItem);
		mLocationSaveAdapter.setOnDeletedItemListener(item -> LocationSearchRecordSaveUtil.deleteItem(item.poiId));
	}

	private void clickLocationSaveItem(LocationSaveItem item) {
		if (TextUtils.isEmpty(item.title)) return;
		et_Search.setText(item.title);
		et_Search.setSelection(item.title.length());
	}

	public void showLocationSaveListView(boolean isShow) {
		mSaveListView.setVisibility(isShow ? View.VISIBLE : View.GONE);
		mTvClearHint.setVisibility(isShow ? View.VISIBLE : View.GONE);
		mHistoryLayout.setVisibility(isShow ? View.VISIBLE : View.GONE);
	}

	public boolean isSaveEmpty() {
		return CommonUtil.isEmptyList(LocationSearchRecordSaveUtil.getSavePoiItems());
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (isReStart) {
			isReStart = false;
			mPresenter.restartRefreshListData();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		mPresenter.onPause();
		isReStart = true;
		DevicesUtil.hideKeyboard(et_Search);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mPresenter.onDestroy();
		mGpsStateUtils.destroy();
	}

	@Override
	public void onGpsState(boolean isGpsState) {

	}

	@Override
	public void cancleDialog() {

	}

	@Override
	public void onDismiss() {
	}
}
