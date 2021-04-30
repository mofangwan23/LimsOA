package cn.flyrise.feep.robot.module;

import android.content.Context;
import cn.flyrise.feep.core.base.component.FEListContract;
import cn.flyrise.feep.core.services.model.AddressBook;
import cn.flyrise.feep.robot.contract.RobotDataLoaderContract;

/**
 * cm
 * 请求会议列表
 */
public class FeMeetingListLoadData implements RobotDataLoaderContract {

    public void requestMeetingList(String searchKey, FEListContract.LoadListCallback callback) {
//        if (searchKey == null) {
//            searchKey = "";
//        }
//        final ListRequest listRequest = new ListRequest();
//        listRequest.setRequestType(RequestType.Meeting);
//        listRequest.setPage(page + "");
//        listRequest.setSearchKey(searchKey);
//        listRequest.setPerPageNums(perPageNums + "");
//        listRequest.setOrderBy("");
//        listRequest.setOrderType("");
//        FEHttpClient.getInstance().post(listRequest, new ResponseCallback<ListResponse>(this) {
//            @Override
//            public void onCompleted(ListResponse listResponse) {
//                if ("0".equals(listResponse.getErrorCode())) {
//                    final ArrayList<MeetingListItemBean> itemBeans = DataManager.getTable(listResponse);
//                    final int totalNumber = Integer.valueOf(listResponse.getTotalNums());
//                    callback.loadListDataSuccess(itemBeans, totalNumber);
//                } else {
//                    onFailure(null);
//                }
//            }
//
//            @Override
//            public void onFailure(RepositoryException repositoryException) {
//                callback.loadListDataError();
//            }
//        });
    }

    @Override
    public void setContext(Context context) {

    }

    @Override
    public void setListener(FeSearchMessageListener listener) {

    }

    @Override
    public void setRequestType(int requestType) {

    }

    @Override
    public void setAddressBoook(AddressBook addressBoook) {

    }

    @Override
    public void requestMessageList(String searchKey) {

    }

    @Override
    public void requestWorkPlanList(String userID) {

    }
}
