package cn.flyrise.feep;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.StrictMode;
import android.os.StrictMode.VmPolicy.Builder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import cn.flyrise.android.library.utility.LoadingHint;
import cn.flyrise.android.shared.model.user.UserInfo;
import cn.flyrise.feep.auth.unknown.NewLoginActivity;
import cn.flyrise.feep.commonality.manager.XunFeiManager;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.GsonUtil;
import cn.flyrise.feep.core.common.utils.PreferencesUtils;
import cn.flyrise.feep.core.common.utils.SpUtil;
import cn.flyrise.feep.core.common.utils.UIUtil;
import cn.flyrise.feep.core.function.FunctionManager;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.notification.NotificationMessage;
import cn.flyrise.feep.core.services.model.UserKickPrompt;
import cn.flyrise.feep.main.modules.PreDefinedModuleRepository;
import cn.flyrise.feep.media.MediaModuleRouteTable;
import cn.flyrise.feep.protocol.BaseApplication;
import cn.flyrise.feep.protocol.FeepAddressBookServices;
import cn.flyrise.feep.protocol.FeepApplicationServices;
import cn.flyrise.feep.protocol.FeepPathServices;
import cn.flyrise.feep.utils.MainModuleRouteTable;
import cn.squirtlez.frouter.FRouter;
import com.alibaba.sdk.android.feedback.impl.FeedbackAPI;
import com.facebook.stetho.Stetho;
import com.hyphenate.chatui.model.EmNotifierBean;
import com.hyphenate.chatui.utils.FeepPushManager;
import com.hyphenate.chatui.utils.HyphenateModuleRouteTable;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.tencent.bugly.crashreport.CrashReport;
import com.tencent.bugly.crashreport.CrashReport.CrashHandleCallback;
import com.tencent.bugly.crashreport.CrashReport.UserStrategy;
import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.WebView;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Refactor by ZYP in 2017-02-25
 */
public class FEApplication extends BaseApplication implements Thread.UncaughtExceptionHandler {

	public static NotificationMessage sNotificationMessage; // 保存推送的消息
	private String[] commonWords;
	//	public boolean hasDownloadManager = false;          // 服务器是否支持下载模块
//	public boolean isSupportFileEncrypt = false;        // 客户端是否支持加密
	public boolean isModify = false;                    // 是否允许修改正文
	public boolean isOnSite = false;                   // 是否支持现场签到
	private UserInfo userInfo;
	private boolean isGroupVersion;
	private int cornerNum = 0;                          // 角标数量

	public int getCornerNum() {
		return cornerNum;
	}

	public void setCornerNum(int cornerNum) {
		this.cornerNum = cornerNum;
		if ("xiaomi".equalsIgnoreCase(Build.MANUFACTURER)) {
			SpUtil.put("notification_badge", cornerNum);
		}
	}

	private boolean hasNewVersion = false; //app是否有新的版本

	private static EmNotifierBean emNotifierBean;

	public static EmNotifierBean getEmNotifierBean() {
		return emNotifierBean;
	}

	public static void setEmNotifierBean(EmNotifierBean emNotifierBean) {
		FEApplication.emNotifierBean = emNotifierBean;
	}

	public boolean hasNewVersion() {
		return hasNewVersion;
	}

	public void setNewVersion(boolean hasNewVersion) {
		this.hasNewVersion = hasNewVersion;
	}

	public boolean isGroupVersion() {
		return isGroupVersion;
	}

	public void setGroupVersion(boolean groupVersion) {
		isGroupVersion = groupVersion;
	}

