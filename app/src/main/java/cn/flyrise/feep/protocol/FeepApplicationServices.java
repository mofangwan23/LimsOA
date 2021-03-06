package cn.flyrise.feep.protocol;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import cn.flyrise.android.protocol.entity.LogoutRequest;
import cn.flyrise.android.shared.bean.UserBean;
import cn.flyrise.feep.FEApplication;
import cn.flyrise.feep.addressbook.processor.AddressBookDownloadServices;
import cn.flyrise.feep.auth.views.ReLoginActivity;
import cn.flyrise.feep.auth.views.fingerprint.FingerprintNewUnLockActivity;
import cn.flyrise.feep.auth.views.gesture.GestureUnLockActivity;
import cn.flyrise.feep.commonality.EditableActivity;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.PreferencesUtils;
import cn.flyrise.feep.core.common.utils.SpUtil;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.services.IApplicationServices;
import cn.flyrise.feep.core.watermark.WMStamp;
import cn.flyrise.feep.dbmodul.utils.UserInfoTableUtils;
import cn.flyrise.feep.fingerprint.FingerprintIdentifier;
import cn.flyrise.feep.location.service.LocationService;
import com.hyphenate.chatui.utils.IMHuanXinHelper;
import com.sangfor.ssl.SangforAuthManager;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ZYP
 * @since 2017-03-01 10:43
 */
public class FeepApplicationServices implements IApplicationServices, Application.ActivityLifecycleCallbacks {

	private final List<Activity> mActivityStacks = new ArrayList<>();
	private int mHomeKeyState;
	private boolean isFinishing;
	public static long sLastTimeUnlockSuccess = 0;

	@Override
	public void setHomeKeyState(int state) {
		this.mHomeKeyState = state;
	}

	@Override
	public int getHomeKeyState() {
		return this.mHomeKeyState;
	}

	private void exitApplication(boolean isLoadLogout) {
		if (CommonUtil.nonEmptyList(mActivityStacks)) {
			isFinishing = true;
			for (Activity activity : mActivityStacks) {
				activity.finish();
			}
			mActivityStacks.clear();
			isFinishing = false;
		}

		WMStamp.getInstance().resetWaterMarkExecutor();
		Context context = CoreZygote.getContext();
		context.stopService(new Intent(context, AddressBookDownloadServices.class));

		FEApplication feApplication = (FEApplication) CoreZygote.getContext();
		feApplication.setUserInfo(null);                                // ??????????????????
		feApplication.setCommonWords(null);
//		if (feApplication.isSupportFileEncrypt) {                       // ????????????????????????
//			try {
//				FileUtil.deleteFiles(CoreZygote.getPathServices().getDownloadDirPath());
//			} catch (Exception exception) {
//				exception.printStackTrace();
//			}
//		}

		try {
			// ?????????????????????????????????????????????????????????????????????????????????
			if (isLoadLogout) {
				FEHttpClient.getInstance().post(new LogoutRequest(), null);     // ?????????????????????
			}
			else {
				IMHuanXinHelper.getInstance().logout(false);       //?????? IM?????????
			}
//			SangforAuth.getInstance().vpnLogout();                          // ?????? VPN ????????????????????????????????????
//			SangforAuth.getInstance().vpnQuit();
			SangforAuthManager.getInstance().vpnLogout();
		} catch (Exception exp) {                                           // ?????????????????????????????????
			FELog.d("????????????????????? ",exp.getMessage());
		}

		//??????????????????
		LocationService.stopLocationService(context);
		FELog.d("FeepApplicationServices--stop");
//		CoreZygote.addLoginUserServices(null);
		CoreZygote.addMobileKeyService(null);
	}

	@Override
	public void exitApplication() {
		exitApplication(true);
	}

	@Override public void reLoginApplication() {
		reLoginApplication(true);
	}

