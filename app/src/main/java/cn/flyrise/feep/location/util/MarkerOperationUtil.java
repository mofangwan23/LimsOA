package cn.flyrise.feep.location.util;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.location.bean.SignInSetAMapStyle;
import com.amap.PoiOverlay;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.core.PoiItem;
import java.util.ArrayList;
import java.util.List;

/**
 * 新建：陈冕;
 * 日期： 2017-8-22-10:07.
 */

public class MarkerOperationUtil {

	private AMap mAMap;
	private Context mContext;
	private SensorEventHelper mSensorHelper; //根据手机方向旋转当前坐标
	private Handler mHandler = new Handler();
	private int animationTime = 100;

	public MarkerOperationUtil(Context context, AMap aMap) {
		this.mAMap = aMap;
		this.mContext = context;
		mSensorHelper = new SensorEventHelper(mContext);
		mSensorHelper.registerSensorListener();
	}

	//设置为考勤点显示模式
	public void moveSignLocationLatlng(SignInSetAMapStyle style) {
		moveSignLocationLatlng(style.latLng, style.saveLatLng, style.signRange, style.isDottedLine, style.isMoveMap);
	}

	//设置为考勤点显示模式
	private void moveSignLocationLatlng(LatLng latlng, LatLng signLatlng, int signRange, boolean isDottedLine, boolean isMoveMap) {
		if (mAMap != null) mAMap.clear();
		if (signLatlng != null) {
			signPositionMarker(signLatlng);
			addAMapCircle(signLatlng, signRange);
			if (isDottedLine) addAMapLine(getLatLngs(latlng, signLatlng));//当前坐标距离考勤点50米以上才显示路线
			if (isMoveMap) mHandler.postDelayed(() -> addZoomTo(signRange), animationTime);
		}
		try {
			userCurrentPositionMarker(latlng, isMoveMap);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void showPoiItemMarkers(LatLng currentLatlng, List<PoiItem> poiItems, boolean isMoveMap) {  //显示考勤列表模式
		if (mAMap == null) return;
		mAMap.clear();
		if (!CommonUtil.isEmptyList(poiItems)) {
			final PoiOverlay poiOverlay = new PoiOverlay(mAMap, poiItems);
			poiOverlay.removeFromMap();
			poiOverlay.addToMap();
		}
		if (isMoveMap)
			mHandler.postDelayed(() -> addZoomTo(getDefultZoomTo(currentLatlng, poiItems)), animationTime);
		if (currentLatlng != null) userCurrentPositionMarker(currentLatlng, isMoveMap);
	}

	private int getDefultZoomTo(LatLng currentLatlng, List<PoiItem> poiItems) {
		if (CommonUtil.isEmptyList(poiItems)) return 500;
		float zoom = AMapUtils.calculateLineDistance(currentLatlng,
				new LatLng(poiItems.get(0).getLatLonPoint().getLatitude(), poiItems.get(0).getLatLonPoint().getLongitude()));
		if (zoom <= 50) return 50;
		else if (zoom > 50 && zoom <= 100) return 100;
		else if (zoom >= 1500 * 1000) return 1500 * 1000;
		else return (int) zoom;
	}

	private List<LatLng> getLatLngs(LatLng latlng, LatLng signLatlng) {
		List<LatLng> latLngs = new ArrayList<>();
		latLngs.add(latlng);
		latLngs.add(signLatlng);
		return latLngs;
	}

	private void userCurrentPositionMarker(LatLng latlng, boolean isMoveMap) { //标记用户当前位置的图标
		if (mAMap == null || latlng == null) return;
		if (mSensorHelper != null) mSensorHelper.setCurrentMarker(getMarker(latlng));//定位图标旋转
		if (isMoveMap) moveToLocation(latlng);
	}

	private Marker getMarker(LatLng latlng) {
		final MarkerOptions markerOptions = new MarkerOptions();
		markerOptions.position(latlng);
		markerOptions.icon(BitmapDescriptorFactory
				.fromBitmap(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.icon_mylocation)));
		markerOptions.setFlat(true);
		markerOptions.anchor(0.5f, 0.5f);
		return mAMap.addMarker(markerOptions);
	}

	public void moveToLocation(LatLng latLng) {//移动到当前位置
		if (mAMap == null) return;
		mHandler.postDelayed(() -> mAMap.animateCamera(CameraUpdateFactory.changeLatLng(latLng)), animationTime);
	}

	private void signPositionMarker(LatLng latlng) { //标记考勤地点
		if (mAMap == null) return;
		mAMap.clear();
		MarkerOptions markerOptions = new MarkerOptions();
		markerOptions.position(latlng);
		markerOptions.icon(BitmapDescriptorFactory
				.fromBitmap(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.icon_attendancepoint)));
		markerOptions.setFlat(true);
		markerOptions.anchor(0.5f, 0.8f);
		mAMap.addMarker(markerOptions);
	}

