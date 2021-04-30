package cn.flyrise.feep.schedule;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.flyrise.android.protocol.entity.schedule.AgendaDetailData;
import cn.flyrise.feep.K;
import cn.flyrise.feep.K.schedule;
import cn.flyrise.feep.R;
import cn.flyrise.feep.addressbook.utils.ContactsIntent;
import cn.flyrise.feep.collaboration.activity.NewCollaborationActivity;
import cn.flyrise.feep.commonality.manager.XunFeiVoiceInput;
import cn.flyrise.feep.commonality.view.TouchableWebView;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.common.DataKeeper;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.DevicesUtil;
import cn.flyrise.feep.core.common.utils.PreferencesUtils;
import cn.flyrise.feep.core.common.utils.SpUtil;
import cn.flyrise.feep.core.dialog.FELoadingDialog;
import cn.flyrise.feep.core.dialog.FEMaterialDialog;
import cn.flyrise.feep.core.dialog.FEMaterialDialog.Builder;
import cn.flyrise.feep.core.function.FunctionManager;
import cn.flyrise.feep.core.image.loader.FEImageLoader;
import cn.flyrise.feep.core.premission.FePermissions;
import cn.flyrise.feep.core.premission.PermissionCode;
import cn.flyrise.feep.core.premission.PermissionGranted;
import cn.flyrise.feep.core.services.model.AddressBook;
import cn.flyrise.feep.event.EventRefresh;
import cn.flyrise.feep.particular.views.ParticularContentView;
import cn.flyrise.feep.particular.views.ParticularReplyEditView;
import cn.flyrise.feep.particular.views.RelativeElegantLayout;
import cn.flyrise.feep.schedule.ScheduleReplyListAdapter.ScheduleReplyClickListener;
import cn.flyrise.feep.schedule.model.ScheduleReply;
import cn.flyrise.feep.schedule.utils.ScheduleUtil;
import cn.flyrise.feep.utils.Patches;
import cn.squirtlez.frouter.annotations.Route;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.greenrobot.eventbus.EventBus;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author ZYP
 * @since 2016-11-29 09:52
 */
@Route("/schedule/detail")
public class ScheduleDetailActivity extends BaseActivity implements ScheduleDetailContract.IView {

	private final int REPLYTEXT_MAX = 1000;

	private TextView mTvScheduleTitle;
	private ImageView mIvUserIcon;
	private TextView mTvScheduleSendUser;
	private TextView mTvScheduleSendTime;

	private TextView mTvDisplayDetail;
	private TextView mTvScheduleStartTime;
	private TextView mTvScheduleEndTime;
	private TextView mTvSchedulePromptTime;
	private TextView mTvScheduleRepeatTime;
	private TextView mTvScheduleSharePersons;
	private LinearLayout mLayoutScheduleDetailInfo;

	private FEToolbar mToolBar;
	private List<String> mMoreItems;

	private TouchableWebView mWebView;
	private FELoadingDialog mLoadingDialog;
	private FEMaterialDialog mMoreDialog;

	private LinearLayout mLyReply;
	private TextView mTvReplyCount;
	private RelativeElegantLayout mLvReplyContent;
	private RelativeLayout mRlBottomReply;
	private Button mBtBottomReply;

	private ParticularReplyEditView mReplyEditView;

	private final Handler mHandler = new Handler();
	private WindowManager mWindowManager;
	private long hindReplyVivewTime;
	private String replyStr;
	private XunFeiVoiceInput mVoiceInput;

	private ScheduleDetailContract.IPresenter mPresenter;

	private String curReplyId;

	public static void startActivity(Activity context, String eventSourceId, String eventSource, String scheduleId) {
		Intent intent = new Intent(context, ScheduleDetailActivity.class);
		intent.putExtra(K.schedule.event_source_id, eventSourceId);
		intent.putExtra(K.schedule.event_source, eventSource);
		intent.putExtra(K.schedule.schedule_id, scheduleId);
		context.startActivityForResult(intent, K.schedule.detail_request_code);
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPresenter = new ScheduleDetailPresenter(this);
		mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		setContentView(R.layout.activity_schedule_detail);
		mPresenter.start(getIntent());

		SpUtil.put(PreferencesUtils.SAVE_REPLY_DATA, "");
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		FePermissions.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
	}

