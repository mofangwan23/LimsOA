package cn.flyrise.feep.more;

import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.MINUTE;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.base.views.UISwitchButton;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import com.borax12.materialdaterangepicker.DateTimePickerDialog;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMOptions;
import com.hyphenate.chatui.db.MessageSettingManager;
import com.hyphenate.chatui.domain.MessageSetting;
import com.hyphenate.chatui.ui.BlacklistActivity;
import com.hyphenate.chatui.utils.IMHuanXinHelper;
import java.text.DecimalFormat;
import java.util.Calendar;

/**
 * Created by klc on 2017/3/10.
 * update by ZYP on 2017-08-11
 */
public class ChatSettingActivity extends BaseActivity {

	private UISwitchButton mReceiveNewMsgBtn;
	private UISwitchButton mShowNotificationBtn;
	private UISwitchButton mSoundBtn;
	private UISwitchButton mVibrateBtn;
	private UISwitchButton mSilenceModeBtn;
	private UISwitchButton mConvDelBtn;
	private UISwitchButton mSpeakerOnBt;

	private View mLayoutNotifySetting;
	private View mLayoutSilence;
	private View mSilenceStartView;
	private View mSilenceEndView;
	private View mBlackListView;

	@SuppressWarnings("all")
	private TextView mTvSilenceStart;
	@SuppressWarnings("all")
	private TextView mTvSilenceEnd;

	private RelativeLayout mLayoutSetting;
	private RelativeLayout mLayoutSound;
	private RelativeLayout mLayoutvibrate;

	private String mImUserID;

	private final DecimalFormat mDecimalFormat = new DecimalFormat("00");
	private MessageSetting mMessageSetting;
	private MessageSettingManager mMessageSettingManager;
	private FEToolbar mToolbar;

