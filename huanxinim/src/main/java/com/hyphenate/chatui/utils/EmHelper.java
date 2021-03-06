package com.hyphenate.chatui.utils;

import android.content.Context;
import android.content.IntentFilter;
import android.text.TextUtils;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.GsonUtil;
import cn.flyrise.feep.core.common.utils.PreferencesUtils;
import cn.flyrise.feep.core.common.utils.SpUtil;
import cn.flyrise.feep.core.services.model.UserKickPrompt;
import com.hyphenate.EMCallBack;
import com.hyphenate.EMConnectionListener;
import com.hyphenate.EMError;
import com.hyphenate.EMGroupChangeListener;
import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMCallOptions;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMCmdMessageBody;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMMessage.ChatType;
import com.hyphenate.chat.EMOptions;
import com.hyphenate.chatui.R;
import com.hyphenate.chatui.db.DbOpenHelper;
import com.hyphenate.chatui.db.MessageSettingManager;
import com.hyphenate.chatui.domain.EmojiconExampleGroupData;
import com.hyphenate.chatui.domain.MessageSetting;
import com.hyphenate.chatui.receiver.CallReceiver;
import com.hyphenate.easeui.EaseUiK.EmChatContent;
import com.hyphenate.easeui.busevent.EMChatEvent.BaseGroupEvent;
import com.hyphenate.easeui.busevent.EMChatEvent.GroupDestroyed;
import com.hyphenate.easeui.busevent.EMChatEvent.UserRemove;
import com.hyphenate.easeui.busevent.EMMessageEvent;
import com.hyphenate.easeui.busevent.EMMessageEvent.ImMessageRefresh;
import com.hyphenate.easeui.controller.EaseUI;
import com.hyphenate.easeui.domain.EaseEmojicon;
import com.hyphenate.easeui.domain.EaseEmojiconGroupEntity;
import com.hyphenate.easeui.listener.SimpleGroupChangeListener;
import com.hyphenate.easeui.listener.SimpleMessageListener;
import com.hyphenate.easeui.model.EaseAtMessageHelper;
import com.hyphenate.easeui.model.EaseNotifier;
import com.hyphenate.easeui.utils.EaseCommonUtils;
import com.hyphenate.easeui.utils.MMPMessageUtil;
import com.hyphenate.exceptions.HyphenateException;
import com.hyphenate.push.EMPushConfig;
import com.hyphenate.push.EMPushHelper;
import com.hyphenate.push.EMPushType;
import com.hyphenate.push.PushListener;
import com.hyphenate.util.EMLog;
import com.hyphenate.util.EasyUtils;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.greenrobot.eventbus.EventBus;


public class EmHelper {

	protected static final String TAG = "EmHelper";
	private static EmHelper sInstance;

	private EaseUI mEaseUI;
	public boolean isVoiceCalling;
	public boolean isVideoCalling;
	private String loginUserId;
	private String mToChatUserId;

	private Context mContext;
	private CallReceiver mCallReceiver;
	private EMConnectionListener mConnectionListener;
	private EMGroupChangeListener mGroupChangeListener;
	private EMMessageListener mMessageListener;
	private MessageSettingManager mMessageSettingManager;
	private Map<String, Integer> mNotifyMessageMap = new HashMap<>();

	public synchronized static EmHelper getInstance() {
		if (sInstance == null) {
			sInstance = new EmHelper();
		}
		return sInstance;
	}


	public void setToChatUserID(String toChatUserID) {
		mToChatUserId = toChatUserID;
		if (mNotifyMessageMap.containsKey(mToChatUserId)) {
			mNotifyMessageMap.put(mToChatUserId, 0);
		}
	}

	/**
	 * init helper
	 * @param context application context
	 */
	public void init(Context context, String appKey, String userID) {

		this.mContext = context;
		this.loginUserId = userID;

		DbOpenHelper.init(context, userID);
		this.mMessageSettingManager = new MessageSettingManager();
		EMOptions options = initChatOptions(context, appKey);

		EaseUI.getInstance().init(mContext, options);

		EMClient.getInstance().setDebugMode(true);//????????????

		this.mEaseUI = EaseUI.getInstance();
		this.setEaseUIProviders();

		EMCallOptions callOptions = EMClient.getInstance().callManager().getCallOptions();
		callOptions.setIsSendPushIfOffline(true);
		callOptions.enableFixedVideoResolution(true);
		setGlobalListeners();

		EMPushHelper.getInstance().setPushListener(new PushListener() {
			@Override
			public void onError(EMPushType pushType, long errorCode) {
				FELog.i("PushClient", "Push client occur a error: " + pushType + " - " + errorCode);
			}

			@Override
			public boolean isSupportPush(EMPushType pushType, EMPushConfig pushConfig) {
				return super.isSupportPush(pushType, pushConfig);
			}
		});

	}

