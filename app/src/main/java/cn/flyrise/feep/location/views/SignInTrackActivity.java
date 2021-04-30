package cn.flyrise.feep.location.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.flyrise.android.shared.utility.FEUmengCfg;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.common.utils.PixelUtil;
import cn.flyrise.feep.core.services.model.AddressBook;
import cn.flyrise.feep.location.adapter.BaseSelectedAdapter.OnSelectedClickeItemListener;
import cn.flyrise.feep.location.adapter.LocationPersonAdapter;
import cn.flyrise.feep.location.bean.LocusPersonLists;
import cn.flyrise.feep.location.contract.SignInTrackContract;
import cn.flyrise.feep.location.dialog.SignInSelectedDialog;
import cn.flyrise.feep.location.presenter.SignInTrackPersenter;
import cn.flyrise.feep.location.util.LocationDayPickerUtil;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.OnMapClickListener;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.TextureMapFragment;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolylineOptions;
import java.util.Calendar;
import java.util.List;

/**
 * 类描述：查看轨迹
 * @author 陈冕
 * @version 1.0
 */
public class SignInTrackActivity extends BaseActivity implements SignInTrackContract.View
		, LocationDayPickerUtil.LocationDayPickerListener
		, OnSelectedClickeItemListener {

	private TextView mTvDate;
	private TextView mTvPerson;
	private AMap aMap;

	private String currentDate;
	private String currentUserId;
	private SignInSelectedDialog mPersonDialog;
	private LocationDayPickerUtil mDayPickerUtil;
	private Marker selectedMarker;

	private SignInTrackContract.presenter mPresenter;

	public static void start(Context context, String day, String userId) {
		Intent intent = new Intent(context, SignInTrackActivity.class);
		intent.putExtra(USER_ID, userId);
		intent.putExtra(LOCATION_DAY, day);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.location_track);
	}

	@Override
	protected void toolBar(FEToolbar toolbar) {
		toolbar.setTitle(R.string.location_calendar_sign_in_track);
	}

	@Override
	public void bindView() {
		aMap = ((TextureMapFragment) getFragmentManager().findFragmentById(R.id.texture_map_fragment)).getMap();
		setUpMap();
		mTvDate = (TextView) this.findViewById(R.id.dateselect);
		mTvPerson = (TextView) this.findViewById(R.id.personselect);
	}

	private void setUpMap() {
		if (aMap == null) return;
		aMap.getUiSettings().setScaleControlsEnabled(true);// 设置默认标尺
		aMap.getUiSettings().setRotateGesturesEnabled(false);// 禁止地图旋转手势
		aMap.getUiSettings().setTiltGesturesEnabled(false);// 禁止倾斜手势
		aMap.setOnMarkerClickListener(markerclicklistener);// 点击地图标记事件
		aMap.setInfoWindowAdapter(InfoWindowAdapter);// 弹出框适配
		aMap.setOnInfoWindowClickListener(OnInfoWindowClickListener);// 设置点击infoWindow事件监听器
		aMap.setOnMapClickListener(OnMapClickListener);// 触摸地图表面事件
		aMap.moveCamera(CameraUpdateFactory.zoomTo(14));// 默认放大到500米
	}

	@Override
	public void bindData() {
		super.bindData();
		mPresenter = new SignInTrackPersenter(this);
		Calendar calendar = Calendar.getInstance();
		if (getIntent() != null) resetMonth(calendar);

		mDayPickerUtil = new LocationDayPickerUtil(this, calendar, this);
		currentDate = mDayPickerUtil.getCalendarToYears(calendar);
		mTvDate.setText(mDayPickerUtil.getCalendarToTextDay(calendar));

		currentUserId = getUserId();
		AddressBook addressBook = CoreZygote.getAddressBookServices().queryUserInfo(currentUserId);
		if (addressBook != null) mTvPerson.setText(addressBook.name);
		mPresenter.requesPersonData(currentUserId, currentDate);
	}

	private String getUserId() {
		return (getIntent() != null && !TextUtils.isEmpty(getIntent().getStringExtra(USER_ID)))
				? getIntent().getStringExtra(USER_ID)
				: CoreZygote.getLoginUserServices().getUserId();
	}

	private void resetMonth(Calendar mCalendar) {
		if (TextUtils.isEmpty(getIntent().getStringExtra(LOCATION_DAY))) return;
		mCalendar.setTime(mPresenter.textToDate(getIntent().getStringExtra(LOCATION_DAY)));
	}

	@Override
	public void bindListener() {
		super.bindListener();
		mTvDate.setOnClickListener(v -> mDayPickerUtil.showMonPicker(currentDate));
		mTvPerson.setOnClickListener(v -> {
			if (mPersonDialog == null)
				mPresenter.requestPerson(currentDate);
			else
				mPersonDialog.setSelectedId(currentUserId).show(getSupportFragmentManager(), "locationPerson");
		});
	}

	@Override
	public void resultData(PolylineOptions polyline) {
		if (aMap != null) aMap.addPolyline(polyline);
	}

	@Override
	public void setAMapMoveCamera(CameraUpdate update) {
		if (aMap != null) aMap.moveCamera(update);
	}

	@Override
	public void setAMapAddMarker(MarkerOptions markeroptions) {
		if (aMap != null) aMap.addMarker(markeroptions);
	}


	public void setAMapClear() {// 清空标记
		if (aMap != null) aMap.clear();
	}

	private final AMap.OnInfoWindowClickListener OnInfoWindowClickListener = marker -> {
		if (marker.isInfoWindowShown()) marker.hideInfoWindow();
	};

	private final OnClickListener photolistener = (arg0) -> {
		if (!TextUtils.isEmpty(mPresenter.getUserPhoto())) {
			final Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + mPresenter.getUserPhoto()));
			SignInTrackActivity.this.startActivity(intent);
		}
	};

	//在地图表面的点击事件顺序必须是： 1、关闭listview 2、关闭标记提示框
	private final AMap.OnMapClickListener OnMapClickListener = new OnMapClickListener() {

		@Override
		public void onMapClick(LatLng arg0) {
			if (selectedMarker.isInfoWindowShown()) selectedMarker.hideInfoWindow();
		}
	};

	private final AMap.OnMarkerClickListener markerclicklistener = new AMap.OnMarkerClickListener() {

		@Override
		public boolean onMarkerClick(Marker marker) {
			selectedMarker = marker;
			if (marker.isInfoWindowShown()) marker.hideInfoWindow();
			return false;
		}
	};

	private final AMap.InfoWindowAdapter InfoWindowAdapter = new AMap.InfoWindowAdapter() {
		@Override
		public View getInfoWindow(Marker marker) {
			return setInfoWindowView(marker);
		}

		@Override
		public View getInfoContents(Marker arg0) {
			return null;
		}
	};

	@SuppressLint("InflateParams")
	private View setInfoWindowView(Marker marker) {
		final View view = LayoutInflater.from(SignInTrackActivity.this).inflate(R.layout.location_locus_marker_layout, null);
		final TextView useraddress = view.findViewById(R.id.useraddress);
		final RelativeLayout userPhotoLayout = view.findViewById(R.id.userPhotoLayout);
		useraddress.setWidth(PixelUtil.dipToPx(180));
		if (!TextUtils.isEmpty(marker.getTitle())) useraddress.setText(marker.getTitle());
		if (!TextUtils.isEmpty(mPresenter.getUserName())) {
			((TextView) view.findViewById(R.id.username)).setText(mPresenter.getUserName());
		}
		if (!TextUtils.isEmpty(mPresenter.getUserPhoto())) {
			userPhotoLayout.setVisibility(View.VISIBLE);
			((TextView) view.findViewById(R.id.userphoto)).setText(mPresenter.getUserPhoto());
			view.findViewById(R.id.photoimage).setOnClickListener(photolistener);
		}
		else userPhotoLayout.setVisibility(View.GONE);
		if (!TextUtils.isEmpty(marker.getSnippet())) {
			((TextView) view.findViewById(R.id.usertimes)).setText(marker.getSnippet());
		}
		return view;
	}

	@Override
	protected void onResume() {
		super.onResume();
		FEUmengCfg.onActivityResumeUMeng(this, FEUmengCfg.LocationLocus);
	}

	@Override
	protected void onPause() {
		super.onPause();
		FEUmengCfg.onActivityPauseUMeng(this, FEUmengCfg.LocationLocus);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mPresenter.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void resultPerson(List<LocusPersonLists> personLists) {//兼容7.0以下领导选人
		if (this.isFinishing()) return;
		mPersonDialog = new SignInSelectedDialog()
				.setSelectedId(currentUserId)
				.setAdapter(new LocationPersonAdapter(this, personLists))
				.setListener(this);
		mPersonDialog.show(getSupportFragmentManager(), "loationPerson");
	}

	@Override
	public void dateDayPicker(String day) {
		currentDate = day;
		mTvDate.setText(mDayPickerUtil.getCalendarToTextDay(day));
		mPresenter.requesPersonData(currentUserId, currentDate);
	}

	@Override
	public void onSelectedClickeItem(String id, int position) {
		currentUserId = id;
		AddressBook addressBook = CoreZygote.getAddressBookServices().queryUserInfo(id);
		if (addressBook != null) mTvPerson.setText(addressBook.name);
		mPresenter.requesPersonData(currentUserId, currentDate);
		if (mPersonDialog != null) mPersonDialog.dismiss();
	}
}
