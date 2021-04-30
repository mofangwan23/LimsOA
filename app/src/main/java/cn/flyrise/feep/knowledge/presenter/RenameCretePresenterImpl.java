package cn.flyrise.feep.knowledge.presenter;


import cn.flyrise.feep.R;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.knowledge.contract.RenameCreateContract;
import cn.flyrise.feep.knowledge.model.FileAndFolder;
import cn.flyrise.feep.knowledge.repository.RenameCreateRepository;
import cn.flyrise.feep.knowledge.util.KnowKeyValue;

/**
 * Created by klc on 2016/12/06
 */

class RenameCretePresenterImpl implements RenameCreateContract.Presenter {


    private RenameCreateContract.View mView;
    private int mFolderType;
    private RenameCreateRepository mRepository;

    RenameCretePresenterImpl(RenameCreateContract.View mView, int folderType) {
        this.mView = mView;
        this.mFolderType = folderType;
        this.mRepository = new RenameCreateRepository();
    }

    @Override
    public void createFolder(String userID, String parentFolderID, int folderLevel) {
        boolean isPersonFolder = mFolderType == KnowKeyValue.FOLDERTYPE_PERSON;
        mView.showInputDialog(R.string.know_create_folder,R.string.know_input_folder_name, isPersonFolder?null:CoreZygote.getContext().getString(R.string.knowledge_extends_permission), (dialog, input, check) -> {
            mView.showDealLoading(true);
            if (isPersonFolder) {
                mRepository.createPersonFolder(parentFolderID, input, userID, folderLevel,mFolderType, new CreateAndRenameCallBack());
            }
            else {
                mRepository.createUnitFolder(parentFolderID, input, userID, folderLevel, check,mFolderType, new CreateAndRenameCallBack());
            }
        });
    }

    @Override
    public void renameFolder(String parentFolderID, FileAndFolder folder) {
        mView.showInputDialog(R.string.know_rename, R.string.know_input_folder_name, null, (dialog, input, check) -> {
            mView.showDealLoading(true);
            mRepository.renamePersonFolder(parentFolderID, folder.folderid, input, new CreateAndRenameCallBack(folder, input));
        });
    }

    @Override
    public void renameUnitFolder(FileAndFolder folder, int level, String userID) {
        mView.showInputDialog(R.string.know_rename,R.string.know_input_folder_name, null, (dialog, input, check) -> {
            mView.showDealLoading(true);
            mRepository.renameUnitFolder(folder.folderid, input, String.valueOf(level), userID, mFolderType,new CreateAndRenameCallBack(folder, input));
        });
    }


    @Override
    public void renameFile(FileAndFolder file) {
        mView.showInputDialog(R.string.know_rename,R.string.know_input_file_name, null, (dialog, input, check) -> {
            mView.showDealLoading(true);
            mRepository.renameFile(file.fileid, input, new CreateAndRenameCallBack(file, input));
        });
    }

    private class CreateAndRenameCallBack implements RenameCreateContract.CreateRenameCallBack {

        private FileAndFolder fileAndFolder;
        private String name;

        CreateAndRenameCallBack() {
        }

        CreateAndRenameCallBack(FileAndFolder fileAndFolder, String name) {
            this.fileAndFolder = fileAndFolder;
            this.name = name;
        }

        @Override
        public void createFolderSuccess() {
            mView.showDealLoading(false);
            mView.showMessage(R.string.know_create_folder_success);
            mView.refreshListByNet();
        }

        @Override
        public void createFolderError() {
            mView.showDealLoading(false);
            mView.showMessage(R.string.know_create_folder_error);
        }

        @Override
        public void renameSuccess() {
            mView.showDealLoading(false);
            mView.showMessage(R.string.know_rename_success);
            if (fileAndFolder.isFolder()){
                fileAndFolder.foldername = name;
            }else{
                fileAndFolder.title = name;
            }
            mView.refreshList();
            mView.dealComplete();
        }

        @Override
        public void renameError() {
            mView.showDealLoading(false);
            mView.showMessage(R.string.know_rename_error);
        }

        @Override public void renameErrorMessage(String errorMessage) {
            mView.showDealLoading(false);
            mView.showErrorMessage(errorMessage);
        }

        @Override
        public void nameExist() {
            mView.showDealLoading(false);
            mView.showMessage(R.string.know_folder_exist);
        }
    }
}