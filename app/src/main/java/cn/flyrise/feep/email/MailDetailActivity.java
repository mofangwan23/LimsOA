package cn.flyrise.feep.email;

import static cn.flyrise.android.protocol.entity.BoxDetailRequest.OPERATOR_DELETE;
import static cn.flyrise.android.protocol.entity.BoxDetailRequest.OPERATOR_REMOVE;
import static cn.flyrise.android.protocol.entity.BoxDetailRequest.OPERATOR_RESTORE;
import static cn.flyrise.android.protocol.entity.EmailReplyRequest.B_DRAFT;
import static cn.flyrise.android.protocol.entity.EmailReplyRequest.B_REPLY;
import static cn.flyrise.android.protocol.entity.EmailReplyRequest.B_TRANSMIT;
import static cn.flyrise.android.protocol.model.EmailNumber.DRAFT;
import static cn.flyrise.android.protocol.model.EmailNumber.INBOX;
import static cn.flyrise.android.protocol.model.EmailNumber.INBOX_INNER;
import static cn.flyrise.android.protocol.model.EmailNumber.SENT;
import static cn.flyrise.android.protocol.model.EmailNumber.TRASH;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebSettings;
import android.widget.TextView;
import cn.flyrise.android.library.utility.LoadingHint;
import cn.flyrise.android.protocol.entity.CommonResponse;
import cn.flyrise.android.protocol.entity.EmailDetailsResponse;
import cn.flyrise.android.protocol.model.EmailNumber;
import cn.flyrise.android.protocol.model.MailAttachment;
import cn.flyrise.feep.K;
import cn.flyrise.feep.R;
import cn.flyrise.feep.commonality.bean.SelectedPerson;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.base.component.NotTranslucentBarActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.DateUtil;
import cn.flyrise.feep.core.common.utils.GsonUtil;
import cn.flyrise.feep.core.dialog.FEMaterialDialog;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.email.presenter.MailDetailPresenter;
import cn.flyrise.feep.email.presenter.MailDetailView;
import cn.flyrise.feep.email.presenter.NewReplyPresenter;
import cn.flyrise.feep.email.views.NiuBiaWebView;
import cn.squirtlez.frouter.annotations.RequestExtras;
import cn.squirtlez.frouter.annotations.Route;
import java.util.ArrayList;
import java.util.List;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * @author ZYP
 * @since 2016/7/14 13:33
 */
@Route("/mail/detail")
@RequestExtras({"extra_type", "extra_box_name", "extra_mail_id"})
public class MailDetailActivity extends NotTranslucentBarActivity implements View.OnClickListener, MailDetailView {

	public static final int ACTION_RESTORE_MAIL = 1;
	public static final int ACTION_PERMANENT_DELETE = 2;
	public static final int ACTION_DELETE_DRAFT = 3;
	public static final int ACTION_DELETE_MAIL = 4;

	private NiuBiaWebView mNiuBiaWebView;
	private TextView mTvMailTitle;
	private TextView mTvMailSender;
	private TextView mTvMailSendTime;
	private TextView mTvMailReceiver;
	private TextView mTvMoreAttachment;

	private TextView mTvFooterBtn1;
	private TextView mTvFooterBtn2;
	private TextView mTvFooterBtn3;

	private MailDetailPresenter mMailDetailPresenter;
	private EmailDetailsResponse mEmailDetailResponse;

	public static void startMailDetailActivity(Context context, String boxName, String mailId) {
		startMailDetailActivity(context, boxName, mailId, null);
	}

	public static void startMailDetailActivity(Context context, String boxName, String mailId, String mailAccount) {
		Intent intent = new Intent(context, MailDetailActivity.class);
		intent.putExtra(K.email.box_name, boxName);
		intent.putExtra(K.email.mail_id, mailId);
		intent.putExtra(K.email.mail_account, mailAccount);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EventBus.getDefault().register(this);
		setContentView(R.layout.email_mail_detail);
		Intent intent = getIntent();
		mMailDetailPresenter = new MailDetailPresenter(
				intent.getStringExtra(K.email.mail_id),
				intent.getStringExtra(K.email.box_name),
				intent.getStringExtra(K.email.mail_account),
				this);
		mMailDetailPresenter.start();
	}


	@Override
	protected void toolBar(FEToolbar toolbar) {
		toolbar.setTitle(getResources().getString(R.string.lbl_message_title_mail_detail));
	}