	@PermissionGranted(PermissionCode.CALENDAR)
	public void onCalendarPermissionGanted() {
		if (mPresenter != null) {
			mPresenter.syncCalendarToSystem(ScheduleDetailActivity.this);
		}
	}

	@Override
	protected void toolBar(FEToolbar toolbar) {
		this.mToolBar = toolbar;
		this.mToolBar.setTitle(R.string.schedule_detail_title);
		mToolBar.setNavigationOnClickListener(v -> {
			if (System.currentTimeMillis() - hindReplyVivewTime > 100) {
				finish();
			}
		});
	}

	@Override
	public void bindView() {
		mTvScheduleTitle = (TextView) findViewById(R.id.tvHeadTitle);
		mIvUserIcon = (ImageView) findViewById(R.id.ivHeadUserIcon);
		mTvScheduleSendUser = (TextView) findViewById(R.id.tvHeadUser);
		mTvScheduleSendTime = (TextView) findViewById(R.id.tvHeadSendTime);

		mTvDisplayDetail = (TextView) findViewById(R.id.tvDisplayDetail);
		mTvScheduleStartTime = (TextView) findViewById(R.id.tvScheduleStartTime);
		mTvScheduleEndTime = (TextView) findViewById(R.id.tvScheduleEndTime);
		mTvSchedulePromptTime = (TextView) findViewById(R.id.tvSchedulePromptTime);
		mTvScheduleRepeatTime = (TextView) findViewById(R.id.tvScheduleRepeatTime);
		mTvScheduleSharePersons = (TextView) findViewById(R.id.tvScheduleSharePersons);
		mLayoutScheduleDetailInfo = (LinearLayout) findViewById(R.id.layoutScheduleDetailInfo);
		mWebView = (TouchableWebView) findViewById(R.id.webView);
		mLyReply = (LinearLayout) findViewById(R.id.layoutReply);
		mLvReplyContent = (RelativeElegantLayout) findViewById(R.id.layoutReplyContent);
		mTvReplyCount = (TextView) findViewById(R.id.tvReplyCount);
		mRlBottomReply = (RelativeLayout) findViewById(R.id.rlReplyBottom);
		mBtBottomReply = (Button) findViewById(R.id.btReply);
		mVoiceInput = new XunFeiVoiceInput(this);
	}

