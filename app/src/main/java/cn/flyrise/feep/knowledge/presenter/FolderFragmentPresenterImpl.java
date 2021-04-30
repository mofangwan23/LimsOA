package cn.flyrise.feep.knowledge.presenter;

import android.content.Context;
import cn.flyrise.feep.R;
import cn.flyrise.feep.knowledge.FolderFileListActivity;
import cn.flyrise.feep.knowledge.MoveFileAndFolderActivity;
import cn.flyrise.feep.knowledge.contract.FolderFragmentContract;
import cn.flyrise.feep.knowledge.contract.KnowBaseContract;
import cn.flyrise.feep.knowledge.contract.RenameCreateContract;
import cn.flyrise.feep.knowledge.model.FileAndFolder;
import cn.flyrise.feep.knowledge.model.Folder;
import cn.flyrise.feep.knowledge.model.FolderManager;
import cn.flyrise.feep.knowledge.repository.FolderFragmentListRepository;
import cn.flyrise.feep.knowledge.util.KnowKeyValue;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by KLC on 2016/12/7.
 */

public class FolderFragmentPresenterImpl implements FolderFragmentContract.Presenter {

    private FolderManager mManager;
    private Folder mNowFolder;
    private FolderFragmentContract.View mView;
    private FolderFragmentListRepository mRepository;

    private RenameCretePresenterImpl renamePresenter;

    public FolderFragmentPresenterImpl(FolderManager mManager, FolderFragmentContract.View mView, RenameCreateContract.View renameView) {
        this.mManager = mManager;
        this.mView = mView;
        this.mNowFolder = mManager.nowFolder;
        this.renamePresenter = new RenameCretePresenterImpl(renameView, mManager.folderType);
        this.mRepository = new FolderFragmentListRepository();

    }

    @Override
    public void createFolder(String userID) {
        renamePresenter.createFolder(userID, mNowFolder.id, mNowFolder.level + 1);
    }


    @Override
    public void refreshListData() {
        mView.showRefreshLoading(true);
        mRepository.loadListData(mNowFolder.currentPage = 1, mManager.folderType, new FolderFragmentContract.LoadListCallback() {
            @Override
            public void loadListDataSuccess(List<FileAndFolder> dataList, int totalPage, boolean isFolderManager) {
                mView.showRefreshLoading(false);
                mNowFolder.totalPage = totalPage;
                mManager.isRootFolderManager = isFolderManager;
                setPermission();
                mView.refreshListData(dataList);
                mView.setCanPullUp(hasMoreData());
            }

            @Override
            public void loadListDataError() {
                mView.showRefreshLoading(false);
                mView.setEmptyView();
            }
        });
    }

    @Override
    public void loadMore() {
        mRepository.loadListData(++mNowFolder.currentPage, mManager.folderType, new FolderFragmentContract.LoadListCallback() {
            @Override
            public void loadListDataSuccess(List<FileAndFolder> dataList, int totalPage, boolean isFolderManager) {
                mNowFolder.totalPage = totalPage;
                mView.loadMoreListData(dataList);
                mView.setCanPullUp(hasMoreData());
            }

            @Override
            public void loadListDataError() {
                mView.loadMoreListFail();
                mNowFolder.currentPage--;
            }
        });
    }

    @Override
    public void setPermission() {
        if (mManager.folderType == KnowKeyValue.FOLDERTYPE_PERSON || mManager.isRootFolderManager) {
            mView.setFloatEnable(true);
        }
        else {
            mView.setFloatEnable(false);
        }
    }

