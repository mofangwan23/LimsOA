package cn.flyrise.feep.knowledge.contract;

import java.util.List;


/**
 * Created by KLC on 2016/12/23.
 */

interface ListContract {

    interface View<T> extends  KnowBaseContract.View{
        void showRefreshLoading(boolean show);

        void refreshListData(List<T> dataList);

        void loadMoreListData(List<T> dataList);

        void loadMoreListFail();

        void dealComplete();

        void setCanPullUp(boolean hasMore);

        void setEmptyView();
    }

    interface Presenter {

    }
}
