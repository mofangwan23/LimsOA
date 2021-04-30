package com.hyphenate.easeui.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMMessage.ChatType;
import com.hyphenate.chatui.R;
import com.hyphenate.easeui.EaseUiK;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

;

public class EaseAtMessageHelper {

	private static final String KEY_AT_GROUPS = "AT_GROUPS";

	private static EaseAtMessageHelper sInstance;
	private SharedPreferences mPreferences;
	private Set<String> mAtMeGroupList;
	private List<String> mToAtUserList = new ArrayList<>();

	public synchronized static EaseAtMessageHelper get() {
		if (sInstance == null) {
			sInstance = new EaseAtMessageHelper();
		}
		return sInstance;
	}


	private EaseAtMessageHelper() {
		mAtMeGroupList = getAtMeGroups();
		if (mAtMeGroupList == null) {
			mAtMeGroupList = new HashSet<>();
		}
	}

	/**
	 * check if be mentioned(@) in the content
	 */
	public boolean containsAtUsername(String content) {
		if (TextUtils.isEmpty(content)) {
			return false;
		}

		if (!content.contains("@")) {
			return false;
		}

		for (String username : mToAtUserList) {
			if (content.contains("@" + username)) {
				return true;
			}
		}
		return false;
	}

	public void addAtUser(String username) {
		if (!mToAtUserList.contains(username)) {
			mToAtUserList.add(username);
		}
	}

	public boolean containsAtAll(String content) {
		String atAll = "@" + CommonUtil.getString(R.string.all_members);
		if (content.contains(atAll)) {
			return true;
		}
		return false;
	}

	/**
	 * 尝试解析出是否存在 [@我 或 @所有人] 的消息
	 * 返回 true 表示存在 @ 的消息
	 */
	public boolean parseAtMeMessages(List<EMMessage> messages) {
		boolean hasChange = false;
		for (EMMessage message : messages) {
			if (message.getChatType() != ChatType.GroupChat) {
				continue;
			}

			String atMeGroupId = getGroupIdInAtMeMessage(message);
			if (!TextUtils.isEmpty(atMeGroupId)) {
				if (!mAtMeGroupList.contains(atMeGroupId)) {
					hasChange = true;
					mAtMeGroupList.add(atMeGroupId);
				}
			}
		}

		if (hasChange)
			setAtMeGroups(mAtMeGroupList);
		return hasChange;
	}

	/**
	 * 这条消息是否有人 @ 我，如果是，返回这条消息所在会话的 GroupId
	 */
	private String getGroupIdInAtMeMessage(EMMessage message) {
		String currentUser = CoreZygote.getLoginUserServices().getUserName();
		String atAttribute;
		try {
			atAttribute = message.getStringAttribute(EaseUiK.EmChatContent.MESSAGE_ATTR_AT_MSG, null);
		} catch (Exception exp) {
			atAttribute = null;
		}

		if (TextUtils.isEmpty(atAttribute)) {
			return null;
		}

		String[] names = atAttribute.split(",");
		for (String name : names) {
			if (TextUtils.equals(currentUser, name)) {
				return message.getTo();
			}
		}
		return null;
	}

	/**
	 * remove group from the list
	 */
	public void removeAtMeGroup(String groupId) {
		if (mAtMeGroupList.remove(groupId)) // 删除，删除成功则 刷新 SP
			setAtMeGroups(mAtMeGroupList);
	}

	/**
	 * check if the input groupId in mAtMeGroupList
	 */
	public boolean hasAtMeMsg(String groupId) {
		return mAtMeGroupList.contains(groupId);
	}

	public String formatAtUserToString() {
		if (mToAtUserList.size() == 0) {
			return null;
		}

		if (mToAtUserList.size() == 1) {
			return mToAtUserList.get(0);
		}

		StringBuilder builder = new StringBuilder(mToAtUserList.get(0));
		for (int i = 1; i < mToAtUserList.size(); i++) {
			builder.append(",").append(mToAtUserList.get(i));
		}

		mToAtUserList.clear();
		return builder.toString();
	}

	public void cleanToAtUserList() {
		synchronized (mToAtUserList) {
			mToAtUserList.clear();
		}
	}

	public void setAtMeGroups(Set<String> groups) {
		if(mPreferences == null) {
			mPreferences = CoreZygote.getContext().getSharedPreferences("EM_SP_AT_MESSAGE", Context.MODE_PRIVATE);
		}
		mPreferences.edit()
				.remove(KEY_AT_GROUPS)
				.putStringSet(KEY_AT_GROUPS, groups)
				.apply();
	}

	public Set<String> getAtMeGroups() {
		if(mPreferences == null) {
			mPreferences = CoreZygote.getContext().getSharedPreferences("EM_SP_AT_MESSAGE", Context.MODE_PRIVATE);
		}
		return mPreferences.getStringSet(KEY_AT_GROUPS, null);
	}
}
