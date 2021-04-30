package cn.flyrise.feep.location.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import cn.flyrise.android.library.utility.LoadingHint;
import cn.flyrise.android.shared.utility.FEUmengCfg;
import cn.flyrise.feep.K.location;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.BitmapUtil;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.GsonUtil;
import cn.flyrise.feep.core.common.utils.PixelUtil;
import cn.flyrise.feep.core.premission.FePermissions;
import cn.flyrise.feep.location.SignInMainTabActivity;
import cn.flyrise.feep.location.adapter.LocationSendReportAdapter;
import cn.flyrise.feep.location.bean.LocationSaveItem;
import cn.flyrise.feep.location.bean.SignPoiItem;
import cn.flyrise.feep.location.contract.LocationSendContract;
import cn.flyrise.feep.location.event.EventCustomCreateAddress;
import cn.flyrise.feep.location.event.EventCustomSettingAddress;
import cn.flyrise.feep.location.presenter.LocationSendPresenter;
import cn.flyrise.feep.location.util.LocationCustomSaveUtil;
import cn.squirtlez.frouter.annotations.RequestExtras;
import cn.squirtlez.frouter.annotations.Route;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.OnMapTouchListener;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.PoiItem;
import java.util.List;
import org.greenrobot.eventbus.EventBus;

/**
 * 发送位置、自定义考勤点
 * cm 2018-5.22
 */
