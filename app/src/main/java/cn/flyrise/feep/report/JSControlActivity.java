/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2013-4-2 上午11:32:51
 */
package cn.flyrise.feep.report;

import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import cn.flyrise.feep.core.common.X.FormNullCheck;
import cn.flyrise.feep.core.common.X.JSControlType;
import cn.flyrise.feep.core.network.TokenInject;
import com.borax12.materialdaterangepicker.DateTimePickerDialog;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cn.flyrise.android.library.utility.LoadingHint;
import cn.flyrise.android.protocol.entity.ReferenceItemsRequest;
import cn.flyrise.android.protocol.model.ReferenceItem;
import cn.flyrise.android.shared.utility.FEPopupWindow;
import cn.flyrise.android.shared.utility.FEPopupWindow.OnActionChangeListener;
import cn.flyrise.android.shared.utility.datepicker.FEDatePicker;
import cn.flyrise.android.shared.utility.picker.FEPicker;
import cn.flyrise.feep.R;
import cn.flyrise.feep.commonality.bean.JSControlInfo;
import cn.flyrise.feep.core.common.X;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.DateUtil;
import cn.flyrise.feep.core.common.utils.GsonUtil;
import cn.flyrise.feep.core.network.listener.OnProgressUpdateListenerImpl;
import cn.flyrise.feep.core.network.request.FileRequest;
import cn.flyrise.feep.core.network.request.FileRequestContent;
import cn.flyrise.feep.core.network.uploader.UploadManager;
import cn.flyrise.feep.form.FormPersonChooseActivity;
import cn.flyrise.feep.form.been.FormCommonWordInfo;
import cn.flyrise.feep.form.been.FormPersonCollection;
import cn.flyrise.feep.form.been.ExecuteResult;
import cn.flyrise.feep.form.been.MeetingBoardData;
import cn.flyrise.feep.media.common.LuBan7;

public class JSControlActivity extends ReportWebViewActivity {

	/**
	 * 添加附件请求码
	 */
	private final int ADD_ATTACHMENT_REQUEST_CODE = 100;
	private final int ADD_PERSON_REQUEST_CODE = 200;
	private final int INTENT_TO_MEETINGBOARD_REQUEST_CODE = 300;

	private final Handler handler = new Handler();

	private JSControlInfo controlInfo;
	/**
	 * 用于表示是否可以相应JS，防止退出此activity后再响应导致的bug
	 */
	protected boolean isResponseAble;

	private ArrayList<String> mSelectedAttachments;

	private FormPersonCollection checkedPersonCollection = new FormPersonCollection();

	private static String attachmentGUID = null;

	private FEPicker spinnerPicker;

	private List<ReferenceItem> spinnerItems;

	/**
	 * 是否从报表界面跳转过来
	 */
	protected static boolean reportForm = false;

	/**
	 * webView 可见区域高度
	 */
	private int mWebViewHeight;
	/**
	 * 触摸 webView 时的Y坐标
	 */
	private int touchY;
	private int rawY;

	@Override
	protected void onResume() {
		super.onResume();
		isResponseAble = true;
	}

	@Override
	public void bindData() {
		super.bindData();
		spinnerPicker = new FEPicker(this);
		spinnerPicker.setCyclic(false);
	}