	@Override
	protected void toolBar(FEToolbar toolbar) {
		mToolbar = toolbar;
		toolbar.setTitle(R.string.em_setting_chat);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat_setting);
	}

	@Override
	public void bindView() {
		mReceiveNewMsgBtn = findViewById(R.id.switchReceiveNewMsg);
		mShowNotificationBtn = findViewById(R.id.switchShowNotification);
		mSoundBtn = findViewById(R.id.switchMsgSound);
		mVibrateBtn = findViewById(R.id.switchMsgVibrate);
		mSilenceModeBtn = findViewById(R.id.switchSilenceMode);
		mConvDelBtn = findViewById(R.id.switchDeleteMsgWhenExist);
		mSpeakerOnBt = findViewById(R.id.switchSpeakerOn);

		mLayoutNotifySetting = findViewById(R.id.layoutNotificationSetting);
		mLayoutSilence = findViewById(R.id.layoutSilenceMode);
		mSilenceStartView = findViewById(R.id.layoutSilenceStartTime);
		mSilenceEndView = findViewById(R.id.layoutSilenceEndTime);
		mBlackListView = findViewById(R.id.layoutBlackList);

		mTvSilenceStart = findViewById(R.id.tvSilenceStartTime);
		mTvSilenceEnd = findViewById(R.id.tvSilenceEndTime);

		mLayoutSetting = findViewById(R.id.open_setting);
		mLayoutSound = findViewById(R.id.sound_layout);
		mLayoutvibrate = findViewById(R.id.vibrate_layout);
	}

	@Override
	public void bindData() {
		EMOptions chatOptions = EMClient.getInstance().getOptions();
		mConvDelBtn.setChecked(chatOptions.isDeleteMessagesAsExitGroup());      // 退出群聊是否删除会话

		mImUserID = CoreZygote.getLoginUserServices().getUserId();
		mImUserID = IMHuanXinHelper.getInstance().getImUserId(mImUserID);
		mMessageSettingManager = new MessageSettingManager();
		mMessageSetting = mMessageSettingManager.query(mImUserID);

		mReceiveNewMsgBtn.setChecked(mMessageSetting.receiveMsg);               // 初始化狗日的布局
		mShowNotificationBtn.setChecked(mMessageSetting.notify);
		mSoundBtn.setChecked(mMessageSetting.sound);
		mVibrateBtn.setChecked(mMessageSetting.vibrate);
		mSilenceModeBtn.setChecked(mMessageSetting.silence);

		mTvSilenceStart.setText(formatSilenceTime(mMessageSetting.silenceST));
		mTvSilenceEnd.setText(formatSilenceTime(mMessageSetting.silenceET));

		mLayoutNotifySetting.setVisibility(mMessageSetting.receiveMsg ? View.VISIBLE : View.GONE);
		mLayoutSilence.setVisibility(mMessageSetting.silence ? View.VISIBLE : View.GONE);

		mSpeakerOnBt.setChecked(mMessageSetting.speakerOn);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			mLayoutSetting.setVisibility(View.VISIBLE);
			mLayoutSound.setVisibility(View.GONE);
			mLayoutvibrate.setVisibility(View.GONE);
			findViewById(R.id.line).setVisibility(View.GONE);
			findViewById(R.id.divider).setVisibility(View.GONE);
		}
		else {
			mLayoutSetting.setVisibility(View.GONE);
			mLayoutSound.setVisibility(View.VISIBLE);
			mLayoutvibrate.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void bindListener() {
		mConvDelBtn.setOnCheckedChangeListener(this::onCheckedChange);
		mReceiveNewMsgBtn.setOnCheckedChangeListener(this::onCheckedChange);
		mShowNotificationBtn.setOnCheckedChangeListener(this::onCheckedChange);

		mSilenceModeBtn.setOnCheckedChangeListener(this::onCheckedChange);
		mSoundBtn.setOnCheckedChangeListener(this::onCheckedChange);
		mVibrateBtn.setOnCheckedChangeListener(this::onCheckedChange);
		mSpeakerOnBt.setOnCheckedChangeListener(this::onCheckedChange);

		mSilenceStartView.setOnClickListener(this::onSilenceTimeLayoutClick);                       // 选择勿扰开始时间
		mSilenceEndView.setOnClickListener(this::onSilenceTimeLayoutClick);                         // 选择勿扰结束时间
		mBlackListView.setOnClickListener(v -> startActivity(new Intent(ChatSettingActivity.this, BlacklistActivity.class)));

		mToolbar.setNavigationOnClickListener(v -> checkSilenceTime());

		mLayoutSetting.setOnClickListener(v->gotoNotificationSetting(this));
	}

	private void onCheckedChange(CompoundButton buttonView, boolean isChecked) {
		if (buttonView == mReceiveNewMsgBtn) {                                                      // 接收新消息通知
			mMessageSetting.receiveMsg = isChecked;
			if (isChecked) {
				mLayoutNotifySetting.setVisibility(View.VISIBLE);
				if (!mMessageSetting.notify && !mMessageSetting.sound && !mMessageSetting.vibrate) {    // 三个都没选，默认发送通知
					mShowNotificationBtn.setChecked(mMessageSetting.notify = true);
				}
			}
			else {
				mSilenceModeBtn.setChecked(mMessageSetting.silence = false);
				mShowNotificationBtn.setChecked(mMessageSetting.notify = false);
				mSoundBtn.setChecked(mMessageSetting.sound = false);
				mVibrateBtn.setChecked(mMessageSetting.vibrate = false);
				mLayoutSilence.setVisibility(View.GONE);
				mLayoutNotifySetting.setVisibility(View.GONE);
			}
			this.notifyDatabaseChange();
		}
		else if (buttonView == mSilenceModeBtn) {                                                   // 勿扰模式
			mMessageSetting.silence = isChecked;
			mLayoutSilence.setVisibility(mMessageSetting.silence ? View.VISIBLE : View.GONE);
			this.notifyDatabaseChange();
		}
		else if (buttonView == mShowNotificationBtn) {                                              // 是否显示将消息显示到通知栏
			mMessageSetting.notify = isChecked;
			this.notifyDatabaseChange();
			this.updateNotifySettingLayout();
		}
		else if (buttonView == mSoundBtn) {                                                         // 声音
			mMessageSetting.sound = isChecked;
			this.notifyDatabaseChange();
			this.updateNotifySettingLayout();
		}
		else if (buttonView == mVibrateBtn) {                                                       // 震动
			mMessageSetting.vibrate = isChecked;
			this.notifyDatabaseChange();
			this.updateNotifySettingLayout();
		}
		else if (buttonView == mConvDelBtn) {                                                       // 退出群聊时是否删除会话
			EMClient.getInstance().getOptions().setDeleteMessagesAsExitGroup(isChecked);
			mMessageSetting.deleteMsg = isChecked;
			this.notifyDatabaseChange();
		}
		else if (buttonView == mSpeakerOnBt) {
			mMessageSetting.speakerOn = isChecked;
			this.notifyDatabaseChange();
		}
	}

	private void onSilenceTimeLayoutClick(final View view) {
		String timeStr = view == mSilenceStartView ? mMessageSetting.silenceST : mMessageSetting.silenceET;
		String[] split = timeStr.split(":");
		int hour = CommonUtil.parseInt(split[0]);
		int minute = CommonUtil.parseInt(split[1]);

		DateTimePickerDialog timePicker = new DateTimePickerDialog();
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, minute);
		timePicker.setDateTime(calendar);
		timePicker.setButtonCallBack(new DateTimePickerDialog.ButtonCallBack() {
			@Override
			public void onClearClick() {
			}

			@Override
			public void onOkClick(Calendar calendar, DateTimePickerDialog dateTimePickerDialog) {
				int hour = calendar.get(HOUR_OF_DAY);
				int minute = calendar.get(MINUTE);
				String silenceTime = hour + ":" + minute;
				String silenceText = formatSilenceTime(hour, minute);
				if (view == mSilenceStartView) {
					if (TextUtils.equals(silenceText, mTvSilenceEnd.getText().toString())) {
						FEToast.showMessage(getString(R.string.silence_start_end_error));
						dateTimePickerDialog.dismiss();
						return;
					}
					if (!TextUtils.equals(mMessageSetting.silenceST, silenceTime)) {
						mMessageSetting.silenceST = silenceTime;
						mTvSilenceStart.setText(silenceText);
						ChatSettingActivity.this.notifyDatabaseChange();
					}
				}
				else {
					if (TextUtils.equals(silenceText, mTvSilenceStart.getText().toString())) {
						FEToast.showMessage(getString(R.string.silence_start_end_error));
						dateTimePickerDialog.dismiss();
						return;
					}
					if (!TextUtils.equals(mMessageSetting.silenceET, silenceTime)) {
						mMessageSetting.silenceET = silenceTime;
						mTvSilenceEnd.setText(silenceText);
						ChatSettingActivity.this.notifyDatabaseChange();
					}
				}
				dateTimePickerDialog.dismiss();
			}
		});
		timePicker.setMinCalendar(Calendar.getInstance());
		timePicker.setTimeLevel(DateTimePickerDialog.TIME_LEVEL_MIN);
		timePicker.setOnlyTime(true);
		timePicker.show(getFragmentManager(), "dateTimePickerDialog");
	}

	private void updateNotifySettingLayout() {
		if (mMessageSetting.notify) {
			return;
		}
		if (mMessageSetting.sound) {
			return;
		}
		if (mMessageSetting.vibrate) {
			return;
		}

		// 不显示通知、没有声音，没有震动，相当于没开启新消息接收
		mReceiveNewMsgBtn.setChecked(mMessageSetting.receiveMsg = false);
		mLayoutNotifySetting.setVisibility(View.GONE);
		this.notifyDatabaseChange();
	}

	private void notifyDatabaseChange() {
		if (TextUtils.isEmpty(mMessageSetting.userId)) {
			mMessageSetting.userId = mImUserID;
			mMessageSettingManager.insert(mMessageSetting);
		}
		else {
			mMessageSettingManager.update(mMessageSetting);
		}
	}

	private String formatSilenceTime(String time) {
		String[] split = time.split(":");
		int hour = CommonUtil.parseInt(split[0]);
		int minute = CommonUtil.parseInt(split[1]);
		return formatSilenceTime(hour, minute);
	}

	private String formatSilenceTime(int hour, int minute) {
		String value;
		if (hour >= 0 && hour < 6) {
			value = getString(R.string.weehours);
		}
		else if (hour >= 6 && hour < 12) {
			value = getString(R.string.noon);
		}
		else if (hour == 12) {
			value = getString(R.string.nooning);
		}
		else if (hour > 12 && hour < 18) {
			value = getString(R.string.afternoon);
		}
		else {
			value = getString(R.string.evening);
		}

		hour = hour > 12 ? hour - 12 : hour;
		return value + mDecimalFormat.format(hour) + ":" + mDecimalFormat.format(minute);
	}

	private void checkSilenceTime() {
		if (!mSilenceModeBtn.isChecked()) {
			finish();
			return;
		}
		if (TextUtils.equals(mTvSilenceStart.getText().toString(), mTvSilenceEnd.getText().toString())) {
			FEToast.showMessage(getString(R.string.silence_start_end_error));
			return;
		}
		finish();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			checkSilenceTime();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		this.notifyDatabaseChange();
		super.onDestroy();
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