@Route("/location/selected")
@RequestExtras({"isSendLocation"})
public class LocationSendActivity extends BaseLocationActivity implements LocationSendContract.View
		, AppBarLayout.OnOffsetChangedListener, OnMapTouchListener {

	private ImageView mIvMarker;
	private ImageView mIvLocation;
	private Marker userMarker;

	private RelativeLayout mLayoutSearch;
	private CoordinatorLayout mLayoutCoordinatro;
	private AppBarLayout mAppBarLayout;
	private SwipeRefreshLayout mSwipeRefresh;
	private RecyclerView mRecyclerView;
	private LocationSendReportAdapter mLocationAdapter;
	private LinearLayoutManager mLinearLayoutManager;

	private LocationSendContract.Presenter mPresenter;

	private int mLocationType;

	public static void start(Context context, boolean isDirectSetting, int locatiionType) {
		Intent intent = new Intent(context, LocationSendActivity.class);
		intent.putExtra("isSendLocation", locatiionType);
		intent.putExtra("is_direct_setting", isDirectSetting);
		context.startActivity(intent);
	}

	public static void start(Context context, int locatiionType, List<String> customItems) {
		Intent intent = new Intent(context, LocationSendActivity.class);
		intent.putExtra("isSendLocation", locatiionType);
		intent.putExtra(LocationSendContract.CUSTOM_POI_ITEMS, GsonUtil.getInstance().toJson(customItems));
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.location_send);
	}

	@Override
	protected void onResume() {
		super.onResume();
		FEUmengCfg.onActivityResumeUMeng(this, FEUmengCfg.LocationChoose);
		mAppBarLayout.addOnOffsetChangedListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		FEUmengCfg.onActivityPauseUMeng(this, FEUmengCfg.LocationChoose);
		mAppBarLayout.removeOnOffsetChangedListener(this);
	}

	@Override
	protected void onDestroy() {
		LoadingHint.hide();
		super.onDestroy();
		mPresenter.onDestroy();
	}

	@Override
	public void bindView() {
		super.bindView();
		mIvLocation = findViewById(R.id.iv_my_location);
		mIvMarker = findViewById(R.id.marker_icon);
		mLayoutSearch = findViewById(R.id.the_contact_relative_search);
		mLayoutCoordinatro = findViewById(R.id.coordinator);
		mAppBarLayout = findViewById(R.id.appbar_layout);

		mSwipeRefresh = this.findViewById(R.id.refresh_layout);
		mSwipeRefresh.setColorSchemeResources(R.color.login_btn_defulit);
		mRecyclerView = findViewById(R.id.locationListView);
		mRecyclerView.setLayoutManager(mLinearLayoutManager = new LinearLayoutManager(this));
		mRecyclerView.setItemAnimator(new DefaultItemAnimator());
	}

	@Override
	public void bindData() {
		mPresenter = new LocationSendPresenter(this);
		super.bindData();
		mToolBar.setRightTextColor(getResources().getColor(R.color.app_icon_bg));
		mLocationAdapter = new LocationSendReportAdapter();
		mRecyclerView.setAdapter(mLocationAdapter);
	}

	@Override
	protected void restartRequesstGPSLocation() {
		mPresenter.getGPSLocation();
	}

	@Override
	protected void locationPermissionGranted() {
		showLocationSendLayout();
	}

	@Override
	public int getLocationType() {
		return mLocationType;
	}

	@Override
	public LocationSendReportAdapter getAdapter() {
		return mLocationAdapter;
	}

	private void showLocationSendLayout() {
		Bundle mBundle = null;
		if (getIntent() != null) mBundle = getIntent().getExtras();
		if (mBundle == null) return;
		mLocationType = mBundle.getInt("isSendLocation");

		mPresenter.getGPSLocation();
		this.mToolBar.setTitle(R.string.send_location);

		if (mLocationType == location.LOCATION_CUSTOM_SETTING) {
			mLayoutSearch.setVisibility(View.VISIBLE);
			mToolBar.setLineVisibility(View.GONE);
			this.mToolBar.setTitle(R.string.location_custom_setting_title);
			mToolBar.setRightText(getResources().getString(R.string.location_custom_sign_save));
		}
		else mToolBar.setRightText(getResources().getString(R.string.submit));
	}

	@Override
	public void bindListener() {
		super.bindListener();
		mSwipeRefresh.setOnRefreshListener(() -> mPresenter.getGPSLocation());
		mIvLocation.setOnClickListener(v -> mPresenter.getGPSLocation());
		mIvMarker.setOnClickListener(v -> setMarkerAnimation());
		mToolBar.setRightTextClickListener(v -> sendLocationDetail());
		mLayoutSearch.setOnClickListener(v -> {//搜索自定义考勤点
			LocationCustomSearchActivity.start(this
					, getIntent() != null ? getIntent().getStringExtra(LocationSendContract.CUSTOM_POI_ITEMS) : ""
					, getIntent() != null && getIntent().getBooleanExtra("is_direct_setting", false));
		});
		aMap.setOnCameraChangeListener(new AMap.OnCameraChangeListener() {
			@Override
			public void onCameraChange(CameraPosition cameraPosition) {
				mPresenter.onCameraChange();
			}

			@Override
			public void onCameraChangeFinish(CameraPosition cameraPosition) {
				mPresenter.onCameraChangeFinish(cameraPosition);
			}
		});

		mRecyclerView.addOnScrollListener(new OnScrollListener() {
			boolean isSlidingToLast = false;

			@Override
			public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
				super.onScrollStateChanged(recyclerView, newState);
				if (newState == RecyclerView.SCROLL_STATE_IDLE) {
					int lastVisibleItem = mLinearLayoutManager.findLastVisibleItemPosition();//获取最后一个完全显示的ItemPosition
					int totalItemCount = recyclerView.getAdapter().getItemCount();// 判断是否滚动到底部
					if (lastVisibleItem == (totalItemCount - 1) && isSlidingToLast) {
						mPresenter.getMorePoiSearch();//加载更多功能的代码
					}
				}
			}

			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				super.onScrolled(recyclerView, dx, dy);
				isSlidingToLast = dy > 0;
			}
		});
		aMap.setOnMapTouchListener(this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 10013 && resultCode == Activity.RESULT_OK) {
			String text = data.getStringExtra("save_item");
			if (TextUtils.isEmpty(text)) return;
			LocationSaveItem saveItem = GsonUtil.getInstance().fromJson(text, LocationSaveItem.class);
			setCustomSetting(saveItem);
		}
	}

	private void sendLocationDetail() {
		if (mLocationAdapter.getSelectedPoiItem() == null) FEToast.showMessage(this.getResources().getString(R.string.location_sign_null));
		else sendLocation(mLocationAdapter.getSelectedPoiItem());
	}

	@Override
	public void swipeRefresh(boolean isRefresh) {
		if (mSwipeRefresh != null) mSwipeRefresh.setRefreshing(isRefresh);
	}

	@Override
	public void refreshListData(List<SignPoiItem> items) {
		mLocationAdapter.setPoiItems(items);
	}

	@Override
	public void loadMoreListData(List<SignPoiItem> items) {
		mLocationAdapter.addPoiItems(items);
	}

	@Override
	public void loadMoreListFail() {
		if (mLocationAdapter != null) mLocationAdapter.setPoiItems(null);
	}

	@Override
	public void setUpMap() {
		super.setUpMap();
		MarkerOptions userMarkerOptions = new MarkerOptions();
		userMarkerOptions.draggable(false);//设置Marker可拖动
		userMarkerOptions.setFlat(true);//设置marker平贴地图效果
		Bitmap userBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.yzx_icon_yourself_lication);
		Bitmap userBitmapS = BitmapUtil.fitBitmap(userBitmap, PixelUtil.pxToDip(260));
		userMarkerOptions.icon(BitmapDescriptorFactory.fromBitmap(userBitmapS));
		userMarker = aMap.addMarker(userMarkerOptions);
		aMap.moveCamera(CameraUpdateFactory.zoomTo(14));
	}

	@Override
	public void setMarkerAnimation() {
		mIvMarker.clearAnimation();
		Animation animation = new TranslateAnimation(0, 0, 0, -52);
		animation.setDuration(400);
		mIvMarker.setAnimation(animation);
		animation.start();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		FePermissions.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
	}

	private void sendLocation(PoiItem poiItem) {
		if (poiItem == null) return;
		if (mLocationType == location.LOCATION_CUSTOM_SETTING) {
			setCustomSetting(mPresenter.poiItemToLocationSaveItem(poiItem));
			return;
		}

		String address = poiItem.getProvinceName() + poiItem.getCityName() + poiItem.getAdName() + poiItem.getSnippet();
		Intent intent = this.getIntent();
		intent.putExtra("latitude", poiItem.getLatLonPoint().getLatitude());
		intent.putExtra("longitude", poiItem.getLatLonPoint().getLongitude());
		intent.putExtra("address", address);
		intent.putExtra("poiTitle", poiItem.getTitle());
		this.setResult(RESULT_OK, intent);
		finish();
	}

	private void setCustomSetting(LocationSaveItem saveItem) {
		if (getIntent().getBooleanExtra("is_direct_setting", false)) {
			if (CommonUtil.isEmptyList(LocationCustomSaveUtil.getSavePoiItems())) saveItem.isCheck = true;
			LocationCustomSaveUtil.setSavePoiItems(saveItem);
			SignInMainTabActivity.Companion.start(this, true);
			finish();
			return;
		}
		if (mPresenter.isModifyRepeat(saveItem.poiId)) return;
		finish();
		if (CoreZygote.getApplicationServices().activityInStacks(SignInCustomModifyActivity.class)) {
			EventBus.getDefault().post(new EventCustomCreateAddress(saveItem));
		}
		else if (CoreZygote.getApplicationServices().activityInStacks(SignInMainTabActivity.class)) {
			EventBus.getDefault().post(new EventCustomSettingAddress(saveItem));
		}
	}

	@Override
	public void moveCurrentPosition(LatLng latlng) {//移动地图到当前位置
		try {
			if (aMap == null) return;
			aMap.moveCamera(CameraUpdateFactory.changeLatLng(latlng));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void userCurrentPosition(LatLng latlng) {//用户当前位置的坐标
		userMarker.setPosition(latlng);
	}

	@Override
	public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
		mSwipeRefresh.setEnabled(verticalOffset >= 0);
	}

	@Override
	public void onTouch(MotionEvent motionEvent) {
		if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
			mLayoutCoordinatro.requestDisallowInterceptTouchEvent(true);
		else if (motionEvent.getAction() == MotionEvent.ACTION_UP)
			mLayoutCoordinatro.requestDisallowInterceptTouchEvent(false);
	}
}