package cn.flyrise.feep.knowledge.repository;


import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.flyrise.android.protocol.entity.BooleanResponse;
import cn.flyrise.android.protocol.entity.knowledge.FolderEqualRequest;
import cn.flyrise.android.protocol.entity.knowledge.FolderEqualResponse;
import cn.flyrise.android.protocol.entity.knowledge.FolderTreeRequest;
import cn.flyrise.android.protocol.entity.knowledge.FolderTreeResponse;
import cn.flyrise.android.protocol.entity.knowledge.FolderTypeRequest;
import cn.flyrise.android.protocol.entity.knowledge.FolderTypeResponse;
import cn.flyrise.android.protocol.entity.knowledge.IsDocTypeRequest;
import cn.flyrise.android.protocol.entity.knowledge.IsPicOrDocResponse;
import cn.flyrise.android.protocol.entity.knowledge.IsPicTypeRequest;
import cn.flyrise.android.protocol.entity.knowledge.MoveFolderAndFileRequest;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.knowledge.contract.FolderFileListContract;
import cn.flyrise.feep.knowledge.model.Folder;
import cn.flyrise.feep.knowledge.util.KnowKeyValue;

/**
 * Created by KLC on 2016/12/8.
 */

public class MoveRepository {

    private Map<String, List<Folder>> mMapTree;

    public MoveRepository() {
        mMapTree = new HashMap<>();
    }

    public void getPersonTree(String folderID, int folderType, LoadListCallBack callBack) {
        FolderTreeRequest request = new FolderTreeRequest(folderType);
        FEHttpClient.getInstance().post(request, new ResponseCallback<FolderTreeResponse>() {
            @Override
            public void onCompleted(FolderTreeResponse response) {
                List<FolderTreeResponse.FolderTree> tree = response.getResult();
                if (folderType == KnowKeyValue.FOLDERTYPE_PERSON)
                    mMapTree = toMapTree(KnowKeyValue.PERSONROOTFOLDERID, tree);
                else if (folderType == KnowKeyValue.FOLDERTYPE_UNIT)
                    mMapTree = toMapTree(KnowKeyValue.UNITROOTFOLDERID, tree);
                else
                    mMapTree = toMapTree(KnowKeyValue.GROUPROOTFOLDERID, tree);
                List<Folder> dataList = mMapTree.get(folderID);
                if (CommonUtil.isEmptyList(dataList)) {
                    callBack.onDataNotAvailable();
                }
                else
                    callBack.loadSuccess(dataList);
            }

            @Override
            public void onFailure(RepositoryException repositoryException) {
                callBack.onDataNotAvailable();
            }
        });
    }

    private Map<String, List<Folder>> toMapTree(String parentId, List<FolderTreeResponse.FolderTree> tree) {
        Map<String, List<Folder>> parentTree = new HashMap<>();
        List<Folder> childTree = new ArrayList<>();
        for (FolderTreeResponse.FolderTree folderTree : tree) {
            if (folderTree.list != null) {
                parentTree.putAll(toMapTree(folderTree.id, folderTree.list));
            }
            childTree.add(new Folder(folderTree.id, folderTree.name, folderTree.canManage));
        }
        parentTree.put(parentId, childTree);
        return parentTree;
    }

    public List<Folder> getChildFoldersById(String parentFolderId) {
        if (mMapTree == null)
            return null;
        else {
            return mMapTree.get(parentFolderId);
        }
    }

    public void toCheckFolderNameEqual(String parentId, String folderIds, String fileIds, String folderType, MoveCallBack callBack) {
        FolderEqualRequest request = new FolderEqualRequest(parentId, folderIds, folderType);
        FEHttpClient.getInstance().post(request, new ResponseCallback<FolderEqualResponse>() {
            @Override
            public void onCompleted(FolderEqualResponse responseContent) {
                if (TextUtils.isEmpty(responseContent.getResult())) {
                    toMoveFileAndFolder(parentId, folderIds, fileIds, callBack);
                }
                else {
                    callBack.onExistEqualFolder();
                }
            }

            @Override
            public void onFailure(RepositoryException repositoryException) {
                super.onFailure(repositoryException);
                callBack.onError();
            }
        });
    }

    public void toMoveFileAndFolder(String moveToFolderID, String folderIds, String fileIds, MoveCallBack callBack) {
        MoveFolderAndFileRequest request = new MoveFolderAndFileRequest(moveToFolderID, folderIds, fileIds);
        FEHttpClient.getInstance().post(request, new ResponseCallback<BooleanResponse>() {
            @Override
            public void onCompleted(BooleanResponse booleanResponse) {
                if (booleanResponse.isSuccess)
                    callBack.onSuccess();
                else
                    callBack.onError();
            }

            @Override
            public void onFailure(RepositoryException repositoryException) {
                super.onFailure(repositoryException);
                callBack.onError();
            }
        });
    }

    public void getFolderType(String folderID, FolderFileListContract.FolderTypeCallBack callBack) {
        final FolderTypeRequest request = new FolderTypeRequest(folderID);
        FEHttpClient.getInstance().post(request, new ResponseCallback<FolderTypeResponse>() {
            @Override
            public void onCompleted(FolderTypeResponse response) {
                if ("0".equals(response.getErrorCode())) {
                    callBack.callBack(response.result.isPic, response.result.isDoc);
                }
            }
        });
    }

    public void checkIsDocType(String folderIDs, IsPicOrDocCallBack callBack) {
        IsDocTypeRequest request = new IsDocTypeRequest(folderIDs);
        FEHttpClient.getInstance().post(request, new ResponseCallback<IsPicOrDocResponse>() {
            @Override
            public void onCompleted(IsPicOrDocResponse isPicOrDocResponse) {
                if ("0".equals(isPicOrDocResponse.getErrorCode())) {
                    if (isPicOrDocResponse.result.length == 0) callBack.success();
                    else callBack.hasNotDocType();
                }
                else {
                    callBack.error();
                }
            }

            @Override
            public void onFailure(RepositoryException repositoryException) {
                super.onFailure(repositoryException);
                callBack.error();
            }
        });
    }

    public void checkIsPicType(String folderIDs, IsPicOrDocCallBack callBack) {
        IsPicTypeRequest request = new IsPicTypeRequest(folderIDs);
        FEHttpClient.getInstance().post(request, new ResponseCallback<IsPicOrDocResponse>() {
            @Override
            public void onCompleted(IsPicOrDocResponse isPicOrDocResponse) {
                if ("0".equals(isPicOrDocResponse.getErrorCode())) {
                    if (isPicOrDocResponse.result.length == 0) callBack.success();
                    else callBack.hasNotPicType();
                }
                else {
                    callBack.error();
                }
            }

            @Override
            public void onFailure(RepositoryException repositoryException) {
                super.onFailure(repositoryException);
                callBack.error();
            }
        });
    }


    public interface LoadListCallBack {
        void loadSuccess(List<Folder> folderList);

        void onDataNotAvailable();
    }

    public interface MoveCallBack {
        void onSuccess();

        void onError();

        void onExistEqualFolder();
    }

    public interface IsPicOrDocCallBack {
        void hasNotDocType();

        void hasNotPicType();

        void success();

        void error();
    }
}
