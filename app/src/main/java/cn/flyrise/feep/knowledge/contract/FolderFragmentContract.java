package cn.flyrise.feep.knowledge.contract;


import android.content.Context;

import java.util.List;

import cn.flyrise.feep.knowledge.model.FileAndFolder;
import cn.flyrise.feep.core.dialog.FEMaterialDialog;

/**
 * Created by KLC on 2016/12/6.
 */

public interface FolderFragmentContract {

    interface View extends ListContract.View<FileAndFolder> {

        void showConfirmDialog(int resourceID, FEMaterialDialog.OnClickListener onClickListener);

        void showBottomMenu(boolean isShowBottomMenu);

        void setBottomEnable(boolean canRename, boolean canMove, boolean canDelete,boolean canCollection);

        void setFloatEnable(boolean canCreate);

        void setTableEnable(boolean enable);

    }

    interface Presenter {
        void createFolder(String userID);

        void refreshListData();

        void loadMore();

        void setPermission();

        void setPermission(int choiceCount, List<FileAndFolder> foldersList);

        void openFolder(Context context, FileAndFolder clickFolder);

        void renameFolder(String userID, List<FileAndFolder> dataList);

        void moveFolderAndFile(Context context, List<FileAndFolder> dataList);

        void deleteFolder(List<FileAndFolder> folders);

        boolean hasMoreData();
    }

    interface LoadListCallback {

        void loadListDataSuccess(List<FileAndFolder> dataList, int totalPage, boolean isFolderManager);

        void loadListDataError();
    }

}