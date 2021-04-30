package cn.flyrise.feep.knowledge.repository;

import cn.flyrise.android.protocol.entity.knowledge.GetFileInfoRequest;
import cn.flyrise.android.protocol.entity.knowledge.GetFileInfoResponse;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.knowledge.contract.FileDetailContract;

/**
 * Created by KLC on 2016/12/6.
 */

public class FileDetailRepository {

    public void getFileDetailInfo(String fileID, FileDetailContract.LoadDetailCallBack callBack) {
        GetFileInfoRequest request = new GetFileInfoRequest(fileID);
        FEHttpClient.getInstance().post(request, new ResponseCallback<GetFileInfoResponse>() {
            @Override
            public void onCompleted(GetFileInfoResponse response) {
                if ("0".equals(response.getErrorCode())) {
                    callBack.loadSuccess(response.getResult());
                }
                else
                    callBack.loadError();
            }

            @Override
            public void onFailure(RepositoryException repositoryException) {
                super.onFailure(repositoryException);
                callBack.loadError();
            }
        });

    }
}