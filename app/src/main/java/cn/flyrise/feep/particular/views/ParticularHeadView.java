package cn.flyrise.feep.particular.views;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.flyrise.android.library.utility.SubTextUtility;
import cn.flyrise.android.library.view.BubbleWindow;
import cn.flyrise.android.protocol.model.AddressBookItem;
import cn.flyrise.android.protocol.model.User;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.DateUtil;
import cn.flyrise.feep.core.function.FunctionManager;
import cn.flyrise.feep.core.image.loader.FEImageLoader;
import cn.flyrise.feep.particular.presenter.ParticularPresenter;
import cn.flyrise.feep.utils.Patches;
import cn.flyrise.feep.workplan7.view.MainFeedDialog;
import java.util.List;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author ZYP
 * @since 2016-10-21 17:32
 * 新闻、公告、会议、审批、计划详情页的 Head IView.
 */
public class ParticularHeadView extends RelativeLayout {

	private String mSendUserId;
	private ImageView mIvAvatar;
	private TextView mTvTitle;
	private TextView mTvUserName;
	private TextView mTvSendTime;
	private TextView mTvNodeName;
	private TextView mTvSendUser;
	private AddressBookItem mSendUserInfo;
	private View mPopupView;
	private BubbleWindow mBubbleWindow;

	public ParticularHeadView(Context context) {
		this(context, null);
	}

