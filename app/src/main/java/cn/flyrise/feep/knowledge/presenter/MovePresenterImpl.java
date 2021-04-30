package cn.flyrise.feep.knowledge.presenter;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.flyrise.feep.R;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.knowledge.contract.FolderFileListContract;
import cn.flyrise.feep.knowledge.contract.MoveContract;
import cn.flyrise.feep.knowledge.contract.RenameCreateContract;
import cn.flyrise.feep.knowledge.model.Folder;
import cn.flyrise.feep.knowledge.model.FolderManager;
import cn.flyrise.feep.knowledge.repository.MoveRepository;
import cn.flyrise.feep.knowledge.util.KnowKeyValue;
import cn.flyrise.feep.knowledge.util.KnowledgeUtil;

/**
 * Created by KLC on 2016/12/8.
 */

public class MovePresenterImpl implements MoveContract.Presenter {

    private MoveContract.View mView;
    private Folder mNowFolder;
    private String moveItemParentID;
    private String moveFolderIDs;
    private String moveFileIDs;
    private ArrayList<String> mMoveFileType;
    private FolderManager folderManager;
    private StringBuilder mPath;
    private Map<String, Folder> folderMap;
    private boolean isPersonFolder;
    private MoveRepository moveRepository;
    private RenameCretePresenterImpl cretePresenter;

    public MovePresenterImpl(MoveContract.View view, RenameCreateContract.View createView, FolderManager folderManager, String moveItemParentID, String moveFolderIDs, String moveFileIDs, ArrayList<String> fileType) {
        this.mView = view;
        this.folderManager = folderManager;
        this.moveItemParentID = moveItemParentID;
        this.moveFolderIDs = moveFolderIDs;
        this.moveFileIDs = moveFileIDs;
        this.mNowFolder = folderManager.nowFolder;
        this.mNowFolder.canManage = folderManager.isRootFolderManager;
        this.folderMap = new HashMap<>();
        this.folderMap.put(this.mNowFolder.id, this.mNowFolder);
        this.moveRepository = new MoveRepository();
        this.cretePresenter = new RenameCretePresenterImpl(createView, folderManager.folderType);
        this.mMoveFileType = fileType;
        this.isPersonFolder = folderManager.folderType == KnowKeyValue.FOLDERTYPE_PERSON;
    }

    @Override
    public void start() {
        mPath = new StringBuilder(mNowFolder.name + "\\");
        refreshListData();
        mView.setPathText(mPath.toString());
        mView.setButtonEnable(!mNowFolder.id.equals(moveItemParentID), isPersonFolder || mNowFolder.canManage);
    }

    @Override
    public void refreshListData() {
        mView.showRefreshLoading(true);
        moveRepository.getPersonTree(mNowFolder.id, folderManager.folderType, loadListCallBack);
    }

    @Override
    public void moveFileAndFolder() {
        if (!TextUtils.isEmpty(moveFileIDs) && mNowFolder.parentFolderID == null) {
            mView.showMessage(R.string.know_can_not_move);
            return;
        }
        mView.showDealLoading(true);
        if (folderManager.folderType != KnowKeyValue.FOLDERTYPE_PERSON || mNowFolder.parentFolderID == null) {
            toMoveFileAndFolder();
            return;
        }
        moveRepository.getFolderType(mNowFolder.id, new FolderFileListContract.FolderTypeCallBack() {
            @Override
            public void callBack(boolean isPic, boolean isDoc) {
                if (isPic) {
                    if (checkPicFileType()) {
                        moveRepository.checkIsPicType(moveFolderIDs, hasNotPicOrDocCallBack);
                    }
                    else {
                        hasNotPicOrDocCallBack.hasNotPicType();
                    }
                }
                else {
                    toMoveFileAndFolder();
                }
            }

            @Override
            public void onError() {
                moveCallBack.onError();
            }
        });
    }


    private void toMoveFileAndFolder() {
        if (TextUtils.isEmpty(moveFileIDs)) moveFileIDs = "";
        if (TextUtils.isEmpty(moveFolderIDs)) {
            moveRepository.toMoveFileAndFolder(mNowFolder.id, moveFolderIDs, moveFileIDs, moveCallBack);
        }
        else {
            moveRepository.toCheckFolderNameEqual(mNowFolder.id, moveFolderIDs, moveFileIDs, null, moveCallBack);
        }
    }


