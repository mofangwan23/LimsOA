package cn.flyrise.feep.location.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import cn.flyrise.feep.R;
import cn.flyrise.feep.commonality.view.SwipeLayout;
import cn.flyrise.feep.core.base.component.FESearchListActivity;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.DevicesUtil;
import cn.flyrise.feep.core.common.utils.GsonUtil;
import cn.flyrise.feep.location.adapter.LocationCustomSearchAdapter;
import cn.flyrise.feep.location.bean.LocationSaveItem;
import cn.flyrise.feep.location.bean.SignPoiItem;
import cn.flyrise.feep.location.contract.LocationSendContract;
import cn.flyrise.feep.location.presenter.LocationCustomSearchPresenter;
import com.google.gson.reflect.TypeToken;
import java.util.List;

/**
 * 新建：陈冕;
 * 日期： 2018-3-21-16:56.
 * 自定义考勤点搜索
 */

public class LocationCustomSearchActivity extends FESearchListActivity<SignPoiItem> {

	public LocationCustomSearchAdapter mAdapter;
	public LocationCustomSearchPresenter mPresenter;

	public static void start(Context context, String customItems, boolean isDirectSetting) {
		Intent intent = new Intent(context, LocationCustomSearchActivity.class);
		intent.putExtra(LocationSendContract.CUSTOM_POI_ITEMS, customItems);
		intent.putExtra("is_direct_setting", isDirectSetting);
		((Activity) context).startActivityForResult(intent, 10013);
	}

	@Override
	public void bindData() {
		super.bindData();
		mPresenter = new LocationCustomSearchPresenter(this);
		mAdapter = new LocationCustomSearchAdapter();
		setPresenter(mPresenter);
		setAdapter(mAdapter);
		listView.setAdapter(mAdapter);
		myHandler.postDelayed(() -> DevicesUtil.showKeyboard(et_Search), 100);
	}

	public void refreshListDatas(List<SignPoiItem> poiItems, String key) {
		mAdapter.setSearchKey(key);
		refreshListData(poiItems);
	}

	@Override
	public void bindListener() {
		super.bindListener();
		listView.setOnScrollStateTouchListener(() -> mAdapter.closeAllSwipeView());
		mAdapter.setOnItemClickListener(new LocationCustomSearchAdapter.OnItemClickListener() {
			@Override
			public void onSignInClick(SwipeLayout swipeLayout, LocationSaveItem saveItem, int position) {
				if (saveItem == null || isModifyRepeat(saveItem.poiId)) return;
				Intent intent = new Intent();
				intent.putExtra("save_item", GsonUtil.getInstance().toJson(saveItem));
				setResult(Activity.RESULT_OK, intent);
				finish();
			}

			@Override
			public void onFrontViewClick() {
			}
		});
	}

	private boolean isModifyRepeat(String poiId) {//考勤组添加判断是否重复:true重复；false不重复
		if (getIntent() == null) return false;
		String data = (getIntent().getStringExtra(LocationSendContract.CUSTOM_POI_ITEMS));
		if (TextUtils.isEmpty(data)) return false;
		List<String> poiIds = GsonUtil.getInstance().fromJson(data, new TypeToken<List<String>>() {}.getType());
		return isModifyError(poiId, poiIds);
	}

	private boolean isModifyError(String poiId, List<String> poiIds) {
		if (CommonUtil.isEmptyList(poiIds)) return false;
		if (poiIds.contains(poiId)) {
			FEToast.showMessage(getResources().getString(R.string.location_custom_error));
			return true;
		}
		return false;
	}
}