	public ParticularHeadView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ParticularHeadView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		inflate(context, R.layout.view_particular_head, this);
		mIvAvatar = findViewById(R.id.ivHeadUserIcon);
		mTvTitle = findViewById(R.id.tvHeadTitle);
		mTvUserName = findViewById(R.id.tvHeadUser);
		mTvSendUser = findViewById(R.id.tvHeadSendUser);
		mTvSendTime = findViewById(R.id.tvHeadSendTime);
		mTvNodeName = findViewById(R.id.tvHeadNodeName);
	}

	/**
	 * 显示当前详情人员的基本信息，通常包含姓名、标题、时间等。
	 */
	public void displayUserInfo(ParticularPresenter.HeadVO headVO) {
		this.displayBaseInfo(headVO.sendUserId, headVO.sendUserName, headVO.title);
		if (FunctionManager.hasPatch(Patches.PATCH_FLOW_CURRENT_NODE)) {
			if (!TextUtils.isEmpty(headVO.nowNodeName)) {
				this.mTvNodeName.setVisibility(View.VISIBLE);
				this.mTvNodeName.setText(headVO.nowNodeName);
			}
		}

		if (!TextUtils.isEmpty(headVO.sendTime)) {
			this.mTvSendUser.setVisibility(View.GONE);
			this.mTvSendTime.setVisibility(View.VISIBLE);
			this.mTvSendTime.setText(DateUtil.formatTimeForDetail(headVO.sendTime));
			return;
		}

		this.mTvSendTime.setVisibility(View.GONE);
		this.mTvSendUser.setVisibility(View.VISIBLE);
		this.mTvSendUser.setOnClickListener(view -> {
			new MainFeedDialog.Builder()
					.mainFeed(stitchName(headVO.receiverUsers))
					.copyToFeed(stitchName(headVO.copyToUsers))
					.noticeFeed(stitchName(headVO.noticeToUsers))
					.startTime(headVO.startTime)
					.endTime(headVO.endTime)
					.build()
					.show(((AppCompatActivity) getContext()).getSupportFragmentManager(), "FE");
		});

	}

	/**
	 * 显示该详情的基本信息
	 * @param userId 详情发起人的 UserId
	 * @param userName 详情发起人的 UserName
	 * @param title 该详情的标题
	 */
	private void displayBaseInfo(String userId, String userName, String title) {
		this.mSendUserId = userId;
		if (!TextUtils.isEmpty(userName)) {
			mTvUserName.setText(userName);
		}
		mTvTitle.setText(SubTextUtility.isTextBook(title) ? SubTextUtility.subTextString(title) : title);
		CoreZygote.getAddressBookServices().queryUserDetail(userId)
				.subscribe(f -> {
					String host = CoreZygote.getLoginUserServices().getServerAddress();
					if (f != null) {
						mTvUserName.setText(f.name);
						String userAvatar = host + f.imageHref;
						FEImageLoader.load(getContext(), mIvAvatar, userAvatar, f.userId, f.name);
					}
					else {
						mIvAvatar.setImageResource(R.drawable.administrator_icon);
					}
				}, error -> {
					mIvAvatar.setImageResource(R.drawable.administrator_icon);
				});
		mIvAvatar.setOnClickListener(this::displaySendUserInformation);
	}


	/**
	 * 设置该详情的发起人联系信息
	 * @param addressBookItem 发起人信息
	 */
	public void configSendUserInformation(AddressBookItem addressBookItem) {
		this.mSendUserInfo = addressBookItem;
	}

	/**
	 * 使用 PopupWindow 的方式显示该详情的发起人信息
	 */
	private void displaySendUserInformation(View view) {
		if (mPopupView == null) {
			mPopupView = inflate(getContext(), R.layout.quick_contact, null);
			View userInfoView = mPopupView.findViewById(R.id.send_user_info_layout);
			View officePhoneView = mPopupView.findViewById(R.id.show_office_phone_lyt);
			View privatePhoneView = mPopupView.findViewById(R.id.show_private_phone_lyt);
			View emailView = mPopupView.findViewById(R.id.show_email_lyt);
			View progressView = mPopupView.findViewById(R.id.show_progress_lyt);

			TextView tvOfficePhone = (TextView) mPopupView.findViewById(R.id.office_phone_tv);
			TextView tvPrivatePhone = (TextView) mPopupView.findViewById(R.id.private_phone_tv);
			TextView tvEmail = (TextView) mPopupView.findViewById(R.id.mail_tv);
			TextView tvNoContact = (TextView) mPopupView.findViewById(R.id.show_no_contact_info_lyt);

			ImageView mBtnOfficePhone = (ImageView) mPopupView.findViewById(R.id.call_offic_phone_btn);
			ImageView mBtnPrivatePhone = (ImageView) mPopupView.findViewById(R.id.call_private_phone_btn);
			ImageView mBtnSms = (ImageView) mPopupView.findViewById(R.id.send_sms_btn);
			ImageView mBtnEmail = (ImageView) mPopupView.findViewById(R.id.send_mail_btn);

			if (mSendUserInfo == null || TextUtils.isEmpty(mSendUserInfo.getTel())
					&& TextUtils.isEmpty(mSendUserInfo.getPhone())
					&& TextUtils.isEmpty(mSendUserInfo.getEmail())) {
				tvNoContact.setVisibility(View.VISIBLE);
				userInfoView.setVisibility(View.GONE);
				progressView.setVisibility(View.GONE);
				officePhoneView.setVisibility(View.GONE);
				privatePhoneView.setVisibility(View.GONE);
				emailView.setVisibility(View.GONE);
				return;
			}
			userInfoView.setVisibility(View.VISIBLE);
			tvNoContact.setVisibility(View.GONE);
			progressView.setVisibility(View.GONE);

			if (TextUtils.isEmpty(mSendUserInfo.getTel())) {
				officePhoneView.setVisibility(View.GONE);
			}
			else {
				tvOfficePhone.setVisibility(View.VISIBLE);
				tvOfficePhone.setText(mSendUserInfo.getTel());
			}

			if (TextUtils.isEmpty(mSendUserInfo.getPhone())) {
				privatePhoneView.setVisibility(View.GONE);
			}
			else {
				tvPrivatePhone.setVisibility(View.VISIBLE);
				tvPrivatePhone.setText(mSendUserInfo.getPhone());
			}

			if (TextUtils.isEmpty(mSendUserInfo.getEmail())) {
				emailView.setVisibility(View.GONE);
			}
			else {
				tvEmail.setVisibility(View.VISIBLE);
				tvEmail.setText(mSendUserInfo.getEmail());
			}

			mBtnOfficePhone.setOnClickListener(v ->
					startIntent(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + tvOfficePhone.getText()))));
			mBtnPrivatePhone.setOnClickListener(v ->
					startIntent(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + tvPrivatePhone.getText()))));
			mBtnEmail.setOnClickListener(v ->
					startIntent(new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + tvEmail.getText()))));
			mBtnSms.setOnClickListener(v ->
					startIntent(new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + tvPrivatePhone.getText()))));
		}

		if (mBubbleWindow == null) {
			mBubbleWindow = new BubbleWindow(mPopupView);
		}
		mBubbleWindow.show(view);
	}

	private String stitchName(List<User> users) {
		if (users != null && users.size() != 0) {
			StringBuilder sb = new StringBuilder();
			final int size = users.size();
			for (int i = 0; i < size; i++) {
				User user = users.get(i);
				if (i == 0) {
					sb.append(user.getName());
				}
				else {
					sb.append(", ").append(user.getName());
				}
			}
			return sb.toString();
		}
		return null;
	}

	private void startIntent(Intent intent) {
		if (intent != null) {
			try {
				getContext().startActivity(intent);
			} catch (Exception exception) {
				FEToast.showMessage(getResources().getString(R.string.lbl_text_not_install_app));
			}
		}
	}
}
