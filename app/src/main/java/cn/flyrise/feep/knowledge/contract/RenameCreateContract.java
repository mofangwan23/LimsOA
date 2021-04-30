package cn.flyrise.feep.knowledge.contract;


import cn.flyrise.feep.core.dialog.FEMaterialEditTextDialog;
import cn.flyrise.feep.knowledge.model.FileAndFolder;

/**
 * Created by KLC on 2016/12/6.
 */

public interface RenameCreateContract {

    interface View extends KnowBaseContract.View {
        void showInputDialog(int titleResourceID, int hintResourceID, String checkBoxText,FEMaterialEditTextDialog.OnClickListener onClickListener);

        void refreshList();

        void refreshListByNet();

        void dealComplete();

        void showErrorMessage(String errorMessage);
    }

    interface Presenter {
        void createFolder(String userID, String parentFolderID, int folderLevel);

        void renameFolder(String parentFolderID, FileAndFolder folder);

        void renameUnitFolder(FileAndFolder folder, int level, String userID);

        void renameFile(FileAndFolder folder);
    }

    interface CreateRenameCallBack {

        void createFolderSuccess();

        void createFolderError();

        void renameSuccess();

        void renameError();

        void renameErrorMessage(String errorMessage);

        void nameExist();
    }
}