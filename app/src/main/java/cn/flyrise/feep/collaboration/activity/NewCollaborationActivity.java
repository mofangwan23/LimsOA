package cn.flyrise.feep.collaboration.activity;

import static jp.wasabeef.richeditor.Utils.tryAddHostToImageBeforeEdit;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.Html;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.flyrise.android.shared.utility.FEUmengCfg;
import cn.flyrise.feep.FEApplication;
import cn.flyrise.feep.K.collaboration;
import cn.flyrise.feep.R;
import cn.flyrise.feep.collaboration.model.Collaboration;
import cn.flyrise.feep.collaboration.presenter.NewCollaborationPresenter;
import cn.flyrise.feep.collaboration.presenter.NewCollaborationView;
import cn.flyrise.feep.collaboration.utility.RichTextContentKeeper;
import cn.flyrise.feep.commonality.manager.XunFeiVoiceInput;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.base.component.BaseEditableActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.base.views.UISwitchButton;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.LanguageManager;
import cn.flyrise.feep.core.dialog.FELoadingDialog;
import cn.flyrise.feep.core.dialog.FEMaterialDialog;
import cn.flyrise.feep.core.function.FunctionManager;
import cn.flyrise.feep.core.network.TokenInject;
import cn.flyrise.feep.core.network.entry.AttachmentBean;
import cn.flyrise.feep.core.premission.FePermissions;
import cn.flyrise.feep.core.premission.PermissionCode;
import cn.flyrise.feep.core.premission.PermissionGranted;
import cn.flyrise.feep.email.views.UrlImageParser;
import cn.flyrise.feep.utils.Patches;
import cn.squirtlez.frouter.annotations.RequestExtras;
import cn.squirtlez.frouter.annotations.Route;
import com.jakewharton.rxbinding.view.RxView;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by klc on 2017/4/24.
 */
@Route("/collaboration/create")
@RequestExtras({"userIds", "fromType", "fromData", "collaborationId"}) //100:fromIm,101:froward
public class NewCollaborationActivity extends BaseEditableActivity implements NewCollaborationView {

	public final static String IMAGE_STYLE = "<style type='text/css'>" +
			"body{word-wrap: break-word!important;" +
			"word-break:break-all!important;" +
			"text-align:justify!important;" +
			"text-justify:inter-ideograph!important;}" +
			"img{width:50%!important;}" +
			"</style>";

	/**
	 * 使用富文本编辑器编辑协同内容
	 */
	private final static int EDIT_RICH_CONTENT_CODE = 201;

	/**
	 * 标题允许最大字数
	 */
	private final static int numTitleMax = 50;

	private FEToolbar mToolBar;
	private EditText etTitle;
	private EditText etContent;
	private EditText etTempEditText;
	private Button btTitleMic;
	private TextView tvNum;
	private TextView tvFlow;
	private TextView tvAttachments;
	private TextView tvAssociation;
	private UISwitchButton btTrace;
	private UISwitchButton btUrgent;
	private UISwitchButton btModify;
	private LinearLayout layoutModify;
	private Button btSubmit;
	private Button btSave;

	private WebView mCollaborationWebView;

	private LinearLayout mLvOldImport;
	private LinearLayout mLvNewImport;
	private TextView mTvImport;

	private FELoadingDialog mLoadingDialog;

	private NewCollaborationPresenter mPresenter;
	private boolean isNewImport;
	private XunFeiVoiceInput mVoiceInput;

	public static void startForWorkPlan(Activity activity, String title, String content, List<AttachmentBean> attachmentList) {
		Intent intent = new Intent(activity, NewCollaborationActivity.class);
		intent.putExtra(collaboration.EXTRA_NEW_COLLABORATION_TITLE, title);
		intent.putExtra(collaboration.EXTRA_NEW_COLLABORATION_CONTENT, content);
		intent.putExtra(collaboration.EXTRA_FORM_TYPE, collaboration.EXTRA_FORM_TYPE_WORKPLAN);
		intent.putParcelableArrayListExtra(collaboration.EXTRA_NEW_COLLABORATION_ATTACHMENT,
				(ArrayList<? extends Parcelable>) attachmentList);
		activity.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		supportRequestWindowFeature(Window.FEATURE_ACTION_MODE_OVERLAY);
		setContentView(R.layout.collaboration_newcollaboration);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		mPresenter.loadData(intent);
	}

	@Override
	protected void toolBar(FEToolbar toolbar) {
		mToolBar = toolbar;
		mToolBar.setTitle(R.string.flow_titlenew);
		toolbar.setNavigationOnClickListener(v -> {
			if (isHasWrote()) {
				showExitDialog();
			}
			else {
				finish();
			}
		});
		if (FunctionManager.hasPatch(Patches.PATCH_COLLABORATION_WAIT_SEND)) {
			toolbar.setRightText(R.string.committed);
			toolbar.setRightTextClickListener(v -> startActivity(new Intent(NewCollaborationActivity.this, WaitingSendListActivity.class)));
		}
	}

