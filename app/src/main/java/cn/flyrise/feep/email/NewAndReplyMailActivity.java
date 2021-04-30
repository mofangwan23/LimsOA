package cn.flyrise.feep.email;

import static cn.flyrise.android.protocol.entity.EmailSendDoRequest.OPERATOR_DRAFT;
import static cn.flyrise.android.protocol.entity.EmailSendDoRequest.OPERATOR_SAVE;
import static cn.flyrise.android.protocol.entity.EmailSendDoRequest.OPERATOR_SEND;
import static jp.wasabeef.richeditor.Utils.tryAddHostToImageBeforeEdit;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import cn.flyrise.android.library.utility.LoadingHint;
import cn.flyrise.android.protocol.entity.CommonResponse;
import cn.flyrise.android.protocol.entity.EmailReplyResponse;
import cn.flyrise.android.protocol.entity.EmailSendDoRequest;
import cn.flyrise.feep.K;
import cn.flyrise.feep.R;
import cn.flyrise.feep.addressbook.utils.ContactsIntent;
import cn.flyrise.feep.collaboration.activity.RichTextEditActivity;
import cn.flyrise.feep.collaboration.utility.RichTextContentKeeper;
import cn.flyrise.feep.commonality.bean.SelectedPerson;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.base.component.NotTranslucentBarActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.common.DataKeeper;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.DevicesUtil;
import cn.flyrise.feep.core.dialog.FEMaterialDialog;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.services.model.AddressBook;
import cn.flyrise.feep.email.presenter.MailDetailPresenter;
import cn.flyrise.feep.email.presenter.NewReplyPresenter;
import cn.flyrise.feep.email.presenter.NewReplyView;
import cn.flyrise.feep.email.views.EmailAddressLayout;
import cn.flyrise.feep.email.views.UrlImageParser;
import cn.flyrise.feep.media.attachments.bean.NetworkAttachment;
import cn.flyrise.feep.media.common.LuBan7;
import cn.squirtlez.frouter.annotations.RequestExtras;
import cn.squirtlez.frouter.annotations.Route;
import com.jakewharton.rxbinding.view.RxView;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.greenrobot.eventbus.EventBus;

/**
 * @author ZYP
 * @since 2016/6/29 09:42
 * <p/>
 * 新建、编辑、草稿、转发
 */
@Route("/mail/create")
@RequestExtras({"userIds"})
public class NewAndReplyMailActivity extends NotTranslucentBarActivity implements NewReplyView, View.OnClickListener {

	public static final Integer FLAG_FINISH_ACTIVITY = -1;
	public static final Integer FLAG_MAIL_DELETE = 0;
	public static final Integer FLAG_SENT_REFRESH = 1;

	/**
	 * 使用富文本编辑计划内容
	 */
	public static final int EDIT_RICH_CONTENT_CODE = 203;

	public static final int CODE_SELECT_ATTACHMENT = 8;

	private NewReplyPresenter mPresenter;
	private EmailAddressLayout mEmailRecipients;
	private EmailAddressLayout mEmailCopyTo;
	private EmailAddressLayout mEmailBlindTo;

	private EditText mEtEmailTitle;
	private EditText mEtEmailContent;
	private TextView mTvEmailSender;
	private TextView mTvAttachment;

	private FEToolbar mToolBar;
	private WebView mWebView;
	private View mCopyAndBlindView;

	private String mMailId;
	private String mUserName;
	private String mMailAccount;
	private boolean hasEmailContentFocus = false;
	private EmailReplyResponse mDetailResponse;

	public static void startNewReplyActivity(Context context, String boxName, String mailId, String transmit, String mailAccount) {
		Intent intent = new Intent(context, NewAndReplyMailActivity.class);
		intent.putExtra(K.email.box_name, boxName);
		intent.putExtra(K.email.mail_id, mailId);
		intent.putExtra(K.email.b_transmit, transmit);
		intent.putExtra(K.email.mail_account, mailAccount);
		context.startActivity(intent);
	}

	public static void startNewReplyActivity(Context context, String mMailAccount, SelectedPerson selectedPerson) {
		Intent intent = new Intent(context, NewAndReplyMailActivity.class);
		intent.putExtra(K.email.mail_account, mMailAccount);
		intent.putExtra(K.email.mail_select_persons, selectedPerson);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		mMailAccount = intent.getStringExtra(K.email.mail_account);
		setContentView(R.layout.email_create_new);

		mPresenter = new NewReplyPresenter(
				mMailId = getIntent().getStringExtra(K.email.mail_id),
				getIntent().getStringExtra(K.email.box_name),
				getIntent().getStringExtra(K.email.b_transmit),
				mMailAccount, this);
		mPresenter.start();
	}

