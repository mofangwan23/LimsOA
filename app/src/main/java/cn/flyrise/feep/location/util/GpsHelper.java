package cn.flyrise.feep.location.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;
import cn.flyrise.feep.K;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.dialog.FEMaterialDialog;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationPurpose;

/**
 * Klc on 2017/3/3.
 */

public class GpsHelper {

	private Context mContext;
	private AMapLocationClient mSingleClient;
	private AMapLocationClient mContinuouClient;
	private long startTime = 0;
	private LocationCallBack mListener;

	public GpsHelper(Context mContext) {
		this.mContext = mContext;
	}

	public GpsHelper(Context context, int locationType, int spaceTime, boolean isRapidlyLocation, LocationCallBack listener) {
		this.mContext = context;
		this.mListener = listener;
		if (!isOpenGPS()) {
			listener.error();
			return;
		}
		if (isSignGpsLocation(locationType) || isRapidlyLocation) getSingleLocation(mListener);
		else getContinuousLocation(spaceTime, mListener);
	}

	public void restartGpsLocation(int type, boolean isRapidlyLocation) {
		if (isSignGpsLocation(type) || isRapidlyLocation) getSingleLocation(mListener);
		else startContinuouslyLocaation();
	}

	private boolean isSignGpsLocation(int type) {
		return type == K.location.LOCATION_DETAIL
				|| type == K.location.LOCATION_CUSTOM_SEARCH
				|| type == K.location.LOCATION_CUSTOM_SETTING
				|| type == K.location.LOCATION_SEND
				|| type == K.location.LOCATION_SEARCH;
	}

	private boolean isOpenGPS() {
		boolean isOpen;
		final LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
		if (locationManager == null) return false;
		isOpen = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		return isOpen;
	}

	//单次定位
	public void getSingleLocation(LocationCallBack locationListener) {
		FELog.d("自动定位", "-->>>>定位sign开始");
		if (mSingleClient == null) mSingleClient = new AMapLocationClient(mContext);
		mSingleClient.setLocationOption(new AMapLocationClientOption()
				.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy)
				.setLocationPurpose(AMapLocationPurpose.SignIn)
				.setLocationCacheEnable(false)
				.setNeedAddress(true)
				.setMockEnable(false));
		mSingleClient.setLocationListener(aMapLocation -> {
			if (aMapLocation != null && aMapLocation.getErrorCode() == 0) {
				FELog.d("自动定位", "-->>>>定位sign成功：" + aMapLocation.getLocationType());
				locationListener.success(aMapLocation);
				stopSingleLocationRequest();
			}
			else {
				locationListener.error();
				stopSingleLocationRequest();
			}
		});
		mSingleClient.stopLocation();
		mSingleClient.startLocation();
	}

	public void stopSingleLocationRequest() {
		if (mSingleClient != null) {
			mSingleClient.stopLocation();
			mSingleClient.onDestroy();
			mSingleClient = null;
		}
	}

	//多次定位
	public void getContinuousLocation(long spaceTime, LocationCallBack locationListener) {
		if (mContinuouClient == null) mContinuouClient = new AMapLocationClient(mContext);
		mContinuouClient.setLocationOption(new AMapLocationClientOption()
				.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy)
				.setNeedAddress(true)
				.setInterval(spaceTime)
				.setLocationCacheEnable(false)
				.setMockEnable(false));
		mContinuouClient.setLocationListener(aMapLocation -> {
			if (aMapLocation == null) return;
			FELog.d("自动定位", "-->>>>定位类型:" + aMapLocation.getLocationType() + "--连续定位时间：" + spaceTime);
			if (aMapLocation.getLocationType() == 4//缓存定位排除掉
					|| aMapLocation.getLocationType() == 2//前一次定位
					|| System.currentTimeMillis() - startTime < 2 * 1000) {//定位异常
				return;
			}
			if (aMapLocation.getErrorCode() == 0) {
				startTime = System.currentTimeMillis();
				locationListener.success(aMapLocation);
			}
			else {
				locationListener.error();
			}
		});
		mContinuouClient.startLocation();
	}

	public void stopContinuouslyLocationRequest() {
		startTime = 0;
		if (mContinuouClient != null) {
			FELog.d("自动定位", "-->>>>定位停止");
			mContinuouClient.stopLocation();
			mContinuouClient.onDestroy();
		}
	}

	private void startContinuouslyLocaation() {
		startTime = 0;
		if (mContinuouClient != null) {
			FELog.d("自动定位", "-->>>>定位开始");
			mContinuouClient.startLocation();
		}
	}

	public void stopContinuouslyLocation() {
		if (mContinuouClient != null) {
			FELog.d("自动定位", "-->>>>定位停止");
			mContinuouClient.stopLocation();
		}
	}

	public void destroyContinuouslyLocation() {
		if (mContinuouClient != null) {
			FELog.d("自动定位", "-->>>>定位销毁");
			mContinuouClient.onDestroy();
		}
	}

	public interface LocationCallBack {

		void success(AMapLocation location);

		void error();
	}

	public static boolean inspectMockLocation(Context context) {
		if (!isMockLocation(context)) return false;
		new FEMaterialDialog.Builder(context)
				.setMessage(context.getResources().getString(R.string.location_mock_finish))
				.setPositiveButton(context.getResources().getString(R.string.lockpattern_setting_button_text), v -> {
					Intent intent = new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
					context.startActivity(intent);
				})
				.setNegativeButton(context.getResources().getString(R.string.cancel), v -> ((Activity) context).finish()).build().show();
		return true;
	}

	private static boolean isMockLocation(Context context) {
		return Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION, 0) != 0;
	}
}
