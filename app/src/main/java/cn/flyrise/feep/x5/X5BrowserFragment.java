package cn.flyrise.feep.x5;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.widget.ProgressBar;
import cn.flyrise.android.protocol.model.ReferenceItem;
import cn.flyrise.android.shared.utility.picker.FEPicker;
import cn.flyrise.feep.K;
import cn.flyrise.feep.R;
import cn.flyrise.feep.auth.views.CaptureActivity;
import cn.flyrise.feep.commonality.CommonWordsActivity;
import cn.flyrise.feep.commonality.bean.JSControlInfo;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.X.JSActionType;
import cn.flyrise.feep.core.common.X.JSControlType;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.DateUtil;
import cn.flyrise.feep.core.common.utils.DevicesUtil;
import cn.flyrise.feep.core.common.utils.UriPathHelper;
import cn.flyrise.feep.core.dialog.FEMaterialDialog;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.premission.FePermissions;
import cn.flyrise.feep.core.premission.PermissionCode;
import cn.flyrise.feep.core.premission.PermissionGranted;
import cn.flyrise.feep.form.FormPersonChooseActivity;
import cn.flyrise.feep.form.MeetingBoardActivity;
import cn.flyrise.feep.form.been.ExecuteResult;
import cn.flyrise.feep.form.been.FormCommonWordInfo;
import cn.flyrise.feep.form.been.FormPersonCollection;
import cn.flyrise.feep.form.been.MeetingBoardData;
import cn.flyrise.feep.form.widget.handWritting.FEWrittingComboDialog;
import cn.flyrise.feep.media.attachments.AttachmentViewer;
import cn.flyrise.feep.media.attachments.downloader.DownloadConfiguration;
import cn.flyrise.feep.media.common.LuBan7;
import cn.flyrise.feep.media.record.camera.CameraManager;
import cn.flyrise.feep.utils.ParseCaptureUtils;
import cn.squirtlez.frouter.FRouter;
import com.borax12.materialdaterangepicker.DateTimePickerDialog;
import com.tencent.smtt.sdk.CookieManager;
import com.tencent.smtt.sdk.CookieSyncManager;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import okhttp3.Cookie;

/**
 * @author 社会主义接班人
 * @since 2018-09-18 10:31
 */
public class X5BrowserFragment extends Fragment {

	public static final int CODE_SELECT_FILE_V4 = 100;     // V4 版本的文件选择
	public static final int CODE_SELECT_FILE_V5 = 101;     // V5 版本的文件选择
	public static final int CODE_ATTACHMENT = 102;         // 附件
	public static final int CODE_PERSON = 103;             // 人员
	public static final int CODE_MEETING_BOARD = 104;      // 会议看板
	public static final int CODE_RECORD = 105;             // 录音
	public static final int CODE_CAMERA = 106;             // 拍照
	public static final int CODE_CONTACT = 107;            // 联系人
	public static final int SCANNING_QR_CODE = 108;        // 二维码扫描

	public static final int TYPE_UPLOAD_RECORD = 1001020;//录音
	public static final int TYPE_UPLOAD_CAMERA = 10103;//拍照
	public static final int IS_WRITTING_COMBO = 10106;//手写
	public static final int TYPE_UPLOAD_WRITTING_COMBO = 10104;

	private WebView webView;
	private ProgressBar progressBar;
	private X5BrowserDelegate delegate;
	private X5WebChromeClient webChromeClient;

	private String homeLink;
	private CameraManager mCamera;
	private boolean isNewForm = false;
	private AttachmentViewer attachmentViewer;
	private int cameraType;//照相机权限类型（二维码、拍照）

