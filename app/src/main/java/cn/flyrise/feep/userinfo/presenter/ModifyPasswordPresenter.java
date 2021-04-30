package cn.flyrise.feep.userinfo.presenter;

import android.content.Context;
import android.text.TextUtils;
import cn.flyrise.android.library.utility.LoadingHint;
import cn.flyrise.android.shared.bean.UserBean;
import cn.flyrise.android.shared.model.user.UserInfo;
import cn.flyrise.feep.FEApplication;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.GsonUtil;
import cn.flyrise.feep.core.common.utils.PreferencesUtils;
import cn.flyrise.feep.core.common.utils.SpUtil;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.dbmodul.utils.UserInfoTableUtils;
import cn.flyrise.feep.userinfo.contract.ModifyPasswordContract;
import cn.flyrise.feep.userinfo.modle.RemotePasswordResponse;
import cn.flyrise.feep.userinfo.modle.UserModifyPasswordRequest;
import cn.flyrise.feep.userinfo.views.ModifyPasswordActivity;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * cm on 2017-4-26.
 */

public class ModifyPasswordPresenter implements ModifyPasswordContract.presenter {

	private Context mContext;

	private ModifyPasswordContract.View mView;

	private String newPassword;

	public ModifyPasswordPresenter(Context context) {
		mContext = context;
		mView = (ModifyPasswordActivity) context;
	}

	@Override
	public void successModifyPassword(String password, String modifyText) {
		if (!regexPassword(modifyText)) {
			mView.inputError(mContext.getResources().getString(R.string.modify_password_error));
//			FEToast.showMessage(mContext.getResources().getString(R.string.modify_password_error));
			return;
		}
		newPassword = modifyText;
		String brfore = CommonUtil.toBase64Password(password);
		String data = CommonUtil.toBase64Password(modifyText);
		submitPassword(brfore, data);
	}

	private boolean regexPassword(String text) {
		Pattern pattern = Pattern.compile(REGEX_PASSWORD);
		Matcher matcher = pattern.matcher(text);
		return matcher.matches();
	}

	//修改密码
	private void submitPassword(String beforePassword, String newPassword) {
		LoadingHint.show(mContext);
		UserModifyPasswordRequest passwordBean = new UserModifyPasswordRequest();
		passwordBean.setPassword(beforePassword);
		passwordBean.setNewPassword(newPassword);
		passwordBean.setCount(PASSWORD_COUNT);
		passwordBean.setMethod(EDIT_PASSWORD_FOR_MOBILE);
		passwordBean.setObj(USER_LOGIC);
		Observable
				.create((Subscriber<? super String> f) -> {
					FEHttpClient.getInstance().post(passwordBean, new ResponseCallback<RemotePasswordResponse>(mContext) {
						@Override
						public void onCompleted(RemotePasswordResponse response) {
							if (response == null || !TextUtils.equals(response.getErrorCode(), SUCCESS_COUNT)) {
								f.onError(new NullPointerException("Request message list failed."));
								return;
							}
							f.onNext("");
						}

						@Override
						public void onFailure(RepositoryException repositoryException) {
							super.onFailure(repositoryException);
							f.onError(new NullPointerException("Request message failed."));
						}
					});
				})
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(results -> {
					LoadingHint.hide();
					modifyPasswordSuccess();
					FEToast.showMessage(mContext.getResources().getString(R.string.modify_info_success));
					mView.finishModify();
				}, exception -> {
					LoadingHint.hide();
					FEToast.showMessage(mContext.getResources().getString(R.string.modify_info_error));
				});
	}

	private void modifyPasswordSuccess() {//密码修改成功，更新缓存的密码
		notifierUserInfoPssword(newPassword);
		notifierGestruePssword(newPassword);
		notifierDataBase(newPassword);
	}

	private void notifierDataBase(String newPassword) {//更改数据库密码
		UserBean userBean = UserInfoTableUtils.find();
		if (userBean == null) {
			return;
		}
		userBean.setPassword(newPassword);
		UserInfoTableUtils.insert(userBean);
	}

	private void notifierUserInfoPssword(String newPassword) {//更改FEApplication缓存的密码
		if (TextUtils.isEmpty(newPassword)) {
			return;
		}
		FEApplication feApplication = (FEApplication) mContext.getApplicationContext();
		UserInfo userInfo = feApplication.getUserInfo();
		if (userInfo == null) {
			return;
		}
		userInfo.setPassword(newPassword);
		feApplication.setUserInfo(userInfo);
	}

	private void notifierGestruePssword(String newPassword) {//更改手势登录用到的密码
		String userGesturesInfo = SpUtil.get(PreferencesUtils.NINEPOINT_USER_INFO, "");
		if (TextUtils.isEmpty(userGesturesInfo)) return;
		UserInfo userInfo = GsonUtil.getInstance().fromJson(userGesturesInfo, UserInfo.class);
		if (userInfo == null) return;
		userInfo.setPassword(newPassword);
		SpUtil.put(PreferencesUtils.NINEPOINT_USER_INFO, GsonUtil.getInstance().toJson(userInfo));
	}

}
