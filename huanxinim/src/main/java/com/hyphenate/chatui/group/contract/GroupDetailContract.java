package com.hyphenate.chatui.group.contract;

import com.hyphenate.chat.EMGroup;

import java.util.List;

import cn.flyrise.feep.core.dialog.FEMaterialDialog;
import cn.flyrise.feep.core.dialog.FEMaterialEditTextDialog;
import cn.flyrise.feep.core.services.model.AddressBook;

/**
 * Created by klc on 2017/3/16.
 */

public interface GroupDetailContract {

	interface IView {

		void showLoading(boolean show);

		void updateGroupSetting(EMGroup mGroup);

		void showGroupUser(List<String> userList);

		void showMoreLayout(boolean show);

		void showMessage(int resourceID);

		void showConfirmDialog(int resourceID, FEMaterialDialog.OnClickListener clickListener);

		void showInputDialog(int resourceID, FEMaterialEditTextDialog.OnClickListener clickListener);

		void openAddUserActivity(List<String> allUser);

		void finish();
	}

	interface IPresenter {

		void messageSilence(boolean block);

		void clearHistory();

		void changeGroupName();

		void changeInvite(boolean isAllow);

		boolean isAllowInvite();

		void loadGroup();

		void leaveGroup();

		void delGroup();

		void getAllUserForServer();

		void addContract(List<AddressBook> addressBooks);

	}
}
