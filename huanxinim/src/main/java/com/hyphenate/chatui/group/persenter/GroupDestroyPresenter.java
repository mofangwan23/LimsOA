package com.hyphenate.chatui.group.persenter;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.FileUtil;
import cn.flyrise.feep.core.dialog.FEMaterialDialog;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chatui.R;
import com.hyphenate.chatui.group.GroupDestroySettingActivity;
import com.hyphenate.chatui.group.GroupListSelecetedActivity;
import com.hyphenate.chatui.group.contract.GroupDestroyContract;
import com.hyphenate.chatui.utils.GroupDestroyUtil;
import com.hyphenate.easeui.EaseUiK;
import com.hyphenate.easeui.EaseUiK.EmChatContent;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 新建：陈冕;
 * 日期： 2018-3-2-14:39.
 */

public class GroupDestroyPresenter implements GroupDestroyContract.IPresenter {

	private EMConversation mConversation;
	private String mGroupTitle;
	private GroupDestroyContract.IView mView;
	private Context mContexct;

	public GroupDestroyPresenter(Context context, String groupId, String title) {
		this.mGroupTitle = title;
		this.mContexct = context;
		this.mView = (GroupDestroyContract.IView) context;
		this.mConversation = EMClient.getInstance().chatManager().getConversation(groupId);
	}

	@Override
	public void allHistoryWriteLocal() {
		writerAllHistoryMessagesDialog();
	}

	@Override
	public void allHistoryReaderNewGoup() {
		((AppCompatActivity) mContexct).startActivityForResult(new Intent(mContexct
				, GroupListSelecetedActivity.class), GroupDestroySettingActivity.SELECTED_GROUP_ID);
	}

	//全部历史消息写入本地
	private void writerAllHistoryMessages() {
		if (mConversation == null) {
			return;
		}
		mView.showLoading(true);
		Observable.create((Observable.OnSubscribe<List<EMMessage>>) subscriber -> {
			List<EMMessage> emMessages = GroupDestroyUtil.getFrontGroupDetroyHistoryMessages(mConversation);
			if (CommonUtil.isEmptyList(emMessages)) {
				subscriber.onError(null);
				return;
			}

			writerSdCard(GroupDestroyUtil.getEmMessageText(emMessages), mGroupTitle, emMessages);
			subscriber.onNext(emMessages);
		}).subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(emMessages -> {
					mView.showLoading(false);
					if (emMessages == null) {
						return;
					}
					FEToast.showMessage(mContexct.getResources().getString(R.string.write_history_data_success));
				}, throwable -> {
					mView.showLoading(false);
					FEToast.showMessage(mContexct.getResources().getString(R.string.write_history_data_error));
				});
	}

	private void writerAllHistoryMessagesDialog() {
		new FEMaterialDialog.Builder(mContexct)
				.setMessage(mContexct.getResources().getString(R.string.write_local_hint))
				.setPositiveButton(mContexct.getResources().getString(R.string.write_local_determine), v -> writerAllHistoryMessages())
				.build()
				.show();
	}

