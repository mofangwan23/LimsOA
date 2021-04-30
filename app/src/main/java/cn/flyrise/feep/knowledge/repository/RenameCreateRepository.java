package cn.flyrise.feep.knowledge.repository;

import android.text.TextUtils;
import cn.flyrise.android.protocol.entity.BooleanResponse;
import cn.flyrise.android.protocol.entity.knowledge.CheckNameExistForRenameUnitRequest;
import cn.flyrise.android.protocol.entity.knowledge.CheckNameExistRequest;
import cn.flyrise.android.protocol.entity.knowledge.CreatePersonFolderRequest;
import cn.flyrise.android.protocol.entity.knowledge.CreateUnitFolderRequest;
import cn.flyrise.android.protocol.entity.knowledge.RenameFileRequest;
import cn.flyrise.android.protocol.entity.knowledge.RenameFileResponse;
import cn.flyrise.android.protocol.entity.knowledge.RenamePersonFolderRequest;
import cn.flyrise.android.protocol.entity.knowledge.RenameUnitFolderRequest;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.knowledge.contract.RenameCreateContract;
import cn.flyrise.feep.knowledge.util.KnowKeyValue;

/**
 * Created by KLC on 2016/12/6.
 */

public class RenameCreateRepository {

    public void createPersonFolder(String parentFolderID, String folderName, String userID, int folderLevel, int folderType, RenameCreateContract.CreateRenameCallBack callBack) {
        checkNameExist(parentFolderID, folderName, folderType, new FolderExistCallBack() {
            @Override
            public void exist() {
                callBack.nameExist();
            }

            @Override
            public void notExist() {
                toCreatePersonFolder(parentFolderID, folderName, userID, String.valueOf(folderLevel), callBack);
            }

            @Override
            public void error() {
                callBack.createFolderError();
            }

            @Override public void errorMessage(String errorMessage) {
                callBack.renameErrorMessage(errorMessage);
            }
        });
    }

    public void createUnitFolder(String parentFolderID, String folderName, String userID, int folderLevel, boolean isInherit, int folderType, RenameCreateContract.CreateRenameCallBack callBack) {
        checkNameExist(parentFolderID, folderName, folderType, new FolderExistCallBack() {
            @Override
            public void exist() {
                callBack.nameExist();
            }

            @Override
            public void notExist() {
                toCreateUnitFolder(parentFolderID, folderName, userID, String.valueOf(folderLevel), isInherit, callBack);
            }

            @Override
            public void error() {
                callBack.createFolderError();
            }

            @Override public void errorMessage(String errorMessage) {
                callBack.renameErrorMessage(errorMessage);
            }
        });
    }

    private void toCreatePersonFolder(String parentFolderID, String folderName, String userID, String level, RenameCreateContract.CreateRenameCallBack callBack) {
        final CreatePersonFolderRequest request = new CreatePersonFolderRequest(parentFolderID, folderName, level, userID);
        FEHttpClient.getInstance().post(request, new ResponseCallback<BooleanResponse>() {
            @Override
            public void onCompleted(BooleanResponse response) {
                if (response.isSuccess) callBack.createFolderSuccess();
                else callBack.createFolderError();
            }

            @Override
            public void onFailure(RepositoryException repositoryException) {
                super.onFailure(repositoryException);
                callBack.createFolderError();
            }
        });
    }

    private void toCreateUnitFolder(String parentFolderID, String folderName, String userID, String level, boolean isInherit, RenameCreateContract.CreateRenameCallBack callBack) {
        final CreateUnitFolderRequest request = new CreateUnitFolderRequest(parentFolderID, folderName, level, userID, isInherit);
        FEHttpClient.getInstance().post(request, new ResponseCallback<BooleanResponse>() {
            @Override
            public void onCompleted(BooleanResponse response) {
                if (response.isSuccess) callBack.createFolderSuccess();
                else callBack.createFolderError();
            }

            @Override
            public void onFailure(RepositoryException repositoryException) {
                super.onFailure(repositoryException);
                callBack.createFolderError();
            }
        });
    }


    private void checkNameExist(String parentFolderID, String folderName, int folderType, FolderExistCallBack callBack) {
        final CheckNameExistRequest request = new CheckNameExistRequest(parentFolderID, folderName, folderType);
        FEHttpClient.getInstance().post(request, new ResponseCallback<BooleanResponse>() {
            @Override
            public void onCompleted(BooleanResponse response) {
                if (response.isSuccess && TextUtils.equals("0",response.getErrorCode())) {
                    callBack.exist();
                }else if(!TextUtils.isEmpty(response.getErrorMessage())){
                    callBack.errorMessage(response.getErrorMessage());
                }else {
                    callBack.notExist();
                }
            }

            @Override
            public void onFailure(RepositoryException repositoryException) {
                callBack.error();
            }
        });
    }


