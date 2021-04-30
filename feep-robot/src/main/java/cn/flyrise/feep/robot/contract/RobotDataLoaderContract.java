package cn.flyrise.feep.robot.contract;

import android.content.Context;

import java.util.List;

import cn.flyrise.feep.core.services.model.AddressBook;
import cn.flyrise.feep.robot.bean.FeSearchMessageItem;

/**
 * 新建：陈冕;
 * 日期： 2017-8-3-16:21.
 */

public interface RobotDataLoaderContract {

    int DATA_NULL = 0;

    int DATA_ERROR = 1;

    int perPageNums = 10;
    int page = 1;

    void setContext(Context context);

    void setListener(FeSearchMessageListener listener);

    void setRequestType(int requestType);

    void setAddressBoook(AddressBook addressBoook);

    void requestMessageList(String searchKey);//请求消息

    void requestWorkPlanList(String userID);//请求计划列表

    interface FeSearchMessageListener {
        void onRobotModuleItem(List<FeSearchMessageItem> feSearchMessageItems);

        void onError(int type);
    }
}
