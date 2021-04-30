package com.hyphenate.chatui.group.contract;

import cn.flyrise.feep.core.base.component.FEListContract;
import cn.flyrise.feep.core.services.model.AddressBook;
import com.hyphenate.chat.EMGroup;
import java.util.List;

/**
 * Created by klc on 2017/3/29.
 */

public interface GroupListContract {

	interface IView extends FEListContract.View<EMGroup> {

		void hideLoading();

		void showLoading();

		void startChatActivity(EMGroup emGroup);

		String getSearchKey();
	}

	interface IPresenter extends FEListContract.Presenter {

		void createNewGroup(List<AddressBook> addressBooks);

		void searchGroup(String groupName);
	}

}