	@Override
	public void bindView() {
		super.bindView();
		etTitle = (EditText) findViewById(R.id.title_voice_input_edit);
		btTitleMic = (Button) findViewById(R.id.title_voice_input_mic_bnt);
		tvNum = (TextView) findViewById(R.id.title_num);
		etContent = (EditText) findViewById(R.id.content_voice_input_edit);
		tvAttachments = (TextView) findViewById(R.id.imagetextbuton_attac);
		tvFlow = (TextView) findViewById(R.id.imagetextbuton_flow);
		tvAssociation = (TextView) findViewById(R.id.tv_association);
		if (FunctionManager.hasPatch(Patches.PATCH_RELATED_MATTERS)) {
			findViewById(R.id.lv_association).setVisibility(View.VISIBLE);
		}
		btModify = (UISwitchButton) findViewById(R.id.newcollaborarion_ismodify_checkbox);
		layoutModify = (LinearLayout) findViewById(R.id.newcollaborarion_ismodify_textview_layout);
		btTrace = (UISwitchButton) findViewById(R.id.newcollaborarion_istrace_checkbox);
		btUrgent = (UISwitchButton) findViewById(R.id.newcollaboration_spinner);
		btUrgent.setChecked(false);
		btSubmit = (Button) findViewById(R.id.submit);
		btSave = (Button) findViewById(R.id.save);
		tvNum.setText(String.format(getResources().getString(R.string.words_can_input), numTitleMax));
		btTitleMic.setVisibility(
				LanguageManager.getCurrentLanguage() == LanguageManager.LANGUAGE_TYPE_CN ? View.VISIBLE : View.GONE);
		btSave.setVisibility(FunctionManager.hasPatch(Patches.PATCH_COLLABORATION_WAIT_SEND) ? View.VISIBLE : View.GONE);
		mLvOldImport = (LinearLayout) findViewById(R.id.lv_oldImport);
		mLvNewImport = (LinearLayout) findViewById(R.id.lv_newImport);
		mTvImport = (TextView) findViewById(R.id.tvImportant);

		mCollaborationWebView = (WebView) findViewById(R.id.collaborationWebView);
		mCollaborationWebView.getSettings().setAppCacheEnabled(true);
		mCollaborationWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
	}

