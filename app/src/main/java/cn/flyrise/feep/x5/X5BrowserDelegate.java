package cn.flyrise.feep.x5;

import static cn.flyrise.feep.x5.X5BrowserFragment.TYPE_UPLOAD_WRITTING_COMBO;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import cn.flyrise.android.library.utility.LoadingHint;
import cn.flyrise.android.protocol.model.ReferenceItem;
import cn.flyrise.feep.R;
import cn.flyrise.feep.commonality.bean.JSControlInfo;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.X.FormNullCheck;
import cn.flyrise.feep.core.common.X.JSActionType;
import cn.flyrise.feep.core.common.X.JSControlType;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.GsonUtil;
import cn.flyrise.feep.form.been.ExecuteResult;
import cn.flyrise.feep.form.been.FormCommonWordInfo;
import cn.flyrise.feep.form.been.FormPersonCollection;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author 社会主义接班人
 * @since 2018-09-18 13:45
 */
public final class X5BrowserDelegate {

	private final X5BrowserFragment browser;
	private final X5BrowserRepository repository;
	private JSControlInfo jsControlInfo;
	private FormPersonCollection formCollection;
	private List<String> mSelectedAttachments;
	private int formIntent;
	private String attachmentGuid;

	public X5BrowserDelegate(X5BrowserFragment browser) {
		this.browser = browser;
		this.repository = new X5BrowserRepository(this);
	}

	public void setFormIntent(int formIntent) {
		this.formIntent = formIntent;
	}

	public void setFormPersonCollection(FormPersonCollection collection) {
		this.formCollection = collection;
	}

	public boolean shouldOverrideUrlLoading(String url) {
		if (!url.contains(":")) return false;
		int colonIndex = url.indexOf(":");
		String protocol = url.substring(0, colonIndex);
		if (TextUtils.equals(protocol, "tel")
				|| TextUtils.equals(protocol, "sms")
				|| TextUtils.equals(protocol, "Mailto")) {
			int markIndex = url.indexOf("?");
			String value = url.substring(colonIndex + 1, markIndex < 0 ? url.length() : markIndex);
			browser.callSystemApp(protocol, value);
			return true;
		}

		String host = "";
		try {
			host = Uri.parse(url).getHost();
		} catch (Exception exp) {
		}
		if (TextUtils.isEmpty(host)) return false;
		if (TextUtils.equals(host, "NotificationReady")) return true;
		if (host.contains("PostNotificationWithId")) {
			int stripIndex = host.indexOf("-");
			if (stripIndex >= 0) {
				String id = host.substring(stripIndex + 1, host.length());
				String jsMethod = "jsBridge.popNotificationObject(" + id + ")";
				browser.callJavaScriptMethod(jsMethod);
			}
			return true;
		}
		return false;
	}

	public void analysisJsonFromJavaScriptCall(String json) {
		try {
			JSONObject jsonObject = new JSONObject(json);
			JSONObject queryObject = jsonObject.getJSONObject("userInfo");

			String queryString = JSControlInfo.formatJsonString(queryObject.toString());
			jsControlInfo = GsonUtil.getInstance().fromJson(queryString, JSControlInfo.class);

			String url = queryObject.getString("ControlDefaultData");
			jsControlInfo.setControlDefaultData(url);
		} catch (Exception exp) {
			exp.printStackTrace();
		}

		int controlType = jsControlInfo == null ? -999 : jsControlInfo.getUiControlType();
		performAnalysisResults(controlType);
	}