    private boolean checkPicFileType() {
        if (CommonUtil.isEmptyList(mMoveFileType)) return true;
        for (String fileType : mMoveFileType) {
            if (!KnowledgeUtil.isPicType(fileType)) {
                return false;
            }
        }
        return true;
    }

    private boolean checkDocFileType() {
        if (CommonUtil.isEmptyList(mMoveFileType)) return true;
        for (String fileType : mMoveFileType) {
            if (!KnowledgeUtil.isDocType(fileType)) {
                return false;
            }
        }
        return true;
    }


    @Override
    public void openFolder(String folderID, String folderName) {
        mPath.append(folderName).append('\\');
        mNowFolder = new Folder(mNowFolder.id, folderID, folderName, mNowFolder.canManage, mNowFolder.level + 1);
        folderMap.put(mNowFolder.id, mNowFolder);
        mView.setPathText(mPath.toString());
        mView.setButtonEnable(!folderID.equals(moveItemParentID), isPersonFolder || mNowFolder.canManage);
        loadChildData();
    }

    @Override
    public void backToParent() {
        if (mNowFolder.parentFolderID == null) {
            mView.finish();
        }
        else {
            mPath.setLength(mPath.length() - mNowFolder.name.length() - 1);
            mNowFolder = folderMap.get(mNowFolder.parentFolderID);
            mView.setPathText(mPath.toString());
            loadChildData();
            mView.setButtonEnable(!mNowFolder.id.equals(moveItemParentID), isPersonFolder || mNowFolder.canManage);
        }
    }

    private void loadChildData() {
        List<Folder> dataList = moveRepository.getChildFoldersById(mNowFolder.id);
        if (mNowFolder.id.equals(moveItemParentID) && !TextUtils.isEmpty(moveFolderIDs)) {
            removeFolder(dataList);
        }
        mView.refreshListData(dataList);
    }

    @Override
    public void createFolder() {
        cretePresenter.createFolder(CoreZygote.getLoginUserServices().getUserId(), mNowFolder.id, mNowFolder.level + 1);
    }


    private MoveRepository.LoadListCallBack loadListCallBack = new MoveRepository.LoadListCallBack() {
        @Override
        public void loadSuccess(List<Folder> folderList) {
            mView.showRefreshLoading(false);
            if (mNowFolder.id.equals(moveItemParentID) && !TextUtils.isEmpty(moveFolderIDs)) {
                removeFolder(folderList);
            }
            mView.refreshListData(folderList);
        }

        @Override
        public void onDataNotAvailable() {
            mView.showRefreshLoading(false);
            mView.setEmptyView();
        }
    };

    private void removeFolder(List<Folder> data) {
        String[] moveIDs = moveFolderIDs.split(",");
        for (String id : moveIDs) {
            for (Folder folder : data) {
                if (folder.id.equals(id)) {
                    data.remove(folder);
                    break;
                }
            }
        }
    }

    private MoveRepository.MoveCallBack moveCallBack = new MoveRepository.MoveCallBack() {
        @Override
        public void onSuccess() {
            mView.showDealLoading(false);
            mView.showMessage(R.string.know_move_success);
            mView.dealComplete();
        }

        @Override
        public void onError() {
            mView.showDealLoading(false);
            mView.showMessage(R.string.know_move_error);
        }

        @Override
        public void onExistEqualFolder() {
            mView.showDealLoading(false);
            mView.showMessage(R.string.know_folder_exist);
        }

    };

    private MoveRepository.IsPicOrDocCallBack hasNotPicOrDocCallBack = new MoveRepository.IsPicOrDocCallBack() {
        @Override
        public void hasNotDocType() {
            mView.showDealLoading(false);
            mView.showMessage(R.string.know_has_notdoc);
        }

        @Override
        public void hasNotPicType() {
            mView.showDealLoading(false);
            mView.showMessage(R.string.know_has_notpic);
        }

        @Override
        public void success() {
            toMoveFileAndFolder();
        }

        @Override
        public void error() {
            mView.showDealLoading(false);
            mView.showMessage(R.string.know_move_error);
        }
    };

}