    /***************************************
     * 重命名个人文件夹
     ****************************************************/
    public void renamePersonFolder(String parentFolderID, String folderId, String folderName, RenameCreateContract.CreateRenameCallBack callBack) {
        checkNameExist(parentFolderID, folderName, KnowKeyValue.FOLDERTYPE_PERSON, new FolderExistCallBack() {
            @Override
            public void exist() {
                callBack.nameExist();
            }

            @Override
            public void notExist() {
                toRenamePersonFolder(folderId, folderName, callBack);
            }

            @Override
            public void error() {
                callBack.renameError();
            }

            @Override public void errorMessage(String errorMessage) {
                callBack.renameErrorMessage(errorMessage);
            }
        });
    }

    private void toRenamePersonFolder(String folderID, final String folderName, RenameCreateContract.CreateRenameCallBack callBack) {
        final RenamePersonFolderRequest request = new RenamePersonFolderRequest(folderID, folderName);
        FEHttpClient.getInstance().post(request, new ResponseCallback<BooleanResponse>() {
            @Override
            public void onCompleted(BooleanResponse response) {
                if (TextUtils.equals("0",response.getErrorCode()) && response.isSuccess){
                    callBack.renameSuccess();
                }else if(!TextUtils.isEmpty(response.getErrorMessage())){
                    callBack.renameErrorMessage(response.getErrorMessage());
                }else {
                    callBack.renameError();
                }
            }

            @Override
            public void onFailure(RepositoryException repositoryException) {
                callBack.renameError();
            }
        });
    }


    /***************************************
     * 重命名单位文件夹
     ****************************************************/
    public void renameUnitFolder(String folderId, String folderName, String level, String userID, int folderType, RenameCreateContract.CreateRenameCallBack callBack) {
        checkUnitFolderExist(folderId, folderName, folderType, new FolderExistCallBack() {
            @Override
            public void exist() {
                callBack.nameExist();
            }

            @Override
            public void notExist() {
                toRenameUnitFolder(folderId, folderName, level, userID, callBack);
            }

            @Override
            public void error() {
                callBack.renameError();
            }

            @Override public void errorMessage(String errorMessage) {
                callBack.renameErrorMessage(errorMessage);
            }
        });
    }


    private void toRenameUnitFolder(String folderID, final String folderName, String level, String userID, RenameCreateContract.CreateRenameCallBack callBack) {
        final RenameUnitFolderRequest request = new RenameUnitFolderRequest(folderID, folderName, level, userID);
        FEHttpClient.getInstance().post(request, new ResponseCallback<BooleanResponse>() {
            @Override
            public void onCompleted(BooleanResponse response) {
                if (TextUtils.equals("0",response.getErrorCode()) && response.isSuccess){
                    callBack.renameSuccess();
                }else if(!TextUtils.isEmpty(response.getErrorMessage())){
                    callBack.renameErrorMessage(response.getErrorMessage());
                }else {
                    callBack.renameError();
                }
            }

            @Override
            public void onFailure(RepositoryException repositoryException) {
                callBack.renameError();
            }
        });
    }


    private void checkUnitFolderExist(String folderID, String folderName, int folderType, FolderExistCallBack callBack) {
        final CheckNameExistForRenameUnitRequest request = new CheckNameExistForRenameUnitRequest(folderID, folderName, folderType);
        FEHttpClient.getInstance().post(request, new ResponseCallback<BooleanResponse>() {
            @Override
            public void onCompleted(BooleanResponse response) {
                if (response.isSuccess) callBack.exist();
                else callBack.notExist();
            }

            @Override
            public void onFailure(RepositoryException repositoryException) {
                callBack.error();
            }
        });
    }


    public void renameFile(String fileID, String name, RenameCreateContract.CreateRenameCallBack callBack) {
        RenameFileRequest request = new RenameFileRequest(fileID, name);
        FEHttpClient.getInstance().post(request, new ResponseCallback<RenameFileResponse>() {
            @Override
            public void onCompleted(RenameFileResponse response) {

                if (TextUtils.equals("0",response.getErrorCode()) && response.getResult() == 1){
                    callBack.renameSuccess();
                }else if(!TextUtils.isEmpty(response.getErrorMessage())){
                    callBack.renameErrorMessage(response.getErrorMessage());
                }else {
                    callBack.renameError();
                }
            }

            @Override
            public void onFailure(RepositoryException repositoryException) {
                callBack.renameError();
            }
        });
    }

    interface FolderExistCallBack {

        void exist();

        void notExist();

        void error();

        void errorMessage(String errorMessage);
    }
}