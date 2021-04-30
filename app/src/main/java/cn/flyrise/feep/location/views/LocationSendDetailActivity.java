package cn.flyrise.feep.location.views;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.flyrise.android.library.utility.LoadingHint;
import cn.flyrise.android.shared.utility.FEUmengCfg;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.common.utils.BitmapUtil;
import cn.flyrise.feep.core.common.utils.PixelUtil;
import cn.flyrise.feep.core.premission.FePermissions;
import cn.flyrise.feep.location.util.GpsHelper;
import cn.flyrise.feep.location.util.GpsHelper.LocationCallBack;
import cn.squirtlez.frouter.annotations.RequestExtras;
import cn.squirtlez.frouter.annotations.Route;
import com.amap.api.location.AMapLocation;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;

/**
 * 查看位置详情 2017-4.21
 */
@Route("/location/detail")
@RequestExtras({"latitude", "longitude", "address", "name", "title"})
public class LocationSendDetailActivity extends BaseLocationActivity implements LocationCallBack {

	private ImageView mIvLocation;
	private Marker userMarker;
	private TextView mTvDetailUserName;
	private TextView mTvDetailAddress;

	private LinearLayout mLayoutDetail;

	private LatLng mLatLngDeatil;//当前详情的位置坐标
	private boolean isActive;

	private GpsHelper mHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.location_detail);
	}

	@Override
	protected void onResume() {
		super.onResume();
		FEUmengCfg.onActivityResumeUMeng(this, FEUmengCfg.LocationChoose);
		isActive = true;
	}

	@Override
	protected void onPause() {
		super.onPause();
		FEUmengCfg.onActivityPauseUMeng(this, FEUmengCfg.LocationChoose);
		isActive = false;
	}

	@Override
	protected void onDestroy() {
		LoadingHint.hide();
		super.onDestroy();
		if (mHelper != null) mHelper.stopSingleLocationRequest();
	}

	@Override
	public void bindView() {
		super.bindView();
		mIvLocation = (ImageView) findViewById(R.id.iv_my_location);
		mTvDetailUserName = (TextView) findViewById(R.id.detail_userName);
		mTvDetailAddress = (TextView) findViewById(R.id.detail_address);
		mLayoutDetail = (LinearLayout) findViewById(R.id.detail_layout);
	}

	@Override
	public void bindData() {
		super.bindData();
		mHelper = new GpsHelper(this);
		this.mToolBar.setTitle(R.string.location_message);
		mToolBar.setRightTextColor(getResources().getColor(R.color.app_icon_bg));
	}

	@Override public void bindListener() {
		super.bindListener();
		mIvLocation.setOnClickListener(v -> mHelper.getSingleLocation(this));
		mLayoutDetail.setOnClickListener(v -> showLocationDetailLayout());
	}

	@Override
	protected void restartRequesstGPSLocation() {
		showLocationDetailLayout();
	}

	@Override
	protected void locationPermissionGranted() {
		showLocationDetailLayout();
	}

	public void showLocationDetailLayout() {
		if (mLatLngDeatil != null) {
			moveToLocation(mLatLngDeatil);
			return;
		}
		Bundle mBundle = null;
		if (getIntent() != null) mBundle = getIntent().getExtras();
		if (mBundle == null) return;
		Double latitude = mBundle.getDouble("latitude");
		Double longitude = mBundle.getDouble("longitude");
		String address = mBundle.getString("address");
		String title = mBundle.getString("title");
		String name = mBundle.getString("name");
		if (!TextUtils.isEmpty(name)) mTvDetailUserName.setText(name);
		if (!TextUtils.isEmpty(address) || !TextUtils.isEmpty(title)) {
			String locationAddress = address + title;
			mTvDetailAddress.setText(locationAddress);
		}
		mLatLngDeatil = new LatLng(latitude, longitude);
		markerDetailAddress(mLatLngDeatil);
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
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		FePermissions.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
	}

	public void moveCurrentPosition(LatLng latlng) {
		try {
			if (aMap == null) return;
			aMap.moveCamera(CameraUpdateFactory.changeLatLng(latlng));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void markerDetailAddress(LatLng latlng) {
		if (mLatLngDeatil == null) return;
		sendAddMarkerIcon(latlng);
		moveCurrentPosition(latlng);
	}

	public void sendAddMarkerIcon(LatLng latlng) {
		if (aMap == null) return;
		MarkerOptions markerOptions = new MarkerOptions();
		markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.location_marker)));
		markerOptions.setFlat(true);
		markerOptions.anchor(0.5f, 0.8f);
		Marker marker = aMap.addMarker(markerOptions);
		marker.setPosition(latlng);
	}

	public boolean isActive() {
		return isActive;
	}

	public void moveToLocation(LatLng latLng) {//移动到当前位置
		if (aMap == null) return;
		aMap.animateCamera(CameraUpdateFactory.changeLatLng(latLng));
	}

	private void markerCurrentLocation(LatLng latLng) {//标记用户当前位置
		if (userMarker != null) userMarker.setPosition(latLng);
		moveToLocation(latLng);
	}

	@Override
	public void success(AMapLocation location) {//定位成功
		if (location == null) return;
		markerCurrentLocation(new LatLng(location.getLatitude(), location.getLongitude()));
	}

	@Override
	public void error() {

	}
}