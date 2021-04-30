package com.hyphenate.chatui.group.persenter;

import android.text.TextUtils;

import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.services.model.AddressBook;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chatui.R;
import com.hyphenate.chatui.group.contract.GroupAllUserContract;
import com.hyphenate.chatui.group.provider.GroupDataProvider;
import com.hyphenate.chatui.group.provider.GroupDataProvider.HandleListener;
import com.hyphenate.easeui.EaseUiK;
import com.hyphenate.easeui.utils.EaseUserUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by klc on 2017/3/31.
 * 显示所有群组成员列表。
 */

public class GroupAllUserPresenter implements GroupAllUserContract.IPresenter {

	private EMGroup mGroup;
	private GroupAllUserContract.IView mView;
	private List<String> allUserList;
	private GroupDataProvider mProvider;

	public GroupAllUserPresenter(String groupId, GroupAllUserContract.IView mView) {
		this.mView = mView;
		this.mProvider = new GroupDataProvider(groupId);
	}

	@Override
	public void loadGroup() {
		mView.showLoading();
		mProvider.loadGroupInfo(new HandleListener<EMGroup>() {
			@Override
			public void onSuccess(EMGroup group) {
				mGroup = group;
				getAllUserForServer();
			}

			@Override
			public void onFail() {
				mView.hideLoading();
			}
		});
	}


	public void getAllUserForServer() {
		mView.showLoading();
		mProvider.loadGroupUser(mGroup.getMemberCount(), new HandleListener<List<String>>() {
			@Override
			public void onSuccess(List<String> strings) {
				mView.hideLoading();
				if (mGroup.getOwner().equals(EMClient.getInstance().getCurrentUser())) {
					strings.add(EaseUiK.EmUserList.em_userList_addUser);
					if (strings.size() > 2 && mGroup.getOwner().equals(EMClient.getInstance().getCurrentUser())) {
						strings.add(EaseUiK.EmUserList.em_userList_removeUser);
					}
				}
				else {
					if (mProvider.isAllowInvite()) {
						strings.add(EaseUiK.EmUserList.em_userList_addUser);
					}
				}
				mView.refreshListData(allUserList = strings);
			}

			@Override
			public void onFail() {
				mView.hideLoading();
			}
		});
	}

	@Override
	public void addContract(List<AddressBook> addressBooks) {
		if (CommonUtil.isEmptyList(addressBooks)) {
			return;
		}
		mView.showLoading();
		mProvider.addContract(addressBooks, new HandleListener<Boolean>() {
			@Override
			public void onSuccess(Boolean aBoolean) {
				mView.hideLoading();
				FEToast.showMessage(R.string.Add_group_members_success);
			}

			@Override
			public void onFail() {
				mView.hideLoading();
				FEToast.showMessage(R.string.Add_group_members_fail);
			}
		});
	}

	@Override
	public void queryContacts(String query) {
		if (TextUtils.isEmpty(query)) {
			mView.refreshListData(allUserList);
			return;
		}
		List<String> searchUser = new ArrayList<>();
		for (String user : allUserList) {
			String userName = EaseUserUtils.getUserNick(user);
			if (userName.contains(query)) {
				searchUser.add(user);
			}
		}
		mView.refreshListData(searchUser);
	}


	public List<String> getAllUserList() {
		List<String> userList = new ArrayList<>();
		userList.add(mGroup.getOwner());
		userList.addAll(mGroup.getMembers());
		return userList;
	}
}
