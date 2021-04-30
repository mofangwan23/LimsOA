package com.hyphenate.chatui.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.Keep;
import android.text.TextUtils;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.PreferencesUtils;
import cn.flyrise.feep.core.common.utils.SpUtil;
import cn.flyrise.feep.core.dialog.FEMaterialDialog.Builder;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.push.Push.Phone;
import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chatui.R;
import com.hyphenate.chatui.domain.OnEventConversationLoad;
import com.hyphenate.chatui.group.GroupListActivity;
import com.hyphenate.chatui.protocol.EaseMobileConfigRequest;
import com.hyphenate.chatui.protocol.EaseMobileConfigResponse;
import com.hyphenate.chatui.ui.ChatActivity;
import com.hyphenate.easeui.EaseUiK;
import com.hyphenate.push.EMPushHelper;
import com.hyphenate.push.EMPushType;
import org.greenrobot.eventbus.EventBus;
import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 环信IM
 */
@Keep
public class IMHuanXinHelper {

	private static boolean isReStartIm = false;
	private static boolean isInitImSuccess = false;//防止未初始化调用IM导致崩溃

	private static class SingletonHolder {

		private static final IMHuanXinHelper INSTANCE = new IMHuanXinHelper();
	}

	public static IMHuanXinHelper getInstance() {
		return SingletonHolder.INSTANCE;
	}


	//连接上IM
	public void login() {
		if (CoreZygote.getLoginUserServices() == null) return;
		String userId = CoreZygote.getLoginUserServices().getUserId();
		if (TextUtils.isEmpty(userId)) return;
		Observable
				.unsafeCreate((OnSubscribe<? super String>) f ->
						FEHttpClient.getInstance().post(new EaseMobileConfigRequest(), new ResponseCallback<EaseMobileConfigResponse>() {
							@Override public void onCompleted(EaseMobileConfigResponse response) {
								if (response == null || !TextUtils.equals(response.getErrorCode(), "0")) {
									f.onError(new RuntimeException("Query EaseMobileConfigFailureException."));
									return;
								}

								try {
									CoreZygote.getLoginUserServices().setCompanyGUID(response.result.companyGUID);
									String username = getImUserId(CoreZygote.getLoginUserServices().getUserId());

									EmHelper.getInstance().init(CoreZygote.getContext(), response.result.appKey, username);
									isInitImSuccess = true;
									FELog.i("-->>>>push-huanxin-userName1:" + username);
									FELog.i("-->>>>push-huanxin-isImLogin:" + isImLogin());
									if (isImLogin()) {
										loginSuccess();
										f.onCompleted();
									}
									else {
										String appKey = EMClient.getInstance().getOptions().getAppKey();
										if (!TextUtils.isEmpty(appKey) && !response.result.appKey.equals(appKey)) {
											EMClient.getInstance().changeAppkey(response.result.appKey);
										}
										f.onNext(userId);
									}
								} catch (Exception exp) {
									f.onError(new NullPointerException("im huan xin login error 1"));
								}
							}

							@Override public void onFailure(RepositoryException e) {
								super.onFailure(e);
								f.onError(new NullPointerException("im huan xin login error 2"));
							}
						}))
				.retry(2)
				.flatMap(oaUserId -> Observable.unsafeCreate((OnSubscribe<? super Integer>) f -> {
					String username = getImUserId((String) oaUserId);
					String password = CommonUtil.getMD5("MUC" + oaUserId);
					FELog.i("-->>>>push-huanxin-userName2:" + username);
					EMClient.getInstance().login(username, password, new EMCallBack() {
						@Override public void onSuccess() {
							if (CoreZygote.getLoginUserServices() != null) {
								EMClient.getInstance().pushManager().updatePushNickname(CoreZygote.getLoginUserServices().getUserName());
							}
							f.onNext(200);
						}

						@Override public void onError(int i, String s) {
							FELog.i("-->>>>push-huanxin-login-error:" + i + "--text:" + s);
							if (i == 200) {
								EMClient.getInstance().logout(false);
							}
							f.onError(new RuntimeException("EMClient login error code = " + i));
						}

						@Override public void onProgress(int i, String s) { }
					});
				}))
				.retry(2)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(resultCode -> {
					isReStartIm = false;
					SpUtil.put(PreferencesUtils.IM_SUCCESS_USER_ID, CoreZygote.getLoginUserServices().getUserId());
					FELog.i("-->>>>push-huanxin-login-success:" + resultCode);
					loginSuccess();
				}, exception -> {
					isReStartIm = true;
					SpUtil.put(PreferencesUtils.IM_SUCCESS_USER_ID, "");
				});
	}