	@Override
	public void bindData() {
		mVoiceInput = new XunFeiVoiceInput(this);
		isNewImport = FunctionManager.hasPatch(Patches.PATCH_COLLABORATION_EMERGENCY_DEGREE);
		mPresenter = new NewCollaborationPresenter(this, isNewImport);
		mPresenter.loadData(getIntent());
		if (isNewImport) {
			mLvNewImport.setVisibility(View.VISIBLE);
		}
		else {
			mLvOldImport.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void bindListener() {
		super.bindListener();
		btTitleMic.setOnClickListener(v -> {
			requestAudioPermission();
			etTitle.requestFocus();
		});
		mLvNewImport.setOnClickListener(v -> mPresenter.importClick());
		tvFlow.setOnClickListener(v -> mPresenter.flowClick(NewCollaborationActivity.this, WorkFlowActivity.COLLABORATION_NEW));
		tvAttachments.setOnClickListener(v -> mPresenter.attachmentClick(NewCollaborationActivity.this));
		tvAssociation.setOnClickListener(v -> mPresenter.associationClick(NewCollaborationActivity.this));

		// 防手残用户点点点...
		RxView.clicks(btSubmit)
				.throttleFirst(1, TimeUnit.SECONDS)
				.subscribe(a -> checkBeforeSend(), exception -> exception.printStackTrace());

		RxView.clicks(btSave)
				.throttleFirst(1, TimeUnit.SECONDS)
				.subscribe(a -> {
					if (isHasWrote()) {
						getViewValue();
						mPresenter.saveCollaboration(NewCollaborationActivity.this);
						return;
					}

					FEToast.showMessage(getString(R.string.collaboration_no_save_hint));
				}, exception -> exception.printStackTrace());

		etTitle.setOnFocusChangeListener(mFocusChangeListener);
		etTitle.addTextChangedListener(new TextWatcher() {

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				Editable editable = etTitle.getText();
				int len = editable.length();
				if (len > numTitleMax) {
					int selEndIndex = Selection.getSelectionEnd(editable);
					String str = editable.toString();
					//截取新字符串
					String newStr = str.substring(0, numTitleMax);
					etTitle.setText(newStr);
					editable = etTitle.getText();
					//新字符串的长度
					int newLen = editable.length();
					//旧光标位置超过字符串长度
					if (selEndIndex > newLen) {
						selEndIndex = editable.length();
					}
					//设置新光标所在的位置
					Selection.setSelection(editable, selEndIndex);
				}
				else {
					tvNum.setText(String.format(getResources().getString(R.string.words_can_input), numTitleMax - len));
				}
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

			etContent.setVisibility(View.GONE);
			mCollaborationWebView.setVisibility(View.VISIBLE);
			findViewById(R.id.btnVoiceInput).setVisibility(View.GONE);

			View richContentLayout = findViewById(R.id.layoutRichContent);
			richContentLayout.setOnClickListener(view -> {
				Intent intent = new Intent(NewCollaborationActivity.this, RichTextEditActivity.class);
				intent.putExtra("title", getString(R.string.lbl_content_hint));
				startActivityForResult(intent, EDIT_RICH_CONTENT_CODE);
			});

			GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
				@Override
				public boolean onSingleTapUp(MotionEvent e) {
					Intent intent = new Intent(NewCollaborationActivity.this, RichTextEditActivity.class);
					intent.putExtra("title", getString(R.string.lbl_content_hint));
					startActivityForResult(intent, EDIT_RICH_CONTENT_CODE);
					return false;
				}
			});

			mCollaborationWebView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
		}
		else {
			etContent.setVisibility(View.VISIBLE);
			mCollaborationWebView.setVisibility(View.GONE);
			etContent.setFocusable(true);
			etContent.setOnFocusChangeListener(mFocusChangeListener);
			findViewById(R.id.btnVoiceInput).setOnClickListener(view -> {
				requestAudioPermission();
				etContent.requestFocus();
			});
		}
		mVoiceInput.setOnRecognizerDialogListener(text ->
				XunFeiVoiceInput.setVoiceInputText(etTempEditText, text, etTempEditText.getSelectionStart()));
	}

	private View.OnFocusChangeListener mFocusChangeListener = (v, hasFocus) -> etTempEditText = (EditText) v;

	private void checkBeforeSend() {
		// 隐藏输入法
		if (this.getCurrentFocus() != null && this.getCurrentFocus().getWindowToken() != null) {
			final InputMethodManager imm = ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE));
			imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
		String title = etTitle.getText().toString().trim();
		if (TextUtils.isEmpty(title) || title.length() == 0) {
			FEToast.showMessage(this.getString(R.string.collaboration_input_title));
			etTitle.requestFocus();
			return;
		}
		else if (title.length() > 110) {
			FEToast.showMessage(getString(R.string.collaboration_toolong_title));
			etTitle.requestFocus();
			return;
		}
		if (!mPresenter.hasFlow()) {
			FEToast.showMessage(this.getString(R.string.collaboration_add_flow));
			mPresenter.flowClick(NewCollaborationActivity.this, WorkFlowActivity.COLLABORATION_NEW);
			return;
		}
		if (isNewImport && mTvImport.getText().toString().equals(getString(R.string.schedule_detail_lbl_share_none))) {
			FEToast.showMessage(getString(R.string.collaboration_select_import));
			mPresenter.importClick();
			return;
		}
		getViewValue();
		mPresenter.handleConfirmBtn(this);
	}

	private void getViewValue() {
		Collaboration collaboration = mPresenter.getCollaboration();
		collaboration.setTrace(btTrace.isChecked());
		collaboration.setModify(btModify.isChecked());
		collaboration.title = etTitle.getText().toString().trim();
		collaboration.content = tryTransformImagePath();
		if (isNewImport) {
			collaboration.important = mTvImport.getText().toString();
		}
		else {
			collaboration.important = btUrgent.isChecked() ? "急件" : "平件";
		}
	}

