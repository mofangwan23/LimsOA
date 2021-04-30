package cn.flyrise.feep.notification;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import cn.flyrise.android.shared.utility.FEUmengCfg;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.common.utils.GsonUtil;
import cn.flyrise.feep.core.common.utils.PreferencesUtils;
import cn.flyrise.feep.core.common.utils.SpUtil;
import cn.flyrise.feep.notification.adapter.NotificationSettingAdapter;
import cn.flyrise.feep.notification.bean.ItemInfo;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.Map;

public class NotificationSettingActivity extends BaseActivity {

	public static final String OPEN_NOTIFICATION = "open_notification";
	public static final String OPEN_NOTIFICATION_SOUND = "open_notificaiton_sound";
	public static final String OPEN_NOTIFICATION_VIBRATE = "open_notification_vibrate";
	private RecyclerView listView;
	private NotificationSettingAdapter adapter;
	private Map<String, Boolean> mapStatus;
	private RelativeLayout relativeLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.notification_setting_layout);
	}

	@Override
	protected void toolBar(FEToolbar toolbar) {
		toolbar.setTitle(R.string.settings_notice);
	}

	@Override
	public void bindView() {
		listView = findViewById(R.id.user_detail_recyclerview);
		listView.setLayoutManager(new LinearLayoutManager(this));
		relativeLayout = findViewById(R.id.open_setting);
	}

	@Override
	public void bindData() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			listView.setVisibility(View.GONE);
			relativeLayout.setVisibility(View.VISIBLE);
		}
		else {
			listView.setVisibility(View.VISIBLE);
			relativeLayout.setVisibility(View.GONE);
		}
		initSettingList();
	}

	@Override public void bindListener() {
		super.bindListener();
		relativeLayout.setOnClickListener(v -> gotoNotificationSetting(this));
	}

	private void initSettingList() {
		String studioText = SpUtil.get(PreferencesUtils.SETTING_NOTIFICATION_STATUS, "");
		if (!TextUtils.isEmpty(studioText)) {
			mapStatus = GsonUtil.getInstance().fromJson(studioText, new TypeToken<Map<String, Boolean>>() {
			}.getType());
		}
		final ArrayList<ItemInfo> itemInfoList = new ArrayList<>();

		final ItemInfo firstInfo = new ItemInfo();
		firstInfo.setNotification(OPEN_NOTIFICATION, getString(R.string.open_push), getString(R.string.open_push));
		itemInfoList.add(firstInfo);

		final ItemInfo SecondInfo = new ItemInfo();
		SecondInfo.setNotification(OPEN_NOTIFICATION_SOUND, getString(R.string.sound_setting), getString(R.string.sound));
		itemInfoList.add(SecondInfo);

		final ItemInfo thirdInfo = new ItemInfo();
		thirdInfo.setNotification(OPEN_NOTIFICATION_VIBRATE, getString(R.string.vibrate_setting), getString(R.string.vibrate));
		itemInfoList.add(thirdInfo);

		adapter = new NotificationSettingAdapter(this, itemInfoList, mapStatus);
		listView.setAdapter(adapter);
	}

	@Override
	protected void onResume() {
		super.onResume();
		FEUmengCfg.onActivityResumeUMeng(this, FEUmengCfg.NotificationSetting);
	}

	@Override
	protected void onPause() {
		super.onPause();
		FEUmengCfg.onActivityPauseUMeng(this, FEUmengCfg.NotificationSetting);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Map<String, Boolean> maps = adapter.getCurrentNotificationStatus();
		if (maps != null) {
			SpUtil.put(PreferencesUtils.SETTING_NOTIFICATION_STATUS, GsonUtil.getInstance().toJson(maps));
		}
	}

	private void gotoNotificationSetting(Activity activity) {
		ApplicationInfo appInfo = activity.getApplicationInfo();
		String pkg = activity.getApplicationContext().getPackageName();
		int uid = appInfo.uid;
		try {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				Intent intent = new Intent();
				intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
				intent.putExtra(Settings.EXTRA_APP_PACKAGE, pkg);
				intent.putExtra(Settings.EXTRA_CHANNEL_ID, uid);
				intent.putExtra("app_package", pkg);
				intent.putExtra("app_uid", uid);
				activity.startActivityForResult(intent, 10);
			}
		} catch (Exception e) {
			Intent intent = new Intent(Settings.ACTION_SETTINGS);
			activity.startActivityForResult(intent, 10);
		}
	}
}
