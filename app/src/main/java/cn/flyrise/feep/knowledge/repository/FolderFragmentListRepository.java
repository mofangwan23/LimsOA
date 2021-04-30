package cn.flyrise.feep.knowledge.repository;

import cn.flyrise.android.protocol.entity.BooleanResponse;
import cn.flyrise.android.protocol.entity.knowledge.DeleteFolderAndFileRequest;
import cn.flyrise.android.protocol.entity.knowledge.FolderAndFileListResponse;
import cn.flyrise.android.protocol.entity.knowledge.LoadRootFolderListRequest;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.knowledge.contract.FolderFragmentContract;
import cn.flyrise.feep.knowledge.contract.KnowBaseContract;
import cn.flyrise.feep.knowledge.util.KnowKeyValue;

import static cn.flyrise.feep.core.network.request.ResponseContent.OK_CODE;


/**
 * Created by KLC on 2016/12/6.
 */

public class FolderFragmentListRepository {

    public void loadListData(int page, int folderType, FolderFragmentContract.LoadListCallback callback) {
        LoadRootFolderListRequest request = new LoadRootFolderListRequest(folderType, page, KnowKeyValue.LOADPAGESIZE);
        FEHttpClient.getInstance().post(request, new ResponseCallback<FolderAndFileListResponse>() {
            @Override
            public void onCompleted(FolderAndFileListResponse response) {
                if (response.getErrorCode().equals(OK_CODE)) {
                    FolderAndFileListResponse.Result result = response.getResult();
                    int totalPage = Integer.valueOf(result.getTotalPage());
                    callback.loadListDataSuccess(result.getList(), totalPage, result.firstfolder);
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

    public void deleteFolder(String folderIDs, KnowBaseContract.DealWithCallBack callBack) {
        final DeleteFolderAndFileRequest request = new DeleteFolderAndFileRequest(folderIDs, "");
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
}