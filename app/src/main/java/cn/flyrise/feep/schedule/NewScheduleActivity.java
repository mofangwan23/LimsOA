package cn.flyrise.feep.schedule;

import static jp.wasabeef.richeditor.Utils.tryAddHostToImageBeforeEdit;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Html;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.EditText;
import cn.flyrise.feep.core.network.entry.AttachmentBean;
import cn.flyrise.feep.K;
import cn.flyrise.feep.R;
import cn.flyrise.feep.addressbook.utils.ContactsIntent;
import cn.flyrise.feep.collaboration.activity.NewCollaborationActivity;
import cn.flyrise.feep.collaboration.activity.RichTextEditActivity;
import cn.flyrise.feep.collaboration.utility.RichTextContentKeeper;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.base.component.BaseEditableActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.common.DataKeeper;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.DateUtil;
import cn.flyrise.feep.core.dialog.FELoadingDialog;
import cn.flyrise.feep.core.dialog.FEMaterialDialog;
import cn.flyrise.feep.core.services.model.AddressBook;
import cn.flyrise.feep.email.views.UrlImageParser;
import cn.flyrise.feep.schedule.view.SchedulePreferenceView;
import cn.squirtlez.frouter.annotations.Route;
import com.borax12.materialdaterangepicker.DateTimePickerDialog;
import com.jakewharton.rxbinding.view.RxView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author ZYP
 * @since 2016-11-28 10:15
 */
@Route("/schedule/native/new")
public class NewScheduleActivity extends BaseEditableActivity implements NewScheduleContract.IView {

	private final static int EDIT_RICH_SCHEDULE_CONTENT_CODE = 201;
	private EditText mEtScheduleTitle;
	private EditText mEtScheduleContent;

	private WebView mScheduleWebView;
	private SchedulePreferenceView mStartTimeView;
	private SchedulePreferenceView mEndTimeView;
	private SchedulePreferenceView mPromptView;
	private SchedulePreferenceView mRepeatView;
	private SchedulePreferenceView mShareView;

	private Calendar mStartCalendar;
	private Calendar mEndCalendar;
	private FELoadingDialog mLoadingDialog;
	private NewScheduleContract.IPresenter mPresenter;

	public static void startActivity(Activity context, String date) {
		Intent intent = new Intent(context, NewScheduleActivity.class);
		intent.putExtra(K.schedule.schedule_default_date, date);
		context.startActivityForResult(intent, K.schedule.new_request_code);
	}

	public static void startActivityFromWorkPlan(Activity activity, String tile, String content, List<AttachmentBean> attachmentList) {
		Intent intent = new Intent(activity, NewScheduleActivity.class);
		intent.putExtra("title", tile);
		intent.putExtra("content", content);
		intent.putExtra("isFromWorkPlan", true);
		intent.putParcelableArrayListExtra("attachment", (ArrayList<? extends Parcelable>) attachmentList);
		activity.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPresenter = new NewSchedulePresenter(this);
		setContentView(R.layout.activity_new_schedule);
		mPresenter.start(getIntent());
	}

	@Override
	protected void toolBar(FEToolbar toolbar) {
		String marsterKey = getIntent().getStringExtra("marsterKey");
		toolbar.setTitle(TextUtils.isEmpty(marsterKey) ? R.string.schedule_new_title : R.string.schedule_update_title);
		toolbar.setNavigationOnClickListener(v -> {
			if (isHasWrote()) {
				showExitDialog();
			}
			else {
				finish();
			}
		});
	}