	@Override
	public void bindListener() {
		mTvDisplayDetail.setOnClickListener(view -> {
			mLayoutScheduleDetailInfo.setVisibility(mLayoutScheduleDetailInfo.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
		});
		mBtBottomReply.setOnClickListener(v -> showReplyEditText(false, null, null));
		mVoiceInput.setOnRecognizerDialogListener(result -> {
			EditText editText = mReplyEditView.getReplyEditText();
			int selection = editText.getSelectionStart();
			XunFeiVoiceInput.setVoiceInputText(editText, result, selection);
		});
	}

	private void initMoreSetting() {
		mMoreItems = new ArrayList<>();
		if (!mPresenter.isSharedSchedule()) {       // 自己的日程
			if (FunctionManager.hasPatch(Patches.PATCH_SCHEDULE_REPLY)) {
				mMoreItems.add(getResources().getString(R.string.schedule_more_update_schedule));
			}
			mMoreItems.add(getResources().getString(R.string.schedule_more_delete_schedule));
			mMoreItems.add(getResources().getString(R.string.schedule_more_share_other));
		}

		Boolean autoSync = ScheduleUtil.hasUserScheduleSetting();
		if (!autoSync) {
			mMoreItems.add(getResources().getString(R.string.schedule_more_sync_calendar));
		}

		mMoreItems.add(getString(R.string.schedule_more_to_collaboration));
		if (CommonUtil.isEmptyList(mMoreItems)) {
			return;
		}
		String[] strings = mMoreItems.toArray(new String[]{});
		if (mMoreDialog == null) {
			mMoreDialog = new FEMaterialDialog.Builder(this)
					.setWithoutTitle(true)
					.setCancelable(true)
					.setItems(strings, (dialog, view, position) -> {
						String text = mMoreItems.get(position);
						if (TextUtils.equals(getResources().getString(R.string.schedule_more_sync_calendar),
								text)) {                       // 添加到本机系统日程
							FePermissions.with(ScheduleDetailActivity.this)
									.permissions(new String[]{Manifest.permission.WRITE_CALENDAR, Manifest.permission.READ_CALENDAR})
									.rationaleMessage(getResources().getString(R.string.permission_rationale_calendar))
									.requestCode(PermissionCode.CALENDAR)
									.request();
						}
						else if (TextUtils
								.equals(getResources().getString(R.string.schedule_more_share_other), text)) {                  // 分享日程
							new ContactsIntent(ScheduleDetailActivity.this)
									.targetHashCode(K.schedule.share_other_request_code)
									.requestCode(K.schedule.share_other_request_code)
									.userCompanyOnly()
									.exceptSelf()
									.title(CommonUtil.getString(R.string.schedule_title_share_other))
									.withSelect()
									.open();
						}
						else if (TextUtils
								.equals(getResources().getString(R.string.schedule_more_delete_schedule), text)) {                // 删除日程
							new FEMaterialDialog.Builder(ScheduleDetailActivity.this)
									.setCancelable(true)
									.setMessage(R.string.schedule_lbl_confirm_delete_schedule)
									.setPositiveButton(R.string.collaboration_recorder_ok, v -> mPresenter.deleteSchedule())
									.setNegativeButton(R.string.collaboration_recorder_cancel, null)
									.build()
									.show();
						}
						else if (TextUtils
								.equals(getString(R.string.schedule_more_update_schedule), text)) {  // 修改日程
							// 跳转到新建界面去
							AgendaDetailData scheduleDetail = mPresenter.getScheduleDetail();
							Intent intent = new Intent(ScheduleDetailActivity.this, NewScheduleActivity.class);
							intent.putExtra("marsterKey", scheduleDetail.marsterKey);
							intent.putExtra("title", scheduleDetail.title);
							intent.putExtra("content", scheduleDetail.content);
							intent.putExtra("repeatTime", scheduleDetail.repeatTime);   // 这个应该是个 key
							intent.putExtra("promptTime", scheduleDetail.promptTime);
							intent.putExtra("startTime", scheduleDetail.startTime);
							intent.putExtra("endTime", scheduleDetail.endTime);
							intent.putExtra("sendUserId", scheduleDetail.sendUserId);
							intent.putExtra("attachmentId", scheduleDetail.attachmentId);

							if (!TextUtils.isEmpty(scheduleDetail.shareOther)) {
								// 参考 Otto ：array 转 ArrayList.
								String[] uIds = scheduleDetail.shareOther.split(",");
								ArrayList<String> userIds = new ArrayList<>(uIds.length);
								Collections.addAll(userIds, uIds);
								intent.putStringArrayListExtra("userIds", userIds);
							}
							startActivityForResult(intent, schedule.modify_schedule_code);
						}
						else if (TextUtils
								.equals(getString(R.string.schedule_more_to_collaboration), text)) {    // 转协同
							AgendaDetailData scheduleDetail = mPresenter.getScheduleDetail();
							Intent intent = new Intent(ScheduleDetailActivity.this, NewCollaborationActivity.class);
							intent.putExtra("fromType", 103);
							intent.putExtra("title", scheduleDetail.title);
							intent.putExtra("content", scheduleDetail.content);

							if (!TextUtils.isEmpty(scheduleDetail.shareOther)) {
								String[] uIds = scheduleDetail.shareOther.split(",");
								ArrayList<String> userIds = new ArrayList<>(uIds.length);
								Collections.addAll(userIds, uIds);
								intent.putStringArrayListExtra("userIds", userIds);
							}
							startActivityForResult(intent, -1);
						}
						dialog.dismiss();
					})
					.build();
		}

		mToolBar.setRightText(R.string.schedule_detail_title_right);
		mToolBar.setRightTextClickListener(v -> mMoreDialog.show());
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == K.schedule.share_other_request_code) {
			List<AddressBook> addressBooks = (List<AddressBook>) DataKeeper.getInstance().getKeepDatas(K.schedule.share_other_request_code);
			if (CommonUtil.isEmptyList(addressBooks)) {
				return;
			}
			StringBuilder names = new StringBuilder();
			StringBuilder ids = new StringBuilder();

			int size = addressBooks.size() - 1;
			for (int i = 0; i < size; i++) {
				AddressBook person = addressBooks.get(i);
				ids.append(person.userId).append(",");
				names.append(person.name).append(",");
			}

			ids.append(addressBooks.get(size).userId);
			names.append(addressBooks.get(size).name);

			new FEMaterialDialog.Builder(this)
					.setCancelable(true)
					.setMessage(getResources().getString(R.string.schedule_lbl_share_to) + names.toString())
					.setPositiveButton(R.string.collaboration_recorder_ok, v -> mPresenter.shareSchedule(ids.toString()))
					.setNegativeButton(R.string.collaboration_recorder_cancel,
							dialog -> DataKeeper.getInstance().removeKeepData(K.schedule.share_other_request_code))
					.build()
					.show();
		}
		else if (requestCode == schedule.modify_schedule_code && resultCode == Activity.RESULT_OK) {
//			mPresenter.start(getIntent());
			EventBus.getDefault().post(new EventRefresh(schedule.modify_schedule_code));
			finish();
		}
	}