	//写入到本地
	private void writerSdCard(String message, String title, List<EMMessage> emMessages) {
		if (TextUtils.isEmpty(message)) {
			return;
		}
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			return;
		}
		try {
			String fileName = GroupDestroyUtil.getTitle(title) + "_" + GroupDestroyUtil.getStartTime(emMessages) + "—" + GroupDestroyUtil
					.getEndTime(emMessages);
			File seeFile = new File(CoreZygote.getPathServices().getTempFilePath() + File.separator + fileName + FILE_TYPE);
			if (seeFile.exists()) {
				seeFile.delete();
			}
			FileUtil.newFile(seeFile);
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(seeFile));
			bos.write(message.getBytes());
			bos.flush();
			bos.close();
			//下载管理中的临时点击文件
			File downFile = new File(CoreZygote.getPathServices().getSafeFilePath() + File.separator + fileName + FILE_TYPE);
			if (downFile.exists()) {
				downFile.delete();
			}
			FileUtil.newFile(downFile);
			BufferedOutputStream downBos = new BufferedOutputStream(new FileOutputStream(downFile));
			downBos.write(message.getBytes());
			downBos.flush();
			downBos.close();
		} catch (Exception exp) {
			exp.printStackTrace();
		}
	}

	@Override
	public void startReaderGroup(String groupId) {//开始导入新群
		if (TextUtils.isEmpty(groupId)) {
			FEToast.showMessage(mContexct.getResources().getString(R.string.get_chat_group_error));
			return;
		}
		EMConversation writerConversation = EMClient.getInstance().chatManager().getConversation(groupId);
		if (writerConversation == null) {
			return;
		}
		mView.showLoading(true);
		allowInsertNewGroup(groupId, writerConversation);
	}

	//新群的创建时间，必须晚于群聊解散的时间
	private void allowInsertNewGroup(final String writerGroupId, final EMConversation writerConversation) {
		Observable.create((Observable.OnSubscribe<List<EMMessage>>) subscriber -> {
			List<EMMessage> newGroupMessages = GroupDestroyUtil.getAllHistoryMessages(writerConversation);
			if (CommonUtil.isEmptyList(newGroupMessages)) {
				subscriber.onError(new NullPointerException());
				return;
			}
			List<EMMessage> emMessages = GroupDestroyUtil.getFrontGroupDetroyHistoryMessages(mConversation);
			if (CommonUtil.isEmptyList(emMessages)) {
				subscriber.onError(new NullPointerException());
				return;
			}
			if (GroupDestroyUtil.getEndTimeLong(emMessages) < GroupDestroyUtil.getStartTimeLong(newGroupMessages)) {
				insertNewGroup(writerGroupId, writerConversation, emMessages);
				subscriber.onNext(emMessages);
			}
			else {
				subscriber.onError(new NullPointerException());
			}
		}).subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(emMessages -> {
					mView.showLoading(false);
					if (emMessages == null) {
						return;
					}
					FEToast.showMessage(mContexct.getResources().getString(R.string.write_history_data_success));
				}, throwable -> {
					mView.showLoading(false);
					FEToast.showMessage(mContexct.getResources().getString(R.string.write_new_group_error));
				});
	}

	private void insertNewGroup(final String writerGroupId, EMConversation writerConversation, List<EMMessage> emMessages) {//插入到新群聊
		if (writerConversation == null) {
			return;
		}
		EMMessage newMsg;
		for (EMMessage msg : emMessages) {
			if (TextUtils.equals(msg.getFrom(), EMClient.getInstance().getCurrentUser())) {
				newMsg = EMMessage.createSendMessage(msg.getType());
				newMsg.setTo(writerGroupId);
			}
			else {
				newMsg = EMMessage.createReceiveMessage(msg.getType());
				newMsg.setFrom(writerGroupId);
			}
			newMsg.addBody(msg.getBody());
			newMsg.setMsgId(msg.getMsgId() + writerGroupId);
			newMsg.setMsgTime(msg.getMsgTime());
			newMsg.setLocalTime(msg.localTime());
			newMsg.setDirection(msg.direct());
			newMsg.setStatus(msg.status());
			newMsg.setChatType(msg.getChatType());
			newMsg.setAcked(msg.isAcked());
			newMsg.setDelivered(msg.isDelivered());
			newMsg.setListened(msg.isListened());
			newMsg.setProgress(msg.progress());
			newMsg.setUnread(msg.isUnread());

			newMsg.setAttribute(EaseUiK.EmChatContent.MESSAGE_COPY_GROUP, true);//是导出的聊天记录
			newMsg.setAttribute(EmChatContent.MESSAGE_COPY_GROUP_USER_ID, msg.getFrom());//聊天发送人

			newMsg.setAttribute(EaseUiK.EmChatContent.MESSAGE_ATTR_IS_VOICE_CALL,
					msg.getBooleanAttribute(EaseUiK.EmChatContent.MESSAGE_ATTR_IS_VOICE_CALL, false));
			newMsg.setAttribute(EaseUiK.EmChatContent.MESSAGE_ATTR_IS_VIDEO_CALL,
					msg.getBooleanAttribute(EaseUiK.EmChatContent.MESSAGE_ATTR_IS_VIDEO_CALL, false));
			newMsg.setAttribute(EaseUiK.EmChatContent.MESSAGE_ATTR_IS_BIG_EXPRESSION,
					msg.getBooleanAttribute(EaseUiK.EmChatContent.MESSAGE_ATTR_IS_BIG_EXPRESSION, false));
			newMsg.setAttribute(EaseUiK.EmChatContent.MESSAGE_ATTR_EXPRESSION_ID,
					msg.getStringAttribute(EaseUiK.EmChatContent.MESSAGE_ATTR_EXPRESSION_ID, ""));
			newMsg.setAttribute(EaseUiK.EmChatContent.MESSAGE_ATTR_AT_MSG,
					msg.getStringAttribute(EaseUiK.EmChatContent.MESSAGE_ATTR_AT_MSG, ""));
			newMsg.setAttribute(EaseUiK.EmChatContent.MESSAGE_ATTR_VALUE_AT_MSG_ALL,
					msg.getStringAttribute(EaseUiK.EmChatContent.MESSAGE_ATTR_VALUE_AT_MSG_ALL, ""));
			newMsg.setAttribute(EaseUiK.EmChatContent.MESSAGE_ATTR_IS_SYSTEM,
					msg.getBooleanAttribute(EaseUiK.EmChatContent.MESSAGE_ATTR_IS_SYSTEM, false));
			newMsg.setAttribute(EaseUiK.EmChatContent.MESSAGE_ATTR_IS_REJECTION,
					msg.getBooleanAttribute(EaseUiK.EmChatContent.MESSAGE_ATTR_IS_REJECTION, false));
			newMsg.setAttribute(EaseUiK.EmChatContent.MESSAGE_ATTR_NOT_SHOW_CONTENT,
					msg.getBooleanAttribute(EaseUiK.EmChatContent.MESSAGE_ATTR_NOT_SHOW_CONTENT, false));
			newMsg.setAttribute(EaseUiK.EmChatContent.MESSAGE_SUPER_MOUDULE,
					msg.getStringAttribute(EaseUiK.EmChatContent.MESSAGE_SUPER_MOUDULE, ""));

			newMsg.setAttribute(EaseUiK.EmChatContent.MESSAGE_ATTR_IS_REPLY,
					msg.getBooleanAttribute(EaseUiK.EmChatContent.MESSAGE_ATTR_IS_REPLY, false));
			newMsg.setAttribute(EaseUiK.EmChatContent.MESSAGE_ATTR_TIME,
					msg.getLongAttribute(EaseUiK.EmChatContent.MESSAGE_ATTR_TIME, msg.getMsgTime()));

			newMsg.setAttribute(EaseUiK.EmChatContent.CMD_ACTION_CHANGE_GROURPNAME,
					msg.getStringAttribute(EaseUiK.EmChatContent.CMD_ACTION_CHANGE_GROURPNAME, ""));
			newMsg.setAttribute(EaseUiK.EmChatContent.CMD_ACTION_RECALL,
					msg.getStringAttribute(EaseUiK.EmChatContent.CMD_ACTION_RECALL, ""));
			newMsg.setAttribute(EaseUiK.EmChatContent.CMD_ACTION_VIDEOCALL,
					msg.getStringAttribute(EaseUiK.EmChatContent.CMD_ACTION_VIDEOCALL, ""));
			newMsg.setAttribute(EaseUiK.EmChatContent.CMD_ACTION_VOICECALL,
					msg.getStringAttribute(EaseUiK.EmChatContent.CMD_ACTION_VOICECALL, ""));
			newMsg.setAttribute(EaseUiK.EmChatContent.CMD_ACTION_UPDATE_GROUP_SETTING,
					msg.getStringAttribute(EaseUiK.EmChatContent.CMD_ACTION_UPDATE_GROUP_SETTING, ""));
			newMsg.setAttribute(EaseUiK.EmChatContent.CMD_GROUPNAME, msg.getStringAttribute(EaseUiK.EmChatContent.CMD_GROUPNAME, ""));

			newMsg.setAttribute(EaseUiK.EmChatContent.CMD_MSGID, msg.getStringAttribute(EaseUiK.EmChatContent.CMD_MSGID, ""));
//			newMsg.setAttribute(EaseUiK.EmChatContent.CMD_MSGTIME,
//					msg.getLongAttribute(EaseUiK.EmChatContent.CMD_MSGTIME, msg.getMsgTime()));
			newMsg.setAttribute(EaseUiK.EmChatContent.CMD_ADD_USERLIST, msg.getStringAttribute(EaseUiK.EmChatContent.CMD_ADD_USERLIST, ""));

			newMsg.setAttribute("type", msg.getStringAttribute("type", ""));
			newMsg.setAttribute("id", msg.getStringAttribute("id", ""));
			newMsg.setAttribute("msgId", msg.getStringAttribute("msgId", ""));
			newMsg.setAttribute("url", msg.getStringAttribute("url", ""));
			newMsg.setAttribute("action", msg.getStringAttribute("action", ""));
			newMsg.setAttribute("title", msg.getStringAttribute("title", ""));
			newMsg.setAttribute("content", msg.getStringAttribute("content", ""));
			newMsg.setAttribute("locationAddress", msg.getStringAttribute("locationAddress", ""));

			writerConversation.insertMessage(newMsg);
		}
	}
}
