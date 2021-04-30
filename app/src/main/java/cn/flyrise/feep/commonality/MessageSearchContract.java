package cn.flyrise.feep.commonality;


import cn.flyrise.feep.core.base.component.FEListContract;

/**
 * Created by KLC on 2017/1/5.
 */

public interface MessageSearchContract {
    interface Presenter extends FEListContract.Presenter{
        void refreshListData(String searchKey);
        void cancelLoad();
    }
}
