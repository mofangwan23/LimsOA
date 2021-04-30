package cn.flyrise.feep.report;


import java.util.List;

import cn.flyrise.feep.core.base.component.FEListContract;
import cn.flyrise.android.protocol.entity.ReportListRequest;
import cn.flyrise.android.protocol.entity.ReportListResponse;
import cn.flyrise.android.protocol.model.ReportListItem;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;

/**
 * Created by Klc on 2017/1/13.
 */

class ReportListRepository {

    void requestReportList(int page, int perPageNum, FEListContract.LoadListCallback<ReportListItem> callback) {
        final ReportListRequest reportListRequest = new ReportListRequest();
        reportListRequest.setPage(page);
        reportListRequest.setPerPageNums(perPageNum);
        FEHttpClient.getInstance().post(reportListRequest, new ResponseCallback<ReportListResponse>(this) {
            @Override
            public void onCompleted(ReportListResponse responseContent) {
                if ("0".equals(responseContent.getErrorCode())) {
                    int totalNum = responseContent.getTotalNums();
                    List<ReportListItem> listData = responseContent.getReportList();
                    callback.loadListDataSuccess(listData, totalNum);
                }
                else
                    callback.loadListDataError();
            }

            @Override
            public void onFailure(RepositoryException repositoryException) {
                callback.loadListDataError();
            }
        });
    }
}