	@Override
	public void bindView() {
		mNiuBiaWebView = findViewById(R.id.niuBiaWebView);
		WebSettings settings = mNiuBiaWebView.getSettings();
		settings.setJavaScriptEnabled(true);// 可用JS

		settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
		settings.setSupportZoom(true);
		settings.setBuiltInZoomControls(true); // 设置是否支持缩放
		settings.setLoadWithOverviewMode(true);
		settings.setDisplayZoomControls(false);
		mNiuBiaWebView.setDrawingCacheEnabled(false);
		mNiuBiaWebView.setAnimationCacheEnabled(false);

		View headView = View.inflate(this, R.layout.email_detail_head_view, null);
		mNiuBiaWebView.setEmbeddedTitleBar(headView);

		mTvMailTitle = headView.findViewById(R.id.tvMailTitle);
		mTvMailSender = headView.findViewById(R.id.tvMailSender);
		mTvMailSendTime = headView.findViewById(R.id.tvMailSendTime);
		mTvMailReceiver = headView.findViewById(R.id.tvMailparticipant);
		mTvMoreAttachment = headView.findViewById(R.id.tvMoreAttachment);

		mTvFooterBtn1 = findViewById(R.id.tvMailBtn1);
		mTvFooterBtn2 = findViewById(R.id.tvMailBtn2);
		mTvFooterBtn3 = findViewById(R.id.tvMailBtn3);
	}

