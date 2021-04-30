package cn.flyrise.feep.robot.operation.message;

import android.text.TextUtils;

import cn.flyrise.feep.robot.util.RobotSearchMessageDataUtil;

/**
 * 新建：陈冕;
 * 日期：2017-6-29.
 * 新闻、公告
 */

public class NewAnnouncementOperation extends BaseOperation {

    @Override
    public void open() {
        openMessage();
    }

    @Override
    public void search() {
        if (mOperationModule.getMessageId() == 6
                && !TextUtils.isEmpty(getSearchText())) {
            RobotSearchMessageDataUtil.getInstance()
                    .setContext(mContext, mOperationModule.getMessageId())
                    .setMessageId(mOperationModule.getMessageId())
                    .setListener(mOperationModule.grammarResultListener)
                    .searchMessageText(getSearchText());
            return;
        }
        searchMessage(mOperationModule.getMessageId());
    }
}
