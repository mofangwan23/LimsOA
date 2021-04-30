package cn.flyrise.feep.cordova.presenter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import cn.flyrise.android.library.utility.LoadingHint;
import cn.flyrise.android.protocol.entity.CommonResponse;
import cn.flyrise.android.protocol.entity.ReferenceItemsRequest;
import cn.flyrise.android.protocol.entity.ReferenceItemsResponse;
import cn.flyrise.android.protocol.model.ReferenceItem;
import cn.flyrise.android.shared.utility.FEPopupWindow;
import cn.flyrise.android.shared.utility.FEPopupWindow.OnActionChangeListener;
import cn.flyrise.android.shared.utility.datepicker.FEDatePicker;
import cn.flyrise.android.shared.utility.picker.FEPicker;
import cn.flyrise.feep.FEApplication;
import cn.flyrise.feep.R;
import cn.flyrise.feep.commonality.CommonWordsActivity;
import cn.flyrise.feep.commonality.bean.JSControlInfo;
import cn.flyrise.feep.commonality.bean.JsSendServiceItem;
import cn.flyrise.feep.commonality.util.RemoveAD;
import cn.flyrise.feep.cordova.CordovaContract;
import cn.flyrise.feep.cordova.utils.FEWebViewJsUtil;
import cn.flyrise.feep.cordova.utils.SendContactsJs;
import cn.flyrise.feep.cordova.utils.SendRecordJs;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.X.JSControlType;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.DateUtil;
import cn.flyrise.feep.core.common.utils.DevicesUtil;
import cn.flyrise.feep.core.common.utils.GsonUtil;
import cn.flyrise.feep.core.dialog.FEMaterialDialog;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.core.network.entry.RecordItem;
import cn.flyrise.feep.core.network.listener.OnProgressUpdateListenerImpl;
import cn.flyrise.feep.core.network.request.FileRequest;
import cn.flyrise.feep.core.network.request.FileRequestContent;
import cn.flyrise.feep.core.network.uploader.UploadManager;
import cn.flyrise.feep.form.been.ExecuteResult;
import cn.flyrise.feep.form.been.FormCommonWordInfo;
import cn.flyrise.feep.form.been.FormPersonCollection;
import cn.flyrise.feep.form.been.MeetingBoardData;
import cn.flyrise.feep.media.attachments.AttachmentViewer;
import cn.flyrise.feep.media.attachments.bean.Attachment;
import cn.flyrise.feep.media.attachments.bean.TaskInfo;
import cn.flyrise.feep.media.attachments.downloader.DownloadConfiguration;
import cn.flyrise.feep.media.attachments.listener.SimpleAttachmentViewerListener;
import cn.flyrise.feep.utils.ParseCaptureUtils;
import com.borax12.materialdaterangepicker.DateTimePickerDialog;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;

/**
 * 陈冕
 * Created by Administrator on 2016-3-24.
 */
public class CordovaPresenter implements CordovaContract.CordovaPresenters {

	private static String attachmentGUID = null;
	private JSControlInfo controlInfo;
	private String recordTime = "";

	public static boolean isResponseAble = true;//用于表示界面是否终止，防止退出此activity后再响应导致的bug
	private CordovaContract.CordovaView mCordovaView;
	private FEPicker spinnerPicker;
	private List<ReferenceItem> spinnerItems;
	private boolean isLoadFail;
	private boolean isWebViewFinished = false;
	private FormPersonCollection checkedPersonCollection = new FormPersonCollection();

	private int mWebViewHeight;
	private int mTouchY;
	private int mRawY;

	private AttachmentViewer mViewer;
	private List<String> mSelectedAttachments;

	public CordovaPresenter(CordovaContract.CordovaView cordovaView) {
		DownloadConfiguration configuration = new DownloadConfiguration.Builder()
				.owner(CoreZygote.getLoginUserServices().getUserId())
				.downloadDir(CoreZygote.getPathServices().getDownloadDirPath())
				.encryptDir(CoreZygote.getPathServices().getSafeFilePath())
				.decryptDir(CoreZygote.getPathServices().getTempFilePath())
				.create();
		this.mViewer = new AttachmentViewer(cordovaView.getContexts(), configuration);
		this.mViewer.setAttachmentViewerListener(new XSimpleAttachmentViewerListener());

		this.mCordovaView = cordovaView;
		cordovaView.getWebView().setWebViewClient(new CordovaWebViewClient());
		cordovaView.getWebView().addJavascriptInterface(new Controller(), "androidJS");
		initData();
	}