	private EMOptions initChatOptions(Context context, String appKey) {
		MessageSetting messageSetting = mMessageSettingManager.query(loginUserId);
		EMOptions options = new EMOptions();
		options.setRequireAck(true);   //????????????
		options.setAppKey(appKey);
		options.setDeleteMessagesAsExitGroup(messageSetting.deleteMsg);
		options.setAutoAcceptGroupInvitation(true);
		options.setPushConfig(setPushConfig(context));//????????????????????????
		return options;
	}

	private EMPushConfig setPushConfig(Context context) {
		EMPushConfig.Builder builder = new EMPushConfig.Builder(context);
		builder.enableMiPush(FeepPushManager.getPushAppId(), FeepPushManager.getPushKey())
				.enableHWPush(); //???????????????????????????????????????????????????
		return builder.build();
	}

	private void setEaseUIProviders() {
		mEaseUI.setSettingsProvider(() -> {
			MessageSetting messageSetting = mMessageSettingManager.query(loginUserId);
			return messageSetting.speakerOn;
		});
		mEaseUI.setEmojiconInfoProvider(emojIconIdentityCode -> {
			EaseEmojiconGroupEntity data = EmojiconExampleGroupData.getData();
			for (EaseEmojicon emojicon : data.getEmojiconList()) {
				if (emojicon.getIdentityCode().equals(emojIconIdentityCode)) {
					return emojicon;
				}
			}
			return null;
		});
	}

	/**
	 * set global listener
	 */
	private void setGlobalListeners() {
		// create the global connection listener
		if (mCallReceiver == null) {
			mCallReceiver = new CallReceiver();
			IntentFilter callFilter = new IntentFilter(
					EMClient.getInstance().callManager().getIncomingCallBroadcastAction());
			mContext.registerReceiver(mCallReceiver, callFilter);
		}
		//register connection listener
		registerConnectListener();
		//register group and contact event listener
		registerGroupListener();
		//register message event listener
		registerMessageListener();
	}

	private void registerConnectListener() {
		if (mConnectionListener != null) {
			mConnectionListener = new EMConnectionListener() {
				@Override
				public void onConnected() {
				}

				@Override
				public void onDisconnected(int error) {
					EMLog.d("global listener", "onDisconnect" + error);
					if (error == EMError.USER_REMOVED || error == EMError.USER_LOGIN_ANOTHER_DEVICE
							|| error == EMError.SERVER_SERVICE_RESTRICTED) {
						onUserException();
					}
				}
			};
			EMClient.getInstance().addConnectionListener(mConnectionListener);
		}
	}

	/**
	 * register group and contact listener, you need register when login
	 */
	private void registerGroupListener() {
		if (mGroupChangeListener == null) {
			mGroupChangeListener = new SimpleGroupChangeListener() {
				@Override
				public void onUserRemoved(String groupId, String groupName) {
					if (TextUtils.isEmpty(groupName)) {
						EMConversation conversations = EMClient.getInstance().chatManager().getConversation(groupId);
						if (conversations != null) {
							EMClient.getInstance().chatManager().deleteConversation(groupId, true);
						}
					}
					else {
						MMPMessageUtil.saveRemoveGroupMessage(mContext, groupId, false);
						EaseCommonUtils.saveConversationToDB(groupId, groupName);
					}
					EventBus.getDefault().post(new UserRemove(groupId, false));
				}

				@Override
				public void onGroupDestroyed(String groupId, String groupName) {
					if (TextUtils.isEmpty(groupName)) {
						EMConversation conversations = EMClient.getInstance().chatManager().getConversation(groupId);
						if (conversations != null) {
							EMClient.getInstance().chatManager().deleteConversation(groupId, true);
						}
					}
					else {
						MMPMessageUtil.saveGroupDestroyMessage(mContext, groupId);
						EaseCommonUtils.saveConversationToDB(groupId, groupName);
					}
					EventBus.getDefault().post(new GroupDestroyed(groupId, false));
				}

				@Override
				public void onMemberExited(String groupId, String memberId) {
					super.onMemberExited(groupId, memberId);
					EMGroup group = EMClient.getInstance().groupManager().getGroup(groupId);
					if (group != null && TextUtils.equals(EMClient.getInstance().getCurrentUser(), group.getOwner())) {
						MMPMessageUtil.memberExitedMsg(groupId, memberId);
						EventBus.getDefault().post(new BaseGroupEvent(groupId));
					}
				}

				@Override
				public void onAutoAcceptInvitationFromGroup(String groupId, String inviter, String inviteMessage) {
					EventBus.getDefault().post(new BaseGroupEvent(groupId));
				}
			};
			EMClient.getInstance().groupManager().addGroupChangeListener(mGroupChangeListener);
		}
	}

