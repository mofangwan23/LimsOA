package cn.flyrise.feep.location.service;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import cn.flyrise.android.protocol.entity.LocationLocusResponse;
import cn.flyrise.android.protocol.entity.location.LocationRequest;
import cn.flyrise.android.shared.bean.UserBean;
import cn.flyrise.android.shared.utility.FEDate;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.X;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.PreferencesUtils;
import cn.flyrise.feep.core.common.utils.SpUtil;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.premission.FePermissions;
import cn.flyrise.feep.dbmodul.utils.UserInfoTableUtils;
import cn.flyrise.feep.location.LocationPermissionActivity;
import cn.flyrise.feep.location.bean.AutoUploadSetting;
import cn.flyrise.feep.location.bean.LocationWorkingTimes;
import cn.flyrise.feep.location.bean.LocusDataProvider;
import cn.flyrise.feep.location.util.GpsHelper;
import com.amap.api.location.AMapLocation;


/**
 * Created by Klc on 2017/3/3.
 */

public class LocationService extends Service {

	public static final int REQUESTCODE = 102;
	public static final int SETTASKCODE = 103;

	private AutoUploadSetting mSetting;

	private AlarmManager mAlarmManager;
	private PendingIntent mStartIntent;
	private GpsHelper mGpsHelper;

