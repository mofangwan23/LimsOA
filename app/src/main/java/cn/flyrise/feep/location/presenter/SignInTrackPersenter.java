package cn.flyrise.feep.location.presenter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import cn.flyrise.android.library.utility.LoadingHint;
import cn.flyrise.android.protocol.entity.LocationLocusResponse;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.location.bean.LocationLists;
import cn.flyrise.feep.location.bean.LocusPersonLists;
import cn.flyrise.feep.location.bean.SignInFieldPersonnel;
import cn.flyrise.feep.location.contract.SignInTrackContract;
import cn.flyrise.feep.location.contract.SignInTrackContract.View;
import cn.flyrise.feep.location.model.SignInStatisModel;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolylineOptions;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 新建：陈冕;
 * 日期： 2018-1-30-17:22.
 */

public class SignInTrackPersenter implements SignInTrackContract.presenter {

	private String userName;
	private String userPhoto;
	private Marker selectedMarker;  // 绘制地图的标记
	private SignInStatisModel mModel;

	//记录用户当前位置的坐标，用于当考勤轨迹为空的时候定位到用户当前位置,默认是珠海飞企地址
	private LatLng userLocationAddressLatlng = new LatLng(22.371993, 113.574035);

	private SignInTrackContract.View mView;
	private Context mContext;

	public SignInTrackPersenter(Context context) {
		this.mContext = context;
		mView = (View) context;
		mModel = new SignInStatisModel();
	}


	private void latLngsEmpty() {
		moveCurrentPosition();
		FEToast.showMessage(mContext.getResources().getString(R.string.location_null));
	}

	@Override
	public void requestPerson(String days) {
		LoadingHint.show(mContext);
//		mModel.requestPerson()
//				.subscribeOn(Schedulers.io())
//				.observeOn(AndroidSchedulers.mainThread())
//				.subscribe(data -> {
//							LoadingHint.hide();
//							mView.resultPerson(data);
//						}
//						, error -> {
//							LoadingHint.hide();
//							FEToast.showMessage("下属人员请求失败");
//						});

		mModel.requestLeaderDay(days)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(data -> {
							LoadingHint.hide();
							mView.resultPerson(fieldPersonToLocusPerson(data.outWork));
						}
						, error -> {
							LoadingHint.hide();
							FEToast.showMessage("外勤人员请求失败");
						});
	}

	private List<LocusPersonLists> fieldPersonToLocusPerson(List<SignInFieldPersonnel> outWorks) {
		if (CommonUtil.isEmptyList(outWorks)) return null;
		List<LocusPersonLists> persons = new ArrayList<>();
		LocusPersonLists person;
		for (SignInFieldPersonnel work : outWorks) {
			if (work == null) continue;
			person = new LocusPersonLists();
			person.setUserId(work.userId);
			persons.add(person);
		}
		return persons;
	}

	@Override
	public void requesPersonData(String userId, String day) {
		mModel.requestTrack(userId, day)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(this::parsingLocationListToData
						, error -> {
							latLngsEmpty();
						});
	}

	@Override
	public String getUserPhoto() {
		return userPhoto;
	}

	@Override
	public String getUserName() {
		return userName;
	}

	private void parsingLocationListToData(LocationLocusResponse responses) {
		if (responses.getLocationList() == null) return;
		List<LatLng> latlngs = new ArrayList<>();
		int currentIndex = 0;
		for (LocationLists item : responses.getLocationList()) {
			if (item == null) continue;
			mapMarkers(new LatLng(Double.valueOf(item.getLatitude())
					, Double.valueOf(item.getLongitude())), item.getAddress(), item.getTime(), currentIndex);
			currentIndex++;
			latlngs.add(new LatLng(Double.valueOf(item.getLatitude()), Double.valueOf(item.getLongitude())));
		}
		userName = responses.getUserName();
		userPhoto = responses.getPhone();
		if (CommonUtil.isEmptyList(latlngs)) {
			moveCurrentPosition();
			latLngsEmpty();
		}
		routeMap(latlngs);
	}


	// 多坐标调用（多位置需要绘制路线）
	private void mapMarkers(LatLng latlng, String time, String address, int currentIndex) {
		final MarkerOptions markeroptions = new MarkerOptions();// 用于改变起始标记
		markeroptions.position(latlng);
		markeroptions.setFlat(true);
		markeroptions.title(address);// 标记贴在地上
		markeroptions.snippet(time);
		if (currentIndex == 0) {
			mView.setAMapMoveCamera(CameraUpdateFactory.changeLatLng(latlng));
			markeroptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
		}
		else {// 非起始位置的标记
			markeroptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
		}
		mView.setAMapAddMarker(markeroptions);
		if (selectedMarker != null) selectedMarker.showInfoWindow();// 每次只显示一个提示语
	}

	// 通过坐标完成路线绘制
	private void routeMap(List<LatLng> latlngs) {
		final PolylineOptions polyline = new PolylineOptions();
		polyline.addAll(latlngs);// 需要绘制路线的坐标
		polyline.width(8);// 设置绘制线的宽度
		polyline.color(Color.parseColor("#3f51b5"));// 设置绘制线的颜色
		mView.resultData(polyline);
	}


	private void moveCurrentPosition() {
		mView.setAMapMoveCamera(CameraUpdateFactory.changeLatLng(userLocationAddressLatlng));
	}

	@SuppressLint("SimpleDateFormat")
	public Date textToDate(String yearMonth) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			return sdf.parse(yearMonth);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return new Date();
	}


	@Override
	public void onDestroy() {
	}
}
