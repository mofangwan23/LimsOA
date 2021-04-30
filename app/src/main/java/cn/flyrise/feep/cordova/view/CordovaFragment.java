package cn.flyrise.feep.cordova.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebView.HitTestResult;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import cn.flyrise.android.library.utility.LoadingHint;
import cn.flyrise.feep.K;
import cn.flyrise.feep.R;
import cn.flyrise.feep.commonality.bean.JSControlInfo;
import cn.flyrise.feep.commonality.fragment.FEFragment;
import cn.flyrise.feep.cordova.CordovaContract;
import cn.flyrise.feep.cordova.presenter.CordovaPresenter;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.X;
import cn.flyrise.feep.core.common.X.FormNullCheck;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.NetworkUtil;
import cn.flyrise.feep.core.common.utils.UriPathHelper;
import cn.flyrise.feep.core.dialog.FEMaterialDialog;
import cn.flyrise.feep.core.network.TokenInject;
import cn.flyrise.feep.form.FormPersonChooseActivity;
import cn.flyrise.feep.form.MeetingBoardActivity;
import cn.flyrise.feep.form.widget.handWritting.FEWrittingComboDialog;
import cn.flyrise.feep.media.DownloadUtils;
import cn.flyrise.feep.media.attachments.AudioPlayer;
import cn.flyrise.feep.media.attachments.bean.Attachment;
import cn.flyrise.feep.media.common.LuBan7;
import cn.flyrise.feep.utils.FEWebChromeClient;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.cordova.Config;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONObject;


public class CordovaFragment extends FEFragment implements CordovaContract.CordovaView {

	protected XCordovaWebView mWebView = null;

	private ProgressBar progressh;
	private FEWebChromeClient mFEWebChromeClient;
	private CordovaPresenter mPresenter;

	private String mUrl;
	private FormJsActivionListener mListener;
	protected FrameLayout mFrameLayout;
	private View errorLayout;
	private OnClickOpenListener mOpenListener;

