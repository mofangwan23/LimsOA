package cn.flyrise.feep.email;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import cn.flyrise.android.protocol.model.MailAttachment;
import cn.flyrise.feep.K;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.GsonUtil;
import cn.flyrise.feep.core.dialog.FELoadingDialog;
import cn.flyrise.feep.media.attachments.NetworkAttachmentListFragment;
import cn.flyrise.feep.media.attachments.bean.NetworkAttachment;
import cn.flyrise.feep.media.common.FileCategoryTable;
import com.google.gson.reflect.TypeToken;
import java.util.List;
import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author ZYP
 * @since 2016/7/22 09:21
 */
public class MailAttachmentActivity extends BaseActivity {

	private String mMailId;
	private FELoadingDialog mLoadingDialog;

	@Override protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		mMailId = intent.getStringExtra(K.email.mail_id);
		setContentView(R.layout.email_attachment);
	}

	@Override protected void toolBar(FEToolbar toolbar) {
		toolbar.setTitle(getResources().getString(R.string.lbl_message_title_mail_attachment));
	}

	@Override public void bindView() {
		mLoadingDialog = new FELoadingDialog.Builder(this).setCancelable(false).create();
		Observable
				.create((OnSubscribe<List<MailAttachment>>) f -> {
					try {

						String attachmentJson = getIntent().getStringExtra(K.email.attachment_json);
						List<MailAttachment> mailAttachments =
								GsonUtil.getInstance().fromJson(attachmentJson, new TypeToken<List<MailAttachment>>() {}.getType());
						f.onNext(mailAttachments);
					} catch (Exception exp) {
						f.onError(exp);
					} finally {
						f.onCompleted();
					}
				})
				.flatMap(Observable::from)
				.map(this::convertFromAttachment)
				.toList()
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(attachments -> {
					hideLoading();
					Fragment fragment = NetworkAttachmentListFragment.newInstance(attachments, null);
					getSupportFragmentManager().beginTransaction()
							.add(R.id.layoutContent, fragment)
							.show(fragment)
							.commit();
				}, exception -> {
					hideLoading();
					FEToast.showMessage("无法查看附件，请稍后重试");
				});
	}

	private void hideLoading() {
		if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
			mLoadingDialog.hide();
			mLoadingDialog = null;
		}
	}

	private NetworkAttachment convertFromAttachment(MailAttachment mailAttachment) {
		NetworkAttachment networkAttachment = new NetworkAttachment();
		networkAttachment.setId(mMailId + "_" + mailAttachment.accId);
		networkAttachment.name = mailAttachment.fileName;
		networkAttachment.size = 0;
		networkAttachment.attachPK = mailAttachment.attachPK;
		networkAttachment.path = CoreZygote.getLoginUserServices().getServerAddress() +
				"/servlet/mobileAttachmentServlet?mailAttachment=1&attachPK=" + mailAttachment.attachPK;
		networkAttachment.type = FileCategoryTable.getType(mailAttachment.fileName);
		return networkAttachment;
	}
}