	//根据坐标和半径画圆形考勤范围
	private void addAMapCircle(LatLng latlng, int range) {
		if (mAMap == null) return;
		mAMap.addCircle(new CircleOptions()
				.center(latlng)
				.radius(range)
				.fillColor(Color.parseColor("#3003a9f4"))
				.strokeColor(Color.parseColor("#6003a9f4"))
				.strokeWidth(2));
	}

	private void addAMapLine(List<LatLng> latLngs) {//连接多个坐标的虚线
		if (mAMap == null) return;
		mAMap.addPolyline(new PolylineOptions().
				addAll(latLngs)
				.setDottedLine(true)
				.width(6)
				.color(Color.parseColor("#03a9f4")));
	}

	private void addZoomTo(int range) {//根据签到半径，调整地图显示比例
		if (mAMap == null) return;
		final int K = 1000;
		int zoomTo = 16;
		if (range <= 50) {
			zoomTo = 19;//10米
		}
		else if (range > 50 && range <= 150) {
			zoomTo = 18; //25米
		}
		else if (range > 150 && range <= 300) {
			zoomTo = 16; //100米
		}
		else if (range > 300 && range <= 800) {
			zoomTo = 15; //200米
		}
		else if (range > 800 && range <= 1400) {
			zoomTo = 14; //500米
		}
		else if (range > 1400 && range <= 2 * K) {
			zoomTo = 13; //1千米
		}
		else if (range > 2 * K && range <= 6 * K) {
			zoomTo = 12; //2千米
		}
		else if (range > 6 * K && range <= 10 * K) {
			zoomTo = 11; //5千米
		}
		else if (range > 10 * K && range <= 30 * K) {
			zoomTo = 10;//10千米
		}
		else if (range > 30 * K && range <= 60 * K) {
			zoomTo = 9;//20千米
		}
		else if (range > 60 * K && range <= 90 * K) {
			zoomTo = 8;//30千米
		}
		else if (range > 90 * K && range <= 200 * K) {
			zoomTo = 7;//50千米
		}
		else if (range > 200 * K && range <= 500 * K) {
			zoomTo = 6;//100千米
		}
		else if (range > 500 * K && range <= 800 * K) {
			zoomTo = 5;//200千米
		}
		else if (range > 800 * K && range <= 1500 * K) {
			zoomTo = 4;//500千米
		}
		else if (range > 1500 * K) {
			zoomTo = 3;//1000千米
		}
		mAMap.moveCamera(CameraUpdateFactory.zoomTo(zoomTo));
	}

	public void onResume() {
		if (mSensorHelper == null) return;
		mSensorHelper.registerSensorListener();
	}

	public void onPause() {
		if (mSensorHelper == null) return;
		mSensorHelper.unRegisterSensorListener();
	}

	public void onDestroy() {
		if (mSensorHelper == null) return;
		mSensorHelper.setCurrentMarker(null);
		mSensorHelper = null;
	}
}
