package cn.flyrise.feep.robot.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;

import cn.flyrise.feep.core.common.X.RequestType;
import java.util.ArrayList;
import java.util.List;

import cn.flyrise.feep.robot.R;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.services.model.AddressBook;
import cn.flyrise.feep.robot.Robot;
import cn.flyrise.feep.robot.contract.RobotDataLoaderContract;
import cn.flyrise.feep.robot.manager.FeepOperationManager;
import cn.flyrise.feep.robot.module.FeMessageLoadData;
import cn.flyrise.feep.robot.bean.FeSearchMessageItem;
import cn.flyrise.feep.robot.module.FeWorkPlanLoadData;
import cn.flyrise.feep.robot.module.RobotModuleItem;

/**
 * 新建：陈冕;
 * 日期： 2017-7-24.
 */

public class RobotSearchMessageDataUtil {

    private FeepOperationManager.OnMessageGrammarResultListener mListener;

    @SuppressLint("StaticFieldLeak")
    private static RobotSearchMessageDataUtil mListDataUtil;

    private RobotDataLoaderContract mRobotDataLoader;

    private int messageId;

    private int mRequestType;

    private Context mContext;

    public static RobotSearchMessageDataUtil getInstance() {
        if (mListDataUtil == null) {
            mListDataUtil = new RobotSearchMessageDataUtil();
        }
        return mListDataUtil;
    }

    public RobotSearchMessageDataUtil setContext(Context context, int requestType) {
        this.mRequestType = requestType;
        this.mContext = context;
        if (requestType == RequestType.OthersWorkPlan) {
            mRobotDataLoader = new FeWorkPlanLoadData();
        } else {
            mRobotDataLoader = new FeMessageLoadData();
        }
        mRobotDataLoader.setContext(context);
        mRobotDataLoader.setRequestType(requestType);
        mRobotDataLoader.setListener(new RobotDataLoaderContract.FeSearchMessageListener() {
            @Override
            public void onRobotModuleItem(List<FeSearchMessageItem> feSearchMessageItems) {
                searchMessageListData(feSearchMessageItems);
            }

            @Override
            public void onError(int type) {
                if (type == RobotDataLoaderContract.DATA_NULL) {
                    searchMessageNull(mContext.getResources().getString(R.string.robot_search_null));
                } else if (type == RobotDataLoaderContract.DATA_ERROR) {
                    searchMessageNull(mContext.getResources().getString(R.string.robot_search_error));
                }
            }
        });
        return this;
    }

    public RobotSearchMessageDataUtil setMessageId(int messageId) {
        this.messageId = messageId;
        return this;
    }

    public RobotSearchMessageDataUtil setAddressBook(AddressBook addressBook) {
        if (mRobotDataLoader != null) {
            mRobotDataLoader.setAddressBoook(addressBook);
        }
        return this;
    }


    public RobotSearchMessageDataUtil setListener(FeepOperationManager.OnMessageGrammarResultListener listener) {
        this.mListener = listener;
        return this;
    }

    public void searchMessageText(String key) {
        if (TextUtils.isEmpty(key)) {
            errorSearch(messageId);
            return;
        }
        if (mRobotDataLoader == null || mRequestType == -1) {
            return;
        }

        if (mRequestType == RequestType.OthersWorkPlan) {
            mRobotDataLoader.requestWorkPlanList(key);
        } else {
            mRobotDataLoader.requestMessageList(key);
        }
    }

    private void searchMessageListData(List<FeSearchMessageItem> listItems) {
        if (mListener == null) {
            return;
        }
        if (CommonUtil.isEmptyList(listItems)) {
            searchMessageNull(mContext.getResources().getString(R.string.robot_search_null));
            return;
        }
        List<RobotModuleItem> robotModuleItems = getSearchMessageRobotItem(listItems);
        if (CommonUtil.isEmptyList(robotModuleItems)) {
            searchMessageNull(mContext.getResources().getString(R.string.robot_search_null));
            return;
        }
        mListener.onGrammarResultItems(robotModuleItems);
    }

    private List<RobotModuleItem> getSearchMessageRobotItem(List<FeSearchMessageItem> listItems) {
        List<RobotModuleItem> robotModuleItems = new ArrayList<>();
        int index = 0;
        for (FeSearchMessageItem listItem : listItems) {
            if (listItem == null) {
                continue;
            }
            robotModuleItems.add(new RobotModuleItem.Builder()
                    .setIndexType(Robot.adapter.ROBOT_CONTENT_LIST)
                    .setProcess(getItemModuleProcess(index, listItems.size()))
                    .setModuleParentType(messageId)
                    .setFeListItem(listItem)
                    .create());
            index++;
        }
        return robotModuleItems;
    }

    private int getItemModuleProcess(int index, int size) {
        if (index == 0) {
            return Robot.search_message.content_start;
        } else if (index == size - 1) {
            return Robot.search_message.content_end;
        }
        return Robot.search_message.content;
    }

    //搜索错误
    private void errorSearch(int messageId) {
        if (mListener == null) {
            return;
        }
        mListener.onGrammarModule(new RobotModuleItem.Builder()
                .setModuleParentType(messageId)
                .setIndexType(Robot.adapter.ROBOT_INPUT_LEFT)
                .setProcess(Robot.search_message.error)
                .setTitle(getTitleText(messageId))
                .create());
    }

    private String getTitleText(int messageId) {
        if (messageId == 44) {
            return mContext.getResources().getString(R.string.robot_error_search_collaboration);
        } else if (messageId == 10) {
            return mContext.getResources().getString(R.string.robot_error_search_from);
        } else if (messageId == 5) {
            return mContext.getResources().getString(R.string.robot_error_search_new);
        } else if (messageId == 6) {
            return mContext.getResources().getString(R.string.robot_error_search_announcement);
        } else if (messageId == 14) {
            return mContext.getResources().getString(R.string.robot_error_search_workplan);
        } else if (messageId == 9) {
            return mContext.getResources().getString(R.string.robot_error_search_metting);
        }
        return mContext.getResources().getString(R.string.grammar_error);
    }

    //内容为空
    private void searchMessageNull(String text) {
        if (mListener == null) {
            return;
        }
        mListener.onGrammarModule(new RobotModuleItem.Builder()
                .setModuleParentType(messageId)
                .setIndexType(Robot.adapter.ROBOT_INPUT_LEFT)
                .setProcess(Robot.search_message.content_null)
                .setTitle(text)
                .create());
    }
}