	@Override
	public void bindView() {
		mEtScheduleTitle = (EditText) findViewById(R.id.etScheduleTitle);
		mEtScheduleContent = (EditText) findViewById(R.id.etScheduleContent);

		if (VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
			mScheduleWebView = (WebView) findViewById(R.id.scheduleWebView);
			mScheduleWebView.getSettings().setAppCacheEnabled(true);
			mScheduleWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
		}

		mStartTimeView = (SchedulePreferenceView) findViewById(R.id.scheduleStartTime);
		mEndTimeView = (SchedulePreferenceView) findViewById(R.id.scheduleEndTime);
		mPromptView = (SchedulePreferenceView) findViewById(R.id.schedulePromptTime);
		mRepeatView = (SchedulePreferenceView) findViewById(R.id.scheduleRepeatTime);
		mShareView = (SchedulePreferenceView) findViewById(R.id.scheduleSharePerson);

		mPromptView.setScheduleText(R.string.schedule_detail_lbl_share_none);
		mRepeatView.setScheduleText(R.string.schedule_detail_lbl_never);
	}

	@Override
	public void bindListener() {
		View submitBtn = findViewById(R.id.btnScheduleSubmit);
		RxView.clicks(submitBtn)
				.throttleFirst(1, TimeUnit.SECONDS)
				.subscribe(a -> {
					String title = mEtScheduleTitle.getText().toString();
					String content = tryTransformImagePath();
					String startTime = mStartTimeView.getScheduleText();
					String endTime = mEndTimeView.getScheduleText();
					mPresenter.saveSchedule(title, content, startTime, endTime);
				});

		mStartTimeView.setOnClickListener(view -> selectTime(view));
		mEndTimeView.setOnClickListener(view -> selectTime(view));
		mPromptView.setOnClickListener(view -> selectRepeatPromptTime(view, mPresenter.getPromptValues()));
		mRepeatView.setOnClickListener(view -> selectRepeatPromptTime(view, mPresenter.getRepeatValues()));

		mShareView.setOnClickListener(view -> {
			int key = NewScheduleActivity.this.hashCode();
			if (CommonUtil.nonEmptyList(mPresenter.getSeletedUsers())) {
				DataKeeper.getInstance().keepDatas(key, mPresenter.getSeletedUsers());
			}
			new ContactsIntent(NewScheduleActivity.this)
					.requestCode(K.schedule.share_person_request_code)
					.targetHashCode(key)
					.userCompanyOnly()
					.exceptSelf()
					.title(CommonUtil.getString(R.string.schedule_title_share_other))
					.withSelect()
					.open();
		});

		if (VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
			mEtScheduleContent.setVisibility(View.GONE);
			mScheduleWebView.setVisibility(View.VISIBLE);
			View richContentLayout = findViewById(R.id.layoutRichContent);
			richContentLayout.setOnClickListener(view -> {
				Intent intent = new Intent(NewScheduleActivity.this, RichTextEditActivity.class);
				intent.putExtra("title", "编辑日程");
				startActivityForResult(intent, EDIT_RICH_SCHEDULE_CONTENT_CODE);
			});

			GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
				@Override
				public boolean onSingleTapUp(MotionEvent e) {
					Intent intent = new Intent(NewScheduleActivity.this, RichTextEditActivity.class);
					intent.putExtra("title", "编辑日程");
					startActivityForResult(intent, EDIT_RICH_SCHEDULE_CONTENT_CODE);
					return false;
				}
			});

