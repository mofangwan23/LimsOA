package cn.flyrise.feep.core;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import cn.flyrise.feep.core.common.utils.SpUtil;
import cn.flyrise.feep.core.services.IAddressBookServices;
import cn.flyrise.feep.core.services.IApplicationServices;
import cn.flyrise.feep.core.services.IConvSTService;
import cn.flyrise.feep.core.services.ILoginUserServices;
import cn.flyrise.feep.core.services.IMobileKeyService;
import cn.flyrise.feep.core.services.IPathServices;
import cn.flyrise.feep.core.services.IRsaService;
import java.util.UUID;

/**
 * @author ZYP
 * @since 2017-02-06 11:20
 */
public class CoreZygote {

	public static final String DEVICES_TOKEN = "DEVICE_TOKEN";

	private static Context sContext;    // This context is an Application.
	private static IPathServices sPathServices;
	private static ILoginUserServices sLoginUserServices;
	private static IAddressBookServices sAddressBookServices;
	private static IApplicationServices sActivityManagerServices;
	private static IConvSTService sNotifySettingServices;
	private static IMobileKeyService sMokeyService;
	private static IRsaService sRsaService;
	private static String sUserAgent = "";

	public static void init(Context context) {
		sContext = context;
	}

	public static void addPathServices(IPathServices pathServices) {
		sPathServices = pathServices;
	}

	public static void addLoginUserServices(ILoginUserServices loginUserServices) {
		sLoginUserServices = loginUserServices;
	}

	public static IPathServices getPathServices() {
		return sPathServices;
	}

	public static ILoginUserServices getLoginUserServices() {
		return sLoginUserServices;
	}

	public static void addAddressBookServices(IAddressBookServices addressBookServices) {
		sAddressBookServices = addressBookServices;
	}

	public static IAddressBookServices getAddressBookServices() {
		return sAddressBookServices;
	}

	public static void addApplicationServices(IApplicationServices activityManagerServices) {
		sActivityManagerServices = activityManagerServices;
	}

	public static IApplicationServices getApplicationServices() {
		return sActivityManagerServices;
	}

	public static void addConvSTServices(IConvSTService services) {
		sNotifySettingServices = services;
	}

	public static IConvSTService getConvSTServices() {
		return sNotifySettingServices;
	}

	public static IMobileKeyService getMobileKeyService() {
		return sMokeyService;
	}

	public static void addMobileKeyService(IMobileKeyService sMokeyService) {
		CoreZygote.sMokeyService = sMokeyService;
	}

	public static void addRsaService(IRsaService rsaService) {
		sRsaService = rsaService;
	}

	public static IRsaService getRsaService() {
		return sRsaService;
	}


	public static Context getContext() {
		if (sContext == null) {
			throw new NullPointerException("The context is null, you must call init() method " +
					"in your application onCreate().");
		}
		return sContext;
	}

	public static String getUserAgent() {
		if (sContext == null) return "";
		if (TextUtils.isEmpty(sUserAgent)) {
			sUserAgent = " " + getContext().getPackageName() + "/Android" + "/" + getAppVersionName();
		}
		return sUserAgent;
	}

	private static String getAppVersionName() {
		String versionName = "";
		try {
			PackageManager packageManager = sContext.getPackageManager();
			PackageInfo packageInfo = packageManager.getPackageInfo(sContext.getPackageName(), 0);
			versionName = packageInfo.versionName;
			if (TextUtils.isEmpty(versionName)) {
				return "";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return versionName;
	}

	public static String getDevicesToken() {
		String devicesToken = SpUtil.get(DEVICES_TOKEN, "");
		if (TextUtils.isEmpty(devicesToken)) {
			devicesToken = UUID.randomUUID().toString().replace("-", "");
			SpUtil.put(DEVICES_TOKEN, devicesToken);
		}
		return devicesToken;
	}

	public static void clearDevicesToken() {
		SpUtil.put(DEVICES_TOKEN, "");
	}

}
