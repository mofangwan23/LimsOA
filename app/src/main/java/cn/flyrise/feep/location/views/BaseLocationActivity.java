package cn.flyrise.feep.location.views;

import android.Manifest;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import cn.flyrise.android.library.utility.LoadingHint;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.NetworkUtil;
import cn.flyrise.feep.core.premission.FePermissions;
import cn.flyrise.feep.core.premission.PermissionCode;
import cn.flyrise.feep.core.premission.PermissionGranted;
import cn.flyrise.feep.location.util.GpsHelper;
import cn.flyrise.feep.location.util.GpsStateUtils;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.TextureMapFragment;

/**
 * 新建：陈冕;
 * 日期： 2017-12-26-14:01.
 */

public abstract class BaseLocationActivity extends BaseActivity implements GpsStateUtils.GpsStateListener {

	protected AMap aMap;
	protected FEToolbar mToolBar;
	private GpsStateUtils mGpsStateUtils;
	private Handler mHandle = new Handler(Looper.getMainLooper());

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mGpsStateUtils = new GpsStateUtils(this);
	}

	@Override
	protected void toolBar(FEToolbar toolbar) {
		super.toolBar(toolbar);
		this.mToolBar = toolbar;
	}

	@Override
	public void bindView() {
		super.bindView();
		aMap = ((TextureMapFragment) getFragmentManager().findFragmentById(R.id.texture_map_fragment)).getMap();
		setUpMap();
		new Thread(() -> {
			if (!NetworkUtil.ping() && mHandle != null) mHandle.post(this::networkError);
		}).start();
	}

	protected void setUpMap() {
		if (aMap == null) return;
		aMap.getUiSettings().setMyLocationButtonEnabled(false);// 设置默认定位按钮是否显示
		aMap.getUiSettings().setRotateGesturesEnabled(false);//旋转地图
		aMap.getUiSettings().setZoomControlsEnabled(false);//缩放图标
		aMap.getUiSettings().setScrollGesturesEnabled(true);
		aMap.getUiSettings().setZoomGesturesEnabled(true);//缩放地图
		aMap.getUiSettings().setTiltGesturesEnabled(true);
		aMap.getUiSettings().setScaleControlsEnabled(true);//显示标尺
		aMap.moveCamera(CameraUpdateFactory.zoomTo(16));
	}

	@Override
	public void bindData() {
		super.bindData();
		mGpsStateUtils.requsetGpsIsState();
		FePermissions.with(this)
				.permissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION})
				.rationaleMessage(getResources().getString(com.hyphenate.chatui.R.string.permission_rationale_location))
				.requestCode(PermissionCode.LOCATION)
				.request();
	}

	private void networkError() {//检测是否连接外网
		if (this.isFinishing()) return;
		if (LoadingHint.isLoading()) LoadingHint.hide();
		FEToast.showMessage(this.getResources().getString(R.string.lbl_retry_network_connection));
	}

	@PermissionGranted(PermissionCode.LOCATION)
	public void onLocationPermissionGranted() {
		if (!mGpsStateUtils.gpsIsOpen()) mGpsStateUtils.openGPSSetting(getString(R.string.lbl_text_open_setting_gps));
		else if (GpsHelper.inspectMockLocation(this)) return;
		locationPermissionGranted();
	}


	protected abstract void restartRequesstGPSLocation();

	protected abstract void locationPermissionGranted();

	@Override
	public void onGpsState(boolean isGpsState) {
		if (isGpsState) restartRequesstGPSLocation();
	}

	@Override
	public void cancleDialog() {

	}

	@Override
	public void onDismiss() {
	}

	@Override
	public void onBackPressed() {
		if (aMap != null) aMap.clear();
		super.onBackPressed();
		finish();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mGpsStateUtils.destroy();
	}
}
