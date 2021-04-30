package com.hyphenate.chatui.group.persenter;

import android.app.Activity;
import android.text.TextUtils;

import cn.flyrise.feep.core.common.FEToast;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chatui.R;
import com.hyphenate.chatui.group.contract.GroupUserManageContract;
import com.hyphenate.chatui.group.model.EmUserItem;
import com.hyphenate.chatui.group.provider.GroupDataProvider;
import com.hyphenate.chatui.group.provider.GroupDataProvider.HandleListener;
import com.hyphenate.easeui.utils.EaseUserUtils;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by klc on 2017/3/15.
 */

public class GroupUserManagePresenter implements GroupUserManageContract.IPresenter {

	private List<EmUserItem> mUserList;
	private GroupUserManageContract.IView mView;
	private EMGroup mGroup;
	private GroupDataProvider mProvider;

	public GroupUserManagePresenter(GroupUserManageContract.IView mView, String groupID) {
		this.mView = mView;
		this.mUserList = new ArrayList<>();
		this.mGroup = EMClient.getInstance().groupManager().getGroup(groupID);
		this.mProvider = new GroupDataProvider(mGroup);
	}

	@Override
	public void getAllUser() {
		mView.showRefresh(true);
		mProvider.loadGroupUser(mGroup.getMemberCount(), new HandleListener<List<String>>() {
			@Override
			public void onSuccess(List<String> strings) {
				mView.showRefresh(false);
				List<EmUserItem> userList = new ArrayList<>();
				for (String userID : strings) {
					userList.add(new EmUserItem(userID));
				}
				mView.refreshListData(mUserList = userList);
			}

			@Override
			public void onFail() {
				mView.showRefresh(false);
				FEToast.showMessage(R.string.load_user_fail);
			}
		});
	}

	@Override
	public void queryContacts(String query) {
		if (TextUtils.isEmpty(query)) {
			mView.refreshListData(mUserList);
			return;
		}
		List<EmUserItem> searchUser = new ArrayList<>();
		for (EmUserItem user : mUserList) {
			String userName = EaseUserUtils.getUserNick(user.userId);
			if (userName.contains(query)) {
				searchUser.add(user);
			}
		}
		mView.refreshListData(searchUser);
	}


	@Override
	public void deleteUser(List<EmUserItem> selectUsers) {
		mView.showLoading();
		final String[] userIds = new String[selectUsers.size()];
		for (int i = 0; i < selectUsers.size(); i++) {
			userIds[i] = selectUsers.get(i).userId;
		}
		mProvider.removeUsers(userIds, new HandleListener<Boolean>() {
			@Override
			public void onSuccess(Boolean aBoolean) {
//				notifyKickOutFromGroup(mGroup.getGroupName(), userIds);
				mUserList.removeAll(selectUsers);
				mView.refreshListData();
				mView.setDeleteCount(0);
				mView.hideLoading();
			}

			@Override
			public void onFail() {
				FEToast.showMessage(R.string.del_user_fail);
				mView.hideLoading();
			}
		});
	}


	@Override
	public void changeOwner(String userID) {
		mView.showLoading();
		mProvider.changeOwner(userID, new HandleListener<Boolean>() {
			@Override
			public void onSuccess(Boolean aBoolean) {
				mView.hideLoading();
				mView.showMessage(R.string.change_the_change_owner_success);
				mView.setResult(Activity.RESULT_OK);
				mView.finish();
			}

			@Override
			public void onFail() {
				mView.hideLoading();
				mView.showMessage(R.string.change_the_owner_failed_please);
			}
		});
	}
}