	private void loginSuccess() {
		EMClient.getInstance().groupManager().loadAllGroups();
		EMClient.getInstance().chatManager().loadAllConversations();
		EventBus.getDefault().post(new OnEventConversationLoad());
		if (CoreZygote.getLoginUserServices() != null) {
			CoreZygote.getLoginUserServices().setImLoginStatus();
		}
		setPushToken();
	}

	//设置小米华为离线推送的Token值
	public void setPushToken() {
		if (!isImLogin()) return;
		if (TextUtils.equals(Build.MANUFACTURER, Phone.huawei)) { //华为
			EMClient.getInstance().sendHMSPushTokenToServer(FeepPushManager.getPushToken());
		}
		else if (TextUtils.equals(Build.MANUFACTURER, Phone.xiaomi)) {//小米
			EMPushHelper.getInstance().onReceiveToken(EMPushType.MIPUSH, FeepPushManager.getPushToken());
		}
	}

	//发起单聊界面
	public void startChatActivity(Context context, String userId) {
		if (isImLogin()) {
			Intent intent = new Intent(context, ChatActivity.class);
			intent.putExtra(EaseUiK.EmChatContent.emChatID, getImUserId(userId));
			context.startActivity(intent);
			return;
		}
		tryIMLogin();
	}

	public void forwardMsg2User(Context context, String userID, String name, String msgID) {
		new Builder(context).setNegativeButton(null, null)
				.setMessage(context.getString(R.string.whether_forward_to) + name)
				.setNegativeButton(null, null)
				.setPositiveButton(null, dialog -> {
					Intent intent = new Intent(context, ChatActivity.class);
					intent.putExtra(EaseUiK.EmChatContent.emChatID, getImUserId(userID));
					intent.putExtra("forward_msg_id", msgID);
					context.startActivity(intent);
				})
				.build()
				.show();
	}


	public void startGroupListActivity(Context context) {
		if (isImLogin()) {
			context.startActivity(new Intent(context, GroupListActivity.class));
			return;
		}
		tryIMLogin();
	}

	public String getImUserId(String userId) {
		if (CoreZygote.getLoginUserServices() == null) {
			return userId;
		}

		String companyId = CoreZygote.getLoginUserServices().getCompanyGUID();
		if (companyId.length() > 32 && companyId.contains("-")) {
			return companyId + "_" + userId;
		}
		return companyId + userId;
	}

	//app为登录状态，默认开启自动连接
	public boolean isImLogin() {
		return isInitImSuccess && EMClient.getInstance().isLoggedInBefore() && !isSwitchUser();
	}

	//是否切换用户,切换用户返回true
	public boolean isSwitchUser() {
		return CoreZygote.getLoginUserServices() == null
				|| !TextUtils.equals(CoreZygote.getLoginUserServices().getUserId()
				, SpUtil.get(PreferencesUtils.IM_SUCCESS_USER_ID, ""));
	}

	private void tryIMLogin() {
		FEToast.showMessage(CommonUtil.getString(R.string.Initializing_im));
		if (!isImLogin() && isReStartIm) {
			isReStartIm = false;
			login();
		}
	}

	//获取消息记录的条数
	public int getUnreadCount() {
		if (isImLogin()) {
			return EMClient.getInstance().chatManager().getUnreadMessageCount();
		}
		return 0;
	}

	//注销IM:注销传true解绑token,被提线传false
	public void logout(boolean isDeviceToken) {
		if (isImLogin()) {
			EmHelper.getInstance().logout(isDeviceToken, null);
		}
	}
}
