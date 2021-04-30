package cn.flyrise.feep.knowledge.contract;


/**
 * Created by KLC on 2016/12/6.
 */

public interface KnowBaseContract {

    interface  View {
        void showDealLoading(boolean show);

        void showMessage(int resourceID);
    }

    interface DealWithCallBack {
        void success();

        void fail();
    }
}