	/**
	 * 获取日程详情成功
	 */
	@Override
	public void getScheduleDetailSuccess(AgendaDetailData detailData) {
		String sendUserId = detailData.sendUserId;
		String host = CoreZygote.getLoginUserServices().getServerAddress();
		CoreZygote.getAddressBookServices().queryUserDetail(sendUserId)
				.subscribe(userInfo -> {
					String sendUserName = (userInfo == null) ? "" : userInfo.name;
					String url = host + ((userInfo == null) ? "/helloworld" : userInfo.imageHref);
					FEImageLoader.load(this, mIvUserIcon, url, sendUserId, sendUserName);
					mTvScheduleSendUser.setText(sendUserName);
				}, error -> {
					FEImageLoader.load(this, mIvUserIcon, R.drawable.administrator_icon);
				});

		mTvScheduleTitle.setText(detailData.title);
		mTvScheduleSendTime.setText(formatTime(detailData.startTime));
		mTvScheduleStartTime.setText(formatTime(detailData.startTime));
		mTvScheduleEndTime.setText(formatTime(detailData.endTime));
		mPresenter.fetchPromptTime(detailData.promptTime);
		mPresenter.fetchRepeatTime(detailData.repeatTime);

		if (TextUtils.isEmpty(detailData.shareOther)) {
			mTvScheduleSharePersons.setText(getResources().getString(R.string.schedule_detail_lbl_share_none));
		}
		else {
			List<AddressBook> userInfos = CoreZygote.getAddressBookServices().queryUserIds(Arrays.asList(detailData.shareOther.split(",")));
			if (CommonUtil.isEmptyList(userInfos)) {
				mTvScheduleSharePersons.setText(getResources().getString(R.string.schedule_detail_lbl_share_none));
			}
			else {
				StringBuilder names = new StringBuilder();
				int size = userInfos.size() - 1;
				for (int i = 0; i < size; i++) {
					names.append(userInfos.get(i).name).append(",");
				}
				names.append(userInfos.get(size).name);
				mTvScheduleSharePersons.setText(names.toString());
			}
		}

		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.getSettings().setSupportZoom(true);
		mWebView.getSettings().setLoadWithOverviewMode(true);
		mWebView.getSettings().setBuiltInZoomControls(true);
		mWebView.getSettings().setDisplayZoomControls(false);

		StringBuilder sb = new StringBuilder(detailData.content);
		sb.append(ParticularContentView.HTML_STYLE_IMAGE_CENTER);
		if (detailData.content.contains("<table") || detailData.content.contains("</table>")) {
			sb.append(ParticularContentView.HTML_STYLE_TABLE_BORDER);
		}

		String content = sb.toString();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			mWebView.loadDataWithBaseURL(CoreZygote.getLoginUserServices().getServerAddress(), content,
					"text/html; charset=utf-8", "UTF-8", null);
		}
		else {
			mWebView.loadData(content, "text/html; charset=utf-8", "UTF-8");
		}

		if (FunctionManager.hasPatch(Patches.PATCH_SCHEDULE_REPLY)
				&& !CoreZygote.getLoginUserServices().getUserId().equals(detailData.sendUserId)) {
			mRlBottomReply.setVisibility(View.VISIBLE);
		}

