package com.hyphenate.chatui.group.provider;


import static rx.Observable.create;
import static rx.Observable.unsafeCreate;

import android.text.TextUtils;

import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.utils.GsonUtil;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.core.services.model.AddressBook;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMCmdMessageBody;
import com.hyphenate.chat.EMCursorResult;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMMessage.ChatType;
import com.hyphenate.chatui.group.model.GroupSetting;
import com.hyphenate.chatui.group.model.RemoveGroupUserRequest;
import com.hyphenate.chatui.group.model.RemoveGroupUserResponse;
import com.hyphenate.chatui.utils.IMHuanXinHelper;
import com.hyphenate.easeui.EaseUiK.EmChatContent;
import com.hyphenate.easeui.utils.EaseCommonUtils;
import com.hyphenate.easeui.utils.MMPMessageUtil;
import com.hyphenate.exceptions.HyphenateException;

import java.util.ArrayList;
import java.util.List;

import rx.Observable.OnSubscribe;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by klc on 2017/8/17.
 */

public class GroupDataProvider {

	private String mGroupId;
	private EMGroup mGroup;

	public GroupDataProvider(String groupId) {
		this.mGroupId = groupId;
	}

	public GroupDataProvider(EMGroup mGroup) {
		this.mGroup = mGroup;
	}

	public void loadGroupInfo(HandleListener<EMGroup> listener) {
		create((OnSubscribe<EMGroup>) subscriber -> {
			try {
				mGroup = EMClient.getInstance().groupManager().getGroupFromServer(mGroupId);
				subscriber.onNext(mGroup);
				subscriber.onCompleted();
			} catch (HyphenateException e) {
				e.printStackTrace();
				subscriber.onError(e);
			}
		}).subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(listener::onSuccess, throwable -> listener.onFail());
	}


	public void loadGroupUser(int count, HandleListener<List<String>> listener) {
		create((OnSubscribe<List<String>>) subscriber -> {
			try {
				List<String> userList = new ArrayList<>();
				EMGroup group = EMClient.getInstance().groupManager().getGroup(mGroup.getGroupId());
				userList.add(group.getOwner());
				EMCursorResult<String> result;
				result = EMClient.getInstance().groupManager().fetchGroupMembers(group.getGroupId(),
						"", count);
				userList.addAll(result.getData());
				subscriber.onNext(userList);
				subscriber.onCompleted();
			} catch (HyphenateException e) {
				e.printStackTrace();
				subscriber.onError(e);
			}
		}).subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(listener::onSuccess, throwable -> listener.onFail());
	}

	public void changeGroupName(String name, HandleListener<Boolean> listener) {
		create((OnSubscribe<Boolean>) subscriber -> {
			try {
				EMClient.getInstance().groupManager().changeGroupName(mGroup.getGroupId(), name);
				sendGroupChangeNameMsg(name);
				subscriber.onNext(Boolean.TRUE);
				subscriber.onCompleted();
			} catch (HyphenateException e) {
				e.printStackTrace();
				subscriber.onError(e);
			}
		}).subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(listener::onSuccess, throwable -> listener.onFail());
	}

	public void changeGroupSetting(GroupSetting setting, HandleListener<Boolean> listener) {
		if (setting != null) {
			create((OnSubscribe<Boolean>) subscriber -> {
				try {
					String extension = GsonUtil.getInstance().toJson(setting);
					EMClient.getInstance().groupManager().updateGroupExtension(mGroup.getGroupId(), extension);
					//拓展消息设置完成后，我们还需要去发送给在线的人员，让他们更新设置。
					sendGroupSettingUpdateMsg();
					subscriber.onNext(Boolean.TRUE);
					subscriber.onCompleted();
				} catch (HyphenateException e) {
					e.printStackTrace();
					subscriber.onError(e);
				}
			}).subscribeOn(Schedulers.io())
					.observeOn(AndroidSchedulers.mainThread())
					.subscribe(listener::onSuccess, throwable -> listener.onFail());
		}
	}

	public void leaveGroup(HandleListener<Boolean> listener) {
		create((OnSubscribe<Boolean>) subscriber -> {
			try {
				EMClient.getInstance().groupManager().leaveGroup(mGroup.getGroupId());
				MMPMessageUtil.saveRemoveGroupMessage(CoreZygote.getContext(), mGroup.getGroupId(), true);
				EaseCommonUtils.saveConversationToDB(mGroup.getGroupId(), mGroup.getGroupName());
				subscriber.onNext(Boolean.TRUE);
				subscriber.onCompleted();
			} catch (HyphenateException e) {
				e.printStackTrace();
				subscriber.onError(e);
			}
		}).subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(listener::onSuccess, throwable -> listener.onFail());
	}