	@Override
	protected void toolBar(FEToolbar toolbar) {
		super.toolBar(toolbar);
		this.mToolBar = toolbar;
		this.mToolBar.setRightText(getResources().getString(R.string.lbl_text_send));

		TextView textView = this.mToolBar.getRightTextView();
		RxView.clicks(textView)
				.throttleFirst(1, TimeUnit.SECONDS)
				.subscribe(a -> {
					DevicesUtil.tryCloseKeyboard(this);
					new Handler().postDelayed(this::sendMail, 500);
				});

		this.mToolBar.setNavigationOnClickListener(v -> {
			if (askForExist()) {
				return;
			}
			finish();
		});
	}

	@Override
	public void bindView() {
		mEmailRecipients = (EmailAddressLayout) findViewById(R.id.tagRecipients);
		mEmailRecipients.initEmailAddress(this, true, mMailAccount);

		SelectedPerson person = getIntent().getParcelableExtra(K.email.mail_select_persons);
		if (person != null && !person.isEmpty()) {
			AddressBook addressBook = new AddressBook();
			addressBook.name = person.userName;
			addressBook.userId = person.userId;
			mEmailRecipients.addPerson(addressBook);
		}

		ArrayList<String> userIds = getIntent().getStringArrayListExtra("userIds");
		if (CommonUtil.nonEmptyList(userIds)) {
			List<AddressBook> addressBooks = CoreZygote.getAddressBookServices().queryUserIds(userIds);
			if (CommonUtil.nonEmptyList(userIds)) {
				mEmailRecipients.setRecipients(addressBooks);
			}
		}

		mEmailCopyTo = (EmailAddressLayout) findViewById(R.id.tagCopyTo);
		mEmailCopyTo.initEmailAddress(this, true, mMailAccount);

		mEmailBlindTo = (EmailAddressLayout) findViewById(R.id.tagBlindCopy);
		mEmailBlindTo.initEmailAddress(this, true, mMailAccount);

		mEtEmailTitle = (EditText) findViewById(R.id.etMailTitle);
		mEtEmailContent = (EditText) findViewById(R.id.etMailContent);
		mTvEmailSender = (TextView) findViewById(R.id.tvMailSender);
		mTvAttachment = (TextView) findViewById(R.id.tvAttachment);
		mCopyAndBlindView = findViewById(R.id.llCopyAndBlind);
		mWebView = (WebView) findViewById(R.id.webView);

		ImageView attachmentIcon = (ImageView) findViewById(R.id.ivMailIcon);
		attachmentIcon.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
	}

	@Override
	public void bindListener() {
		mTvEmailSender.setOnClickListener(this);
		mTvAttachment.setOnClickListener(this);
		mEmailRecipients.setRightButtonClickListener(this);
		mEmailCopyTo.setRightButtonClickListener(this);
		mEmailBlindTo.setRightButtonClickListener(this);

		mEtEmailContent.setOnFocusChangeListener((v, hasFocus) -> {
			hasEmailContentFocus = hasFocus;
			onEditTextFocusChange(hasFocus);
		});

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			mEtEmailContent.setFocusable(false);
			mEtEmailContent.setLongClickable(false);
			mEtEmailContent.setOnClickListener(view -> {
				Intent intent = new Intent(NewAndReplyMailActivity.this, RichTextEditActivity.class);
				intent.putExtra("title", "编辑邮件");
				startActivityForResult(intent, EDIT_RICH_CONTENT_CODE);
			});
		}
		else {
			mEtEmailContent.setFocusable(true);
		}

		mEtEmailTitle.setOnFocusChangeListener((v, hasFocus) -> onEditTextFocusChange(hasFocus));

