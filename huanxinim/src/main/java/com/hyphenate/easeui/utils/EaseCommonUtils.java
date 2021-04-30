/**
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hyphenate.easeui.utils;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.utils.BitmapUtil;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation.EMConversationType;
import com.hyphenate.chat.EMFileMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.chatui.R;
import com.hyphenate.easeui.EaseUiK;
import com.hyphenate.util.EMLog;
import java.util.List;


public class EaseCommonUtils {

	private static final String TAG = "CommonUtils";

	/**
	 * check if network avalable
	 */
	public static boolean isNetWorkConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
			if (mNetworkInfo != null) {
				return mNetworkInfo.isAvailable() && mNetworkInfo.isConnected();
			}
		}

		return false;
	}

	/**
	 * check if sdcard exist
	 */
	public static boolean isSdcardExist() {
		return android.os.Environment.getExternalStorageState()
				.equals(android.os.Environment.MEDIA_MOUNTED);
	}

	public static EMMessage createExpressionMessage(String toChatUsername, String expressioName,
			String identityCode) {
		EMMessage message = EMMessage
				.createTxtSendMessage("[" + expressioName + "]", toChatUsername);
		if (identityCode != null) {
			message.setAttribute(EaseUiK.EmChatContent.MESSAGE_ATTR_EXPRESSION_ID, identityCode);
		}
		message.setAttribute(EaseUiK.EmChatContent.MESSAGE_ATTR_IS_BIG_EXPRESSION, true);
		return message;
	}

	/**
	 * Get digest according message type and content
	 */
	public static String getMessageDigest(EMMessage message, Context context) {
		String digest;
		switch (message.getType()) {
			case LOCATION:
				if (message.direct() == EMMessage.Direct.RECEIVE) {
					digest = getString(context, R.string.location_recv);
					return digest;
				}
				else {
					digest = getString(context, R.string.location_prefix);
				}
				break;
			case IMAGE:
				digest = getString(context, R.string.picture);
				break;
			case VOICE:
				digest = getString(context, R.string.voice_prefix);
				break;
			case VIDEO:
				digest = getString(context, R.string.video);
				break;
			case TXT:
				EMTextMessageBody txtBody = (EMTextMessageBody) message.getBody();
				if (message.getBooleanAttribute(EaseUiK.EmChatContent.MESSAGE_ATTR_IS_VOICE_CALL, false)) {
					digest = getString(context, R.string.voice_call) + txtBody.getMessage();
				}
				else if (message
						.getBooleanAttribute(EaseUiK.EmChatContent.MESSAGE_ATTR_IS_VIDEO_CALL, false)) {
					digest = getString(context, R.string.video_call) + txtBody.getMessage();
				}
				else if (message
						.getBooleanAttribute(EaseUiK.EmChatContent.MESSAGE_ATTR_IS_BIG_EXPRESSION, false)) {
					if (!TextUtils.isEmpty(txtBody.getMessage())) {
						digest = txtBody.getMessage();
					}
					else {
						digest = getString(context, R.string.dynamic_expression);
					}
				}
				else {
					digest = txtBody.getMessage();
				}
				break;
			case FILE:
				EMFileMessageBody fileMessageBody = (EMFileMessageBody) message.getBody();
				if (fileMessageBody != null && BitmapUtil
						.isPictureGif(fileMessageBody.getFileName())) {
					digest = getString(context, R.string.dynamic_expression);
				}
				else {
					digest = getString(context, R.string.file);
				}
				break;
			default:
				EMLog.e(TAG, "error, unknow type");
				return "";
		}

		return digest;
	}

	static String getString(Context context, int resId) {
		return context.getResources().getString(resId);
	}

	/**
	 * get top activity
	 */
	public static String getTopActivity(Context context) {
		ActivityManager manager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1);

		if (runningTaskInfos != null) {
			return runningTaskInfos.get(0).topActivity.getClassName();
		}
		else {
			return "";
		}
	}


	/**
	 * change the chat type to EMConversationType
	 */
	public static EMConversationType getConversationType(int chatType) {
		if (chatType == EaseUiK.EmChatContent.em_chatType_single) {
			return EMConversationType.Chat;
		}
		else if (chatType == EaseUiK.EmChatContent.em_chatType_group) {
			return EMConversationType.GroupChat;
		}
		else {
			return EMConversationType.ChatRoom;
		}
	}


	/**
	 * \~chinese
	 * 判断是否是免打扰的消息,如果是app中应该不要给用户提示新消息
	 * @param message return <p> \~english check if the message is kind of slient message, if that's it, app should not play tone or
	 * vibrate
	 */
	public static boolean isSilentMessage(EMMessage message) {
		return message.getBooleanAttribute("em_ignore_notification", false);
	}


	/**
	 * 保存会话消息到数据库中，退群后选择不删除会话记录，群名字会显示错误。
	 */
	public static void saveConversationToDB(String groupId, String groupName) {
		if (!EMClient.getInstance().getOptions().isDeleteMessagesAsExitGroup()) {
				CoreZygote.getConvSTServices().makeConversationGroud(groupId, groupName);
		}
	}

	public static boolean isExtendMessage(EMMessage message) {
		String moduleId = message.getStringAttribute("type", "");
		return !TextUtils.isEmpty(moduleId);
	}
}