			mScheduleWebView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == K.schedule.share_person_request_code) {
			List<AddressBook> addressBooks = (List<AddressBook>) DataKeeper.getInstance().getKeepDatas(NewScheduleActivity.this.hashCode());
			mPresenter.setSeletedUsers(addressBooks);
		}
		else if (requestCode == EDIT_RICH_SCHEDULE_CONTENT_CODE && resultCode == RESULT_OK) {
			if (RichTextContentKeeper.getInstance().hasContent()) {
				mEtScheduleContent.setVisibility(View.GONE);
				if (mScheduleWebView.getVisibility() != View.VISIBLE) {
					mScheduleWebView.setVisibility(View.VISIBLE);
				}

				String richTextContent = tryTransformImagePath();
				mScheduleWebView.loadDataWithBaseURL(CoreZygote.getLoginUserServices().getServerAddress(),
						NewCollaborationActivity.IMAGE_STYLE + richTextContent, "text/html; charset=utf-8", "UTF-8", null);
			}
			else {
				mScheduleWebView.loadUrl("");
			}
		}
	}

	private void selectRepeatPromptTime(View view, List<String> values) {
		if (CommonUtil.isEmptyList(values)) {
			String message = getResources().getString(view == mPromptView
					? R.string.schedule_get_prompt_failed : R.string.schedule_get_repeat_failed);
			FEToast.showMessage(message);
			return;
		}

		new FEMaterialDialog.Builder(this)
				.setWithoutTitle(true)
				.setItems(values.toArray(new String[]{}), (dialog, v, position) -> {
					((SchedulePreferenceView) view).setScheduleText(values.get(position));
					if (view == mPromptView) {
						mPresenter.setPrompt(position);
					}
					else {
						mPresenter.setRepeat(position);
					}
					dialog.dismiss();
				})
				.build()
				.show();
	}

	private void selectTime(View view) {
		DateTimePickerDialog dateTimePickerDialog = new DateTimePickerDialog();
		Calendar calendar = view == mStartTimeView ? mStartCalendar : mEndCalendar;
		dateTimePickerDialog.setDateTime(calendar);
		dateTimePickerDialog.setTimeLevel(DateTimePickerDialog.TIME_LEVEL_MIN);
		dateTimePickerDialog.setButtonCallBack(new DateTimePickerDialog.ButtonCallBack() {
			@Override
			public void onClearClick() {
			}

			@Override
			public void onOkClick(Calendar calendar, DateTimePickerDialog dateTimePickerDialog) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
				String format = sdf.format(calendar.getTime());
				((SchedulePreferenceView) view).setScheduleText(format);
				dateTimePickerDialog.dismiss();
				if (view == mStartTimeView) {
					mStartCalendar = calendar;
				}
				else {
					mEndCalendar = calendar;
				}
			}
		});