	@Override
	public void bindListener() {
		super.bindListener();
		mWebView.setOnTouchListener((v, event) -> {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				mWebView.requestFocus();
				mWebViewHeight = mWebView.getMeasuredHeight();
				rawY = (int) event.getRawY();
				touchY = (int) event.getY();
				FELog.i("rawY:" + rawY + "--Y:" + touchY);
			}
			return false;
		});
		spinnerPicker.setOnPickerButtonClickListener(pickerButtonListener);
		/*--点击重新加载--*/
		spinnerPicker.setOnActionChangeLisenter(actionChangeListener);
	}

	private final OnActionChangeListener actionChangeListener = new OnActionChangeListener() {
		@Override
		public void show(FEPopupWindow pw, View contentView) {
			if (pw instanceof FEDatePicker) {
				spinnerPicker.dismiss();
			}
			final int viewDistance = getViewDistance();// 输入框离屏幕底部的距离
			final int contentViewHight = pw.getContentViewHight();// 获取控件的高度
			final int scrollDistance = rawY - touchY;// 计算Webview滚动的距离
			if (contentViewHight > viewDistance) {
				mWebView.scrollBy(0, contentViewHight - viewDistance + scrollDistance);
			}
		}

		@Override
		public void dismiss(FEPopupWindow pw, View contentView) {
			final int viewDistance = getViewDistance();
			final int contentViewHight = pw.getContentViewHight();
			if (contentViewHight > viewDistance) {
				mWebView.scrollTo(0, rawY - touchY);
			}
			else {
				mWebView.scrollTo(0, 0);
			}
		}

		/**
		 * 获取输入框离屏幕底部的距离
		 */
		private int getViewDistance() {
			return mWebViewHeight - touchY;
		}
	};

	@Override
	public void webViewSetting(WebView webView) {
		super.webViewSetting(webView);
		webView.addJavascriptInterface(new Controller(), "androidJS");
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case ADD_ATTACHMENT_REQUEST_CODE:// 添加附件返回来的结果
				if (data != null) {
					ArrayList<String> selectedAttachments = data.getStringArrayListExtra("extra_local_file");
					addSelectedAttachments(selectedAttachments);
					if (controlInfo != null) {
						final ExecuteResult info = createSentToJsData(getSelectedAttachmentCount(), null, null);
						sendToJavascript(info.getProperties());
					}
				}
				break;
			case ADD_PERSON_REQUEST_CODE:// 添加人员来的结果
				if (data != null) {
					checkedPersonCollection = (FormPersonCollection) data.getSerializableExtra("CheckedPersons");
					if (controlInfo != null) {
						ExecuteResult info = createSentToJsData(getSelectedAttachmentCount(), null, null);
						sendToJavascript(info.getProperties());
					}
				}
				break;
			case INTENT_TO_MEETINGBOARD_REQUEST_CODE:// 从会议看板回来
				if (data != null) {
					if (controlInfo != null) {
						MeetingBoardData meetingBoardData = (MeetingBoardData) data.getSerializableExtra("MeetingBoardData");
						ExecuteResult info = createSentToJsData(getSelectedAttachmentCount(), null, null);
						info.setMeetingBoardData(meetingBoardData);
						sendToJavascript(info.getProperties());
					}
				}
				break;

			default:
				break;
		}
	}

	private ExecuteResult createSentToJsData(int attachmentCount, String dateValue, List<ReferenceItem> referenceItems) {
		final ExecuteResult info = new ExecuteResult();
		info.setUiControlType(controlInfo.getUiControlType());
		info.setUiControlId(controlInfo.getUiControlId());
		info.setAttachmentCount(attachmentCount);
		info.setDateValue(dateValue);
		info.setReferenceItems(referenceItems);
		info.setIdItems(checkedPersonCollection == null ? null : checkedPersonCollection.getPersonArray());
		FELog.i("发送：" + info.getProperties().toString());
		return info;
	}

	/**
	 * 向js发送数据
	 */
	protected void sendToJavascript(JSONObject jsonObject) {
		String javaScript = "jsBridge.trigger('SetWebHTMLEditorContent'," + jsonObject + ")";
		TokenInject.setCookie(this, mWebView);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			mWebView.evaluateJavascript(javaScript, value -> {
			});
		}
		else {
			mWebView.loadUrl("javascript:" + javaScript);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		isResponseAble = false;
		if (spinnerPicker != null && spinnerPicker.isShowing()) {
			spinnerPicker.dismiss();
		}
	}

	private void openDatePicket(String dateTime, String format) {
		Calendar calendar = DateUtil.str2Calendar(dateTime);
		DateTimePickerDialog dateTimePickerDialog = new DateTimePickerDialog();
		dateTimePickerDialog.setTimeLevel(DateUtil.getTimeLevelForFormat(format));
		dateTimePickerDialog.setDateTime(calendar);
		dateTimePickerDialog.setButtonCallBack(new DateTimePickerDialog.ButtonCallBack() {
			@Override
			public void onClearClick() {
				sendToJavascript(createSentToJsData(0, "", null).getProperties());
			}

			@Override
			public void onOkClick(Calendar calendar, DateTimePickerDialog dateTimePickerDialog) {
				String data;
//                record = DateUtil.calendar2StringDateTimeSs(calendar);
				data = DateUtil.calendar2StringDateTime(calendar);
				sendToJavascript(createSentToJsData(0, data, null).getProperties());
				dateTimePickerDialog.dismiss();
			}
		});
		dateTimePickerDialog.setCanClear(true);
		dateTimePickerDialog.show(this.getFragmentManager(), "dateTimePickerDialog");
	}

	private FEPicker.OnPickerButtonClickListener pickerButtonListener = new FEPicker.OnPickerButtonClickListener() {
		@Override
		public void OnButtonClick(View view, String data) {
			final List<ReferenceItem> items = new ArrayList<>();
			if (TextUtils.isEmpty(data)) {
				final ReferenceItem item = new ReferenceItem();
				item.setKey("");
				item.setValue("");
				items.add(item);
			}
			else if (spinnerItems != null) {
				// 判断是否是报表界面过来的，是那么取值getvalue，否那么取值getkey
//                if (reportForm) {
				for (final ReferenceItem item : spinnerItems) {
					if (data.equals(item.getValue())) {
						items.add(item);
					}
				}
			}
			spinnerPicker.dismiss();
			sendToJavascript(createSentToJsData(0, null, items).getProperties());
		}
	};

	/**
	 * html控件点击事件的响应
	 */
	private class Controller {

		@JavascriptInterface
		public void runOnAndroidJavaScript(final String jsonStr) {
			FELog.i("dd", "--->>> js返回的json" + jsonStr);
			handler.post(new Runnable() {

				@Override
				public void run() {
					if (!isResponseAble) {
						return;
					}
					String iqContent;
					try {
						final JSONObject properties = new JSONObject(jsonStr);
						final JSONObject iq = properties.getJSONObject("userInfo");
						iqContent = iq.toString();
						controlInfo = GsonUtil.getInstance().fromJson(JSControlInfo.formatJsonString(iqContent), JSControlInfo.class);
						final int controlType = controlInfo.getUiControlType();
						if (controlType == JSControlType.Date) {// 点击日期
							JSControlActivity.this
									.runOnUiThread(() -> openDatePicket(controlInfo.getControlDefaultData(), controlInfo.getDataFormat()));
						}
						else if (controlType == JSControlType.Person) {// 点击选择人员
							clickPersonChoose();
						}
						else if (controlType == JSControlType.Attachment) {// 点击添加附件按钮
							clickAddAttachment();
						}
						else if (controlType == JSControlType.Reference) {// 点击参照项按钮
							clickReference();
							FELog.i("dd", "2");
						}
						else {// 点击发送按钮后返回数据（2222222）
							clickSendButton();
						}
					} catch (final Exception e) {
						e.printStackTrace();
					}
				}

				private void clickCommonWord() {
					final FormCommonWordInfo info = new FormCommonWordInfo();
					info.setUiControlId(controlInfo.getUiControlId());
					info.setUiControlType(controlInfo.getUiControlType());

					LoadingHint.show(JSControlActivity.this);

					final ReferenceItemsRequest commonWordsReq = new ReferenceItemsRequest();
					commonWordsReq.setRequestType(ReferenceItemsRequest.TYPE_COMMON_WORDS);
				}

				/** 点击发送按钮后返回数据调用此方法 */
				private void clickSendButton() {
					boolean isLoadingHint = true;
					FELog.i("ddd", "-->>>controlInfo:---响应：" + controlInfo.getActionType());
					final int actionType = controlInfo.getActionType();
					if (actionType == X.JSActionType.Error) {
						LoadingHint.hide();
						return;
					}
					else if (actionType == X.JSActionType.Send) {
						JSActionSend(controlInfo);
						return;
					}
					else if (actionType == X.JSActionType.FetchData) { // 选择人员那一步-----------------
						JSActionGetData(controlInfo);
						return;
					}
					else if (actionType == X.JSActionType.Search) {
						JSActionSearch(controlInfo);
						return;
					}
					final int nullCheckResult = controlInfo.getNullCheckResult(); // 非空性检查的结果
					if (nullCheckResult == FormNullCheck.Null
							&& actionType == X.JSActionType.Check) {
						FEToast.showMessage(JSControlActivity.this.getString(R.string.form_need_input));
					}
					else if (nullCheckResult == FormNullCheck.NonNull) {
						if (uploadFile(attachmentGUID)) {// 有附件先上传附件,并且不隐藏加载框
							isLoadingHint = false;
						}
						else {
							isLoadingHint = true;
							doAfterCheck(controlInfo);
						}
					}
					else if (nullCheckResult == FormNullCheck.DataExist) {
						FEToast.showMessage(getString(R.string.form_null_checked_exit_data));
					}
					else if (nullCheckResult == FormNullCheck.NonFormID) {
						FEToast.showMessage(getString(R.string.form_null_checked_non_formid));
					}
					// 显示toast后隐藏loading
					if (isLoadingHint) {
						LoadingHint.hide();
					}
				}

				/** 点击参照项按钮调用此方法 */
				private void clickReference() {
					spinnerItems = controlInfo.getReferenceItems();
					final List<String> values = new ArrayList<>();
					if (spinnerItems != null) {
						for (final ReferenceItem referenceItem : spinnerItems) {
							values.add(referenceItem.getValue());
						}
					}
					spinnerPicker.showWithData(values);
				}

				/** 点击添加附件调用此方法 */
				private void clickAddAttachment() {
					attachmentGUID = controlInfo.getAttachmentGUID();
					LuBan7.pufferGrenades(JSControlActivity.this, mSelectedAttachments, null, ADD_ATTACHMENT_REQUEST_CODE);
				}

				/** 点击选择人员调用此方法 */
				private void clickPersonChoose() {
					final Intent intent = new Intent(JSControlActivity.this, FormPersonChooseActivity.class);
					intent.putExtra("NewFormChooseNodeData", controlInfo);
					startActivityForResult(intent, ADD_PERSON_REQUEST_CODE);
				}
			});
		}
	}

	/***
	 * 上传附件
	 */
	private boolean uploadFile(String attachmentGUID) {
		if (getSelectedAttachmentCount() <= 0) {
			return false;
		}

		final FileRequestContent fileRequestContent = new FileRequestContent();
		fileRequestContent.setAttachmentGUID(attachmentGUID);
		fileRequestContent.setFiles(CommonUtil.isEmptyList(mSelectedAttachments)
				? new ArrayList<>()
				: mSelectedAttachments);

		final FileRequest fileRequest = new FileRequest();
		fileRequest.setFileContent(fileRequestContent);

		new UploadManager(this)
				.fileRequest(fileRequest)
				.progressUpdateListener(new OnProgressUpdateListenerImpl() {
					@Override
					public void onPreExecute() {
						LoadingHint.show(JSControlActivity.this);
					}

					@Override
					public void onProgressUpdate(long currentBytes, long contentLength, boolean done) {
						int progress = (int) (currentBytes * 100 / contentLength * 1.0F);
						LoadingHint.showProgress(progress);
					}

//                    @Override
//                    public void (final Response response) {
//                        LoadingHint.hide();
//                        doAfterCheck(controlInfo);
//                    }
				})
				.execute();
		return true;
	}

	/**
	 * 新建表单的发送
	 */
	protected void JSActionSend(JSControlInfo controlInfo) {
	}

	/**
	 * 处理表单的下一步
	 */
	protected void JSActionGetData(JSControlInfo controlInfo) {
	}

	/**
	 * 报表搜索
	 */
	protected void JSActionSearch(JSControlInfo controlInfo) {
	}

	/**
	 * 进行非空检查后，非空所需要做的事
	 */
	protected void doAfterCheck(JSControlInfo controlInfo) {
		if (!LoadingHint.isLoading()) {
			LoadingHint.show(this);
		}
	}

	public void addSelectedAttachments(List<String> selectedAttachments) {
		if (mSelectedAttachments == null) {
			mSelectedAttachments = new ArrayList<>();
		}

		mSelectedAttachments.clear();
		mSelectedAttachments.addAll(selectedAttachments);
	}

	public int getSelectedAttachmentCount() {
		return CommonUtil.isEmptyList(mSelectedAttachments) ? 0 : mSelectedAttachments.size();
	}
}
