package cn.flyrise.feep.knowledge.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.flyrise.android.protocol.entity.BooleanResponse;
import cn.flyrise.android.protocol.entity.knowledge.DeleteFolderAndFileRequest;
import cn.flyrise.android.protocol.entity.knowledge.FolderAndFileListRequest;
import cn.flyrise.android.protocol.entity.knowledge.FolderAndFileListResponse;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.knowledge.contract.FolderFileListContract;
import cn.flyrise.feep.knowledge.contract.KnowBaseContract;
import cn.flyrise.feep.knowledge.model.FileAndFolder;
import cn.flyrise.feep.knowledge.util.KnowKeyValue;

import static cn.flyrise.feep.core.network.request.ResponseContent.OK_CODE;


/**
 * Created by KLC on 2016/12/6.
 */

public class FolderFileListRepository {

    private int mFolderType;
    private Map<String, List<FileAndFolder>> mLocalDataList;

    public FolderFileListRepository(int folderType) {
        this.mFolderType = folderType;
        this.mLocalDataList = new HashMap<>();
    }

    public void loadListData(String folderID, int page, FolderFileListContract.LoadListCallback callback) {
        FolderAndFileListRequest request = new FolderAndFileListRequest(mFolderType, folderID, page, KnowKeyValue.LOADPAGESIZE);
        FEHttpClient.getInstance().post(request, new ResponseCallback<FolderAndFileListResponse>() {
            @Override
            public void onCompleted(FolderAndFileListResponse response) {
                if (response.getErrorCode().equals(OK_CODE)) {
                    int totalPage = Integer.valueOf(response.getResult().getTotalPage());
                    List<FileAndFolder> dataList = response.getResult().getList();
                    callback.loadListDataSuccess(response.getResult().getList(), totalPage);
                    if (page == 1)
                        mLocalDataList.put(folderID, dataList);
                    else
                        mLocalDataList.get(folderID).addAll(dataList);
                }
                else
                    onFailure(null);
            }

            @Override
            public void onFailure(RepositoryException repositoryException) {
                callback.loadListDataError();
            }
        });
    }

    public void deleteFolderAndFile(String fileIDs, String folderIDs, KnowBaseContract.DealWithCallBack callBack) {
        final DeleteFolderAndFileRequest request = new DeleteFolderAndFileRequest(folderIDs, fileIDs);
        FEHttpClient.getInstance().post(request, new ResponseCallback<BooleanResponse>() {
            @Override
            public void onCompleted(BooleanResponse response) {
                if (response.isSuccess) callBack.success();
                else callBack.fail();
            }

            @Override
            public void onFailure(RepositoryException repositoryException) {
                callBack.fail();
            }
        });
    }

    public List<FileAndFolder> getLocalData(String folderID) {
        if (mLocalDataList.containsKey(folderID))
            return mLocalDataList.get(folderID);
        else
            return null;
    }
}