//        dateTimePickerDialog.setMinCalendar(Calendar.getInstance());
		dateTimePickerDialog.show(getFragmentManager(), "dateTimePickerDialog");
	}

	@Override
	public void initNewSchedule(String title, String content, String startTime, String endTime) {
		// 初始化日程标题和内容，如果内容不为空的话
		mEtScheduleTitle.setText(title);

		// 富文本编辑框搞一下
		if (!TextUtils.isEmpty(content)) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
				mEtScheduleContent.setVisibility(View.GONE);
				mScheduleWebView.setVisibility(View.VISIBLE);
				mScheduleWebView.loadDataWithBaseURL(CoreZygote.getLoginUserServices().getServerAddress(),
						NewCollaborationActivity.IMAGE_STYLE + content, "text/html; charset=utf-8", "UTF-8", null);

				RichTextContentKeeper.getInstance().setRichTextContent(tryAddHostToImageBeforeEdit(content));
			}
			else {
				mEtScheduleContent.setVisibility(View.VISIBLE);
				mEtScheduleContent.setText(Html.fromHtml(content, new UrlImageParser(mEtScheduleContent,
						CoreZygote.getLoginUserServices().getServerAddress()), null));
			}
		}
		mEtScheduleContent.setText(TextUtils.isEmpty(content) ? content : Html.fromHtml(content));        // 这里可能需要进行一下转换

		// 初始化日程开始时间和结束时间
		mStartTimeView.setScheduleText(startTime);
		mEndTimeView.setScheduleText(endTime);

		// 初始化日历对象
		this.mStartCalendar = Calendar.getInstance();
		this.mStartCalendar.setTime(DateUtil.strToDate(startTime, "yyyy-MM-dd HH:mm"));

		this.mEndCalendar = Calendar.getInstance();
		this.mEndCalendar.setTime(DateUtil.strToDate(endTime, "yyyy-MM-dd HH:mm"));

		// 初始化分享人
		mShareView.setScheduleText(R.string.schedule_detail_lbl_share_none);
		ArrayList<String> userIds = getIntent().getStringArrayListExtra("userIds");
		if (CommonUtil.nonEmptyList(userIds)) {
			List<AddressBook> addressBooks = CoreZygote.getAddressBookServices().queryUserIds(userIds);
			mPresenter.setSeletedUsers(addressBooks);
		}
	}

	@Override
	public void configRepeatTime(List<String> repeatTimeList, String defaultValue) {
		if (CommonUtil.nonEmptyList(repeatTimeList)) {
			mRepeatView.setScheduleText(TextUtils.isEmpty(defaultValue) ? repeatTimeList.get(0) : defaultValue);
		}
		else {
			mRepeatView.setScheduleText(R.string.schedule_detail_lbl_never);  // 无 value is 0
			mRepeatView.setClickable(false);
		}
	}

	@Override
	public void configPromptTime(List<String> promptTimeList, String defaultValue) {
		if (CommonUtil.nonEmptyList(promptTimeList)) {
			mPromptView.setScheduleText(TextUtils.isEmpty(defaultValue) ? promptTimeList.get(0) : defaultValue);
		}
		else {
			mPromptView.setScheduleText(R.string.schedule_detail_lbl_share_none);       // 无 value is 0.
			mPromptView.setClickable(false);
		}
	}

	@Override
	public void saveScheduleSuccess() {
		if (mPresenter.isEdit()) {
			FEToast.showMessage(getResources().getString(R.string.schedule_update_save_success));
			setResult(Activity.RESULT_OK);
		}
		else {
			FEToast.showMessage(getResources().getString(R.string.schedule_new_save_success));
			setResult(K.schedule.new_result_code);
		}
		finish();
	}

	@Override
	public void saveScheduleFailed(String errorMessage) {
		if (!TextUtils.isEmpty(errorMessage)) {
			FEToast.showMessage(errorMessage);
		}
	}

	@Override
	public void setSelectedUsers(String selectedUserNames) {
		mShareView.setScheduleText(selectedUserNames);
	}

	@Override
	public void showLoading() {
		if (mLoadingDialog == null) {
			mLoadingDialog = new FELoadingDialog.Builder(this).setCancelable(true).create();
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
	protected void onDestroy() {
		RichTextContentKeeper.getInstance().removeCache();
		RichTextContentKeeper.getInstance().removeCompressImagePath();
		super.onDestroy();
		DataKeeper.getInstance().removeKeepData(NewScheduleActivity.this.hashCode());
	}

	/**
	 * 判断用户是否在此页面填写过东西
	 */
	private boolean isHasWrote() {
		String titleText = mEtScheduleTitle.getText().toString().trim();
		boolean hasSelectedUsers = CommonUtil.nonEmptyList(mPresenter.getSeletedUsers());
		boolean hasTitle = !TextUtils.isEmpty(titleText);
		if (VERSION.SDK_INT < VERSION_CODES.KITKAT) {
			String contentText = mEtScheduleContent.getText().toString().trim();
			return hasTitle || hasSelectedUsers || !TextUtils.isEmpty(contentText);
		}
		return hasTitle || hasSelectedUsers || RichTextContentKeeper.getInstance().hasContent();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (isHasWrote()) {
				showExitDialog();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	private String tryTransformImagePath() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
			return mEtScheduleContent.getText().toString();
		}

		if (!RichTextContentKeeper.getInstance().hasContent()) {
			return null;
		}

		List<String> compressImagePaths = RichTextContentKeeper.getInstance().getCompressImagePaths();
		String richText = RichTextContentKeeper.getInstance().getRichTextContent();
		richText = richText.replace(CoreZygote.getLoginUserServices().getServerAddress(), "");
		if (CommonUtil.isEmptyList(compressImagePaths)) {
			return richText;
		}

		for (String path : compressImagePaths) {
			String url = "/AttachmentServlet39?attachPK="
					+ RichTextContentKeeper.getInstance().getGUIDByLocalPath(path)
					+ "&actionType=download";
			richText = richText.replace(path, url);
		}
		return richText;
	}
}
