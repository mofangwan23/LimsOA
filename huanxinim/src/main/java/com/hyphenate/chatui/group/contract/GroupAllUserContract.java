package com.hyphenate.chatui.group.contract;


import java.util.List;

import cn.flyrise.feep.core.services.model.AddressBook;


/**
 * Created by klc on 2017/3/15.
 */

public interface GroupAllUserContract {

    interface IView {

        void refreshListData();

        void refreshListData(List<String> dataList);

        void hideLoading();

        void showLoading();


    }

    interface IPresenter {

        void loadGroup();

        void getAllUserForServer();

        List<String> getAllUserList();


        void addContract(List<AddressBook> addressBooks);

        void queryContacts(String query);

    }
}
