package cn.flyrise.feep.auth.views;

import android.content.Intent;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;
import cn.flyrise.android.shared.bean.UserBean;
import cn.flyrise.android.shared.model.user.UserInfo;
import cn.flyrise.feep.FEMainActivity;
import cn.flyrise.feep.R;
import cn.flyrise.feep.auth.VpnAuthPresenter;
import cn.flyrise.feep.auth.unknown.NewLoginActivity;
import cn.flyrise.feep.core.common.utils.GsonUtil;
import cn.flyrise.feep.core.common.utils.PreferencesUtils;
import cn.flyrise.feep.core.common.utils.SpUtil;
import cn.flyrise.feep.core.dialog.FEMaterialDialog;
import cn.flyrise.feep.core.image.loader.FEImageLoader;
import cn.flyrise.feep.dbmodul.utils.UserInfoTableUtils;
import cn.flyrise.feep.utils.FEUpdateVersionUtils;

/**
 * @author ZYP
 * @since 2017-05-05 09:59
 * 指纹解锁登录、手势解锁登录的父类
 */
public class BaseThreeLoginActivity extends BaseAuthActivity {

	public static final String EXTRA_CLEAR_SECURITY_SETTING = "extra_clear_security_setting";

	protected ImageView mIvUserIcon;
	protected TextView mTvErrorPrompt;
	protected TextView mTvRetryPrompt;
	protected UserBean mUserBean;

	@Override public void bindView() {
		mIvUserIcon = (ImageView) findViewById(R.id.ivUserIcon);
		mTvRetryPrompt = (TextView) findViewById(R.id.tvGestureLabel);
		mTvErrorPrompt = (TextView) findViewById(R.id.tvGesturePrompt);
	}

	@Override public void bindData() {
		mAuthPresenter = new VpnAuthPresenter(this);
		mUserBean = UserInfoTableUtils.find();
		String userGesturesInfo = SpUtil.get(PreferencesUtils.NINEPOINT_USER_INFO, "");
		UserInfo userInfo = GsonUtil.getInstance().fromJson(userGesturesInfo, UserInfo.class);

		tryFixUserBeanInfo(mUserBean, userInfo);

		String userId = mUserBean.getUserID();
		String userName = mUserBean.getUserName();
		String userImageHref = userInfo.getUrl() + userInfo.getAvatarUrl();
		FEImageLoader.load(this, mIvUserIcon, userImageHref, userId, userName);
		mAuthPresenter.initAuthPresenter(mUserBean);
		mAuthPresenter.tryToShowUserKickDialog();
	}

	@Override public void bindListener() {
		findViewById(R.id.tvForgetPassword).setOnClickListener(v -> startPasswordVerification());
	}

	protected void startPasswordVerification() {
		Intent intent = new Intent(BaseThreeLoginActivity.this, NewLoginActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.putExtra(EXTRA_FORGET_PASSWORD, true);
		intent.putExtra(EXTRA_CLEAR_SECURITY_SETTING, false);
		startActivity(intent);
		sendBroadcast(new Intent(FEMainActivity.CANCELLATION_LOCK_RECEIVER));
	}

	@Override public void loginSuccess() {
		Intent intent = new Intent(this, FEMainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(intent);
		finish();
	}

	@Override public void toUpdate(String errorMessage) {
		new FEMaterialDialog.Builder(this)
				.setMessage(errorMessage)
				.setCancelable(false)
				.setPositiveButton("去更新", dialog ->
						new FEUpdateVersionUtils(this, () -> {

						}).showUpdateVersionDialog()
				).build()
				.show();
	}

	protected void tryFixUserBeanInfo(UserBean userBean, UserInfo userInfo) {
		if (TextUtils.isEmpty(userBean.getUserID())) {
			userBean.setUserID(userInfo.getUserID());
		}

		if (TextUtils.isEmpty(userBean.getUserName())) {
			userBean.setUserName(userInfo.getUserName());
		}

		if (TextUtils.isEmpty(userBean.getLoginName())) {
			userBean.setLoginName(userInfo.getLoginName());
		}

		if (TextUtils.isEmpty(userBean.getServerAddress())) {
			userBean.setServerAddress(userInfo.getServerAddress());
		}

		if (TextUtils.isEmpty(userBean.getServerPort())) {
			userBean.setServerPort(userInfo.getServerPort());
		}

		if (TextUtils.isEmpty(userBean.getPassword())) {
			userBean.setPassword(userInfo.getPassword());
		}

		userBean.setSavePassword(userInfo.isSavePassword());
		userBean.setAutoLogin(userInfo.isAutoLogin());
		userBean.setHttps(userInfo.isHttps());
		userBean.setVPN(userInfo.isVpn());
	}

}
