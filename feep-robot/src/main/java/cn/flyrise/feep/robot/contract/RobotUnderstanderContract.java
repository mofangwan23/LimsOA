package cn.flyrise.feep.robot.contract;

import java.util.List;

import cn.flyrise.feep.robot.bean.RobotAdapterListData;
import cn.flyrise.feep.robot.module.RobotModuleItem;
import cn.flyrise.feep.robot.presenter.RobotUnderstanderPresenter;

/**
 * 新建：陈冕;
 * 日期： 2017-6-16.
 */

public interface RobotUnderstanderContract {

    interface Presenter {

        String TAG = RobotUnderstanderPresenter.class.getSimpleName();

        void startRobotUnderstander();

        void stopRobotUnderstander();

        void onPause();

        void onDestroy();

        void showWhatCanSayFragment(); //提示用户输入更多语句

        boolean isShowMoreFragment(); //判断是否显示的提示语详情界面

        void robotOperationItem(RobotModuleItem detail);
    }

    interface View {

        void startWaveView();

        void stopWaveView();

        void setWaveViewSeep(int seep);

        void setMoreLayout(boolean isShow);//显示更多提示界面

        List<RobotAdapterListData> getCurrentProcess();

    }
}
