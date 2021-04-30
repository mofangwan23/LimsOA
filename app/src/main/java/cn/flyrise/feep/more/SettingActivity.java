package cn.flyrise.feep.more;

import static cn.flyrise.feep.core.common.X.Func.Schedule;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.flyrise.android.library.utility.LoadingHint;
import cn.flyrise.android.shared.utility.FEUmengCfg;
import cn.flyrise.feep.R;
import cn.flyrise.feep.addressbook.utils.AddressBookCacheInvoker;
import com.hyphenate.chatui.utils.FeepPushManager;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.base.views.UISwitchButton;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.FileUtil;
import cn.flyrise.feep.core.common.utils.PreferencesUtils;
import cn.flyrise.feep.core.common.utils.SpUtil;
import cn.flyrise.feep.core.dialog.FEMaterialDialog;
import cn.flyrise.feep.core.function.FunctionManager;
import cn.flyrise.feep.core.premission.FePermissions;
import cn.flyrise.feep.core.premission.FePermissions.OnRequestPermissionDeniedListener;
import cn.flyrise.feep.core.premission.PermissionCode;
import cn.flyrise.feep.core.premission.PermissionGranted;
import cn.flyrise.feep.notification.NotificationSettingActivity;
import cn.flyrise.feep.schedule.utils.ScheduleUtil;
import cn.flyrise.feep.utils.Patches;
import com.hyphenate.chatui.utils.IMHuanXinHelper;
import java.io.File;


/**
 * @author ZYP
 * @since 2016/6/2 15:36
 */
public class SettingActivity extends BaseActivity {