	@Override public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle arguments = getArguments();
		int formIntent = -1;
		if (arguments != null) {
			homeLink = arguments.getString("homeLink");
			formIntent = arguments.getInt("formIntent", -1);
			isNewForm = arguments.getBoolean("isNewForm", false);
		}
		delegate = new X5BrowserDelegate(this);
		delegate.setFormIntent(formIntent);
		mCamera = new CameraManager(getActivity());
	}

	@Nullable @Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_x5_browser, container, false);
	}

	@Override public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (CoreZygote.getLoginUserServices() != null) {
			DownloadConfiguration configuration = new DownloadConfiguration.Builder()
					.owner(CoreZygote.getLoginUserServices().getUserId())
					.downloadDir(CoreZygote.getPathServices().getDownloadDirPath())
					.encryptDir(CoreZygote.getPathServices().getSafeFilePath())
					.decryptDir(CoreZygote.getPathServices().getTempFilePath())
					.create();
			this.attachmentViewer = new AttachmentViewer(getActivity(), configuration);
			this.attachmentViewer.setAttachmentViewerListener(null);
		}
		View contentView = getView();
		webView = contentView.findViewById(R.id.x5WebView);
		progressBar = contentView.findViewById(R.id.progressBar);
		initX5WebViewSetting();
		this.loadUrl(homeLink);
	}

	private void initX5WebViewSetting() {
		String dir = getActivity().getDir("database", Context.MODE_PRIVATE).getPath();
		WebSettings webSettings = webView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webSettings.setSupportZoom(false);
		webSettings.setUseWideViewPort(false);
		webSettings.setBuiltInZoomControls(false);
		webSettings.setAllowFileAccess(true);
		webSettings.setAllowContentAccess(true);
		webSettings.setDatabaseEnabled(true);
		webSettings.setDefaultTextEncodingName("utf-8");
		webSettings.setAllowFileAccessFromFileURLs(true);
		webSettings.setAppCacheEnabled(true);
		webSettings.setGeolocationEnabled(true);
		webSettings.setGeolocationDatabasePath(dir);
		webSettings.setDomStorageEnabled(true);
		String ua = webSettings.getUserAgentString();
		webSettings.setUserAgentString(ua + CoreZygote.getUserAgent());

		CookieSyncManager.createInstance(getActivity());
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.setAcceptCookie(true);
		if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
			cookieManager.setAcceptThirdPartyCookies(webView, true);
		}
		if (CoreZygote.getLoginUserServices() != null) {
			List<Cookie> allCookies = FEHttpClient.getInstance().getAllCookies();
			if (CommonUtil.nonEmptyList(allCookies)) {
				String host = FEHttpClient.getInstance().getHost();
				for (Cookie cookie : allCookies) {
					String cookieString = cookie.name() + "=" + cookie.value() + "; domain=" + cookie.domain();
					cookieManager.setCookie(host, cookieString);
				}
			}
		}

		if (VERSION.SDK_INT < VERSION_CODES.LOLLIPOP) {
			CookieSyncManager.getInstance().sync();
		}
		else {
			cookieManager.flush();
		}

		webChromeClient = new X5WebChromeClient(getActivity());
		webChromeClient.setOnProgressChangeListener(progress -> {
			if (progress == 100) {
				progressBar.setVisibility(View.GONE);
				return;
			}
			progressBar.setVisibility(View.VISIBLE);
			progressBar.setProgress(progress);
		});

		webView.setWebChromeClient(webChromeClient);
		webView.setWebViewClient(new X5WebViewClient(delegate, isNewForm));
		webView.addJavascriptInterface(new X5JavaScriptCallback(delegate), "androidJS");
	}

	private void loadUrl(String url) {
		this.homeLink = url;
		Map<String, String> headers = new HashMap<>();
		if (CoreZygote.getLoginUserServices() != null) {
			String accessToken = CoreZygote.getLoginUserServices().getAccessToken();
			if (!TextUtils.isEmpty(accessToken)) {
				headers.put("token", accessToken);
			}
		}
		webView.loadUrl(homeLink, headers);
	}

	@Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == CODE_SELECT_FILE_V4) {                                                  // Android 5.0 以下
			ValueCallback<Uri> uploadMessage = webChromeClient.getUploadMessage();
			Uri result = (data == null || resultCode != Activity.RESULT_OK) ? null : data.getData();
			if (uploadMessage != null) {
				if (result != null) {
					String path = UriPathHelper.getPath(getContext(), result);
					if (!TextUtils.isEmpty(path)) result = Uri.fromFile(new File(path));
				}
				uploadMessage.onReceiveValue(result);
				webChromeClient.setUploadMessage(null);
			}
		}
		else if (requestCode == CODE_SELECT_FILE_V5) {                                                // Android 5.0 及以上
			ValueCallback<Uri[]> uploadMessages = webChromeClient.getUploadMessages();
			Uri[] result = (data == null || resultCode != Activity.RESULT_OK) ? null : new Uri[]{data.getData()};
			if (uploadMessages != null) {
				uploadMessages.onReceiveValue(result);
				webChromeClient.setUploadMessages(null);
			}
		}
		else if (requestCode == CODE_ATTACHMENT) {
			if (data != null) {
				ArrayList<String> selectedAttachments = data.getStringArrayListExtra("extra_local_file");
				delegate.addSelectedAttachments(selectedAttachments);
				ExecuteResult executeResult = delegate.getExecuteResult(delegate.getAttachmentCount(), null, null);
				callJavaScriptMethod(executeResult.getJsMethod());
			}
		}
		else if (requestCode == CODE_PERSON) {
			if (data != null) {
				delegate.setFormPersonCollection((FormPersonCollection) data.getSerializableExtra("CheckedPersons"));
				ExecuteResult executeResult = delegate.getExecuteResult(delegate.getAttachmentCount(), null, null);
				callJavaScriptMethod(executeResult.getJsMethod());
			}
		}
		else if (requestCode == CODE_MEETING_BOARD) {
			if (data != null) {
				MeetingBoardData meetingBoardData = (MeetingBoardData) data.getSerializableExtra("MeetingBoardData");
				ExecuteResult executeResult = delegate.getExecuteResult(delegate.getAttachmentCount(), null, null);
				executeResult.setMeetingBoardData(meetingBoardData);
				callJavaScriptMethod(executeResult.getJsMethod());
			}
		}
		else if (requestCode == CODE_RECORD) {
			if (data != null) {
				String recordTime = data.getStringExtra("RecordTime");
				String recordFile = data.getStringExtra("Record");
				delegate.uploadFile(Arrays.asList(recordFile), recordTime, TYPE_UPLOAD_RECORD);
			}
		}
		else if (requestCode == CODE_CAMERA && resultCode == Activity.RESULT_OK) {
			if (mCamera.isExistPhoto()) {
//				String photoFile = data.getStringExtra("photo_path");
				delegate.uploadFile(Arrays.asList(mCamera.getAbsolutePath()), null, TYPE_UPLOAD_CAMERA);
			}
		}
		else if (requestCode == CODE_CONTACT) {
			if (data != null) {
				Uri contactURI = data.getData();
				if (contactURI != null) {
					delegate.parseContactURI(contactURI);
				}
			}
		}
		else if (requestCode == SCANNING_QR_CODE) {//二维码扫
			String zxingData = data.getStringExtra(ParseCaptureUtils.CAPTURE_RESULT_DATA);
			final ExecuteResult info = new ExecuteResult();
			info.setUiControlType(cameraType);
			info.setData(zxingData);
			callJavaScriptMethod(info.getJsMethod());
		}
	}

	// 请求手机通讯录权限
	public void requestContactPermission() {
		FePermissions.with(this)
				.rationaleMessage(getResources().getString(R.string.permission_rationale_contact))
				.permissions(new String[]{Manifest.permission.READ_CONTACTS})
				.requestCode(PermissionCode.CONTACTS)
				.request();
	}

	// 通讯录权限请求成功
	@PermissionGranted(PermissionCode.CONTACTS) public void onContactPermissionGranted() {
		Uri uri = Uri.parse("content://contacts/people");
		Intent intent = new Intent(Intent.ACTION_PICK, uri);
		startActivityForResult(intent, CODE_CONTACT);
	}

	// 请求录音权限
	public void requestRecordPermission() {
		FePermissions.with(this)
				.permissions(new String[]{Manifest.permission.RECORD_AUDIO})
				.rationaleMessage(getResources().getString(R.string.permission_rationale_record))
				.requestCode(PermissionCode.RECORD)
				.request();
	}

	// 录音权限请求成功
	@PermissionGranted(PermissionCode.RECORD) public void onRecordPermissionGranted() {
		FRouter.build(getActivity(), "/media/recorder")
				.requestCode(CODE_RECORD)
				.go();
	}

	// 请求相机权限
	public void requestCameraPermission(int type) {
		this.cameraType = type;
		FePermissions.with(this)
				.permissions(new String[]{Manifest.permission.CAMERA})
				.rationaleMessage(getResources().getString(cn.flyrise.feep.media.R.string.permission_rationale_camera))
				.requestCode(PermissionCode.CAMERA)
				.request();
	}

	// 相机权限请求成功
	@PermissionGranted(PermissionCode.CAMERA) public void onCameraPermissionGranted() {
		if (cameraType == JSControlType.TakePhoto) {
			mCamera.start(CODE_CAMERA);
		}
		else if (cameraType == JSControlType.ZXing) {
			if (DevicesUtil.isCameraCanUsed(getContext())) {
				startActivityForResult(new Intent(getContext(), CaptureActivity.class), SCANNING_QR_CODE);
			}
		}

	}

	// 打开手写板
	public void openWrittingCombo() {
		new FEWrittingComboDialog()
				.setConfirmListener(path -> delegate.uploadFile(Arrays.asList(path), null, IS_WRITTING_COMBO))
				.show(getChildFragmentManager(), "oO皿Oo");
	}

	// 打开日期选择器
	public void openDatePicker(String dateTime, String dateTimeFormat) {
		Calendar calendar = DateUtil.str2Calendar(dateTime);
		DateTimePickerDialog dateTimePickerDialog = new DateTimePickerDialog();
		dateTimePickerDialog.setTimeLevel(DateUtil.getTimeLevelForFormat(dateTimeFormat));
		dateTimePickerDialog.setDateTime(calendar);
		dateTimePickerDialog.setButtonCallBack(new DateTimePickerDialog.ButtonCallBack() {
			@Override
			public void onClearClick() {
				callJavaScriptMethod(delegate.getExecuteResult(0, "", null).getJsMethod());
			}

			@Override
			public void onOkClick(Calendar calendar, DateTimePickerDialog dateTimePickerDialog) {
				String data = DateUtil.calendar2StringDateTime(calendar);
				callJavaScriptMethod(delegate.getExecuteResult(0, data, null).getJsMethod());
				dateTimePickerDialog.dismiss();
			}
		});
		dateTimePickerDialog.setCanClear(true);
		dateTimePickerDialog.show(getActivity().getFragmentManager(), "");
	}

	// 打开常用语
	public void openCommonWords(FormCommonWordInfo commonWordInfo, String[] commonWords) {
		new FEMaterialDialog.Builder(getActivity())
				.setCancelable(true)
				.setTitle(getResources().getString(R.string.common_language))
				.setItems(commonWords, (dialog, view, position) -> {
					dialog.dismiss();
					commonWordInfo.setUseCommonValue(commonWords[position]);
					callJavaScriptMethod(commonWordInfo.getJson());
				})
				.setPositiveButton(getResources().getString(R.string.lbl_text_edit), dialog -> {
					Intent intent = new Intent(getActivity(), CommonWordsActivity.class);
					startActivity(intent);
				})
				.setNegativeButton(null, null)
				.build()
				.show();
	}

	// 请求人员选择
	public void requestStaff(JSControlInfo jsControlInfo) {
		Intent intent = new Intent(getActivity(), FormPersonChooseActivity.class);
		intent.putExtra("NewFormChooseNodeData", jsControlInfo);
		startActivityForResult(intent, CODE_PERSON);
	}

	// 请求会议看板
	public void requestMeetingBoard(JSControlInfo jsControlInfo) {
		Intent intent = new Intent(getActivity(), MeetingBoardActivity.class);
		intent.putExtra(K.form.TITLE_DATA_KEY, getString(R.string.form_new_form_meetingboard_title));
		intent.putExtra(K.form.URL_DATA_KEY, jsControlInfo.getMeetingBoardURL());
		startActivityForResult(intent, CODE_MEETING_BOARD);
	}

	// 请求附件选择
	public void requestAttachment() {
		LuBan7.pufferGrenades(getActivity(), delegate.getSelectedAttachments(), null, CODE_ATTACHMENT);
	}

	// 打开参照项
	public void openReference(List<String> referenceValues) {
		FEPicker picker = new FEPicker(getActivity());
		picker.setCyclic(false);
		picker.setOnPickerButtonClickListener((view, data) -> {
			List<ReferenceItem> referenceItems = new ArrayList<>();
			List<ReferenceItem> orginSource = delegate.getJsControlInfo().getReferenceItems();
			if (CommonUtil.nonEmptyList(orginSource)) {
				for (ReferenceItem item : orginSource) {
					if (TextUtils.equals(item.getValue(), data)) {
						referenceItems.add(item);
					}
				}
			}

			if (CommonUtil.isEmptyList(referenceItems)) {
				ReferenceItem defaultItem = new ReferenceItem();
				defaultItem.setKey("");
				defaultItem.setValue("");
				referenceItems.add(defaultItem);
			}

			ExecuteResult executeResult = delegate.getExecuteResult(0, null, referenceItems);
			callJavaScriptMethod(executeResult.getJsMethod());
			picker.dismiss();
		});
		picker.showWithData(referenceValues);
	}

	// 打开预览附件
	public void openAttachment(String url, String attachmentGUID, String fileName) {
		if (TextUtils.isEmpty(url)) return;
		attachmentViewer.openAttachment(url, attachmentGUID, fileName);
	}

	// 调用系统功能：电话、短信、邮箱
	public void callSystemApp(String protocol, String value) {
		String action = Intent.ACTION_VIEW;
		Uri uri = null;
		if (protocol.startsWith("tel")) {
			action = Intent.ACTION_CALL;
			uri = Uri.parse("tel:" + value);
		}
		else if (protocol.startsWith("sms")) {
			action = Intent.ACTION_VIEW;
			uri = Uri.parse("smsto:" + value);
		}
		else if (protocol.startsWith("Mailto")) {
			action = Intent.ACTION_SENDTO;
			uri = Uri.parse("mailto:" + value);
		}
		try {
			startActivity(new Intent(action, uri));
		} catch (Exception exp) {
			exp.printStackTrace();
			FEToast.showMessage("操作失败，请重试！");
		}
	}

	public void callJavaScriptMethod(String jsMethod) {
		Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
			if (VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
				webView.evaluateJavascript(jsMethod, null);
			}
			else {
				webView.loadUrl("javascript:" + jsMethod);
			}
		});
	}

	public boolean goBack() {
		if (webView.canGoBack()) {
			webView.goBack();
			return true;
		}
		return false;
	}

	public boolean goForward() {
		if (webView.canGoForward()) {
			webView.goForward();
			return true;
		}
		return false;
	}

	public void reload() {
		if (!TextUtils.isEmpty(homeLink)) {
			loadUrl(homeLink);
		}
	}

	public WebView getWebView() {
		return webView;
	}

	public void requestSendForm() {
		ExecuteResult result = new ExecuteResult();
		result.setActionType(JSActionType.Check);
		callJavaScriptMethod(result.getJsMethod());
	}

	@Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		FePermissions.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
	}

	@Override public void onDestroy() {
		super.onDestroy();
		if (webView == null) return;
		webView.removeJavascriptInterface("androidJS");
		webView.removeAllViews();
		webView.destroyDrawingCache();
		webView.destroy();

		try {
			//X5浏览器内核
			CookieSyncManager.createInstance(CoreZygote.getContext());
			CookieManager x5cookieManager = CookieManager.getInstance();
			x5cookieManager.removeAllCookie();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