	@Override
	public void bindListener() {
		mTvFooterBtn1.setOnClickListener(this);
		mTvFooterBtn2.setOnClickListener(this);
		mTvFooterBtn3.setOnClickListener(this);
		mTvMoreAttachment.setOnClickListener(this);
		mTvMailReceiver.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.tvMailBtn1) {
			switch (mMailDetailPresenter.getBoxName()) {
				case INBOX_INNER:
				case INBOX:
					transmitMail();
					break;
				case SENT:
					sendAgain();
					break;
				case DRAFT:
					editDraft();
					break;
				case TRASH:
					askForConfirm(getResources().getString(R.string.lbl_text_confirm_restore_mail), ACTION_RESTORE_MAIL);
					break;
			}
		}
		else if (v.getId() == R.id.tvMailBtn2) {
			switch (mMailDetailPresenter.getBoxName()) {
				case INBOX_INNER:
				case INBOX:
					replyMail();
					break;
				case DRAFT:
					mMailDetailPresenter.sendDraft();
					break;
				case TRASH:
					askForConfirm(getResources().getString(R.string.lbl_text_confirm_delete_mail_per), ACTION_PERMANENT_DELETE);
					break;
			}
		}
		else if (v.getId() == R.id.tvMailBtn3) {
			switch (mMailDetailPresenter.getBoxName()) {
				case INBOX_INNER:
				case INBOX:
					askForConfirm(getResources().getString(R.string.lbl_text_confirm_delete_mail), ACTION_DELETE_MAIL);
					break;
				case SENT:
					askForConfirm(getResources().getString(R.string.lbl_text_confirm_delete_mail), ACTION_DELETE_MAIL);
					break;
				case DRAFT:
					askForConfirm(getResources().getString(R.string.lbl_text_confirm_delete_draft), ACTION_DELETE_DRAFT);
					break;
			}
		}
		else if (v.getId() == R.id.tvMoreAttachment) {
			Intent intent = new Intent(this, MailAttachmentActivity.class);
			String json = (String) v.getTag();
			intent.putExtra(K.email.attachment_json, json);
			intent.putExtra(K.email.mail_id, mMailDetailPresenter.getMailId());
			String boxName = TextUtils.equals(mMailDetailPresenter.getBoxName(), EmailNumber.INBOX) ?
					mMailDetailPresenter.getBoxName() + "/" + mMailDetailPresenter.getMailAccount() :
					mMailDetailPresenter.getBoxName();

			intent.putExtra(K.email.box_name, boxName);
			startActivity(intent);
		}
		else if (v.getId() == R.id.tvMailparticipant) {
			MailParticipantsActivity.startMailParticipantsActivity(this,
					mMailDetailPresenter.getMailAccount(),
					mEmailDetailResponse.mailFrom, mEmailDetailResponse.sendUserId,
					mEmailDetailResponse.tto, mEmailDetailResponse.ttoUserId,
					mEmailDetailResponse.cc, mEmailDetailResponse.ccUserId,
					mMailDetailPresenter.isBoxNameWithMail());
		}
	}

	@Override
	public int getPaddingTop() {
		return 0;
	}

	@Override
	public void onLoadDetailMailSuccess(EmailDetailsResponse rsp) {
		mEmailDetailResponse = rsp;

		if (mEmailDetailResponse.isEmailEmpty()) {
			new FEMaterialDialog.Builder(this)
					.setTitle(null)
					.setMessage(getResources().getString(R.string.lbl_message_mail_has_delete))
					.setDismissListener(dialog -> finish())
					.setPositiveButton(null, dialog -> {
					})
					.build()
					.show();
			return;
		}

		mTvMailTitle.setText(mEmailDetailResponse.title);
		mTvMailSender.setText(mEmailDetailResponse.mailFrom);
		mTvMailSendTime.setText(DateUtil.formatTimeForDetail(mEmailDetailResponse.sendTime));

		List<MailAttachment> mailAttachments = mEmailDetailResponse.mailAttachments;
		if (mailAttachments != null && mailAttachments.size() > 0) {
			mTvMoreAttachment.setVisibility(View.VISIBLE);
			String json = GsonUtil.getInstance().toJson(mailAttachments);
			mTvMoreAttachment.setTag(json);
		}

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
					mNiuBiaWebView.loadDataWithBaseURL(CoreZygote.getLoginUserServices().getServerAddress(),
							MailDetailPresenter.getHtml(mMailDetailPresenter.getBoxName(), mEmailDetailResponse.context),
							"text/html; charset=utf-8", "UTF-8", null);
				}
				else {
					mNiuBiaWebView.loadData(MailDetailPresenter.getHtml(mMailDetailPresenter.getBoxName(), mEmailDetailResponse.context),
							"text/html; charset=utf-8", "UTF-8");
				}

			}
		}, 200);
	}

	@Override
	public void onLoadDetailMailFailed(RepositoryException repositoryException) {
		finish();
	}

	@Override
	public void onSendDraftSuccess(CommonResponse rsp) {
		FEToast.showMessage(getResources().getString(R.string.lbl_text_mail_send_success));
		EventBus.getDefault().post(NewAndReplyMailActivity.FLAG_MAIL_DELETE);
		finish();
	}

	@Override
	public void onSendDraftFailed(RepositoryException repositoryException) {
	}

	@Override
	public void checkDraftFailed(String errorMessage) {
		FEToast.showMessage(errorMessage);
	}

	@Override
	public void displayInboxFooter() {
		mTvFooterBtn1.setText(getResources().getString(R.string.lbl_text_zhuanfa));
		mTvFooterBtn2.setText(getResources().getString(R.string.reply));
		mTvFooterBtn3.setText(getResources().getString(R.string.delete_group_chat));
	}

	@Override
	public void displayDraftFooter() {
		mTvFooterBtn1.setText(getResources().getString(R.string.lbl_text_edit));
		mTvFooterBtn2.setText(getResources().getString(R.string.submit));
		mTvFooterBtn3.setText(getResources().getString(R.string.delete_group_chat));
	}

	@Override
	public void displaySentFooter() {
		mTvFooterBtn1.setText(getResources().getString(R.string.lbl_message_title_send_again));
		mTvFooterBtn2.setVisibility(View.GONE);
		mTvFooterBtn3.setText(getResources().getString(R.string.delete_group_chat));
	}

	@Override
	public void displayTrashFooter() {
		mTvFooterBtn1.setText(getResources().getString(R.string.lbl_text_resore_mail));
		mTvFooterBtn2.setText(getResources().getString(R.string.lbl_text_delete_pre));
		mTvFooterBtn3.setVisibility(View.GONE);
	}

	@Override
	public void onDealWithActionSuccess(int action, String content) {
		FEToast.showMessage(content);
		EventBus.getDefault().post(NewAndReplyMailActivity.FLAG_MAIL_DELETE);
		finish();
	}

	@Override
	public void onDealWithActionFailed(int action, RepositoryException repositoryException) {
	}

	public void transmitMail() {
		NewAndReplyMailActivity.startNewReplyActivity(this,
				mMailDetailPresenter.getBoxName(),
				mMailDetailPresenter.getMailId(), B_TRANSMIT,
				mMailDetailPresenter.getMailAccount());
	}

	public void replyMail() {
		String[] tto = mEmailDetailResponse.tto.split(",");
		if (tto.length > 1) {
			final String[] items = {getResources().getString(R.string.lbl_text_reply_all), getResources().getString(R.string.reply)};
			new AlertDialog
					.Builder(this)
					.setCancelable(true)
					.setItems(items, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (which == 0) {
								replyAllMail();
							}
							else {
								replySingleMail();
							}
							dialog.dismiss();
						}
					})
					.create()
					.show();
		}
		else {
			replySingleMail();
		}
	}

	public void replyAllMail() {
		Intent intent = new Intent(this, NewAndReplyMailActivity.class);
		intent.putExtra(K.email.box_name, mMailDetailPresenter.getBoxName());
		intent.putExtra(K.email.mail_id, mMailDetailPresenter.getMailId());
		intent.putExtra(K.email.b_transmit, B_REPLY);
		intent.putExtra(K.email.mail_account, mMailDetailPresenter.getMailAccount());

		String loginUserId = CoreZygote.getLoginUserServices().getUserId();
		SelectedPerson loginUsertt = null;
		ArrayList<SelectedPerson> tto = NewReplyPresenter.buildRecipientLists(mEmailDetailResponse.ttoUserId, mEmailDetailResponse.tto);
		if (!CommonUtil.isEmptyList(tto)) {
			for (SelectedPerson p : tto) {
				if (TextUtils.equals(p.userId, loginUserId)) {
					loginUsertt = p;
					break;
				}
			}
		}
		if (loginUsertt != null) tto.remove(loginUsertt);
		intent.putParcelableArrayListExtra(K.email.mail_tto_list, tto);
		SelectedPerson loginUsercc = null;
		ArrayList<SelectedPerson> cc = NewReplyPresenter.buildRecipientLists(mEmailDetailResponse.ccUserId, mEmailDetailResponse.cc);
		if (!CommonUtil.isEmptyList(cc)) {
			for (SelectedPerson p : cc) {
				if (TextUtils.equals(p.userId, loginUserId)) {
					loginUsercc = p;
					break;
				}
			}
		}
		if (loginUsercc != null) cc.remove(loginUsercc);
		intent.putParcelableArrayListExtra(K.email.mail_cc_list, cc);
		startActivity(intent);
	}


	public void replySingleMail() {
		Intent intent = new Intent(this, NewAndReplyMailActivity.class);
		intent.putExtra(K.email.box_name, mMailDetailPresenter.getBoxName());
		intent.putExtra(K.email.mail_id, mMailDetailPresenter.getMailId());
		intent.putExtra(K.email.b_transmit, B_REPLY);
		intent.putExtra(K.email.mail_account, mMailDetailPresenter.getMailAccount());
		ArrayList<SelectedPerson> selectedPersons = new ArrayList<>();
		SelectedPerson recipient = new SelectedPerson();
		recipient.userId =  mEmailDetailResponse.sendUserId;
		recipient.userName =  mEmailDetailResponse.mailFrom;
		selectedPersons.add(recipient);
		intent.putParcelableArrayListExtra(K.email.mail_tto_list, selectedPersons);
		startActivity(intent);
	}

	public void editDraft() {
		NewAndReplyMailActivity.startNewReplyActivity(this,
				mMailDetailPresenter.getBoxName(),
				mMailDetailPresenter.getMailId(), B_DRAFT,
				mMailDetailPresenter.getMailAccount());
	}

	public void sendAgain() {
		NewAndReplyMailActivity.startNewReplyActivity(this,
				mMailDetailPresenter.getBoxName(),
				mMailDetailPresenter.getMailId(), B_DRAFT,
				mMailDetailPresenter.getMailAccount());
		finish();
	}

	private void askForConfirm(String content, final int action) {
		new FEMaterialDialog.Builder(this)
				.setTitle(null)
				.setMessage(content)
				.setPositiveButton(null, new FEMaterialDialog.OnClickListener() {
					@Override
					public void onClick(AlertDialog dialog) {
						dealWith(action);
					}
				})
				.setNegativeButton(null, null)
				.build()
				.show();
	}

	private void dealWith(final int action) {
		if (action == ACTION_RESTORE_MAIL) {
			mMailDetailPresenter.dealWithAction(action, OPERATOR_RESTORE);
		}
		else if (action == ACTION_PERMANENT_DELETE) {
			mMailDetailPresenter.dealWithAction(action, OPERATOR_DELETE);
		}
		else if (action == ACTION_DELETE_DRAFT) {
			mMailDetailPresenter.dealWithAction(action, OPERATOR_REMOVE);
		}
		else if (action == ACTION_DELETE_MAIL) {
			mMailDetailPresenter.dealWithAction(action, OPERATOR_REMOVE);
		}
	}

	@Override
	public void showLoading() {
		LoadingHint.show(this);
	}

	@Override
	public void hideLoading() {
		LoadingHint.hide();
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onFinishEvent(Integer closeFlag) {
		if (closeFlag == NewAndReplyMailActivity.FLAG_FINISH_ACTIVITY) {
			finish();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		EventBus.getDefault().unregister(this);
	}
}