	private void initData() {
		spinnerPicker = new FEPicker(mCordovaView.getCordovaContext());
		spinnerPicker.setCyclic(false);
		spinnerPicker.setOnPickerButtonClickListener(mPickerButtonListener);//时间选择按钮点击监听
		spinnerPicker.setOnActionChangeLisenter(mActionChangeListener);//点击重新加载
	}

	@Override
	public void referencePicker() {
		spinnerItems = controlInfo.getReferenceItems();
		final List<String> values = new ArrayList<>();
		if (spinnerItems != null) {
			for (final ReferenceItem referenceItem : spinnerItems) {
				values.add(referenceItem.getValue());
			}
		}
		spinnerPicker.showWithData(values);
	}

	@Override
	public void showPickerView() {
		mCordovaView.getCordovaContext()
				.runOnUiThread(() -> openDatePicket(controlInfo.getControlDefaultData(), controlInfo.getDataFormat()));
	}

	@Override
	public void MeasuredHeight(MotionEvent event) {
		mWebViewHeight = mCordovaView.getWebView().getMeasuredHeight();
		mRawY = (int) event.getRawY();
		mTouchY = (int) event.getY();
	}

	@Override
	public void sendHandlerMessage(int what) {
		if (CommonUtil.nonEmptyList(mSelectedAttachments)) {
			attachmentGUID = controlInfo.getAttachmentGUID();
			uploadFile(what);
		}
	}

	private class CordovaWebViewClient extends WebViewClient {//点击html控件的监听