	public void destroyGroup(HandleListener<Boolean> listener) {
		create((OnSubscribe<Boolean>) subscriber -> {
			try {
				EMClient.getInstance().groupManager().destroyGroup(mGroup.getGroupId());
				MMPMessageUtil.saveGroupDestroyMessage(CoreZygote.getContext(), mGroup.getGroupId());
				EaseCommonUtils.saveConversationToDB(mGroup.getGroupId(), mGroup.getGroupName());
				subscriber.onNext(Boolean.TRUE);
				subscriber.onCompleted();
			} catch (HyphenateException e) {
				e.printStackTrace();
				subscriber.onError(e);
			}
		}).subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(listener::onSuccess, throwable -> listener.onFail());
	}

	public void addContract(List<AddressBook> addressBooks, HandleListener<Boolean> listener) {
		unsafeCreate((OnSubscribe<Boolean>) subscriber -> {
			String[] members = new String[addressBooks.size()];
			for (int i = 0, n = addressBooks.size(); i < n; i++) {
				members[i] = IMHuanXinHelper.getInstance().getImUserId(addressBooks.get(i).userId);
			}
			try {
				if (EMClient.getInstance().getCurrentUser().equals(mGroup.getOwner())) {
					EMClient.getInstance().groupManager().addUsersToGroup(mGroup.getGroupId(), members);
				}
				else {
					EMClient.getInstance().groupManager().inviteUser(mGroup.getGroupId(), members, null);
				}
				MMPMessageUtil.sendInviteMsg(mGroup.getGroupId(), addressBooks);
				subscriber.onNext(Boolean.TRUE);
				subscriber.onCompleted();
			} catch (HyphenateException e) {
				e.printStackTrace();
				subscriber.onError(e);
			}
		}).subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(listener::onSuccess, throwable -> listener.onFail());
	}


	public void removeUsers(String[] userIds, HandleListener<Boolean> listener) {
		FEHttpClient.getInstance()
				.post(new RemoveGroupUserRequest(userIds, mGroup.getGroupId()), new ResponseCallback<RemoveGroupUserResponse>() {
					@Override
					public void onCompleted(RemoveGroupUserResponse removeGroupUserResponse) {
						if (TextUtils.isEmpty(removeGroupUserResponse.result.exception)) {
							listener.onSuccess(true);
						}
						else {
							onFailure(null);
						}
					}

					@Override
					public void onFailure(RepositoryException repositoryException) {
						super.onFailure(repositoryException);
						listener.onFail();
					}
				});
	}


	public void changeOwner(String userId, HandleListener<Boolean> listener) {
		create((rx.Observable.OnSubscribe<Boolean>) subscriber -> {
			try {
				EMClient.getInstance().groupManager().changeOwner(mGroup.getGroupId(), userId);
				subscriber.onNext(Boolean.TRUE);
				subscriber.onCompleted();
			} catch (Exception e) {
				subscriber.onNext(Boolean.FALSE);
			}
		}).subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(listener::onSuccess, throwable -> listener.onFail());
	}

	private void sendGroupChangeNameMsg(String groupName) {//透传消息群组修改时，会排除修改人
		EMMessage cmdMsg = EMMessage.createSendMessage(EMMessage.Type.CMD);
		String action = EmChatContent.CMD_ACTION_CHANGE_GROURPNAME;
		EMCmdMessageBody cmdBody = new EMCmdMessageBody(action);
		cmdMsg.setChatType(ChatType.GroupChat);
		cmdMsg.setTo(mGroup.getGroupId());
		cmdMsg.setAttribute(EmChatContent.CMD_GROUPNAME, groupName);
		cmdMsg.addBody(cmdBody);
		EMClient.getInstance().chatManager().sendMessage(cmdMsg);
	}

	private void sendGroupSettingUpdateMsg() {
		EMMessage cmdMsg = EMMessage.createSendMessage(EMMessage.Type.CMD);
		String action = EmChatContent.CMD_ACTION_UPDATE_GROUP_SETTING;
		EMCmdMessageBody cmdBody = new EMCmdMessageBody(action);
		cmdMsg.setChatType(ChatType.GroupChat);
		cmdMsg.setTo(mGroup.getGroupId());
		cmdMsg.addBody(cmdBody);
		EMClient.getInstance().chatManager().sendMessage(cmdMsg);
	}

	public boolean isAllowInvite() {
		String extension = mGroup.getExtension();
		if (TextUtils.isEmpty(extension)) {
			return false;
		}
		else {
			GroupSetting groupSetting = GsonUtil.getInstance().fromJson(extension, GroupSetting.class);
			return groupSetting.isAllowInvite();
		}
	}

	public interface HandleListener<T> {

		void onSuccess(T t);

		void onFail();

	}
}