    @Override
    public void setPermission(int choiceCount, List<FileAndFolder> foldersList) {
        boolean canRename = true, canMove = true, canDelete = true, canCollection = true;
        mView.setTableEnable(false);
        mView.setFloatEnable(false);
        if (choiceCount == 0) {
            mView.setBottomEnable(false, false, false,false);
            mView.showBottomMenu(false);
            return;
        }
        mView.showBottomMenu(true);
        if (choiceCount > 1) {
            canRename = false;
            canCollection = false;
        }
        if (mManager.folderType == KnowKeyValue.FOLDERTYPE_PERSON) {
            if(foldersList.size()==1){
                FileAndFolder folder = foldersList.get(0);
                if ("个人文档".equals(folder.foldername) || "个人图片".equals(folder.foldername)) {
                    canRename = canMove = canDelete = canCollection = false;
                    mView.setBottomEnable(canRename, canMove, canDelete,canCollection);
                    return;
                }
            }
            mView.setBottomEnable(canRename, canMove, canDelete,canCollection);
            return;
        }
        for (FileAndFolder folder : foldersList) {
            if (!folder.canManage) {
                canDelete = false;
                canMove = false;
                canRename = false;
                canCollection = false;
            }
        }
        mView.setBottomEnable(canRename, canMove, canDelete,canCollection);
    }


    @Override
    public void openFolder(Context context, FileAndFolder clickFolder) {
        Folder folder;
        if (mManager.folderType == KnowKeyValue.FOLDERTYPE_PERSON) {
            folder = Folder.CreatePersonFolder(null, clickFolder.folderid, clickFolder.foldername, mNowFolder.level + 1);
            if ("个人图片".equals(clickFolder.foldername)) {
                folder.isPicFolder = true;
            }
        }
        else {
            folder = Folder.CreateUnitFolder(null, clickFolder.folderid, clickFolder.foldername, mNowFolder.level + 1, clickFolder.rightPower, clickFolder.canManage);
        }
        FolderFileListActivity.startChildFileListActivity(context, new FolderManager(mManager.folderType, mManager.isRootFolderManager, folder));
    }

    @Override
    public void renameFolder(String userID, List<FileAndFolder> dataList) {
        dataList = getChoiceItem(dataList);
        FileAndFolder clickItem = dataList.get(0);
        if (clickItem.isFolder()) {
            if (mManager.folderType == KnowKeyValue.FOLDERTYPE_PERSON) {
                renamePresenter.renameFolder(mNowFolder.id, clickItem);
            }
            else {
                renamePresenter.renameUnitFolder(clickItem, mNowFolder.level + 1, userID);
            }
        }
    }


    @Override
    public void moveFolderAndFile(Context context, List<FileAndFolder> dataList) {
        dataList = getChoiceItem(dataList);
        StringBuilder folders = new StringBuilder();
        for (FileAndFolder item : dataList) {
            folders.append(",").append(item.folderid);
        }
        folders.deleteCharAt(0);
        String moveFolders = folders.toString();
        MoveFileAndFolderActivity.startMoveActivity(context, mNowFolder.id, moveFolders, null, null, mManager);
    }


    @Override
    public void deleteFolder(List<FileAndFolder> folders) {
        folders = getChoiceItem(folders);
        StringBuilder foldIDs = new StringBuilder();
        if(folders==null || folders.size()==0){
            return;
        }
        for (FileAndFolder item : folders) {
            if(!"个人文档".equals(item.foldername) && !"个人图片".equals(item.foldername)){
                foldIDs.append(item.folderid).append(",");
            }
        }
        mView.showConfirmDialog(R.string.know_delete_file_or_folder, dialog -> {
            mView.showDealLoading(true);
            mRepository.deleteFolder(foldIDs.toString(), new KnowBaseContract.DealWithCallBack() {
                @Override
                public void success() {
                    mView.showDealLoading(false);
                    mView.showMessage(R.string.delete_success);
                    mView.dealComplete();
                    refreshListData();
                }

                @Override
                public void fail() {
                    mView.showDealLoading(false);
                    mView.showMessage(R.string.delete_fail);
                }
            });
        });
    }


    private List<FileAndFolder> getChoiceItem(List<FileAndFolder> dataList) {
        List<FileAndFolder> choiceList = new ArrayList<>();
        for (FileAndFolder folder : dataList) {
            if (folder.isChoice) {
                choiceList.add(folder);
            }
        }
        return choiceList;
    }


    @Override
    public boolean hasMoreData() {
        return mNowFolder.currentPage < mNowFolder.totalPage;
    }

}