	private String mUserId;
	private TextView mTvCacheSize;
	private RelativeLayout mNotificationLayout;
	private UISwitchButton mAutoSyncScheduleBtn;
	public static final long KB = 1024;
	public static final long MB = KB * 1024;
	public static final long GB = MB * 1024;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.personal_setting);
	}

	@Override
	protected void toolBar(FEToolbar toolbar) {
		toolbar.setTitle(R.string.more_setting_title);
	}

	@Override
	public void bindView() {
		mTvCacheSize = (TextView) findViewById(R.id.tvCacheSize);
		mNotificationLayout = (RelativeLayout) findViewById(R.id.setting_notification);

		boolean isNative = FunctionManager.isNative(Schedule);
		View viewAutoSyncSchedule = findViewById(R.id.rlAutoSyncSchedule);
		viewAutoSyncSchedule.setVisibility(isNative ? View.VISIBLE : View.GONE);
	}

	@Override
	public void bindData() {
		updateCacheSize();
	}

	@Override
	public void bindListener() {
		findViewById(R.id.rlClearCache).setOnClickListener(v -> clearCache());

		findViewById(R.id.btnLogout).setOnClickListener(v -> new FEMaterialDialog.Builder(SettingActivity.this)
				.setTitle(null)
				.setMessage(getResources().getString(R.string.cancellation_dialog_context))
				.setPositiveButton(null, dialog -> {
					SpUtil.put(PreferencesUtils.LOGIN_GESTRUE_PASSWORD, false);
					SpUtil.put(PreferencesUtils.FINGERPRINT_IDENTIFIER, false);
					SpUtil.put(PreferencesUtils.USER_ID, "");
					IMHuanXinHelper.getInstance().logout(true);
					FeepPushManager.deleteAlias();
					CoreZygote.getApplicationServices().reLoginApplication();
					NotificationManager message = ((NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE));
					if (message != null) message.cancelAll();
				})
				.setNegativeButton(null, null)
				.build()
				.show());

		mNotificationLayout.setOnClickListener(v -> startActivity(new Intent(SettingActivity.this, NotificationSettingActivity.class)));

		mUserId = CoreZygote.getLoginUserServices().getUserId();
		mAutoSyncScheduleBtn = (UISwitchButton) findViewById(R.id.chkAutoSyncSchedule);
		Boolean autoSync = ScheduleUtil.hasUserScheduleSetting();
		mAutoSyncScheduleBtn.setChecked(autoSync);
		mAutoSyncScheduleBtn.setOnCheckedChangeListener((buttonView, isChecked) -> {
			if (isChecked) {
				checkCalendarPermission();
			}
			SpUtil.put(mUserId, isChecked);
		});

		if (FunctionManager.hasPatch(Patches.PATCH_HUANG_XIN) && IMHuanXinHelper.getInstance().isImLogin()) {
			findViewById(R.id.setting_im)
					.setOnClickListener(v -> startActivity(new Intent(SettingActivity.this, ChatSettingActivity.class)));
		}
		else {
			findViewById(R.id.setting_im).setVisibility(View.GONE);
		}
	}

	private void checkCalendarPermission() {
		FePermissions.with(SettingActivity.this)
				.permissions(new String[]{Manifest.permission.WRITE_CALENDAR, Manifest.permission.READ_CALENDAR})
				.rationaleMessage(getResources().getString(R.string.permission_rationale_calendar))
				.requestCode(PermissionCode.CALENDAR)
				.request();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		FePermissions.onRequestPermissionsResult(this, requestCode, permissions, grantResults, new OnRequestPermissionDeniedListener() {
			@Override
			public void onRequestPermissionDenied(int requestCode, String[] permissions, int[] grantResults, String deniedMessage) {
				mAutoSyncScheduleBtn.setChecked(false);                // 权限被拒绝
				SpUtil.put(mUserId, false);
			}
		});
	}


	@PermissionGranted(PermissionCode.CALENDAR)
	public void onCalendarPermissionGranted() {
		SpUtil.put(mUserId, true);
	}

	private void clearCache() {
		final File cacheFile = getApplication().getCacheDir();
		final File knowledgeCacheFile = new File(CoreZygote.getPathServices().getKnowledgeCachePath());
		long cacheSize;
		try {
			cacheSize = FileUtil.getFolderSize(cacheFile) + FileUtil.getFolderSize(knowledgeCacheFile);
		} catch (Exception e) {
			cacheSize = 0;
		}

		if (cacheSize == 0) {
			return;
		}

		LoadingHint.show(SettingActivity.this);
		new Thread(() -> {
			FileUtil.deleteFolderFile(cacheFile.getAbsolutePath(), false);
			FileUtil.deleteFolderFile(knowledgeCacheFile.getAbsolutePath(), false);
			FileUtil.deleteFolderFile(CoreZygote.getPathServices().getCommonUserId(), false);
			try {
				Thread.sleep(1500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			String path = CoreZygote.getPathServices().getAddressBookPath();
			String mark = CommonUtil.getMD5(CoreZygote.getLoginUserServices().getUserId());
			AddressBookCacheInvoker.createClearMark(path, mark);

			runOnUiThread(() -> {
				updateCacheSize();
				LoadingHint.hide();
			});
		}).start();
	}

	private void updateCacheSize() {
		long cacheSize;
		long knowledgeCacheSize;
		long commonCacheSize;
		try {
			cacheSize = FileUtil.getFolderSize(getApplication().getCacheDir());
		} catch (Exception e) {
			cacheSize = 0;
		}
		try {
			knowledgeCacheSize = FileUtil.getFolderSize(new File(CoreZygote.getPathServices().getKnowledgeCachePath()));
		} catch (Exception e) {
			knowledgeCacheSize = 0;
		}
		try {
			commonCacheSize = FileUtil.getFolderSize(new File(CoreZygote.getPathServices().getCommonUserId()));
		} catch (Exception e) {
			commonCacheSize = 0;
		}
		mTvCacheSize.setText(displayFileSize(cacheSize + knowledgeCacheSize + commonCacheSize));
	}

	public String displayFileSize(long size) {
		float value = (float) size / MB;
		return String.format(value > 100 ? "%.0f MB" : "%.1f MB", value);
	}

	@Override
	protected void onResume() {
		super.onResume();
		FEUmengCfg.onFragmentResumeUMeng(FEUmengCfg.SettingActivity);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		FEUmengCfg.onFragmentPauseUMeng(FEUmengCfg.SettingActivity);
	}
}