	@Override
	public void reLoginApplication(boolean isLoadLogout) {
		for (Activity activity : mActivityStacks) {
			if (activity instanceof EditableActivity) {
				EditableActivity editable = (EditableActivity) activity;
				editable.saveCache();
			}
		}

		UserBean userBean = UserInfoTableUtils.find();
		userBean.setAutoLogin(false);
		UserInfoTableUtils.insert(userBean);                                // ????????????

//        Class<? extends Activity> reloginActivityClass = null;
//        long startTime = System.currentTimeMillis();
//        boolean isGestureLogin = SpUtil.get(PreferencesUtils.LOGIN_GESTRUE_PASSWORD, false);
//        if (isGestureLogin) {
//            reloginActivityClass = GestureLoginActivity.class;
//        }
//
//        if (reloginActivityClass == null) {
//            boolean isFingerprintLogin = SpUtil.get(PreferencesUtils.FINGERPRINT_IDENTIFIER, false);
//            Activity frontActivity = getFrontActivity();
//            if (frontActivity == null)
//                FELog.e("frontActivity is null");
//            FingerprintIdentifier identifier = new FingerprintIdentifier(frontActivity);
//            if (isFingerprintLogin && identifier.isFingerprintEnable()) {
//                reloginActivityClass = FingerprintLoginActivity.class;
//            }
//        }
//
//        if (reloginActivityClass == null) {
//            reloginActivityClass = LoginActivity.class;
//        }
//
//        FELog.e("restartTime:--->" + (System.currentTimeMillis() - startTime));

		this.exitApplication(isLoadLogout);
		Context context = CoreZygote.getContext();

		Intent intent = new Intent(context, ReLoginActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

	@Override
	public Activity getFrontActivity() {
		if (CommonUtil.isEmptyList(mActivityStacks)) {
			return null;
		}
		int index = mActivityStacks.size() - 1;
		return mActivityStacks.get(index);
	}

	@Override
	public boolean activityInStacks(Class<? extends Activity> activityClass) {
		for (Activity activity : mActivityStacks) {
			if (activity.getClass().equals(activityClass)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
		if (activity != null) {
			mActivityStacks.add(activity);
		}
	}

	@Override
	public void onActivityStarted(Activity activity) {
	}


	@Override
	public void onActivityResumed(Activity activity) {
		if (mHomeKeyState == HOME_PRESS) {                                                                  // ???????????????home???
			if (mHomeKeyState != HOME_PRESS_AND_UN_LOCKED) {
				boolean isGestureUnLock = SpUtil.get(PreferencesUtils.LOGIN_GESTRUE_PASSWORD, false);       // ???????????????????????????
				if (isGestureUnLock) {
					if (!isNeedVerify()) {
						// TODO ??????????????????????????????????????????
						return;
					}
					Intent lockIntent = new Intent(activity, GestureUnLockActivity.class);
					lockIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
					lockIntent.putExtra("lockMainActivity", true);
					activity.startActivity(lockIntent);
					setHomeKeyState(HOME_PRESS_BUT_LOCKING);
					return;
				}

				boolean isFingerprintUnLock = SpUtil.get(PreferencesUtils.FINGERPRINT_IDENTIFIER, false);   // ?????????????????????????????????
				if (isFingerprintUnLock) {
					if (!isNeedVerify()) {
						// TODO ?????????????????????????????????????????????
						return;
					}
					FingerprintIdentifier fingerprintIdentifier = new FingerprintIdentifier(activity);
					if (fingerprintIdentifier.isFingerprintEnable()) {
						Intent lockIntent = new Intent(activity, FingerprintNewUnLockActivity.class);
						lockIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
						lockIntent.putExtra("lockMainActivity", true);
						activity.startActivity(lockIntent);
						setHomeKeyState(HOME_PRESS_BUT_LOCKING);
					}
				}
			}
		}
	}

	@Override
	public void onActivityPaused(Activity activity) {
	}

	@Override
	public void onActivityStopped(Activity activity) {
	}

	@Override
	public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
	}

	@Override
	public void onActivityDestroyed(Activity activity) {
		if (activity != null && !isFinishing && mActivityStacks.contains(activity)) {
			mActivityStacks.remove(activity);
		}
		FEHttpClient.cancel(activity);
	}

	private boolean isNeedVerify() {
		return System.currentTimeMillis() - sLastTimeUnlockSuccess > 30000;
	}
}
