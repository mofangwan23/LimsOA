package cn.flyrise.feep;

import static cn.flyrise.feep.utils.Patches.PATCH_APPLICATION_BUBBLE;
import static cn.flyrise.feep.utils.Patches.PATCH_WATERMARK;
import static cn.flyrise.feep.utils.Patches.PATCH_WATER_DROP_READ_MESSAGE;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.GridView;
import cn.flyrise.android.library.utility.LoadingHint;
import cn.flyrise.android.protocol.entity.UserDetailsRequest;
import cn.flyrise.android.protocol.entity.UserDetailsResponse;
import cn.flyrise.android.protocol.model.BadgeCount;
import cn.flyrise.android.shared.utility.FEUmengCfg;
import cn.flyrise.feep.FEApplication;
import cn.flyrise.feep.R;
import cn.flyrise.feep.commonality.MainMenuRecyclerViewActivity;
import cn.flyrise.feep.commonality.adapter.MenuAdapter;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.FEStatusBar;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.X.Func;
import cn.flyrise.feep.core.common.X.MainMenu;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.DevicesUtil;
import cn.flyrise.feep.core.common.utils.NetworkUtil;
import cn.flyrise.feep.core.common.utils.PreferencesUtils;
import cn.flyrise.feep.core.common.utils.SpUtil;
import cn.flyrise.feep.core.function.AppTopMenu;
import cn.flyrise.feep.core.function.FunctionDataSet;
import cn.flyrise.feep.core.function.FunctionManager;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.core.premission.FePermissions;
import cn.flyrise.feep.core.premission.PermissionCode;
import cn.flyrise.feep.core.premission.PermissionGranted;
import cn.flyrise.feep.core.services.IApplicationServices;
import cn.flyrise.feep.core.watermark.WMStamp;
import cn.flyrise.feep.event.EventIgnoreVersion;
import cn.flyrise.feep.event.EventJPushRefreshNoticeMessageMenu;
import cn.flyrise.feep.event.EventNotifierUpdateApp;
import cn.flyrise.feep.main.NewMainMessageFragment;
import cn.flyrise.feep.main.PersonalFragment;
import cn.flyrise.feep.main.modules.MainModuleFragment;
import cn.flyrise.feep.main.modules.Sasigay;
import cn.flyrise.feep.mobilekey.event.MoKeyNormalEvent;
import cn.flyrise.feep.notification.NotificationController;
import cn.flyrise.feep.push.EventHuaweiPushInitSuccess;
import cn.flyrise.feep.study.fragment.ExamMainFragment;
import cn.flyrise.feep.study.fragment.StudyMainFragment;
import cn.flyrise.feep.utils.AddressSubordinatesHelper;
import cn.flyrise.feep.utils.FEUpdateVersionUtils;
import cn.flyrise.feep.utils.InitDuHelper;
import cn.flyrise.feep.utils.Patches;
import cn.flyrise.feep.utils.TempDirCleaner;
import com.drop.CoverManager;
import com.drop.DropCover.OnDragCompeteListener;
import com.hyphenate.chatui.utils.IMHuanXinHelper;
import java.lang.ref.WeakReference;
import java.util.List;
import me.leolin.shortcutbadger.ShortcutBadger;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class FEMainActivity extends AppCompatActivity {

	public static final String OPEN_LOCK_RECEIVER = "OPEN_LOCK_RECEIVER";
	public static final String CANCELLATION_LOCK_RECEIVER = "CANCELLATION_LOCK_RECEIVER";

	private MenuAdapter mMenuAdapter;
	private GridView mMenuView;

	private static final int MAIN = 0;
	private static final int STUDY = 1;
	private static final int EXAM = 2;
	private static final int PERSONAL = 3;

	private static final int BLOG = 4;
	private static final int APP = 5;
	private static final int CONTACT = 6;

	private static final int APP_VERSION = 405;//检测到有新的版本
	private String mCurrentPage;

	private Fragment mainFragment;
	private Fragment blogFragment;
	private Fragment appFragment;
	private Fragment personalFragment;
	private Fragment studyMainFragment;
	private Fragment examMainFragment;
	private SparseArray<Fragment> fragmentMap;

	private long exitTimes = 0;
	private boolean isUseLock = false;
	//	private boolean isNetWorkErrorBefore = false;
	private WeakHandler mHandler = new WeakHandler(this);

	private static class WeakHandler extends Handler {

		private WeakReference<Context> mActivity;

		WeakHandler(Context activity) {
			this.mActivity = new WeakReference<>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			FEMainActivity activity = (FEMainActivity) mActivity.get();
			if (activity != null) {
				switch (msg.what) {
					case APP_VERSION:
						boolean isAppUpdata = (boolean) msg.obj;
						((FEApplication) activity.getApplication()).setNewVersion(isAppUpdata);
						if (activity.mMenuAdapter != null) {
							activity.mMenuAdapter.notifyDataSetChanged();
						}
						EventNotifierUpdateApp updateApp = new EventNotifierUpdateApp();
						updateApp.isUpdataApp = isAppUpdata;
						EventBus.getDefault().post(updateApp);
						break;
				}
			}
		}
	}

	@Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (TextUtils.equals(mCurrentPage, MainMenu.Associate) && blogFragment != null) {
			blogFragment.onActivityResult(requestCode, resultCode, data);
		}
		else if (TextUtils.equals(mCurrentPage, MainMenu.Application) && appFragment != null) {
			appFragment.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TempDirCleaner.clear();     // 清理已经解密过的辣鸡文件

		if (CoreZygote.getLoginUserServices() == null
				|| TextUtils.isEmpty(CoreZygote.getLoginUserServices().getUserId())) {
			CoreZygote.getApplicationServices().reLoginApplication();
			return;
		}
		setTranslucent(null);
		setContentView(R.layout.fe_main);
		EventBus.getDefault().register(this);

		registerSystemEventReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_LOCALE_CHANGED);
		intentFilter.addAction(CANCELLATION_LOCK_RECEIVER);
		intentFilter.addAction(OPEN_LOCK_RECEIVER);
		registerReceiver(mUnLockReceiver, intentFilter);
		findView();
		new AddressSubordinatesHelper();
	}

	@Override protected void onResume() {
		super.onResume();
		FEUmengCfg.onFragmentActivityResumeUMeng(this);
//		changeAfterResume();
//		showMainMessage();

	}

	@Override protected void onPause() {
		super.onPause();
		FEUmengCfg.onFragmentActivityPauseUMeng(this);
	}

	private void findView() {
		mMenuView = findViewById(R.id.fe_main_menu_gridview);
		detectionUpdateVersion();
		LoadingHint.show(this);
		FunctionManager.getInstance().fetchFunctions()
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.doOnNext(ds -> {
					// 1. 处理底部菜单
					List<AppTopMenu> bottomMenus = FunctionManager.getAppTopMenu();
					if (CommonUtil.nonEmptyList(bottomMenus)) {
						mMenuView.setNumColumns(bottomMenus.size());
						mMenuAdapter = new MenuAdapter(this, bottomMenus, new OnDragCompeteListener() {
							@Override public void onDrag() {
								if (mainFragment != null) ((NewMainMessageFragment) mainFragment).allMessageRead();
							}

							@Override public void onDownDrag(boolean isDownDrag) { }
						});
						mMenuView.setAdapter(mMenuAdapter);
					}

					// 2. 用户未登录情况下,点击通知栏
					if (FEApplication.sNotificationMessage != null) {
						NotificationController.startDetailActivity(FEMainActivity.this, FEApplication.sNotificationMessage);
						FEApplication.sNotificationMessage = null;
					}
					// 3. 处理未读消息
					if (FunctionManager.hasPatch(PATCH_APPLICATION_BUBBLE)) setUnreadApplicationMessage(ds.hasUnreadMessage);
					// 4. 启动水印
					if (FunctionManager.hasPatch(PATCH_WATERMARK)) launchWatermark();
					// 5. 设置嘟嘟
					if (FunctionManager.hasModule(Func.Dudu)) setDuduPhoneNumber(FEMainActivity.this);
					// 6. 启动气泡消除
					if (FunctionManager.hasPatch(PATCH_WATER_DROP_READ_MESSAGE)) CoverManager.getInstance().init(FEMainActivity.this);
					//7.启动IM
					if (FunctionManager.hasPatch(Patches.PATCH_HUANG_XIN)) IMHuanXinHelper.getInstance().login();
				})
				.subscribe(ds -> {
					LoadingHint.hide();
					if (ds.resultCode == FunctionDataSet.CODE_FETCH_FAILURE
							|| ds.resultCode == FunctionDataSet.CODE_NO_FUNCTION) {
						FEToast.showMessage(getString(R.string.login_error_contact_manager));
						SpUtil.put(PreferencesUtils.LOGIN_GESTRUE_PASSWORD, false);
						CoreZygote.getApplicationServices().reLoginApplication();
						return;
					}

					displayFragment(MAIN);
					// 底部菜单点击事件
					mMenuView.setOnItemClickListener((parent, view, position, arg3) -> {
						if (MainMenuRecyclerViewActivity.isShowMainMenuRecyclerView) return;
						String menu = mMenuAdapter.getMenu(position);
						if (menu == null) return;
						if (TextUtils.equals(mCurrentPage, menu) && TextUtils.equals(menu, MainMenu.Associate)) {
							if (!NetworkUtil.isNetworkAvailable(this)) return;
						}
						mMenuAdapter.setCurrentPosition(position);
						mCurrentPage = menu;
						switch (menu) {
							case MainMenu.Message:
								displayFragment(MAIN);
								break;
							case MainMenu.Study:
								displayFragment(STUDY);
								break;
							case MainMenu.EXAM:
								displayFragment(EXAM);
								break;
							case MainMenu.Mine:
								displayFragment(PERSONAL);
								break;
						}
					});
				}, exception -> {
					FEToast.showMessage(getString(R.string.login_error_contact_manager));
					exception.printStackTrace();
					LoadingHint.hide();
					SpUtil.put(PreferencesUtils.LOGIN_GESTRUE_PASSWORD, false);
					CoreZygote.getApplicationServices().reLoginApplication();
				});
	}

	private void detectionUpdateVersion() {
		new FEUpdateVersionUtils(this, (needUpdate, isNoIgnoreVersion) -> {//存在最新版本
			Message message = mHandler.obtainMessage();
			message.what = APP_VERSION;
			message.obj = needUpdate && isNoIgnoreVersion;
			mHandler.sendMessage(message);
		}).detectionUpdateVerson();
	}

	private void displayFragment(int type) {
		final FragmentManager fragmentmanager = this.getSupportFragmentManager();
		final FragmentTransaction fragmentTransaction = fragmentmanager.beginTransaction();

		if (fragmentMap == null) {
			fragmentMap = new SparseArray<>();
			fragmentMap.put(MAIN, mainFragment = new NewMainMessageFragment());
			fragmentTransaction.add(R.id.framelayout, mainFragment);
		}

		if (fragmentMap.get(type) == null) {
			switch (type) {
				case STUDY:
					fragmentMap.put(STUDY, studyMainFragment = new StudyMainFragment());
					fragmentTransaction.add(R.id.framelayout, studyMainFragment);
					break;
				case EXAM:
					fragmentMap.put(EXAM, examMainFragment = new ExamMainFragment());
					fragmentTransaction.add(R.id.framelayout, examMainFragment);
					break;
				case PERSONAL:
					fragmentMap.put(PERSONAL, personalFragment = new PersonalFragment());
					fragmentTransaction.add(R.id.framelayout, personalFragment);
					break;
			}
		}

		for (int i = 0; i < fragmentMap.size(); i++) {
			int key = fragmentMap.keyAt(i);
			if (key == type) {
				fragmentTransaction.show(fragmentMap.get(key));
			}
			else {
				fragmentTransaction.hide(fragmentMap.get(key));
			}
		}

		fragmentTransaction.commitAllowingStateLoss();
		setTranslucent(fragmentMap.get(type));
	}

	//设置状态栏颜色
	private void setTranslucent(Fragment fragment) {
		if (DevicesUtil.isSpecialDevice()) return;
		if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
			WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
			localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);
			if (FEStatusBar.canModifyStatusBar(getWindow())) {
				if (fragment instanceof MainModuleFragment) {
					if (((MainModuleFragment) fragment).isHeaderExpand()) {
						FEStatusBar.setDarkStatusBar(this);
					}
					else {
						FEStatusBar.setLightStatusBar(this);
					}
				}
				else {
					FEStatusBar.setDarkStatusBar(this);
				}
			}
			return;
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			boolean statusBarColorApplySuccess = false;
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

			if (FEStatusBar.canModifyStatusBar(getWindow())) {
				if (fragment instanceof MainModuleFragment) {
					if (((MainModuleFragment) fragment).isHeaderExpand()) {
						statusBarColorApplySuccess = FEStatusBar.setDarkStatusBar(this);
						getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
					}
					else {
						statusBarColorApplySuccess = FEStatusBar.setLightStatusBar(this);
						getWindow().setStatusBarColor(getResources().getColor(R.color.defaultColorPrimaryDarkV7));
					}
				}
				else {
					statusBarColorApplySuccess = FEStatusBar.setDarkStatusBar(this);
//					getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
					getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
				}
			}

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				if (fragment instanceof MainModuleFragment) {
					if (((MainModuleFragment) fragment).isHeaderExpand()) {
						getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
						getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
					}
					else {
						getWindow().setStatusBarColor(getResources().getColor(R.color.defaultColorPrimaryDarkV7));
					}
				}
				else {
					getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
//					getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
					getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
				}
				return;
			}
			if (!statusBarColorApplySuccess) getWindow().setStatusBarColor(Color.BLACK);
		}
	}

	public void switchToAssociate() {
		mCurrentPage = MainMenu.Associate;
		displayFragment(BLOG);
		mMenuAdapter.setCurrentPosition(BLOG);
		mMenuAdapter.notifyDataSetChanged();
	}

	public void updateStatusBar(Fragment fragment) {
		setTranslucent(fragment);
	}

	private void setDuduPhoneNumber(FEMainActivity activity) {//设置嘟嘟默认电话
		FePermissions.with(activity)
				.rationaleMessage(activity.getResources().getString(R.string.permission_rationale_contact))
				.permissions(new String[]{android.Manifest.permission.WRITE_CONTACTS})
				.requestCode(PermissionCode.CONTACTS)
				.request();
	}

	public void setUnreadApplicationMessage(boolean hasUnreadApplicationMessage) {
		if (mMenuAdapter != null) mMenuAdapter.setUnreadApplicationMessage(hasUnreadApplicationMessage);
	}

	private void launchWatermark() {
		WMStamp.getInstance().launchWaterMarkExecutor();
		UserDetailsRequest request = new UserDetailsRequest();
		FEHttpClient.getInstance().post(request, new ResponseCallback<UserDetailsResponse>() {
			@Override public void onCompleted(UserDetailsResponse response) {
				String mobile = "";
				if (response != null && TextUtils.equals(response.getErrorCode(), "0")) {
					mobile = response.getResult().getPhone();
				}
				WMStamp.getInstance().appendWaterMark(mobile);
			}

			@Override public void onFailure(RepositoryException repositoryException) {
				WMStamp.getInstance().appendWaterMark("");
			}
		});
	}

	private void registerSystemEventReceiver() {
		boolean isGestureLock = SpUtil.get(PreferencesUtils.LOGIN_GESTRUE_PASSWORD, false);
		boolean isFingerprintLock = SpUtil.get(PreferencesUtils.FINGERPRINT_IDENTIFIER, false);
		if ((isGestureLock || isFingerprintLock) && !isUseLock) {
			IntentFilter filter = new IntentFilter();
			filter.addAction(Intent.ACTION_SCREEN_OFF);             // 屏幕灭屏广播
			filter.addAction(Intent.ACTION_SCREEN_ON);              // 屏幕亮屏广播
			filter.addAction(Intent.ACTION_USER_PRESENT);           // 屏幕解锁广播
			filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);   // 短按HOME键广播
			registerReceiver(mSystemEventReceiver, filter);
			isUseLock = true;
		}
	}

	private void showMainMessage() {
		if (getIntent() == null) return;
		if (!"xiaomi".equalsIgnoreCase(Build.MANUFACTURER)) return;//小米
		if (!getIntent().getBooleanExtra("notification_opent", false)) return;
		if (TextUtils.equals(mCurrentPage, MainMenu.Mine) && mainFragment != null) displayFragment(MAIN);
	}

	private void changeAfterResume() {  // 适配魅族
		mHandler.postDelayed(() -> {
			if (TextUtils.equals(mCurrentPage, MainMenu.Application) && appFragment != null) setTranslucent(appFragment);
		}, 200);
	}

	@Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		FePermissions.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
	}

	@Override public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// 判断两次点击的时间间隔（默认设置为2秒）
			if ((System.currentTimeMillis() - exitTimes) > 2000) {// 2S内则退出程序
				FEToast.showMessage(getResources().getString(R.string.list_exit));
				exitTimes = System.currentTimeMillis();
				return true;
			}
			else {
				CoreZygote.getApplicationServices().exitApplication();
				if (mainFragment != null) ((NewMainMessageFragment) mainFragment).onDestroy();
				exitTimes = 0;
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void refreshProceedingMenu(EventJPushRefreshNoticeMessageMenu nm) {
		try {
			final String type = nm.type;
			final String nums = nm.totalNums;
			final String circleNums = nm.circleNums;
			final BadgeCount menuNums = new BadgeCount();
			menuNums.setTotalNums(nums);
			menuNums.setCircleNums(circleNums);
			menuNums.setType(type);
			if ("20".equals(type)) {//消息界面
				ShortcutBadger.applyCount(FEMainActivity.this, Integer.parseInt(nums));
				mMenuAdapter.setBadgeCount(menuNums);
			}
		} catch (final NullPointerException e) {
			FELog.e("ddd", "刷新菜单气泡信息出错了");
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void eventIgnoreVerson(EventIgnoreVersion ignoreVersion) {//忽略版本更新
		detectionUpdateVersion();
	}

	@PermissionGranted(PermissionCode.CONTACTS)
	public void onContactPermissionGranted() {
		if (FunctionManager.hasModule(Func.Dudu)) new InitDuHelper(this).insertToContacts();
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void setMobileKeyActivitState(MoKeyNormalEvent event) {
		mMenuAdapter.setMobileKeyIsActive(event.isNormal);
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void eventPushInitSuccess(EventHuaweiPushInitSuccess success) {
		IMHuanXinHelper.getInstance().setPushToken();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		EventBus.getDefault().unregister(this);

		try {
			unregisterReceiver(mUnLockReceiver);
			mUnLockReceiver = null;
		} catch (Exception exp) {
		}

		if (isUseLock) {
			try {
				unregisterReceiver(mSystemEventReceiver);
				mSystemEventReceiver = null;
			} catch (Exception exp) {
			}
		}
		Sasigay.INSTANCE.saveCompany(null);
		FEApplication feApplication = (FEApplication) this.getApplicationContext();
		feApplication.setNewVersion(false);
		FunctionManager.emptyData();

		mHandler.removeCallbacksAndMessages(null);
		mHandler = null;
	}

	/**
	 * 接收系统事件的广播：按下 Home 键、开屏、锁屏、解锁
	 */
	private BroadcastReceiver mSystemEventReceiver = new BroadcastReceiver() {
		@Override public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			switch (action) {
				case Intent.ACTION_CLOSE_SYSTEM_DIALOGS:
					String reason = intent.getStringExtra("reason");
					if (TextUtils.equals(reason, "homekey")) {
						CoreZygote.getApplicationServices().setHomeKeyState(IApplicationServices.HOME_PRESS);
					}
					break;
				case Intent.ACTION_SCREEN_ON:               // 开屏
				case Intent.ACTION_SCREEN_OFF:              // 锁屏
				case Intent.ACTION_USER_PRESENT:            // 解锁
					CoreZygote.getApplicationServices().setHomeKeyState(IApplicationServices.HOME_PRESS);
					break;
			}
		}
	};

	/**
	 * 接收是否开启手势解锁的广播，收到广播后进行相应处理
	 */
	private BroadcastReceiver mUnLockReceiver = new BroadcastReceiver() {
		@Override public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(OPEN_LOCK_RECEIVER)) {                            // 开启手势锁
				registerSystemEventReceiver();
			}
			else if (action.equals(CANCELLATION_LOCK_RECEIVER)) {               // 关闭手势锁
				if (isUseLock) {
					unregisterReceiver(mSystemEventReceiver);
					isUseLock = false;
				}
			}
			else if (TextUtils.equals(Intent.ACTION_LOCALE_CHANGED, action)) {   // 切换语言
				CoreZygote.getApplicationServices().reLoginApplication();
			}
		}
	};

}
