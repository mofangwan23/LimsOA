package com.hyphenate.chatui.group.contract;

import com.hyphenate.chatui.group.model.EmUserItem;


import java.util.List;

import cn.flyrise.feep.core.dialog.FEMaterialDialog;

/**
 * Created by klc on 2017/3/15.
 */

public interface GroupUserManageContract {

    interface IView {

        void showRefresh(boolean show);

        void refreshListData();

        void refreshListData(List<EmUserItem> dataList);

        void showConfirmDialog(String msg, FEMaterialDialog.OnClickListener clickListener);

        void hideLoading();

        void showLoading();

        void showMessage(int resourceID);

        void finish();

        void setResult(int code);

        void setDeleteCount(int count);
    }

    interface IPresenter {

        void getAllUser();

        void queryContacts(String query);

        void deleteUser(List<EmUserItem> selectUsers);

        void changeOwner(String userID);
    }
}