		initMoreSetting();
	}

	/**
	 * 获取日程详情失败
	 */
	@Override
	public void getScheduleDetailFailed(String errorMessage) {
		new FEMaterialDialog.Builder(this)
				.setMessage(R.string.schedule_lbl_get_schedule_detail_failed)
				.setPositiveButton(null, dialog -> finish())
				.build()
				.show();
	}

	/**
	 * 删除日程成功
	 */
	@Override
	public void deleteScheduleSuccess(String scheduleId) {
		Intent intent = new Intent();
		intent.putExtra(K.schedule.schedule_id, scheduleId);
		setResult(K.schedule.detail_result_code, intent);
		finish();
	}

	/**
	 * 删除日程失败
	 */
	@Override
	public void deleteScheduleFailed() {
		FEToast.showMessage(getResources().getString(R.string.schedule_lbl_delete_schedule_failed));
	}

	/**
	 * 获取提醒时间
	 */
	@Override
	public void getPromptTimeValue(String promptTime) {
		mTvSchedulePromptTime.setText(getResources().getString(R.string.schedule_detail_lbl_promptTime) + promptTime);
	}

	/**
	 * 获取重复周期
	 */
	@Override
	public void getRepeatTimeValue(String repeatTime) {
		mTvScheduleRepeatTime.setText(getResources().getString(R.string.schedule_detail_lbl_repeatTime) + repeatTime);
	}

	/**
	 * 日程分享成功
	 */
	@Override
	public void shareOtherSuccess() {
		FEToast.showMessage(getResources().getString(R.string.schedule_lbl_share_success));
		setResult(K.schedule.share_result_code);
		finish();
	}

	/**
	 * 日程分享失败
	 */
	@Override
	public void shareOtherFailed() {
		FEToast.showMessage(getResources().getString(R.string.schedule_lbl_share_failed));
	}

	/**
	 * 日程同步成功
	 */
	@Override
	public void syncCalendarSuccess() {
		FEToast.showMessage(getResources().getString(R.string.schedule_lbl_sync_success));
	}

	/**
	 * 日程同步失败
	 */
	@Override
	public void syncCalendarFailed() {
		new FEMaterialDialog.Builder(this)
				.setMessage(getResources().getString(R.string.schedule_lbl_sync_failed))
				.setPositiveButton(null, dialog -> dialog.dismiss())
				.build()
				.show();
	}

	@Override
	public void showLoading() {
		if (mLoadingDialog == null) {
			mLoadingDialog = new FELoadingDialog.Builder(this).create();
		}
		mLoadingDialog.show();
	}

	@Override
	public void hideLoading() {
		if (mLoadingDialog != null) {
			mLoadingDialog.hide();
		}
		mLoadingDialog = null;
	}

	@Override
	public void getScheduleReplySuccess(List<ScheduleReply> replyList) {
		if (CommonUtil.isEmptyList(replyList)) {
			mLyReply.setVisibility(View.GONE);
		}
		else {
			mLyReply.setVisibility(View.VISIBLE);
			mTvReplyCount.setText(String.format(getResources().getString(R.string.reply_count_tip), replyList.size()));
			ScheduleReplyListAdapter replyAdapter = new ScheduleReplyListAdapter(this, R.layout.item_schedule_reply, replyList);
			mLvReplyContent.setAdapter(replyAdapter);
			replyAdapter.setClickListener(new ScheduleReplyClickListener() {
				@Override
				public void onDeleteClick(String replyId) {
					new Builder(ScheduleDetailActivity.this)
							.setMessage(R.string.schedule_del_reply_hint)
							.setPositiveButton(null, dialog -> mPresenter.deleteReply(replyId)).setNegativeButton(null, null)
							.build().show();
				}

				@Override
				public void onEditClick(String replyId, String content) {
					showReplyEditText(true, replyId, content);
				}
			});
		}
	}

	@Override
	public void getScheduleReplyFailed() {
		FEToast.showMessage(getString(R.string.schedule_get_reply_fail));
	}

	@Override
	public void deleteReplySuccess() {
		FEToast.showMessage(getString(R.string.schedule_del_reply_success));
	}

	@Override
	public void deleteReplyFailed() {
		FEToast.showMessage(getString(R.string.schedule_del_reply_failed));
	}

	@Override
	public void updateReplySuccess() {
		FEToast.showMessage(getString(R.string.schedule_update_reply_success));
		SpUtil.put(PreferencesUtils.SAVE_REPLY_DATA, "");
	}

	@Override
	public void updateReplyFailed() {
		FEToast.showMessage(getString(R.string.schedule_update_reply_failed));
	}

	@Override
	public void replySuccess() {
		FEToast.showMessage(getString(R.string.schedule_reply_success));
		SpUtil.put(PreferencesUtils.SAVE_REPLY_DATA, "");
	}

	@Override
	public void replyFailed() {
		FEToast.showMessage(getString(R.string.schedule_reply_failed));
	}

	private void showReplyEditText(boolean isUpdate, String replyId, String replyContent) {
		if (!TextUtils.equals(curReplyId, replyId)) {
			SpUtil.put(PreferencesUtils.SAVE_REPLY_DATA, "");
		}
		curReplyId = replyId;
		if (mReplyEditView != null) {
			return;
		}
		mReplyEditView = new ParticularReplyEditView(this);
		// 1. 初始化底部回复框
		WindowManager.LayoutParams params = new WindowManager.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG,
				WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
				PixelFormat.TRANSLUCENT);
		params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
		mWindowManager.addView(mReplyEditView, params);
		mReplyEditView.setFocusable(true);
		mReplyEditView.setMaxTextNum(REPLYTEXT_MAX);
		mReplyEditView.setOnRecordButtonClickListener(view -> FePermissions.with(this)
				.permissions(new String[]{Manifest.permission.RECORD_AUDIO})
				.rationaleMessage(getResources().getString(R.string.permission_rationale_record))
				.requestCode(PermissionCode.RECORD)
				.request());
		mReplyEditView.setAttachmentButtonVisibility(View.GONE);
		mReplyEditView.setOnTouchListener((view, event) -> {
			if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
				hindReplyVivewTime = System.currentTimeMillis();
				replyStr = mReplyEditView.getReplyContent();
				removeReplyEditView();
			}
			return true;
		});
		if (isUpdate) {
			mReplyEditView.setEditTextContent(replyContent);
		}
		else if (!TextUtils.isEmpty(replyStr)) {
			mReplyEditView.setEditTextContent(replyStr);
		}
		mReplyEditView.setOnKeyListener((v, keyCode, event) -> {
			if (event.getKeyCode() == KeyEvent.KEYCODE_BACK || event.getKeyCode() == KeyEvent.KEYCODE_SETTINGS) {
				replyStr = mReplyEditView.getReplyContent();
				removeReplyEditView();
			}
			return false;
		});
		mHandler.postDelayed(showKeyBordRunnable, 50);
		mReplyEditView.setOnReplySubmitClickListener(view -> {
			if (TextUtils.isEmpty(mReplyEditView.getReplyContent())) {
				FEToast.showMessage(R.string.input_reply);
				return;
			}
			if (isUpdate) {
				mPresenter.updateReply(replyId, mReplyEditView.getReplyContent());
			}
			else {
				mPresenter.reply(mReplyEditView.getReplyContent());
			}
			replyStr = null;
			if (mReplyEditView != null) {
				removeReplyEditView();
			}
		});
	}

	private void removeReplyEditView() {
		if (mReplyEditView == null || mWindowManager == null) {
			return;
		}
		mHandler.removeCallbacks(showKeyBordRunnable);
		DevicesUtil.hideKeyboard(mReplyEditView.getReplyEditText());
		if (mReplyEditView.getWindowToken() != null) {
			mWindowManager.removeView(mReplyEditView);
		}
		mReplyEditView = null;
	}

	private Runnable showKeyBordRunnable = () -> ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
			.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);

	@PermissionGranted(PermissionCode.RECORD)
	public void onRecordPermissionGranted() {
		if (mVoiceInput != null) mVoiceInput.show();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		DataKeeper.getInstance().removeKeepData(K.schedule.share_other_request_code);
		SpUtil.put(PreferencesUtils.SAVE_REPLY_DATA, "");
	}

	private String formatTime(String time) {
		if (time.length() > 16) {
			return time.substring(0, 16);
		}
		return time;
	}
}