	private FEWrittingComboDialog mWrittingComboDialog;

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (progressh != null) {
				progressh.setVisibility(View.VISIBLE);
				int progress = progressh.getProgress();
				progressh.setProgress(++progress);
				if (progress >= 100) {
					mHandler.removeMessages(MSG_PROGRESS_UPDATE);
					progressh.setVisibility(View.GONE);
				}
			}
			mHandler.sendEmptyMessageDelayed(MSG_PROGRESS_UPDATE, 50);
		}
	};

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		try {
			mHandler.sendEmptyMessage(MSG_PROGRESS_UPDATE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressLint("ClickableViewAccessibility") @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Config.init(getActivity());
		LayoutInflater localInflater = inflater.cloneInContext(new CordovaFragment.CordovaContext(getActivity()));
		View rootView = localInflater.inflate(R.layout.cordova_fragment, container, false);
		try {
			mWebView = rootView.findViewById(R.id.rl_title);
			progressh = rootView.findViewById(R.id.progressh);
			mFrameLayout = rootView.findViewById(R.id.fragment_layout);
			errorLayout = rootView.findViewById(R.id.error_layout);
			setListeners();
			final WebSettings webSettings = mWebView.getSettings();
			webSettings.setJavaScriptEnabled(true);
			webSettings.setSupportZoom(false);
			webSettings.setUseWideViewPort(false);
			webSettings.setBuiltInZoomControls(false);
			webSettings.setAllowFileAccess(true);
			webSettings.setAllowContentAccess(true);
			webSettings.setDatabaseEnabled(true);
			webSettings.setBlockNetworkImage(true);//先禁用图片的加载
			webSettings.setDefaultTextEncodingName("utf-8");
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				webSettings.setAllowFileAccessFromFileURLs(true);
			}
			webSettings.setAppCacheEnabled(true);
			webSettings.setGeolocationEnabled(true);
			webSettings.setDomStorageEnabled(true);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				try {
					CookieManager cookieManager = CookieManager.getInstance();
					cookieManager.setAcceptThirdPartyCookies(this.mWebView, true);
				} catch (Exception ex) {
				}
			}
			mFEWebChromeClient = new FEWebChromeClient(this.getActivity(), progressh);
			mWebView.setWebChromeClient(mFEWebChromeClient);
			mPresenter = new CordovaPresenter(CordovaFragment.this);
			progressh.setVisibility(View.VISIBLE);
			if (!TextUtils.isEmpty(mUrl)) {
				TokenInject.injectToken(mWebView, mUrl);
			}
			mWebView.setOnTouchListener((v, event) -> {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					mWebView.requestFocus();
					mPresenter.MeasuredHeight(event);
				}
				return false;
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rootView;
	}

	private void setListeners() {
		errorLayout.setOnClickListener(v -> {
			if (!NetworkUtil.isNetworkAvailable(getActivity())) {
				FEToast.showMessage(CommonUtil.getString(R.string.lbl_retry_network_connection));
				return;
			}
			if (mWebView != null) mWebView.reload();
		});

		mWebView.setOnLongClickListener(v -> {
			HitTestResult hitTestResult = mWebView.getHitTestResult();
			if (hitTestResult.getType() == WebView.HitTestResult.IMAGE_TYPE) {
				new FEMaterialDialog.Builder(getActivity())
						.setItems(new String[]{"下载"}, (dialog, view, position) -> {
							if (position == 0) {
								DownloadUtils.downloadImage(getActivity(), hitTestResult.getExtra());
							}
							dialog.dismiss();
						}).build().show();
			}
			return false;
		});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == FEWebChromeClient.FILECHOOSER_RESULTCODE) {                                                  // Android 5.0 以下
			ValueCallback<Uri> uploadMessage = mFEWebChromeClient.getUploadMessage();
			Uri result = (intent == null || resultCode != Activity.RESULT_OK) ? null : intent.getData();
			if (uploadMessage != null) {
				if (result != null) {
					String path = UriPathHelper.getPath(getContext(), result);
					if (!TextUtils.isEmpty(path)) result = Uri.fromFile(new File(path));
				}
				uploadMessage.onReceiveValue(result);
				mFEWebChromeClient.setUploadMessage(null);
			}
		}
		else if (requestCode == FEWebChromeClient.REQUEST_SELECT_FILE) {                                                // Android 5.0 及以上
			ValueCallback<Uri[]> uploadMessages = mFEWebChromeClient.getUploadMessages();
			Uri[] result = (intent == null || resultCode != Activity.RESULT_OK) ? null : new Uri[]{intent.getData()};
			if (uploadMessages != null) {
				uploadMessages.onReceiveValue(result);
				mFEWebChromeClient.setUploadMessages(null);
			}
		}
		else mPresenter.onActivityResult(requestCode, resultCode, intent);
	}

	@Override
	public Handler getHandler() {
		return mHandler;
	}

	@Override
	public CordovaWebView getWebView() {
		return mWebView;
	}

	@Override
	public FragmentActivity getCordovaContext() {
		return getActivity();
	}

	@Override
	public Context getContexts() {
		return getActivity();
	}

	@Override
	public void onPostExecute(JSControlInfo controlInfo) {
		if (mListener != null) mListener.doAfterCheck(controlInfo);
	}

	@Override
	public void openRecord() {

	}

	@Override
	public void openPhoto() {

	}

	@Override
	public void openWrittingCombo() {
		mPresenter.addAttachment();
		mWrittingComboDialog = new FEWrittingComboDialog();
		mWrittingComboDialog.setConfirmListener((path) -> {
			List<String> writtingCombos = new ArrayList<>();
			writtingCombos.add(path);
			mPresenter.addSelectedAttachments(writtingCombos);
			mPresenter.uploadFile(CordovaContract.CordovaPresenters.IS_WRITTING_COMBO);
		}).setCancelListener(() -> {});
		mWrittingComboDialog.show(getFragmentManager(), "writtingCombo");
	}

	private class CordovaContext extends ContextWrapper implements CordovaInterface {

		Activity activity;
		protected final ExecutorService threadPool = Executors.newCachedThreadPool();

		public CordovaContext(Activity activity) {
			super(activity);
			this.activity = activity;
		}

		public void startActivityForResult(CordovaPlugin command, Intent intent, int requestCode) {
			activity.startActivityForResult(intent, requestCode);
		}

		public void setActivityResultCallback(CordovaPlugin plugin) {
		}

		public Activity getActivity() {
			return activity;
		}

		public Object onMessage(String id, Object data) {
			if (ONPAGEFINISHED.equalsIgnoreCase(id)) {
				mHandler.removeMessages(MSG_PROGRESS_UPDATE);
				progressh.setVisibility(View.GONE);
			}
			return null;
		}

		public ExecutorService getThreadPool() {
			return threadPool;
		}

	}

	@Override
	public void onDestroy() {
		if (mWrittingComboDialog != null) mWrittingComboDialog.dismiss();
		super.onDestroy();
		if (mWebView != null) {
			try {
				Handler handler = mWebView.getHandler();
				if (handler != null) handler.removeCallbacksAndMessages(null);
				mHandler.removeCallbacksAndMessages(null);
				mWebView.handleDestroy();
				mWebView.removeAllViews();
				mWebView.destroy();
				mWebView = null;
				mFEWebChromeClient = null;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onPageFinished() {

	}

	@Override
	public void setViewVisible(boolean isLoadSuccess) {
		if (isLoadSuccess) {
			mWebView.setVisibility(View.VISIBLE);
			errorLayout.setVisibility(View.GONE);
		}
		else {
			mWebView.setVisibility(View.GONE);
			errorLayout.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void clickType(int controlType) {
		if (mOpenListener != null) mOpenListener.clickType(controlType);
	}

	@Override
	public void playAudio(Attachment attachment, String audioPath) {
		AudioPlayer player = AudioPlayer.newInstance(attachment, audioPath);
		player.show(getChildFragmentManager(), "Audio");
	}

	@Override
	public void openAttachment(Intent intent) {
		if (intent == null) {
			FEToast.showMessage("暂不支持查看此文件类型");
			return;
		}

		try {
			startActivity(intent);
		} catch (Exception exp) {
			FEToast.showMessage("无法打开，建议安装查看此类型文件的软件");
		}
	}

	@Override
	public void clickMeetingRoom(JSControlInfo controlInfo) {
		final Intent intent = new Intent(getActivity(), MeetingBoardActivity.class);
		intent.putExtra(K.form.TITLE_DATA_KEY, getString(R.string.form_new_form_meetingboard_title));
		intent.putExtra(K.form.URL_DATA_KEY, controlInfo.getMeetingBoardURL());
		startActivityForResult(intent, mPresenter.INTENT_TO_MEETINGBOARD_REQUEST_CODE);
	}

	@Override
	public void clickSendButton(JSControlInfo controlInfo) {//点击发送按钮后返回数据调用此方法
		boolean isLoadingHint = true;
		final int actionType = controlInfo.getActionType();
		if (actionType == X.JSActionType.Error) {
			return;
		}
		else if (actionType == X.JSActionType.Send) {
			if (mListener != null) mListener.JSActionSend(controlInfo);
			return;
		}
		else if (actionType == X.JSActionType.FetchData) { // 选择人员那一步-----------------
			if (mListener != null) mListener.JSActionGetData(controlInfo);
			return;
		}
		else if (actionType == X.JSActionType.Search) {
			if (mListener != null) mListener.JSActionSearch(controlInfo);
			return;
		}
		final int nullCheckResult = controlInfo.getNullCheckResult(); // 非空性检查的结果
		if (nullCheckResult == FormNullCheck.Null && actionType == X.JSActionType.Check) {
			FEToast.showMessage(getActivity().getString(R.string.form_need_input));
		}
		else if (nullCheckResult == FormNullCheck.NonNull) {
			if (mPresenter.uploadFile(0)) {// 有附件先上传附件,并且不隐藏加载框
				isLoadingHint = false;
			}
			else {
				isLoadingHint = true;
				if (mListener != null) mListener.doAfterCheck(controlInfo);
			}
		}
		else if (nullCheckResult == FormNullCheck.DataExist) {
			FEToast.showMessage(getString(R.string.form_null_checked_exit_data));
		}
		else if (nullCheckResult == FormNullCheck.NonFormID) {
			FEToast.showMessage(getString(R.string.form_null_checked_non_formid));
		}
		// 显示toast后隐藏loading
		if (isLoadingHint) LoadingHint.hide();
	}

	@Override
	public void clickAddAttachment() {//点击添加附件调用此方法
		mPresenter.addAttachment();
		LuBan7.pufferGrenades(getActivity(), mPresenter.getSelectedAttachments(),
				null, CordovaContract.CordovaPresenters.ADD_ATTACHMENT_REQUEST_CODE);
	}

	@Override
	public void clickPersonChoose(JSControlInfo controlInfo) {//点击选择人员调用此方法
		final Intent intent = new Intent(getActivity(), FormPersonChooseActivity.class);
		intent.putExtra(NEW_FORM_CHOOSE_NODE_DATA, controlInfo);
		getActivity().startActivityForResult(intent, CordovaContract.CordovaPresenters.ADD_PERSON_REQUEST_CODE);
	}

	public void goBack() {
		if (mWebView == null) return;
		if (VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
			mWebView.evaluateJavascript("javascript:pageBack();", null);
		}
		else {
			mWebView.loadUrl("javascript:pageBack();");
		}
	}

	public void goForward() {
		if (mWebView == null) return;
		mWebView.goForward();
	}

	public void goReload() {
		if (mWebView == null) return;
		mWebView.reload();
	}


	public void loadUrl(String url) {
		FELog.i("CordovaFragment", "-->>>>Url->" + url);
		this.mUrl = url;
	}

	public void sendToJavascript(JSONObject json) {
		if (mPresenter != null) mPresenter.sendToJavascript(json);
	}

	public void setFormJsActionListener(FormJsActivionListener listener) {
		this.mListener = listener;
	}

	public void setScrollBy(double distanceX, double distanceY) {
		if (mWebView == null) return;
		mWebView.scrollBy(-(int) distanceX, -(int) distanceY);
	}

	public interface FormJsActivionListener {

		void JSActionSend(JSControlInfo controlInfo);//新建表单的发送

		void JSActionGetData(JSControlInfo controlInfo);//处理表单的下一步

		void JSActionSearch(JSControlInfo controlInfo);//报表搜索

		void doAfterCheck(JSControlInfo controlInfo);//进行非空检查后，非空所需要做的事
	}

	public interface OnClickOpenListener {

		void clickType(int controlType);
	}

	public void setOnClickOpenListener(OnClickOpenListener openListener) {
		this.mOpenListener = openListener;
	}

}
