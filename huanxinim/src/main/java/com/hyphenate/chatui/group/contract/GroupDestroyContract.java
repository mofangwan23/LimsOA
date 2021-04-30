package com.hyphenate.chatui.group.contract;

import cn.flyrise.feep.core.CoreZygote;

/**
 * 新建：陈冕;
 * 日期： 2018-3-2-14:36.
 */

public interface GroupDestroyContract {

    interface IView {
        int SELECTED_GROUP_ID = 1012;//获取群聊id

        void showLoading(boolean show);
    }

    interface IPresenter {

        String FILE_TYPE = ".txt";

        void allHistoryWriteLocal();

        void allHistoryReaderNewGoup();

        void startReaderGroup(String groudId);
    }
}
