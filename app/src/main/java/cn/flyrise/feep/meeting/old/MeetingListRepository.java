package cn.flyrise.feep.meeting.old;

import cn.flyrise.feep.core.common.X.RequestType;
import java.util.ArrayList;

import cn.flyrise.feep.core.base.component.FEListContract;
import cn.flyrise.android.protocol.entity.ListRequest;
import cn.flyrise.android.protocol.entity.ListResponse;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;

/**
 * Created by KLC on 2016/12/27.
 */
public class MeetingListRepository {

    public void requestMeetingList(int page, String pageSize, String searchKey, FEListContract.LoadListCallback callback) {
        if (searchKey == null) {
            searchKey = "";
        }
        final ListRequest listRequest = new ListRequest();
        listRequest.setRequestType(RequestType.Meeting);
        listRequest.setPage(String.valueOf(page));
        listRequest.setSearchKey(searchKey);
        listRequest.setPerPageNums(pageSize);
        listRequest.setOrderBy("");
        listRequest.setOrderType("");
        FEHttpClient.getInstance().post(listRequest, new ResponseCallback<ListResponse>(this) {
            @Override
            public void onCompleted(ListResponse listResponse) {
                if ("0".equals(listResponse.getErrorCode())) {
                    final ArrayList<MeetingListItemBean> itemBeans = DataManager.getTable(listResponse);
                    final int totalNumber = Integer.valueOf(listResponse.getTotalNums());
                    callback.loadListDataSuccess(itemBeans, totalNumber);
                }
                else {
                    onFailure(null);
                }
            }

            @Override
            public void onFailure(RepositoryException repositoryException) {
                callback.loadListDataError();
            }
        });
    }

}
