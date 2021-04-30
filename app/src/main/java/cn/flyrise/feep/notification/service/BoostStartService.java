/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2013-9-29 上午10:37:36
 */
package cn.flyrise.feep.notification.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.text.TextUtils;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.services.ILoginUserServices;
import cn.flyrise.feep.location.service.LocationService;
import com.hyphenate.chatui.utils.FeepPushManager;
import java.util.Date;

/**
 * 类功能描述：</br>
 * @author zms
 * @version 1.0 </p> 修改时间：</br> 修改备注：</br>
 */
public class BoostStartService extends Service {

	/**
	 * 十分钟重新注册一次TOKEN
	 */
	private static final long RESTART_DURATION = 10 * 60 * 1000;

	private long lastRestartTime;

	public static void start(Context context){
		final Intent service = new Intent(context, BoostStartService.class);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			context.startForegroundService(service);
		} else {
			context.startService(service);
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		setForeground();
		reStartJPush();
	}

	private void setForeground() {
		Context context = CoreZygote.getContext();
		if (context == null) {
			startForeground(1, new Notification());
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
			startForeground(1, notification);
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// Log.e("dd", "BoostStartService onStartCommand>>>>>>>>>>>>>>>>>>");
		if (new Date().getTime() - lastRestartTime > RESTART_DURATION) {
			reStartJPush();
		}
		return super.onStartCommand(intent, flags, startId);
	}

	private void reStartJPush() {
//        FeepPushManager.setJpushAlias();
		FeepPushManager.getPushToken();
		lastRestartTime = new Date().getTime();
		// 自动上报位置
		ILoginUserServices loginUserServices = CoreZygote.getLoginUserServices();
		if (loginUserServices == null) {
			return;
		}
		if (TextUtils.isEmpty(loginUserServices.getUserId())) {
			return;
		}
		if (!CommonUtil.serviceIsWorking(this, LocationService.class)) {
			LocationService.startLocationService(this, LocationService.REQUESTCODE);
		}
	}
}