		@Override
		@JavascriptInterface
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			if (CommonUtil.isPhoneNumber(url)) {
				DevicesUtil.DialTelephone(mCordovaView.getContexts(), CommonUtil.subUrlNumber(url));
				return true;
			}
			if (CommonUtil.isSendSms(url)) {
				DevicesUtil.sendSms(mCordovaView.getContexts(), CommonUtil.subUrlNumber(url));
				return true;
			}
			if (CommonUtil.isSendEmail(url)) {
				DevicesUtil.sendEmail(mCordovaView.getContexts(), CommonUtil.subUrlNumber(url));
				return true;
			}
			String host = null;
			try {
				host = Uri.parse(url).getHost();
			} catch (final Exception e) {
				e.printStackTrace();
			}
			if (view == null || host == null) return super.shouldOverrideUrlLoading(view, url);
			if (NOTIFICATION_READY.equals(host)) {
				return true;
			}
			else if (host.contains(POST_NOTIFICATION_WITH_ID)) { // JS通知Android有数据需要过去取
				final int index = host.indexOf(HOST_CODE);
				if (index != -1) {
					String s = host.substring(index + 1, host.length());
					String javaScript = NOTIFICATION_BEFORE + s + NOTIFICATION_LAST;
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
						view.evaluateJavascript(javaScript, value -> {
						});
					}
					else {
						view.loadUrl(JAVASCRIPT + COLON_CODE + javaScript);
					}
				}
				return true;
			}
			return super.shouldOverrideUrlLoading(view, url);
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
			FELog.e("CordovaWebView onPageStarted : " + url);
			isLoadFail = false;
		}

		@Override
		public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {// 断网或者网络连接超时
			super.onReceivedError(view, errorCode, description, failingUrl);
			FELog.e("CordovaWebView onReceivedError : " + errorCode);
			if (errorCode == ERROR_HOST_LOOKUP || errorCode == ERROR_CONNECT || errorCode == ERROR_TIMEOUT) {
				isLoadFail = true;
			}
		}

		@Override
		public void onPageFinished(WebView view, String url) {//onPageFinished指页面加载完成,完成后取消计时器
			super.onPageFinished(view, url);
			FELog.e("CordovaWebView onPageFinished : " + url);
			if (!isWebViewFinished) {
				isWebViewFinished = true;
				FEWebViewJsUtil.isFromApp(mCordovaView.getWebView());
			}
			view.requestFocus();
			if (isLoadFail) {
				isLoadFail = false;
				mCordovaView.setViewVisible(false);
			}
			else {
				mCordovaView.setViewVisible(true);
			}

			if (LoadingHint.isLoading()) LoadingHint.hide();
			mCordovaView.onPageFinished();
		}

		@Override
		public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
			if (RemoveAD.contentAD(url)) return new WebResourceResponse(null, null, null);
			return null;
		}
	}

	private class Controller {//html控件点击事件的响应

		@JavascriptInterface
		public void runOnAndroidJavaScript(final String jsonStr) {
			mCordovaView.getHandler().post(() -> {
				mCordovaView.getContexts().isRestricted();
				if (!isResponseAble) return;
				JSControlInfo controlInfo = runJavaScriptJson(jsonStr);
				final int controlType = controlInfo != null ? controlInfo.getUiControlType() : -999;
				clickFormUiControlType(controlInfo, controlType);
			});
		}
	}

	private void clickFormUiControlType(JSControlInfo mControlInfo, int controlType) {
		mCordovaView.clickType(controlType);
		if (controlType == JSControlType.Date) {// 点击日期
			DevicesUtil.hideKeyboard(mCordovaView.getCordovaContext().getCurrentFocus());
			showPickerView();
		}
		else if (controlType == JSControlType.Person) {// 点击选择人员
			mCordovaView.clickPersonChoose(mControlInfo);
		}
		else if (controlType == JSControlType.Attachment) {// 点击添加附件按钮
			mCordovaView.clickAddAttachment();
		}
		else if (controlType == JSControlType.Reference) {// 点击参照项按钮
			referencePicker();
		}
		else if (controlType == JSControlType.MeetingBoard) {// 点击会议看板
			mCordovaView.clickMeetingRoom(mControlInfo);
		}
		else if (controlType == JSControlType.CommonWords) { // 点击常用语控件
			clickCommonWord();
		}
		else if (controlType == JSControlType.Download) {//查看除图片外的附件，需下载
			downLoadAttachment();
		}
		else if (controlType == JSControlType.Error) {//链接异常
			CoreZygote.getApplicationServices().reLoginApplication();
		}
		else if (controlType == JSControlType.Record) {//录音
			mCordovaView.openRecord();
		}
		else if (controlType == JSControlType.TakePhoto) {//拍照
			mCordovaView.openPhoto();
		}
		else if (controlType == JSControlType.WrittingCombo) {//手写
			mCordovaView.openWrittingCombo();
		}
		else if (controlType == -999 || controlType == -99) {// 点击发送按钮后返回数据（2222222）
			mCordovaView.clickSendButton(mControlInfo);
		}
	}

	private JSControlInfo runJavaScriptJson(String jsonStr) {//获取到js返回的数据并解析
		try {
			final JSONObject properties = new JSONObject(jsonStr);
			final JSONObject iq = properties.getJSONObject(JSON_USER_INFO);
			final String url = iq.optString(JSON_CONTROL_DEFAULT_DATA);
			controlInfo = GsonUtil.getInstance().fromJson(JSControlInfo.formatJsonString(iq.toString()), JSControlInfo.class);
			controlInfo.setControlDefaultData(url);
			return controlInfo;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean uploadFile(int type) {//上传附件
		if (CommonUtil.isEmptyList(mSelectedAttachments)) return false;
		final FileRequestContent fileRequestContent = new FileRequestContent();
		fileRequestContent.setAttachmentGUID(attachmentGUID);
		fileRequestContent.setFiles(mSelectedAttachments);

		if (!TextUtils.isEmpty(recordTime)) fileRequestContent.setAudioTime(recordTime);
		if (!LoadingHint.isLoading()) LoadingHint.show(mCordovaView.getContexts());

		if (controlInfo != null && !CommonUtil.isEmptyList(controlInfo.sendService)) {
			Map<String, String> serviceMap = new HashMap<>();
			for (JsSendServiceItem serviceItem : controlInfo.sendService) {
				serviceMap.put(serviceItem.name, serviceItem.value);
			}
			fileRequestContent.setValueMap(serviceMap);
		}

		final FileRequest fileRequest = new FileRequest();
		fileRequest.setFileContent(fileRequestContent);
		try {
			new UploadManager(mCordovaView.getContexts())
					.fileRequest(fileRequest)
					.progressUpdateListener(new OnProgressUpdateListenerImpl() {
						@Override
						public void onPreExecute() {
							LoadingHint.show(mCordovaView.getContexts());
						}

						@Override
						public void onProgressUpdate(long currentBytes, long contentLength, boolean done) {
							int progress = (int) (currentBytes * 100 / contentLength * 1.0F);
							LoadingHint.showProgress(progress);
						}

						@Override
						public void onPostExecute(final String jsonBody) {
							LoadingHint.hide();
							mSelectedAttachments.clear();
							recordTime = "";
							if (type == IS_RECORD_INTENT || type == IS_PHOTO_INTENT) {//录音、拍照
								recordShowTime(jsonBody);
							}
							else {
								mCordovaView.onPostExecute(controlInfo);
							}
						}

						@Override
						public void onFailExecute(Throwable ex) {
							LoadingHint.hide();
						}
					})
					.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	private void recordShowTime(String jsonBody) {
		JSONObject iq = null;
		String query = "";
		try {
			JSONObject properties = new JSONObject(jsonBody);
			if (properties.has(JSON_IQ)) iq = properties.getJSONObject(JSON_IQ);
			if (iq != null && iq.has(JSON_QUERY)) query = iq.get(JSON_QUERY).toString();

			List<RecordItem> attaItems = null;
			if (!TextUtils.isEmpty(query)) {
				CommonResponse commonResponse = GsonUtil.getInstance().fromJson(query, CommonResponse.class);
				if (commonResponse != null) attaItems = commonResponse.getAttaItems();
			}

			Map<String, String> serviceMap = new HashMap<>();
			if (attaItems != null) {
				for (RecordItem item : attaItems) {
					serviceMap.put("guid", item.getGuid());
					serviceMap.put("master_key", item.getMaster_key());
					serviceMap.put("time", item.getTime());
				}
			}
			if (!CommonUtil.isEmptyList(controlInfo.sendService)) {
				for (JsSendServiceItem serviceItem : controlInfo.sendService) {
					serviceMap.put(serviceItem.name, serviceItem.value);
				}
			}

			List<Map<String, String>> services = new ArrayList<>();
			services.add(serviceMap);
			LoadingHint.hide();
			SendRecordJs sendRecord = SendRecordJs.setData(controlInfo.getUiControlTypeValue(), controlInfo.getUiControlId(), services);
			sendToJavascript(stringToJson(sendRecord));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void addAttachment() {
		attachmentGUID = controlInfo.getAttachmentGUID();
	}

	@Override
	public void clickCommonWord() {
		final FormCommonWordInfo info = new FormCommonWordInfo();
		info.setUiControlId(controlInfo.getUiControlId());
		info.setUiControlType(controlInfo.getUiControlType());

		final FEApplication application = (FEApplication) CoreZygote.getContext();
		String[] commonWords = application.getCommonWords();
		if (commonWords != null) {
			showCommonWordDialog(info, CommonWordsActivity.convertCommonWord(commonWords));
			return;
		}

		LoadingHint.show(mCordovaView.getContexts());

		final ReferenceItemsRequest commonWordsReq = new ReferenceItemsRequest();
		commonWordsReq.setRequestType(ReferenceItemsRequest.TYPE_COMMON_WORDS);
		FEHttpClient.getInstance().post(commonWordsReq, new ResponseCallback<ReferenceItemsResponse>(this) {
			@Override
			public void onCompleted(ReferenceItemsResponse referenceItemsResponse) {
				LoadingHint.hide();
				final List<ReferenceItem> items = referenceItemsResponse.getItems();
				if ("-98".equals(referenceItemsResponse.getErrorCode())) {
					application.setCommonWords(mCordovaView.getContexts().getResources().getStringArray(R.array.words));
				}
				else {
					if (CommonUtil.nonEmptyList(items)){
						application.setCommonWords(CommonWordsActivity.convertCommonWords(items));
					}
				}
				showCommonWordDialog(info, CommonWordsActivity.convertCommonWord(application.getCommonWords()));
			}

			@Override
			public void onFailure(RepositoryException repositoryException) {
				LoadingHint.hide();
				FEToast.showMessage(mCordovaView.getCordovaContext().getResources().getString(R.string.lbl_retry_operator));
			}
		});
	}

	private void showCommonWordDialog(final FormCommonWordInfo formCommonWordInfo, final String[] commonWords) {
		if (CommonUtil.isEmptyList(commonWords)){
			return;
		}
		new FEMaterialDialog
				.Builder(mCordovaView.getContexts())
				.setCancelable(true)
				.setTitle(mCordovaView.getCordovaContext().getResources().getString(R.string.common_language))
				.setItems(commonWords, new FEMaterialDialog.OnItemClickListener() {
					@Override
					public void onItemClickListener(AlertDialog dialog, View view, int position) {
						dialog.dismiss();
						formCommonWordInfo.setUseCommonValue(commonWords[position]);
						sendToJavascript(formCommonWordInfo.getProperties());
					}
				})
				.setPositiveButton(mCordovaView.getCordovaContext().getResources().getString(R.string.lbl_text_edit),
						dialog -> mCordovaView.getContexts().startActivity(new Intent(mCordovaView.getContexts()
								, CommonWordsActivity.class)))
				.setNegativeButton(null, null)
				.build()
				.show();
	}

	private void sendContactsJs(int type, String name, List<String> phones) {//发送通讯录
		SendContactsJs contactJson = SendContactsJs.setSendContactsJs(type, name, phones);
		sendToJavascript(stringToJson(contactJson));
	}

	@Override
	@JavascriptInterface
	public void sendToJavascript(Object data) {//向js发送数据
		if (data == null) return;
		String javaScript = BRIDGE_BEFORE + data + BRIDGE_LAST;
		FELog.i("-->>>>javaScript:" + javaScript);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			mCordovaView.getWebView().evaluateJavascript(javaScript, FELog::i);
		}
		else mCordovaView.getWebView().loadUrl(JAVASCRIPT + COLON_CODE + javaScript);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (intent == null) return;
		if (resultCode == Activity.RESULT_OK) getContacts(intent);
		switch (requestCode) {
			case ADD_ATTACHMENT_REQUEST_CODE:// 添加附件返回来的结果
				ArrayList<String> selectedAttachments = intent.getStringArrayListExtra("extra_local_file");
				addSelectedAttachments(selectedAttachments);
				if (controlInfo != null) {
					final ExecuteResult info = createSentToJsData(getSelectedAttachmentCount(), null, null);
					sendToJavascript(info.getProperties());
				}
				break;
			case ADD_PERSON_REQUEST_CODE:// 添加人员来的结果
				checkedPersonCollection = (FormPersonCollection) intent.getSerializableExtra("CheckedPersons");
				if (controlInfo != null) {
					final ExecuteResult info = createSentToJsData(getSelectedAttachmentCount(), null, null);
					sendToJavascript(info.getProperties());
				}
				break;
			case INTENT_TO_MEETINGBOARD_REQUEST_CODE:// 从会议看板回来
				if (controlInfo != null) {
					final MeetingBoardData meetingBoardData = (MeetingBoardData) intent.getSerializableExtra("MeetingBoardData");
					final ExecuteResult info = createSentToJsData(getSelectedAttachmentCount(), null, null);
					info.setMeetingBoardData(meetingBoardData);
					sendToJavascript(info.getProperties());
				}
				break;
			case RECORD_RESULT:// 录音文件
				recordTime = intent.getStringExtra("RecordTime");
				startLoadFile(intent.getStringExtra("Record"), IS_RECORD_INTENT);
				break;
			case PHOTO_RESULT:
				startLoadFile(intent.getStringExtra("photo_path"), IS_PHOTO_INTENT);
				break;
			case SCANNING_QR_CODE://二维码扫描
				String data = intent.getStringExtra(ParseCaptureUtils.CAPTURE_RESULT_DATA);
				final ExecuteResult info = new ExecuteResult();
				info.setUiControlType(controlInfo.getUiControlType());
				info.setData(data);
				sendToJavascript(info.getProperties());
				break;
			default:
				break;
		}
	}

	private void startLoadFile(String path, int type) {//开始上传文件
		if (TextUtils.isEmpty(path)) return;
		if (mSelectedAttachments == null) mSelectedAttachments = new ArrayList<>();
		mSelectedAttachments.add(path);
		sendHandlerMessage(type);
	}

	private static JSONObject stringToJson(Object object) {//把json格式字符串转换成js接收的json对象
		try {
			return new JSONObject(TO_JSON_BEFORE + GsonUtil.getInstance().toJson(object) + TO_JSON_LAST);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
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
				data = DateUtil.calendar2StringDateTime(calendar);
				sendToJavascript(createSentToJsData(0, data, null).getProperties());
				dateTimePickerDialog.dismiss();
			}
		});
		dateTimePickerDialog.setCanClear(true);
		dateTimePickerDialog.show(mCordovaView.getCordovaContext().getFragmentManager(), "dateTimePickerDialog");
	}

	private FEPicker.OnPickerButtonClickListener mPickerButtonListener = new FEPicker.OnPickerButtonClickListener() {
		@Override
		public void OnButtonClick(View view, String data) {
			final List<ReferenceItem> items = new ArrayList<>();
			if (data == null || "".equals(data)) {
				final ReferenceItem item = new ReferenceItem();
				item.setKey("");
				item.setValue("");
				items.add(item);
			}
			else if (spinnerItems != null) {
				for (final ReferenceItem item : spinnerItems) {// 判断是否是报表界面过来的，是那么取值getvalue，否那么取值getkey
					if (data.equals(item.getValue())) items.add(item);
				}
			}
			spinnerPicker.dismiss();
			sendToJavascript(createSentToJsData(0, null, items).getProperties());
		}
	};

	private OnActionChangeListener mActionChangeListener = new OnActionChangeListener() {
		@Override
		public void show(FEPopupWindow pw, View contentView) {
			if (pw instanceof FEDatePicker) {
				spinnerPicker.dismiss();
			}
			/* 当选择控件出现时，判断是否会挡住下面的输入框，如果挡住就向上滚 */
			final int viewDistance = getViewDistance();// 输入框离屏幕底部的距离
			final int contentViewHight = pw.getContentViewHight();// 获取控件的高度
			final int scrollDistance = mRawY - mTouchY;// 计算Webview滚动的距离
			if (contentViewHight > viewDistance) {
				mCordovaView.getWebView().scrollBy(0, contentViewHight - viewDistance + scrollDistance);
			}
		}

		@Override
		public void dismiss(FEPopupWindow pw, View contentView) {
			final int viewDistance = getViewDistance();
			final int contentViewHight = pw.getContentViewHight();
			if (contentViewHight > viewDistance) {
				mCordovaView.getWebView().scrollTo(0, mRawY - mTouchY);
			}
			else {
				mCordovaView.getWebView().scrollTo(0, 0);
			}
		}
	};

	private int getViewDistance() {//获取输入框离屏幕底部的距离
		return mWebViewHeight - mTouchY;
	}

	private ExecuteResult createSentToJsData(int attachmentCount, String dateValue, List<ReferenceItem> referenceItems) {
		final ExecuteResult info = new ExecuteResult();
		info.setUiControlType(controlInfo.getUiControlType());
		info.setUiControlId(controlInfo.getUiControlId());
		info.setAttachmentCount(attachmentCount);
		info.setDateValue(dateValue);
		info.setReferenceItems(referenceItems);
		info.setIdItems(checkedPersonCollection == null ? null : checkedPersonCollection.getPersonArray());
		return info;
	}

	@Override
	public void downLoadAttachment() {
		if (controlInfo == null) return;
		if (TextUtils.isEmpty(controlInfo.getControlDefaultData())) return;

		String feUrl = CoreZygote.getLoginUserServices().getServerAddress();
		mViewer.openAttachment(feUrl + controlInfo.getControlDefaultData(),
				controlInfo.getAttachmentGUID(), controlInfo.getUiControlId());
	}

	@Override public List<String> getSelectedAttachments() {
		return mSelectedAttachments;
	}

	private void getContacts(Intent intent) {
		if (mCordovaView.getCordovaContext() == null) return;
		ContentResolver reContentResolverol = mCordovaView.getCordovaContext().getContentResolver();
		if (reContentResolverol == null) return;
		Uri contactData = intent.getData();
		if (contactData == null) return;

		@SuppressWarnings("deprecation")
		Cursor cursor = mCordovaView.getCordovaContext().managedQuery(contactData, null, null, null, null);
		cursor.moveToFirst();
		String username = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
		String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
		Cursor phone = reContentResolverol.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
				ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
		String usernumber;
		List<String> phones = new ArrayList<>();
		while (phone != null && phone.moveToNext()) {
			usernumber = phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
			phones.add(usernumber);
		}
		int type = JSControlType.Contacts;
		sendContactsJs(type, username, phones);
	}

	public void addSelectedAttachments(List<String> selectedAttachments) {
		if (mSelectedAttachments == null) mSelectedAttachments = new ArrayList<>();
		mSelectedAttachments.clear();
		mSelectedAttachments.addAll(selectedAttachments);
	}

	private int getSelectedAttachmentCount() {
		return CommonUtil.isEmptyList(mSelectedAttachments) ? 0 : mSelectedAttachments.size();
	}

	private class XSimpleAttachmentViewerListener extends SimpleAttachmentViewerListener {

		@Override public void onDownloadBegin(TaskInfo taskInfo) {
			FEToast.showMessage(mCordovaView.getCordovaContext().getResources().getString(R.string.lbl_text_download_wait));
		}

		@Override public void prepareOpenAttachment(Intent intent) {
			mCordovaView.openAttachment(intent);
		}

		@Override public void preparePlayAudioAttachment(Attachment attachment, String attachmentPath) {
			mCordovaView.playAudio(attachment, attachmentPath);
		}
	}
}
