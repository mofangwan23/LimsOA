package cn.flyrise.feep.auth;

import android.content.Context;

import cn.flyrise.android.shared.bean.UserBean;
import cn.flyrise.feep.auth.views.NetIPSettingActivity;

/**
 * @author ZYP
 * @since 2016-10-19 13:56
 */
public interface AuthContract {

	interface AuthPresenter {

		/**
		 * 初始化 AuthPresenter {@link UserBean}
		 */
		void initAuthPresenter(UserBean userBean);

		/**
		 * 当用户修改服务器配置或其他配置时，通知 AuthPresenter 刷新 {@link UserBean}
		 */
		void notifyUserInfoChange();

		/**
		 * 根据用户输入的账号密码开始登录
		 */
		void startLogin(String loginName, String password);

		/**
		 * 直接执行登录操作，用户记住了密码、或者设置了手势密码，会进行 vpn 判断
		 */
		void executeLogin();

		/**
		 * 直接发起网络请求
		 */
		void executeLoginRequest();

		/**
		 * 尝试显示用户被踢下线的 Dialog.
		 */
		void tryToShowUserKickDialog();

		void activieMokey(String safePwd);
	}

	interface AuthView {

		/**
		 * URL Code
		 */
		int URL_CODE_NETIP_SETTING_ACTIVITY = 0x11;

		/**
		 * URL Code
		 */
		int URL_CODE_LOGIN_ACTIVITY = 0x12;

		/**
		 * 登录成功
		 */
		void loginSuccess();

		/**
		 * 登录失败，提示失败信息
		 * @param errorMessage 提示信息，可直接用 Toast 显示。
		 */
		void loginError(String errorMessage);

		/**
		 * 强制更新
		 * */
		void toUpdate(String errorMessage);

		/**
		 * 正在加载
		 */
		void showLoading();

		/**
		 * 取消加载圈
		 */
		void hideLoading();

		/**
		 * UI 的跳转
		 * @param urlCode {@link #URL_CODE_LOGIN_ACTIVITY} 和 {@link #URL_CODE_NETIP_SETTING_ACTIVITY}
		 */
		void uiDispatcher(int urlCode);

		/**
		 * 返回当前 view 的 Context
		 * @return {@link Context}
		 */
		Context getContext();

		void initVpnSetting();

		void openMokeySafePwdActivity();

	}

}
