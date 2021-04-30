package cn.flyrise.feep.knowledge.contract;

/**
 * Created by KLC on 2016/12/7.
 */

public interface PublicFileContract {
    interface View extends KnowBaseContract.View {

        void publishSuccess();
    }

    public interface Presenter {
        void publicFile(String receiver, String publicUserID, String startTime, String endTime);
    }

}
