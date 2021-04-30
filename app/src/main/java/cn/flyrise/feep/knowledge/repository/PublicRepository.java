package cn.flyrise.feep.knowledge.repository;

import cn.flyrise.android.protocol.entity.BooleanResponse;
import cn.flyrise.android.protocol.entity.knowledge.PublishFileRequest;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.knowledge.contract.KnowBaseContract;

/**
 * Created by KLC on 2016/12/8.
 */

public class PublicRepository {

    public void publishFile(String publicFileIDs, String receiverIDs, String publicUserID, String folderID, String startTime, String endTime, KnowBaseContract.DealWithCallBack callBack) {
        PublishFileRequest request = new PublishFileRequest(publicFileIDs, folderID, receiverIDs, publicUserID, startTime, endTime);
        FEHttpClient.getInstance().post(request, new ResponseCallback<BooleanResponse>() {
            @Override
            public void onCompleted(BooleanResponse response) {
                if (response.isSuccess) {
                    callBack.success();
                }
                else {
                    callBack.fail();
                }
            }

            @Override
            public void onFailure(RepositoryException repositoryException) {
                callBack.fail();
            }
        });
    }

}
