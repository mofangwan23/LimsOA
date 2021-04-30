package cn.flyrise.feep.robot.operation.message;

import cn.squirtlez.frouter.FRouter;

/**
 * Created by Administrator on 2017-6-29.
 * 会议
 */

public class MeetingOperation extends BaseOperation {

    @Override
    public void open() {
        openMessage();
    }

    @Override
    public void search() {
        FRouter.build(mContext, "/meeting/search").go();
    }

}
