package cn.flyrise.feep.email;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import cn.flyrise.android.library.utility.LoadingHint;
import cn.flyrise.android.protocol.entity.EmailNumberRequest;
import cn.flyrise.android.protocol.entity.EmailNumberResponse;
import cn.flyrise.android.protocol.model.EmailNumber;
import cn.flyrise.feep.K;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.image.loader.FEImageLoader;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.core.services.ILoginUserServices;
import java.util.List;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * 邮箱首页
 * @author ZYP
 * @since 2016/7/11 10:21
 */
public class MailBoxHomeActivity extends BaseActivity {

	private List<String> mMailAccountList;
	private String mCurrentMailAccount;
	private boolean isFirstLoad = true;
	private ListView mListView;
	private TextView mTvMailBox;
	private TextView mTvUserName;
	private TextView mTvUnReadLabel;
	private MailBoxHomeAdapter mAdapter;

	@Override protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EventBus.getDefault().register(this);
		setContentView(R.layout.email_mailbox_home);
	}

	@Override protected void toolBar(FEToolbar toolbar) {
		toolbar.setTitle(getResources().getString(R.string.lbl_message_title_mail_home));
		toolbar.setRightText(getResources().getString(R.string.lbl_text_write_mail));
		toolbar.setRightTextClickListener(v -> {
			Intent intent = new Intent(MailBoxHomeActivity.this, NewAndReplyMailActivity.class);
			if (!TextUtils.equals(mCurrentMailAccount, CoreZygote.getLoginUserServices().getUserName())) {
				intent.putExtra(K.email.mail_account, mCurrentMailAccount);
			}
			startActivity(intent);
		});
	}

	@Override public void bindView() {
		mListView = findViewById(R.id.listView);
		mTvMailBox = findViewById(R.id.tvMailBox);
		mTvUnReadLabel = findViewById(R.id.tvUnReadLabel);
	}

	@Override public void bindData() {
		mTvUserName = findViewById(R.id.tvUserName);
		ImageView ivUserAvatar = findViewById(R.id.ivUserIcon);

		ILoginUserServices services = CoreZygote.getLoginUserServices();
		FEImageLoader.load(this, ivUserAvatar,
				services.getServerAddress() + services.getUserImageHref(),
				services.getUserId(), services.getUserName());
	}

	@Override protected void onResume() {
		super.onResume();
		requestMailBoxInfo();
	}

	@Override public void bindListener() {
		mListView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
			EmailNumber emailNumber = (EmailNumber) mAdapter.getItem(position);
			String boxName = emailNumber.getBoxName();
			if (TextUtils.equals(boxName, EmailNumber.INBOX_INNER)) {
				String defaultMailAccount = CoreZygote.getLoginUserServices().getUserName();
				if (!TextUtils.equals(defaultMailAccount, mCurrentMailAccount)) {
					boxName = EmailNumber.INBOX;
				}
				MailBoxActivity.startMailBoxActivity(MailBoxHomeActivity.this,
						emailNumber.getType(),
						boxName,
						mCurrentMailAccount);
				return;
			}
			MailBoxActivity.startMailBoxActivity(MailBoxHomeActivity.this, emailNumber.getType(), emailNumber.getBoxName(),
					mCurrentMailAccount);
		});

		mTvMailBox.setOnClickListener(v -> {//未读按钮
			String defaultMailAccount = CoreZygote.getLoginUserServices().getUserName();
			if (TextUtils.equals(defaultMailAccount, mCurrentMailAccount)) {
				MailBoxActivity.startMailBoxActivity(MailBoxHomeActivity.this, getResources().getString(R.string.lbl_text_mail_box),
						EmailNumber.INBOX_INNER, null, true);
			}
			else {
				MailBoxActivity.startMailBoxActivity(MailBoxHomeActivity.this, getResources().getString(R.string.lbl_text_mail_box),
						EmailNumber.INBOX, mCurrentMailAccount, true);
			}
		});

		mTvUserName.setOnClickListener(v -> {
			if (mMailAccountList != null && mMailAccountList.size() > 0) {
				MailSettingFragment fragment = new MailSettingFragment();
				fragment.setMailLists(mMailAccountList);
				fragment.show(getSupportFragmentManager(), "Account");
			}
		});
	}

	private void requestMailBoxInfo() {
		if (mCurrentMailAccount == null) {
			mCurrentMailAccount = CoreZygote.getLoginUserServices().getUserName();
		}
		EmailNumberRequest request = new EmailNumberRequest(mCurrentMailAccount);
		FEHttpClient.getInstance().post(request, new ResponseCallback<EmailNumberResponse>(this) {
			@Override public void onPreExecute() {
				if (isFirstLoad) {
					isFirstLoad = !isFirstLoad;
					LoadingHint.show(MailBoxHomeActivity.this);
				}
			}

			@Override public void onCompleted(EmailNumberResponse response) {
				LoadingHint.hide();
				mMailAccountList = response.mailList;
				if (mCurrentMailAccount == null) {
					mCurrentMailAccount = mMailAccountList.get(0);
				}
				setUnReadMailNumber(response.getEmailNumbers());
				mListView.setAdapter(mAdapter = new MailBoxHomeAdapter(response.getEmailNumbers()));
				mTvUnReadLabel.setText(getString(isInternalMail() ? R.string.mail_internal : R.string.mail_external));
			}

			@Override public void onFailure(RepositoryException responseException) {
				LoadingHint.hide();
			}
		});
	}

	private boolean isInternalMail() {
		return TextUtils.equals(CoreZygote.getLoginUserServices().getUserName(), mCurrentMailAccount);
	}

	private void setUnReadMailNumber(List<EmailNumber> emailNumbers) {
		int unRead = 0;
		for (EmailNumber emailNumber : emailNumbers) {
			if (TextUtils.equals(getResources().getString(R.string.lbl_text_mail_box), emailNumber.getType())) {
				unRead = emailNumber.read;
				break;
			}
		}

		mTvMailBox.setTextColor(Color.parseColor(unRead > 0 ? "#00a7e6" : "#CCCCCC"));
		mTvMailBox.setEnabled(unRead > 0);
		mTvMailBox.setText(unRead > 0 ? "(" + unRead + ")" : "0");
		mTvUserName.setText(getResources().getString(R.string.lbl_text_hello) + mCurrentMailAccount);
		mTvUserName.setClickable(false);
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMailAccountChange(MailSettingFragment.OnMailAccountChangeEvent event) {
		if (TextUtils.equals(mCurrentMailAccount, event.newAccount)) {
			return;
		}
		mTvUserName.setText("你好，" + event.newAccount);
		mCurrentMailAccount = event.newAccount;
		requestMailBoxInfo();
	}

	private class MailBoxHomeAdapter extends BaseAdapter {

		private List<EmailNumber> emailNumbers;

		MailBoxHomeAdapter(List<EmailNumber> emailNumbers) {
			this.emailNumbers = emailNumbers;
		}

		@Override public int getCount() {
			return this.emailNumbers != null ? emailNumbers.size() : 0;
		}

		@Override public Object getItem(int position) {
			return this.emailNumbers != null ? emailNumbers.get(position) : null;
		}

		@Override public long getItemId(int position) {
			return position;
		}

		@Override public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = View.inflate(parent.getContext(), R.layout.email_item_mailbox_home, null);
				holder = new ViewHolder(convertView);
				convertView.setTag(holder);
			}
			else {
				holder = (ViewHolder) convertView.getTag();
			}

			EmailNumber emailNumber = emailNumbers.get(position);
			holder.tvEmailBoxType.setText(emailNumber.getType());
			holder.ivEmailIcon.setImageResource(emailNumber.getIcon());
			if (TextUtils.equals(emailNumber.getBoxName(), EmailNumber.INBOX_INNER)) {
				holder.tvEmailNumber.setVisibility(View.VISIBLE);
				holder.tvEmailNumber.setText(String.valueOf(emailNumber.read + "/" + emailNumber.total));
			}
			else {
				if (emailNumber.total > 0) {
					holder.tvEmailNumber.setVisibility(View.VISIBLE);
					holder.tvEmailNumber.setText(String.valueOf(emailNumber.total));
				}
				else {
					holder.tvEmailNumber.setVisibility(View.GONE);
				}
			}
			return convertView;
		}

		private class ViewHolder {

			public TextView tvEmailBoxType;
			public TextView tvEmailNumber;
			private ImageView ivEmailIcon;

			public ViewHolder(View convertView) {
				tvEmailBoxType = convertView.findViewById(R.id.tvEmailBoxType);
				tvEmailNumber = convertView.findViewById(R.id.tvEmailNumber);
				ivEmailIcon = convertView.findViewById(R.id.ivMailIcon);
			}
		}
	}

	@Override protected void onDestroy() {
		super.onDestroy();
		EventBus.getDefault().unregister(this);
	}
}
