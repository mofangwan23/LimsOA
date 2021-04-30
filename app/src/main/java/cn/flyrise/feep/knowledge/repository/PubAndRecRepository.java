package cn.flyrise.feep.knowledge.repository;

import java.util.List;

import cn.flyrise.android.protocol.entity.BooleanResponse;
import cn.flyrise.android.protocol.entity.knowledge.CancelPublishRequest;
import cn.flyrise.android.protocol.entity.knowledge.GetReceiveListForMsgRequest;
import cn.flyrise.android.protocol.entity.knowledge.GetReceiveListForMsgResponse;
import cn.flyrise.android.protocol.entity.knowledge.PublishAndReceiveListRequest;
import cn.flyrise.android.protocol.entity.knowledge.PublishAndReceiveListResponse;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.knowledge.contract.KnowBaseContract;
import cn.flyrise.feep.knowledge.contract.PubAndRecListContract;
import cn.flyrise.feep.knowledge.model.FileDetail;
import cn.flyrise.feep.knowledge.util.KnowKeyValue;

/**
 * Created by KLC on 2016/12/8.
 */

public class PubAndRecRepository {

    public void getPubOrRecList(int listType, int nowPage, PubAndRecListContract.LoadListCallback callback) {
        PublishAndReceiveListRequest request = new PublishAndReceiveListRequest(listType, nowPage, KnowKeyValue.LOADPAGESIZE);
        FEHttpClient.getInstance().post(request, new ResponseCallback<PublishAndReceiveListResponse>() {
            @Override
            public void onCompleted(PublishAndReceiveListResponse response) {
                int totalPage = response.getResult().getTotalPage();
                callback.loadListDataSuccess(response.getResult().getList(), totalPage);
            }

            @Override
            public void onFailure(RepositoryException repositoryException) {
                super.onFailure(repositoryException);
                callback.loadListDataError();
            }
        });
    }

    public void getRecListForMsgID(String msgID, PubAndRecListContract.LoadListCallback callback) {
        GetReceiveListForMsgRequest request = new GetReceiveListForMsgRequest(msgID);
        FEHttpClient.getInstance().post(request, new ResponseCallback<GetReceiveListForMsgResponse>() {
            @Override
            public void onCompleted(GetReceiveListForMsgResponse response) {
                List<FileDetail> data = response.getList();
                callback.loadListDataSuccess(data, 1);
            }

            @Override
            public void onFailure(RepositoryException repositoryException) {
                super.onFailure(repositoryException);
                callback.loadListDataError();
            }
        });
    }

    public void cancelPublish(String selectIDs, KnowBaseContract.DealWithCallBack callBack) {
        CancelPublishRequest request = new CancelPublishRequest(selectIDs);
        FEHttpClient.getInstance().post(request, new ResponseCallback<BooleanResponse>() {
            @Override
            public void onCompleted(BooleanResponse booleanResponse) {
                if (booleanResponse.isSuccess) {
                    callBack.success();
                }
                else {
                    callBack.fail();
                }
            }

            @Override
            public void onFailure(RepositoryException repositoryException) {
                super.onFailure(repositoryException);
                callBack.fail();
            }
        });
    }

}