	private String tryTransformImagePath() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
			return etContent.getText().toString();
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (mPresenter.onActivityResult(requestCode, resultCode, data)) {
			return;
		}
		if (requestCode == EDIT_RICH_CONTENT_CODE && resultCode == RESULT_OK) {
			if (RichTextContentKeeper.getInstance().hasContent()) {
				etContent.setVisibility(View.GONE);
				if (mCollaborationWebView.getVisibility() != View.VISIBLE) {
					mCollaborationWebView.setVisibility(View.VISIBLE);
				}

				String richTextContent = tryTransformImagePath();
				mCollaborationWebView.loadDataWithBaseURL(CoreZygote.getLoginUserServices().getServerAddress(),
						IMAGE_STYLE + richTextContent, "text/html; charset=utf-8", "UTF-8", null);
			}
			else {
				TokenInject.injectToken(mCollaborationWebView, "");
			}
		}
	}

	/**
	 * 判断用户是否在此页面填写过东西
	 */
	private boolean isHasWrote() {
		final String titleText = etTitle.getText().toString();
		final String contentText = etContent.getText().toString();
		return !TextUtils.isEmpty(titleText) || !TextUtils.isEmpty(contentText) || mPresenter.hasFlow() || mPresenter.hasFile()
				|| mPresenter.hasAssociation() || RichTextContentKeeper.getInstance().hasContent();
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


	private void requestAudioPermission() {
		FePermissions.with(this)
				.permissions(new String[]{android.Manifest.permission.RECORD_AUDIO})
				.rationaleMessage(getResources().getString(R.string.permission_rationale_record))
				.requestCode(PermissionCode.RECORD)
				.request();
	}

	@PermissionGranted(PermissionCode.RECORD)
	public void onRecordPermissionGranted() {
		if (mVoiceInput != null) mVoiceInput.show();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		FePermissions.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
	}

	@Override
	protected void onResume() {
		super.onResume();
		FEUmengCfg.onActivityResumeUMeng(this, FEUmengCfg.NewCollaboration);
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mVoiceInput != null) mVoiceInput.dismiss();
		FEUmengCfg.onActivityPauseUMeng(this, FEUmengCfg.NewCollaboration);
	}

	@Override
	protected void onDestroy() {
		RichTextContentKeeper.getInstance().removeCache();
		RichTextContentKeeper.getInstance().removeCompressImagePath();
		super.onDestroy();
	}

	@Override
	public void showLoading() {
		hideLoading();
		mLoadingDialog = new FELoadingDialog.Builder(NewCollaborationActivity.this)
				.setCancelable(false)
				.create();
		mLoadingDialog.show();
	}

	@Override
	public void showProgress(int progress) {
		if (mLoadingDialog != null) {
			mLoadingDialog.updateProgress(progress);
		}
	}

	@Override
	public void hideLoading() {
		if (mLoadingDialog != null) {
			mLoadingDialog.hide();
		}
		mLoadingDialog = null;
	}

	@Override
	public void displayView(Collaboration collaboration) {
		if (!TextUtils.isEmpty(collaboration.title)) {
			if (collaboration.title.contains("《"))
				collaboration.title = collaboration.title.substring(1, collaboration.title.length() - 1);
			etTitle.setText(collaboration.title);
			etTitle.setSelection(collaboration.title.length());
		}
		if (!TextUtils.isEmpty(collaboration.content)) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
				etContent.setVisibility(View.GONE);
				mCollaborationWebView.setVisibility(View.VISIBLE);
				mCollaborationWebView.loadDataWithBaseURL(CoreZygote.getLoginUserServices().getServerAddress(),
						IMAGE_STYLE + collaboration.content, "text/html; charset=utf-8", "UTF-8", null);

				RichTextContentKeeper.getInstance().setRichTextContent(tryAddHostToImageBeforeEdit(collaboration.content));
			}
			else {
				mCollaborationWebView.setVisibility(View.GONE);
				etContent.setVisibility(View.VISIBLE);
				etContent.setText(Html.fromHtml(collaboration.content, new UrlImageParser(etContent,
						CoreZygote.getLoginUserServices().getServerAddress()), null));
			}
		}
		btTrace.setChecked(collaboration.isTrace());
		btUrgent.setChecked(collaboration.isUrgent());
		if (((FEApplication) getApplication()).isModify) {
			btModify.setVisibility(View.VISIBLE);
			layoutModify.setVisibility(View.VISIBLE);
			btModify.setChecked(collaboration.isModify());
		}
		else {
			btModify.setVisibility(View.GONE);
			layoutModify.setVisibility(View.GONE);
		}
		if (isNewImport) {
			mTvImport.setText(collaboration.important);
		}
		else {
			btUrgent.setChecked(!TextUtils.equals(collaboration.important, "平件"));
		}
	}

	@Override
	public void setTitle(int text) {
		mToolBar.setTitle(text);
	}

	@Override
	public void setFileTextCount(int count) {
		tvAttachments.setText(count == 0 ? getString(R.string.collaboration_attachment)
				: String.format(getString(R.string.collaboration_has_attachment), count));
	}

	@Override
	public void setHasFlow(boolean hasFlow) {
		tvFlow.setText(!hasFlow ? R.string.collaboration_flow_not : R.string.collaboration_flow_yes);
	}

	@Override
	public void setAssociationCount(int count) {
		tvAssociation.setText(count == 0 ? getString(R.string.association) : String.format(getString(R.string.association_has), count));
	}

	@Override
	public void setImportValue(String value) {
		mTvImport.setText(value);
	}

	@Override
	public void hideSaveButton() {
		btSave.setVisibility(View.GONE);
	}

	@Override
	public void showImportDialog(String[] value) {
		new FEMaterialDialog.Builder(NewCollaborationActivity.this)
				.setWithoutTitle(true)
				.setItems(value, (dialog, view, position) -> {
					mTvImport.setText(value[position]);
					dialog.dismiss();
				})
				.build()
				.show();
	}

}
