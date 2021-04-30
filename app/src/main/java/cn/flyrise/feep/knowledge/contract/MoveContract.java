package cn.flyrise.feep.knowledge.contract;

import java.util.List;

import cn.flyrise.feep.knowledge.model.Folder;

/**
 * Created by KLC on 2016/12/6.
 */

public interface MoveContract {

    interface View extends KnowBaseContract.View {

        void showRefreshLoading(boolean show);

        void refreshListData(List<Folder> dataList);

        void setEmptyView();

        void dealComplete();

        void setPathText(String pathText);

        void setButtonEnable(boolean canMove, boolean canCreate);

        void finish();

    }

    interface Presenter {
        void start();

        void refreshListData();

        void moveFileAndFolder();

        void openFolder(String folderID, String folderName);

        void backToParent();

        void createFolder();
    }

}