	/**
	 * user met some exception: conflict, removed or forbidden
	 */
	private void onUserException() {
		UserKickPrompt ukp = new UserKickPrompt(
				CommonUtil.getString(R.string.message_please_login_again), true);
		SpUtil.put(PreferencesUtils.USER_KICK_PROMPT, GsonUtil.getInstance().toJson(ukp));
		CoreZygote.getApplicationServices().reLoginApplication();
	}

	/**
	 * Global listener
	 * If this event already handled by an activity, you don't need handle it again
	 * activityList.size() <= 0 means all activities already in background or not in Activity Stack
	 */
	private void registerMessageListener() {
		if (mMessageListener == null) {                                                                 // ????????????
			mMessageListener = new SimpleMessageListener() {
				@Override
				public void onMessageReceived(List<EMMessage> messages) {
					for (EMMessage message : messages) {
						FELog.i("-->>>emHelper:" + message.getStringAttribute("type", "-1"));
						//???????????????????????????
						if (EaseCommonUtils.isExtendMessage(message)) {
							String from = message.getFrom();
							EMConversation conversation = EMClient.getInstance().chatManager().getConversation(from);
							if (conversation != null) {
								conversation.markMessageAsRead(message.getMsgId());
							}
						}
					}
					EventBus.getDefault().post(new ImMessageRefresh(messages));                                 //?????????????????????????????????????????????
					EaseAtMessageHelper.get().parseAtMeMessages(messages);                              // ?????? @ ????????????
					EmHelper.this.executeMessageNotify(messages);                                       // ??????????????????
				}


				@Override
				public void onCmdMessageReceived(List<EMMessage> list) {
					for (EMMessage message : list) {
						EMCmdMessageBody emMessageBody = (EMCmdMessageBody) message.getBody();
						if (EmChatContent.CMD_ACTION_CHANGE_GROURPNAME.equals(emMessageBody.action())) {
							new Thread(() -> {
								try {
									EMGroup group = EMClient.getInstance().groupManager().getGroupFromServer(message.getTo());
									EventBus.getDefault().post(new EMMessageEvent.CmdChangeGroupName(group));
								} catch (HyphenateException e) {
									e.printStackTrace();
								}
							}).start();
						}
						else if (EmChatContent.CMD_ACTION_RECALL.equals(emMessageBody.action())) {
							try {
								EMConversation conversation;
								if (message.getChatType() == ChatType.Chat) {
									conversation = EMClient.getInstance().chatManager().getConversation(message.getFrom());
								}
								else {
									conversation = EMClient.getInstance().chatManager().getConversation(message.getTo());
								}
								conversation.removeMessage(message.getStringAttribute(EmChatContent.CMD_MSGID));
								MMPMessageUtil.saveRecallMessage(mContext, message, false);
								EventBus.getDefault().post(new ImMessageRefresh(list));
							} catch (HyphenateException e) {
								e.printStackTrace();
							}
						}
						else if (EmChatContent.CMD_ACTION_VOICECALL.equals(emMessageBody.action())) {
							String hint = CoreZygote.getContext().getResources().getString(R.string.did_not_answer);
							MMPMessageUtil.saveCallMsg(true, false, message.getFrom(), message.getMsgTime(), hint);
							EventBus.getDefault().post(new ImMessageRefresh(list));
						}
						else if (EmChatContent.CMD_ACTION_VIDEOCALL.equals(emMessageBody.action())) {
							String hint = CoreZygote.getContext().getResources().getString(R.string.did_not_answer);
							MMPMessageUtil.saveCallMsg(false, false, message.getFrom(), message.getMsgTime(), hint);
							EventBus.getDefault().post(new ImMessageRefresh(list));
						}
						else if (EmChatContent.CMD_ACTION_UPDATE_GROUP_SETTING.equals(emMessageBody.action())) {
							EventBus.getDefault().post(new EMMessageEvent.CmdGroupSettingUpdate(message.getTo()));
						}
					}
				}

				@Override
				public void onMessageRead(List<EMMessage> list) {
					EventBus.getDefault().post(new ImMessageRefresh(list));
				}

				@Override
				public void onMessageChanged(EMMessage emMessage, Object o) {
					EventBus.getDefault().post(new ImMessageRefresh(Collections.singletonList(emMessage)));
				}
			};
			EMClient.getInstance().chatManager().addMessageListener(mMessageListener);
		}

	}

	/**
	 * if ever logged in
	 */

	public boolean isLoggedIn() {
		return EMClient.getInstance().isLoggedInBefore();
	}

