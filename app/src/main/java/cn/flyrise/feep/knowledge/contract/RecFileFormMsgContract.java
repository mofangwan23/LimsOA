package cn.flyrise.feep.knowledge.contract;

import java.util.List;

import cn.flyrise.feep.knowledge.model.FileDetail;
import cn.flyrise.feep.knowledge.model.PubAndRecFile;

/**
 * Created by KLC on 2016/12/6.
 */

public interface RecFileFormMsgContract {

    interface View extends KnowBaseContract.View {

        void showRefreshLoading(boolean show);

        void refreshListData(List<PubAndRecFile> dataList);

    }

    interface Presenter {
        void onStart();
    }

    interface LoadListCallback {

        void loadListDataSuccess(List<FileDetail> dataList);

        void loadListDataError();
    }

}