		findViewById(R.id.ivMailIcon).setOnClickListener(this);
	}

	private void onEditTextFocusChange(boolean hasFocus) {
		if (hasFocus) {
			mEmailRecipients.showPreview();
			if (mEmailCopyTo.isEmptyTag() && mEmailBlindTo.isEmptyTag()) {
				mCopyAndBlindView.setVisibility(View.GONE);
				mTvEmailSender.setText(getResources().getString(R.string.lbl_text_mail_receiver_lbl) + mUserName);
			}
			else {
				mEmailRecipients.showPreview();
				mEmailCopyTo.showPreview();
				mEmailBlindTo.showPreview();
				mTvEmailSender.setText(getResources().getString(R.string.lbl_text_mail_sender) + "：" + mUserName);
			}
		}
	}

	private void selectRecipients(int requestCode, List<AddressBook> addressBooks) {
		if (CommonUtil.nonEmptyList(addressBooks)) {
			DataKeeper.getInstance().keepDatas(requestCode, addressBooks);
		}
		new ContactsIntent(this)
				.title(CommonUtil.getString(R.string.lbl_message_title_mail_choose))
				.targetHashCode(requestCode)
				.requestCode(requestCode)
				.withSelect()
				.open();
	}

	private void sendMail() {
		EmailSendDoRequest request = buildRequest();
		if (request == null) {
			return;
		}

		if (mDetailResponse == null) {
			mPresenter.sendMail(request, OPERATOR_SEND);
		}
		else {
			request.sa01 = mDetailResponse.guid;
			request.operator = mPresenter.isDraft() ? OPERATOR_DRAFT : OPERATOR_SEND;
			mPresenter.sendMail(request);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CODE_SELECT_ATTACHMENT) {
			if (data != null) {
				ArrayList<String> localAttachments = data.getStringArrayListExtra("extra_local_file");
				mPresenter.addLocalAttachments(localAttachments);
				ArrayList<NetworkAttachment> networkAttachments = data.getParcelableArrayListExtra("extra_network_file");
				mPresenter.addNetworkAttachments(networkAttachments);
				int selectedAttachmentSize = mPresenter.getSelectedAttachmentSize();
				mTvAttachment.setText(selectedAttachmentSize == 0
						? getString(R.string.collaboration_attachment)
						: getString(R.string.collaboration_attachment) + "(" + selectedAttachmentSize + ")");
			}
		}
		else if (requestCode == K.email.receiver_request_code) {    // 选人
			List<AddressBook> addressBooks = (List<AddressBook>) DataKeeper.getInstance().getKeepDatas(K.email.receiver_request_code);
			mEmailRecipients.setRecipients(addressBooks, false);
		}
		else if (requestCode == K.email.copy_to_request_code) {       // 抄送
			List<AddressBook> addressBooks = (List<AddressBook>) DataKeeper.getInstance().getKeepDatas(K.email.copy_to_request_code);
			mEmailCopyTo.setRecipients(addressBooks, false);
		}
		else if (requestCode == K.email.blind_to_request_code) {      // 密送
			List<AddressBook> addressBooks = (List<AddressBook>) DataKeeper.getInstance().getKeepDatas(K.email.blind_to_request_code);
			mEmailBlindTo.setRecipients(addressBooks, false);
		}
		else if (requestCode == EDIT_RICH_CONTENT_CODE && resultCode == RESULT_OK) {
			if (RichTextContentKeeper.getInstance().hasContent()) {
				String richTextContent = tryTransformImagePath();
				mEtEmailContent.setText(Html.fromHtml(richTextContent, new UrlImageParser(mEtEmailContent,
						CoreZygote.getLoginUserServices().getServerAddress()), null));
			}
			else {
				mEtEmailContent.setText("");
			}
		}
	}

	@Override
	public void showLoading() {
		LoadingHint.show(this);
	}

	@Override
	public void setTitle(String title) {
		mToolBar.setTitle(title);
		mUserName = CoreZygote.getLoginUserServices().getUserName();
		if (mMailAccount != null && mMailAccount.contains("@")) {
			mUserName = mMailAccount;
		}
		mTvEmailSender.setText(getResources().getString(R.string.lbl_text_mail_receiver_lbl) + mUserName);
	}

	@Override
	public void onLoadReplyDataSuccess(EmailReplyResponse rsp) {
		mDetailResponse = rsp;
		mEtEmailTitle.setText(mDetailResponse.title);

		if (mPresenter.isDraft()) {
			// 草稿的情况下，将内容转为 文本. 过程中会失去很多样式.
			mEtEmailContent.setText(Html.fromHtml(mDetailResponse.content,
					new UrlImageParser(mEtEmailContent, CoreZygote.getLoginUserServices().getServerAddress()), null));
			RichTextContentKeeper.getInstance().setRichTextContent(tryAddHostToImageBeforeEdit(mDetailResponse.content));
		}
		else {
			// 其他情况下，原邮件显示就好。
			mWebView.setVisibility(View.VISIBLE);
			mEtEmailContent.setMinLines(3);
			WebSettings settings = mWebView.getSettings();
			settings.setJavaScriptEnabled(true);

			settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
			settings.setSupportZoom(true);
			settings.setBuiltInZoomControls(true); // 设置是否支持缩放
			settings.setLoadWithOverviewMode(true);
			settings.setDisplayZoomControls(false);
			settings.setDefaultTextEncodingName("utf-8");
			mWebView.setDrawingCacheEnabled(false);
			mWebView.setAnimationCacheEnabled(false);

			// To adapter for android 4.2 previous version.
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
				mWebView.loadDataWithBaseURL(CoreZygote.getLoginUserServices().getServerAddress(),
						MailDetailPresenter.getHtml(mPresenter.getBoxName(), mDetailResponse.content),
						"text/html; charset=utf-8", "utf-8", null);
			}
			else {
				mWebView.loadData(MailDetailPresenter.getHtml(mPresenter.getBoxName(), mDetailResponse.content),
						"text/html; charset=utf-8", "utf-8");
			}

			final GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
				@Override
				public boolean onSingleTapUp(MotionEvent e) {
					if (!hasEmailContentFocus) {
						FEToast.showMessage(getResources().getString(R.string.lbl_text_long_press_edit));
					}
					mEtEmailContent.requestFocus();
					DevicesUtil.showKeyboard(mEtEmailContent);
					return false;
				}

				@Override
				public void onLongPress(MotionEvent e) {
					DevicesUtil.tryCloseKeyboard(NewAndReplyMailActivity.this);
					askForEdit();
				}
			});

			mWebView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

			mWebView.setOnLongClickListener(v -> true);

		}

		if (!CommonUtil.isEmptyList(mDetailResponse.accessoryList)) {
			mPresenter.buildFileInfo(mDetailResponse.accessoryList);
			mTvAttachment.setText(getString(R.string.collaboration_attachment) + "(" + mPresenter.getSelectedAttachmentSize() + ")");
		}

		List<SelectedPerson> ttos = getIntent().getParcelableArrayListExtra(K.email.mail_tto_list);
		if (CommonUtil.isEmptyList(ttos) || !TextUtils.equals(mDetailResponse.su00, CoreZygote.getLoginUserServices().getUserId())) {
			mEmailRecipients.setRecipients(mPresenter.buildDefaultRecipients(mDetailResponse.su00, mDetailResponse.fromname, ttos));
		}
		else {
			mEmailRecipients.setRecipients(mPresenter.buildDefaultRecipients(null, null, ttos));
		}

		List<SelectedPerson> ccs = getIntent().getParcelableArrayListExtra(K.email.mail_cc_list);
		if (CommonUtil.isEmptyList(ccs) || !TextUtils.equals(mDetailResponse.su00cc, CoreZygote.getLoginUserServices().getUserId())) {
			mEmailCopyTo.setRecipients(mPresenter.buildDefaultRecipients(mDetailResponse.su00cc, mDetailResponse.cc, ccs));
		}
		else {
			mEmailCopyTo.setRecipients(mPresenter.buildDefaultRecipients(null, null, ccs));
		}

		mEmailBlindTo.setRecipients(mPresenter.buildDefaultRecipients(mDetailResponse.su00bcc, mDetailResponse.bcc, null));
		if (!mEmailBlindTo.isEmptyReceiver() || !mEmailCopyTo.isEmptyReceiver()) {
			mCopyAndBlindView.setVisibility(View.VISIBLE);
			mTvEmailSender.setText(getResources().getString(R.string.lbl_text_mail_sender) + "：" + mUserName);
		}
		else {
			mTvEmailSender.setText(getResources().getString(R.string.lbl_text_mail_receiver_lbl) + mUserName);
		}
	}

	@Override
	public void onLoadReplyDataFailed(RepositoryException repository) {
		FEToast.showMessage(getResources().getString(R.string.lbl_text_get_mail_detail_error));
		finish();
	}

	@Override
	public void onSendEmailSuccess(CommonResponse rsp) {
		String text = rsp.getErrorMessage();
		if (TextUtils.isEmpty(text)) {
			text = getResources().getString(R.string.message_operation_alert);
		}
		FEToast.showMessage(text);
		EventBus.getDefault().post(FLAG_FINISH_ACTIVITY);
		if (mPresenter.isDraft()) {
			EventBus.getDefault().post(FLAG_MAIL_DELETE);
		}
		else if (mPresenter.isSent()) {
			EventBus.getDefault().post(FLAG_SENT_REFRESH);
		}
		finish();
	}

	@Override
	public void onGetMailGUIDFail(String errorMessage) {
		FEToast.showMessage(errorMessage);
	}

	@Override
	public void onSendEmailFailed(RepositoryException exception, int stage) {
	}

	@Override
	public void onUploadAttachmentProgress(int progress) {
		LoadingHint.showProgress(progress);
	}

	@Override
	public void onUploadAttachmentFailed(RepositoryException repository) {
		new FEMaterialDialog.Builder(this)
				.setTitle(null)
				.setMessage(getResources().getString(R.string.lbl_message_attchment_upload_failed))
				.setPositiveButton(null, null)
				.build()
				.show();
	}

	@Override
	public void hideLoading() {
		LoadingHint.hide();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		DataKeeper.getInstance().removeKeepData(K.email.receiver_request_code);
		DataKeeper.getInstance().removeKeepData(K.email.copy_to_request_code);
		DataKeeper.getInstance().removeKeepData(K.email.blind_to_request_code);
		RichTextContentKeeper.getInstance().removeCache();
		RichTextContentKeeper.getInstance().removeCompressImagePath();
		mWebView.removeAllViews();
		mWebView.destroy();
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.tvMailSender) {
			if (mCopyAndBlindView.getVisibility() == View.VISIBLE) {
				return;
			}
			mEtEmailContent.clearFocus();
			mCopyAndBlindView.setVisibility(View.VISIBLE);
			mTvEmailSender.setText(getResources().getString(R.string.lbl_text_mail_sender) + "：" + mUserName);
		}
		else if (v == mEmailRecipients.getRightImageView() || v == mEmailRecipients.getEditText()) {
			selectRecipients(K.email.receiver_request_code, mEmailRecipients.getRecipients());
		}
		else if (v == mEmailCopyTo.getRightImageView() || v == mEmailCopyTo.getEditText()) {
			selectRecipients(K.email.copy_to_request_code, mEmailCopyTo.getRecipients());
		}
		else if (v == mEmailBlindTo.getRightImageView() || v == mEmailBlindTo.getEditText()) {
			selectRecipients(K.email.blind_to_request_code, mEmailBlindTo.getRecipients());
		}
		else if (v == mTvAttachment || v.getId() == R.id.ivMailIcon) {
			LuBan7.pufferGrenades(this, mPresenter.getSelectedLocalAttachments(),
					mPresenter.getSelectedNetworkAttachments(), CODE_SELECT_ATTACHMENT);
		}
	}

	@Override
	public void onBackPressed() {
		if (askForExist()) {
			return;
		}
		super.onBackPressed();
	}

	private boolean askForExist() {
		String title = mEtEmailTitle.getText().toString().trim();
		String content = mEtEmailContent.getText().toString().trim();

		if (!mEmailRecipients.isEmptyReceiver() && !TextUtils.isEmpty(title) && !TextUtils.isEmpty(content)) {
			EmailSendDoRequest request = buildRequest();
			if (request == null) {
				return true;
			}
			askForSaveDraft(request);
			return true;
		}

		if (!mEmailRecipients.isEmptyReceiver() || !TextUtils.isEmpty(title) || !TextUtils.isEmpty(content)) {
			showExitDialog();
			return true;
		}
		return false;
	}

	private EmailSendDoRequest buildRequest() {
		EmailSendDoRequest request = new EmailSendDoRequest();
		if (this.getCurrentFocus() != null && this.getCurrentFocus().getWindowToken() != null) {
			final InputMethodManager imm = ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE));
			imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
		final String recipientIds = mEmailRecipients.getRecipientIds();
		final String recipientEmails = mEmailRecipients.getEmailAddress();

		if (mEmailRecipients.hasErrorTag()) {
			FEToast.showMessage(getResources().getString(R.string.lbl_text_mail_address_error));
			return null;
		}

		if (mEmailRecipients.isEmptyReceiver()) {
			String message = getResources().getString(R.string.lbl_text_mail_receiver_empty);
			if (!TextUtils.isEmpty(mEmailRecipients.getEditText().getText().toString())) {
				FEToast.showMessage(getResources().getString(R.string.lbl_text_mail_recipient_not_exist));
				return null;
			}
			FEToast.showMessage(message);
			return null;
		}

		final String copyToIds = mEmailCopyTo.getRecipientIds();
		final String copyToEmails = mEmailCopyTo.getEmailAddress();
		if (mEmailCopyTo.hasErrorTag()) {
			FEToast.showMessage(getResources().getString(R.string.lbl_text_mail_copy_address_error));
			return null;
		}

		if (mEmailCopyTo.isEmptyReceiver()) {
			if (!TextUtils.isEmpty(mEmailCopyTo.getEditText().getText().toString())) {
				FEToast.showMessage(getResources().getString(R.string.lbl_text_mail_copy_not_exist));
				return null;
			}
		}

		final String blindToIds = mEmailBlindTo.getRecipientIds();
		final String blindToEmails = mEmailBlindTo.getEmailAddress();
		if (mEmailBlindTo.hasErrorTag()) {
			FEToast.showMessage(getResources().getString(R.string.lbl_text_mail_blind_address_error));
			return null;
		}

		if (mEmailBlindTo.isEmptyReceiver()) {
			if (!TextUtils.isEmpty(mEmailBlindTo.getEditText().getText().toString())) {
				FEToast.showMessage(getResources().getString(R.string.lbl_text_mail_blind_not_exist));
				return null;
			}
		}

		final String userId = CoreZygote.getLoginUserServices().getUserId();
		String title = mEtEmailTitle.getText().toString().trim();
		if (TextUtils.isEmpty(title.trim())) {
			FEToast.showMessage(getResources().getString(R.string.lbl_text_mail_theme_not_empty));
			return null;
		}

		request.mailname = userId;
		request.title = title;
		request.content = tryTransformImagePath();
		request.to = recipientIds;
		request.cc = copyToIds;
		request.bcc = blindToIds;

		if (mMailAccount != null && mMailAccount.contains("@")) {
			request.to1 = recipientEmails;
			request.cc = copyToEmails;
			request.bcc = blindToEmails;
			request.mailname = mMailAccount;
		}

		if (!mPresenter.isNewMail()) {
			request.mailid = mMailId;
			if (!mPresenter.isDraft() && mWebView.getVisibility() == View.VISIBLE) {
				if (TextUtils.isEmpty(request.content)) {
					request.content = mDetailResponse.content;
				}
				else {
					request.content += mDetailResponse.content;
				}
			}
		}

		return request;
	}

	private void askForSaveDraft(final EmailSendDoRequest request) {
		new FEMaterialDialog.Builder(this)
				.setTitle(null)
				.setMessage(getResources().getString(R.string.lbl_message_save_darft))
				.setPositiveButton(null, dialog -> mPresenter.sendMail(request, OPERATOR_SAVE))
				.setNegativeButton(null, dialog -> finish())
				.build()
				.show();
	}

	private void askForEdit() {
		new FEMaterialDialog.Builder(this)
				.setTitle(null)
				.setMessage(getResources().getString(R.string.lbl_message_will_lost_format))
				.setPositiveButton(null, new FEMaterialDialog.OnClickListener() {
					@Override
					public void onClick(AlertDialog dialog) {
						mWebView.setVisibility(View.GONE);
						mWebView.clearView();
						mWebView.destroy(); // 点击跳转到新界面

						RichTextContentKeeper.getInstance().appendRichTextContent(tryAddHostToImageBeforeEdit(mDetailResponse.content));
						Intent intent = new Intent(NewAndReplyMailActivity.this, RichTextEditActivity.class);
						intent.putExtra("title", getString(R.string.lbl_message_title_mail_detail));
						startActivityForResult(intent, EDIT_RICH_CONTENT_CODE);
					}
				})
				.setNegativeButton(null, null)
				.build()
				.show();
	}

	private String tryTransformImagePath() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
			return mEtEmailContent.getText().toString();
		}

		if (!RichTextContentKeeper.getInstance().hasContent()) {
			return null;
		}

		List<String> compressImagePaths = RichTextContentKeeper.getInstance().getCompressImagePaths();
		String richText = RichTextContentKeeper.getInstance().getRichTextContent();
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

	public void showExitDialog() {
		new FEMaterialDialog.Builder(this)
				.setTitle(null)
				.setMessage(getString(cn.flyrise.feep.core.R.string.exit_edit_tig))
				.setPositiveButton(null, dialog -> finish())
				.setNegativeButton(null, null)
				.build()
				.show();
	}
}
