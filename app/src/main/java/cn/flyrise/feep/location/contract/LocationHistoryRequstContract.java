package cn.flyrise.feep.location.contract;

import cn.flyrise.feep.commonality.bean.FEListInfo;

/**
 * 新建：陈冕;
 * 日期： 2018-1-24-10:46.
 */

public interface LocationHistoryRequstContract {

    void request(String id, int page, String userId);//请求数据(位置上报历史)

    void request(String date, int page, String userId, int perPageNums);

    void request(String date, int page, String userId, int perPageNums,int sumId);

    void cancleRquestData();

    interface RequstListener {

        void refreshNums(int nums);

        void refreshHistoryData(FEListInfo mCurrentItemInfo, int pageNum);
    }

}