	private long endTime;

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}


	public static void startLocationService(Context context, int code) {
		Intent intent = new Intent(context, LocationService.class);
		intent.putExtra("actionCode", code);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			context.startForegroundService(intent);
		} else {
			context.startService(intent);
		}
	}

	public static void stopLocationService(Context context) {
		Intent intent = new Intent(context, LocationService.class);
		context.stopService(intent);
	}


	@Override
	public void onCreate() {
		super.onCreate();
		setForeground();
		mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		Intent intent = new Intent(this, LocationService.class);
		mStartIntent = PendingIntent.getService(this, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		if (mGpsHelper == null) {
			mGpsHelper = new GpsHelper(getApplicationContext());
		}
	}

	private void setForeground() {
		Context context = CoreZygote.getContext();
		if (context == null) {
			startForeground(3, new Notification());
			return;
		}
		//适配8.0service
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		NotificationChannel mChannel;
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
			mChannel = new NotificationChannel("service_01", getString(R.string.app_name),
					NotificationManager.IMPORTANCE_LOW);
			notificationManager.createNotificationChannel(mChannel);
			Notification notification = new Notification.Builder(getApplicationContext(), "service_01").build();
			startForeground(3, notification);
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			int actionCode = intent.getIntExtra("actionCode", -1);
			if (actionCode == REQUESTCODE) {
				requestTime();
			}
			else {
				setTask(mSetting);
			}
		}
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mGpsHelper != null) {
			mGpsHelper.stopContinuouslyLocationRequest();
		}
		mAlarmManager.cancel(mStartIntent);
	}

	GpsHelper.LocationCallBack locationCallBack = new GpsHelper.LocationCallBack() {
		@Override
		public void success(AMapLocation location) {
			if (System.currentTimeMillis() > endTime) {
				stopLocationService();
				return;
			}
			uploadLocation(location);// 上报数据
		}

		@Override
		public void error() {
			if (System.currentTimeMillis() > endTime) {
				stopLocationService();
			}
		}
	};

	private void uploadLocation(AMapLocation location) {
		if (getHttpClient() == null || location == null) {
			return;
		}
		String userID = SpUtil.get(PreferencesUtils.USER_ID, "");
		if (!TextUtils.isEmpty(userID)) {
			final LocationRequest locationRequest = new LocationRequest();
			locationRequest.setAddress(location.getAddress());
			locationRequest.setSendType("3");// 3代表是后台自动上报位置的类型
			locationRequest.setLatitude(location.getLatitude() + "");
			locationRequest.setLongitude(location.getLongitude() + "");
			locationRequest.setName(location.getStreet());
			locationRequest.setAccessToken(CoreZygote.getDevicesToken());
			locationRequest.setUserId(userID);
			FEHttpClient.getInstance().post(locationRequest, null);
		}
	}


	public void requestTime() {
		boolean isReport = SpUtil.get(PreferencesUtils.LOCATION_LOCUS_IS_REPORT, true);
		String userID = SpUtil.get(PreferencesUtils.USER_ID, "");
		if (!isReport || TextUtils.isEmpty(userID)) {
			stopLocationService();
			return;
		}
		if (getHttpClient() == null) {
			return;
		}
		final LocusDataProvider dataProvider = new LocusDataProvider(this);
		dataProvider.setResponseListener(responseListener);
		dataProvider.requestWorkingTime();
	}

	/**
	 * 请求数据完毕后的监听器
	 */
	private final LocusDataProvider.OnLocationResponseListener responseListener = new LocusDataProvider.OnLocationResponseListener() {
		@Override
		public void onSuccess(LocationLocusResponse responses, String locationType) {
			//判断是否需要做自动定位上报服务。
			if (!TextUtils.equals(locationType, X.LocationType.WorkingTime)
					|| CommonUtil.isEmptyList(responses.getWorkingTimes())) {
				stopLocationService();
				return;
			}
			LocationWorkingTimes workTimes = responses.getWorkingTimes().get(0);
			if (TextUtils.isEmpty(workTimes.getEachTime())
					|| "0".equals(workTimes.getEachTime())
					|| TextUtils.isEmpty(workTimes.getBeTime())) {
				stopLocationService();
				return;
			}
			final String[] dates = workTimes.getBeTime().split(",");// 打卡时间
			if (dates.length == 0) {
				stopLocationService();
				return;
			}
			AutoUploadSetting netUploadTime = new AutoUploadSetting();
			netUploadTime.duration = Integer.valueOf(workTimes.getEachTime()) * 60000;
			netUploadTime.serviceTime = FEDate.getDateSS(workTimes.getServiceTime()).getTime();
			netUploadTime.startTime = FEDate.getDateSS(dates[0]).getTime();
			netUploadTime.endTime = FEDate.getDateSS(dates[1]).getTime();
			netUploadTime.requestTime = System.currentTimeMillis();

			if (netUploadTime.serviceTime > netUploadTime.endTime) {
				stopLocationService();
				return;
			}

			if (mSetting != null
					&& mSetting.duration == netUploadTime.duration
					&& mSetting.startTime == netUploadTime.startTime
					&& mSetting.endTime == netUploadTime.endTime) {
				return;
			}
			setTask(mSetting = netUploadTime);
		}

		@Override public void onFailed(Throwable error, String content) { }
	};

	private void setTask(AutoUploadSetting uploadTime) {
		if (uploadTime == null) {
			stopSelf();
			return;
		}

		if (!FePermissions.checkSelfPermission(LocationService.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
			new Handler(Looper.getMainLooper()).postDelayed(() -> {
				Intent intent = new Intent(LocationService.this, LocationPermissionActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}, 5000);
			return;
		}
		if (mGpsHelper != null) {
			mGpsHelper.stopContinuouslyLocationRequest();
		}

		mAlarmManager.cancel(mStartIntent);

		long actualStartTime = uploadTime.startTime < uploadTime.serviceTime ?
				System.currentTimeMillis() :
				uploadTime.requestTime + (uploadTime.startTime - uploadTime.serviceTime);

		//一切条件都符合了，可以开始做自动位置上报了。
		endTime = uploadTime.endTime - uploadTime.serviceTime + uploadTime.requestTime;

		if (System.currentTimeMillis() >= actualStartTime) {
			mGpsHelper.getContinuousLocation(mSetting.duration, locationCallBack);
		}
		else {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
				mAlarmManager.setExact(AlarmManager.RTC_WAKEUP, actualStartTime, mStartIntent);
			}
			else {
				mAlarmManager.set(AlarmManager.RTC_WAKEUP, actualStartTime, mStartIntent);
			}
		}
	}

	private void stopLocationService() {
		stopSelf();
	}

	private FEHttpClient getHttpClient() {
		try {
			return FEHttpClient.getInstance();
		} catch (Exception e) {
			UserBean userBean = UserInfoTableUtils.find();
			String serverAddress = userBean.getServerAddress();
			String serverPort = userBean.getServerPort();
			boolean isHttps = userBean.isHttps();
			new FEHttpClient.Builder(getApplication())
					.address(serverAddress)
					.port(serverPort)
					.isHttps(isHttps)
					.keyStore(CoreZygote.getPathServices().getKeyStoreFile())
					.build();
		}
		return FEHttpClient.getInstance();
	}
}