	public UserInfo getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(UserInfo userInfo) {
		this.userInfo = userInfo;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		if (canQuickStart(this)) {
			return;
		}
		CoreZygote.init(this);

		Thread.setDefaultUncaughtExceptionHandler(this);

//        LeakCanary.install(this);

		UIUtil.updatePopupMenuItemLayout(R.layout.item_popup_menu_item_layout);
		Stetho.initialize(Stetho.newInitializerBuilder(this)
				.enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
				.build());

		XunFeiManager.init(getApplicationContext());// 讯飞语音
		FeepPushManager.init(this);//推送初始化
//		new EyeKeyCheckManager().initFace(getApplicationContext());//人脸识别
//		FeedbackAPI.init(this, "23634684", "c2a0a50e062f8f9108e9fedc2f54ea86");//阿里巴巴意见反馈
		FlowManager.init(getApplicationContext());
		FunctionManager.getInstance().init(this, new PreDefinedModuleRepository(this));//初始化所有模块

		CoreZygote.addPathServices(new FeepPathServices());
		FeepApplicationServices activityManagerServices = new FeepApplicationServices();
		registerActivityLifecycleCallbacks(activityManagerServices);
		CoreZygote.addApplicationServices(activityManagerServices);
		CoreZygote.addAddressBookServices(new FeepAddressBookServices());

		UserStrategy strategy = new UserStrategy(this);
		strategy.setCrashHandleCallback(new CrashHandleCallback() {
			public Map<String, String> onCrashHandleStart(int crashType, String errorType, String errorMessage, String errorStack) {
				LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
				String x5CrashInfo = WebView.getCrashExtraMessage(getApplicationContext());
				map.put("x5crashInfo", x5CrashInfo);
				return map;
			}

			@Override
			public byte[] onCrashHandleStart2GetExtraDatas(int crashType, String errorType, String errorMessage, String errorStack) {
				try {
					return "Extra data.".getBytes("UTF-8");
				} catch (Exception e) {
					return null;
				}
			}
		});

//		if (!BuildConfig.DEBUG){
//			CrashReport.initCrashReport(getApplicationContext(), "900026272", true, strategy);
//		}
		FEHttpClient.addNetworkExceptionHandler(this::networkExceptionHandler);
		QbSdk.initX5Environment(this, new QbSdk.PreInitCallback() {
			@Override
			public void onViewInitFinished(boolean arg0) {
				//x5內核初始化完成的回调，为true表示x5内核加载成功
				//否则表示x5内核加载失败，会自动切换到系统内核。
				Log.d("x5初始化", " onViewInitFinished is " + arg0);
			}

			@Override
			public void onCoreInitFinished() {
				Log.d("x5初始化", " onCoreInitFinished ");
			}
		});
		QbSdk.setDownloadWithoutWifi(true);
		FRouter.register(new MainModuleRouteTable(), new HyphenateModuleRouteTable(), new MediaModuleRouteTable());
		FRouter.addRouteNotFoundCallback(routeInfo -> FEToast.showMessage(getString(R.string.not_open_Interface)));

		Builder builder = new Builder();
		StrictMode.setVmPolicy(builder.build());
		if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR2) {
			builder.detectFileUriExposure(); //文件uri暴露
		}
	}


	public String[] getCommonWords() {
		return commonWords;
	}

	public void setCommonWords(String[] commonWords) {
		this.commonWords = commonWords;
	}

	private void networkExceptionHandler(boolean reLogin, boolean isLoadLogout, String errorMessage) {
		LoadingHint.hide();
		if (!reLogin) {
			if (!TextUtils.isEmpty(errorMessage) && !errorMessage.contains("500")) {
				FEToast.showMessage(errorMessage);
			}
			return;
		}
		String e = TextUtils.isEmpty(errorMessage) ? CommonUtil.getString(R.string.message_please_login_again) : errorMessage;
		UserKickPrompt ukp = new UserKickPrompt(e, true);
		SpUtil.put(PreferencesUtils.USER_KICK_PROMPT, GsonUtil.getInstance().toJson(ukp));
		CoreZygote.getApplicationServices().reLoginApplication(isLoadLogout);
	}


	@Override
	public void uncaughtException(Thread t, Throwable e) {
		try {
			e.printStackTrace();
			FELog.writeSdCard(e);
			Intent intent = new Intent(getApplicationContext(), NewLoginActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			PendingIntent restartIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);
			AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
			mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, restartIntent);    // 1秒钟后重启应用
		} catch (Exception exp) {

		}

		CoreZygote.getApplicationServices().exitApplication();
		android.os.Process.killProcess(android.os.Process.myPid());
		System.exit(1);
		System.gc();
	}
}