	private void performAnalysisResults(int controlType) {
		if (controlType == -99 || controlType == -999) {//-99说明uiControlType为空
			int actionType = jsControlInfo.getActionType();
			if (actionType == JSActionType.Error) return;
			if (actionType == JSActionType.Send) {
				FormMolecule molecule = new FormMolecule();
				molecule.actionType = JSActionType.Send;
				molecule.formKeyId = jsControlInfo.getFormKeyId();
				EventBus.getDefault().post(molecule);
				return;
			}

			if (actionType == JSActionType.FetchData) {
				FormMolecule molecule = new FormMolecule();
				molecule.actionType = JSActionType.FetchData;
				molecule.formKeyId = jsControlInfo.getWebData();
				EventBus.getDefault().post(molecule);
				return;
			}

			int nullCheckResult = jsControlInfo.getNullCheckResult();
			if (nullCheckResult == FormNullCheck.Null && actionType == JSActionType.Check) {
				FEToast.showMessage(CommonUtil.getString(R.string.form_need_input));
				return;
			}

			if (nullCheckResult == FormNullCheck.DataExist) {
				FEToast.showMessage(CommonUtil.getString(R.string.form_null_checked_exit_data));
				return;
			}

			if (nullCheckResult == FormNullCheck.NonFormID) {
				FEToast.showMessage(CommonUtil.getString(R.string.form_null_checked_non_formid));
				return;
			}

			if (nullCheckResult == FormNullCheck.NonNull) {
				if (getAttachmentCount() == 0) {
					if (formIntent != -1) {
						ExecuteResult result = new ExecuteResult();
						result.setActionType(formIntent);
						browser.callJavaScriptMethod(result.getJsMethod());
					}
					return;
				}

				uploadFile(mSelectedAttachments, null, TYPE_UPLOAD_WRITTING_COMBO);
			}
			return;
		}

		switch (controlType) {
			case JSControlType.Contacts:                // 申请通讯录权限
				browser.requestContactPermission();
				break;
			case JSControlType.Record:                  // 申请录音权限
				attachmentGuid = jsControlInfo.getAttachmentGUID();
				browser.requestRecordPermission();
				break;
			case JSControlType.TakePhoto:               // 申请拍照权限
				attachmentGuid = jsControlInfo.getAttachmentGUID();
				browser.requestCameraPermission(JSControlType.TakePhoto);
				break;
			case JSControlType.Attachment:              // 选择附件
				attachmentGuid = jsControlInfo.getAttachmentGUID();
				browser.requestAttachment();
				break;
			case JSControlType.MeetingBoard:            // 会议看板
				browser.requestMeetingBoard(jsControlInfo);
				break;
			case JSControlType.WrittingCombo:           // 手写板
				attachmentGuid = jsControlInfo.getAttachmentGUID();
				browser.openWrittingCombo();
				break;
			case JSControlType.Date:                    // 选择日期
				String dateTime = jsControlInfo.getControlDefaultData();
				String dateTimeFormat = jsControlInfo.getDataFormat();
				browser.openDatePicker(dateTime, dateTimeFormat);
				break;
			case JSControlType.Person:                  // 选择人员
				browser.requestStaff(jsControlInfo);
				break;
			case JSControlType.Reference:               // 选择参照项
				List<ReferenceItem> referenceItems = jsControlInfo.getReferenceItems();
				if (CommonUtil.nonEmptyList(referenceItems)) {
					List<String> referenceValues = new ArrayList<>(referenceItems.size());
					for (ReferenceItem item : referenceItems) {
						referenceValues.add(item.getValue());
					}
					browser.openReference(referenceValues);
				}
				break;
			case JSControlType.CommonWords:             // 常用语
				repository.queryCommonWords(browser.getActivity())
						.subscribeOn(Schedulers.io())
						.doOnSubscribe(() -> LoadingHint.show(browser.getActivity()))
						.subscribeOn(AndroidSchedulers.mainThread())
						.observeOn(AndroidSchedulers.mainThread())
						.subscribe(commonWords -> {
							LoadingHint.hide();
							FormCommonWordInfo commonWordInfo = new FormCommonWordInfo();
							commonWordInfo.setUiControlId(jsControlInfo.getUiControlId());
							commonWordInfo.setUiControlType(jsControlInfo.getUiControlType());
							browser.openCommonWords(commonWordInfo, commonWords);
						}, exception -> {
							LoadingHint.hide();
							exception.printStackTrace();
						});
				break;
			case JSControlType.Download:                // 下载
				if (!TextUtils.isEmpty(jsControlInfo.getControlDefaultData())) {
					String host = CoreZygote.getLoginUserServices().getServerAddress();
					String attachmentURL = host + jsControlInfo.getControlDefaultData();
					browser.openAttachment(attachmentURL,
							jsControlInfo.getAttachmentGUID(), jsControlInfo.getUiControlId());
				}
				break;
			case JSControlType.Break:                   // 结束
				browser.getActivity().finish();
				break;
			case JSControlType.Error:                   // 错误
				CoreZygote.getApplicationServices().reLoginApplication();
				break;
			case JSControlType.ZXing:
				browser.requestCameraPermission(JSControlType.ZXing);     //申请权限、二维码
				break;
			default:
				break;
		}
	}

	public void uploadFile(List<String> attachments, String recordTime, int uploadType) {
		if (TextUtils.isEmpty(attachmentGuid)) attachmentGuid = UUID.randomUUID().toString();
		Observable<String> upload = repository.updateFile(attachments, recordTime, attachmentGuid);
		if (upload == null) return;

		if (uploadType == X5BrowserFragment.TYPE_UPLOAD_CAMERA
				|| uploadType == X5BrowserFragment.TYPE_UPLOAD_RECORD
				|| uploadType == X5BrowserFragment.IS_WRITTING_COMBO) {
			repository.mapFromObservable(upload, jsControlInfo, attachmentGuid)
					.subscribeOn(Schedulers.io())
					.observeOn(AndroidSchedulers.mainThread())
					.subscribe(result -> {
						String json = GsonUtil.getInstance().toJson(result);
						String jsMethod = "jsBridge.trigger('SetWebHTMLEditorContent',{\"OcToJs_JSON\":" + json + "})";
						browser.callJavaScriptMethod(jsMethod);
					});
			return;
		}
		upload.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(result -> {
					if (formIntent != -1) {
						ExecuteResult results = new ExecuteResult();
						results.setActionType(formIntent);
						browser.callJavaScriptMethod(results.getJsMethod());
					}
				});
	}

	public void parseContactURI(Uri contactUri) {
		repository.queryContacts(browser.getActivity(), contactUri)
				.map(contactJs -> contactJs.getJsMethod())
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(browser::callJavaScriptMethod);
	}

	public JSControlInfo getJsControlInfo() {
		return jsControlInfo;
	}

	public ExecuteResult getExecuteResult(int attachmentCount, String dateValue, List<ReferenceItem> referenceItems) {
		ExecuteResult result = new ExecuteResult();
		result.setUiControlType(jsControlInfo.getUiControlType());
		result.setUiControlId(jsControlInfo.getUiControlId());
		result.setAttachmentCount(attachmentCount);
		result.setDateValue(dateValue);
		result.setReferenceItems(referenceItems);
		result.setIdItems(formCollection == null ? null : formCollection.getPersonArray());
		return result;
	}

	public void addSelectedAttachments(List<String> attachments) {
		if (mSelectedAttachments == null) {
			mSelectedAttachments = new ArrayList<>();
		}
		mSelectedAttachments.clear();
		mSelectedAttachments.addAll(attachments);
	}

	public List<String> getSelectedAttachments() {
		return mSelectedAttachments;
	}

	public int getAttachmentCount() {
		return CommonUtil.isEmptyList(mSelectedAttachments) ? 0 : mSelectedAttachments.size();
	}

	public Context getContext() {
		return browser.getActivity();
	}
}