	/**
	 * logout
	 * @param unbindDeviceToken whether you need unbind your device token
	 * @param callback callback
	 */
	void logout(boolean unbindDeviceToken, final EMCallBack callback) {
		endCall();
		reset();
		EMClient.getInstance().logout(unbindDeviceToken, new EMCallBack() {
			@Override
			public void onSuccess() {
				FELog.e("IM Login Ease logout :???????????? ");
				if (callback != null) callback.onSuccess();
			}

			@Override
			public void onProgress(int progress, String status) {
				if (callback != null) callback.onProgress(progress, status);
			}

			@Override
			public void onError(int code, String error) {
				FELog.e("IM Login Ease logout :??????????????? " + code + "," + error);
				if (callback != null) callback.onError(code, error);
			}
		});
	}

	/**
	 * get sInstance of EaseNotifier
	 */
	private EaseNotifier getNotifier() {
		return mEaseUI.getNotifier();
	}

	private void endCall() {
		try {
			EMClient.getInstance().callManager().endCall();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private synchronized void reset() {
		EMClient.getInstance().removeConnectionListener(mConnectionListener);
		EMClient.getInstance().chatManager().removeMessageListener(mMessageListener);
		EMClient.getInstance().groupManager().removeGroupChangeListener(mGroupChangeListener);
		mContext.unregisterReceiver(mCallReceiver);
		mMessageListener = null;
		mGroupChangeListener = null;
		mConnectionListener = null;
		mCallReceiver = null;
		DbOpenHelper.getInstance().closeDB();
	}

	//??????home????????????????????????????????????,??????????????????????????????
	private void executeMessageNotify(List<EMMessage> messages) {
		String mobileBrand = android.os.Build.MANUFACTURER;
		if (EasyUtils.isAppRunningForeground(CoreZygote.getContext()) && !TextUtils.equals("Xiaomi", mobileBrand)) {
			return;
		}

		String userId = EMClient.getInstance().getCurrentUser();
		MessageSetting msgST = mMessageSettingManager.query(userId);
		if (!msgST.receiveMsg) {                                                    // ????????????????????????
			return;
		}

		if (msgST.silence && inSilenceTime(msgST.silenceST, msgST.silenceET)) {
			return;
		}

		List<EMMessage> activeMessages = queryActiveMessages(messages);             // ??????????????????????????????????????????????????????????????????
		if (CommonUtil.isEmptyList(activeMessages)) {                               // ??????????????????????????????
			return;
		}

		// ?????????????????????????????????????????????????????????????????????????????? Notification ???
		if (msgST.notify) {                                                         // ????????????
			getNotifier().executeNotify(mNotifyMessageMap, activeMessages);
		}

		if (msgST.sound) {                                                          // ??????????????????
			getNotifier().executeMessageRingtone();
		}

		if (msgST.vibrate) {                                                        // ??????????????????
			getNotifier().executeMessageVibrate();
		}
	}

	/**
	 * ?????????????????????????????????????????????????????????
	 * ????????????
	 * 1. ???????????? < ????????????
	 * 2. ???????????? > ????????????(????????????????????????)
	 * @param startTime ???????????? HH:mm ??????
	 * @param endTime ??????
	 */
	private boolean inSilenceTime(String startTime, String endTime) {
		if (TextUtils.isEmpty(startTime) || TextUtils.isEmpty(endTime)) {
			return false;
		}

		Calendar calendar = Calendar.getInstance();
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		boolean isInSilenceTime;

		try {
			SimpleDateFormat format = new SimpleDateFormat("HH:mm");
			Date currentDate = format.parse(hour + ":" + minute);
			Date silenceSTDate = format.parse(startTime);
			Date silenceETDate = format.parse(endTime);

			if (silenceSTDate.getTime() < silenceETDate.getTime()) {
				isInSilenceTime = silenceSTDate.getTime() <= currentDate.getTime() && currentDate.getTime() <= silenceETDate.getTime();
			}
			else {
				isInSilenceTime = silenceSTDate.getTime() <= currentDate.getTime() || currentDate.getTime() <= silenceETDate.getTime();
			}
		} catch (Exception exp) {
			isInSilenceTime = false;
		}

		return isInSilenceTime;
	}


	private List<EMMessage> queryActiveMessages(List<EMMessage> messages) {
		List<EMMessage> activeMessages = new ArrayList<>();
		for (EMMessage message : messages) {
//			String conversationId = message.conversationId();
//			if (TextUtils.equals(conversationId, mToChatUserId)) {  // ????????????????????????????????????
//				continue;
//			}

			if (EaseCommonUtils.isExtendMessage(message)) {//IM??????????????????????????????????????????????????????????????????
				continue;
			}

			if (isSilenceMessage(message)) {//??????????????????
				continue;
			}

			if (!message.isUnread()) {//?????????????????????
				continue;
			}

			activeMessages.add(message);
		}
		return activeMessages;
	}

	private boolean isSilenceMessage(EMMessage message) {
		return CoreZygote.getConvSTServices().isSilence(message.conversationId());
	}
}
