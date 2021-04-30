package com.hyphenate.chatui.retrieval;

import android.text.TextUtils;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.services.model.AddressBook;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMCursorResult;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chatui.R;
import java.util.ArrayList;
import java.util.List;
import rx.Observable;

/**
 * @author ZYP
 * @since 2018-05-08 15:50
 */
public class GroupRepository {

	/**
	 * 查找群聊中包含这个关键字的人
	 */
	public Observable<List<GroupInfo>> queryGroupInfo(String keyword, int maxSize) {
		return Observable.create(f -> {
			try {
				EMClient.getInstance().groupManager().getJoinedGroupsFromServer();
				List<EMGroup> allGroups = EMClient.getInstance().groupManager().getAllGroups();
				if (CommonUtil.isEmptyList(allGroups)) {
					f.onError(new RuntimeException(""));
					return;
				}

				List<GroupInfo> results = maxSize > 0 ? new ArrayList<>(maxSize) : new ArrayList<>();
				for (EMGroup group : allGroups) {
					if (maxSize > 0 && results.size() == maxSize) break;
					GroupInfo groupInfo = new GroupInfo();
					groupInfo.imageRes = R.drawable.em_group_icon;
					groupInfo.conversationName = group.getGroupName();
					groupInfo.conversationId = group.getGroupId();
					groupInfo.memberCount = group.getMemberCount();

					if (groupInfo.conversationName.contains(keyword)) { // 1. 优先查找群名称
						results.add(groupInfo);
						continue;
					}

					// 查找群成员
					List<String> userIds = new ArrayList<>(groupInfo.memberCount + 1);
					userIds.add(group.getOwner());                          // 添加群主

					EMCursorResult<String> result = EMClient.getInstance()
							.groupManager().fetchGroupMembers(groupInfo.conversationId, "", groupInfo.memberCount);
					userIds.addAll(result.getData());                       // 添加群成员

					List<AddressBook> addressBooks = CoreZygote.getAddressBookServices().queryUserIds(userIds);
					groupInfo.content = getContent(addressBooks, keyword);  // 是否包含对应成员
					if (!TextUtils.isEmpty(groupInfo.content)) {
						results.add(groupInfo);
						continue;
					}
				}

				if (CommonUtil.isEmptyList(results)) {
					f.onError(new RuntimeException("Empty results!"));
					return;
				}

				f.onNext(results);
			} catch (Exception exp) {
				f.onError(exp);
			} finally {
				f.onCompleted();
			}
		});
	}

	private String getContent(List<AddressBook> addressBooks, String keyword) {
		String content = null;
		for (AddressBook addressBook : addressBooks) {
			if (addressBook.name.contains(keyword)) {
				content = "包含：" + addressBook.name;
				break;
			}
		}
		return content;
	}
}
