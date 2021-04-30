package com.hyphenate.chatui.group.persenter;

import android.text.TextUtils;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chatui.R;
import com.hyphenate.chatui.group.contract.GroupDetailContract;
import com.hyphenate.chatui.group.model.GroupSetting;
import com.hyphenate.chatui.group.provider.GroupDataProvider;
import com.hyphenate.chatui.group.provider.GroupDataProvider.HandleListener;
import com.hyphenate.easeui.EaseUiK;
import com.hyphenate.easeui.busevent.EMChatEvent;
import com.hyphenate.easeui.busevent.EMMessageEvent;
import com.hyphenate.easeui.utils.MMPMessageUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.GsonUtil;
import cn.flyrise.feep.core.services.IConvSTService;
import cn.flyrise.feep.core.services.model.AddressBook;

/**
 * Created by klc on 2017/3/16.
 */
public class GroupDetailPresenter implements GroupDetailContract.IPresenter {

    private GroupDetailContract.IView mView;
    private EMGroup mGroup;
    private final static int MAX_SHOW_MEMBER_SIZE = 24;
    private GroupDataProvider mProvider;
    private String groupId;

    public GroupDetailPresenter(GroupDetailContract.IView mView, String groupID) {
        this.mView = mView;
        this.groupId = groupID;
        this.mProvider = new GroupDataProvider(groupID);
    }

    @Override
    public void loadGroup() {
        mView.showLoading(true);
        mProvider.loadGroupInfo(new HandleListener<EMGroup>() {
            @Override
            public void onSuccess(EMGroup group) {
                mGroup = group;
                loadGroupSimpleUsers();
                mView.updateGroupSetting(mGroup);
            }

            @Override
            public void onFail() {
                mView.showLoading(false);
                if (mGroup == null) {
                    mView.finish();
                }
            }
        });
    }

    private void loadGroupSimpleUsers() {
        boolean isOwner = mGroup.getOwner().equals(EMClient.getInstance().getCurrentUser());
        boolean canAdd = isOwner || isAllowInvite();
        int memberCount = MAX_SHOW_MEMBER_SIZE;
        if (isOwner) {
            memberCount = MAX_SHOW_MEMBER_SIZE - 2;
        } else if (canAdd) {
            memberCount = MAX_SHOW_MEMBER_SIZE - 1;
        }
        mProvider.loadGroupUser(memberCount, new HandleListener<List<String>>() {
            @Override
            public void onSuccess(List<String> userList) {
                mView.showMoreLayout(mGroup.getMemberCount() > userList.size());
                if (canAdd) {
                    userList.add(EaseUiK.EmUserList.em_userList_addUser);
                }
                if (isOwner && userList.size() > 2) {
                    userList.add(EaseUiK.EmUserList.em_userList_removeUser);
                }
                mView.showGroupUser(userList);
                mView.showLoading(false);
            }

            @Override
            public void onFail() {
                mView.showLoading(false);
            }
        });
    }

    @Override
    public void clearHistory() {
        mView.showConfirmDialog(R.string.sure_to_empty_this, dialog -> {
            EMConversation conversation = EMClient.getInstance().chatManager()
                    .getConversation(mGroup.getGroupId(), EMConversation.EMConversationType.GroupChat);
            EMMessage lastMessage = conversation.getLastMessage();
            conversation.clearAllMessages();
            if (lastMessage != null) {
                MMPMessageUtil.saveClearHistoryMsg(mGroup.getGroupId(), true, lastMessage.getMsgTime());
            }
            mView.showMessage(R.string.messages_are_empty);
        });
    }

    @Override
    public void changeGroupName() {
        mView.showInputDialog(R.string.im_input_groupname, (dialog, input, check) -> {
            mView.showLoading(true);
            mProvider.changeGroupName(input, new HandleListener<Boolean>() {
                @Override
                public void onSuccess(Boolean aBoolean) {
                    mView.showLoading(false);
                    mView.showMessage(R.string.Modify_the_group_name_successful);
                    mView.updateGroupSetting(mGroup);
                    EventBus.getDefault().post(new EMMessageEvent.CmdChangeGroupName(mGroup));//修改成功，更新自己的群标题
                }

                @Override
                public void onFail() {
                    mView.showLoading(false);
                    mView.showMessage(R.string.change_the_group_name_failed_please);
                }
            });
        });
    }

    @Override
    public void changeInvite(boolean isAllow) {
        mView.showLoading(true);
        String extension = mGroup.getExtension();
        GroupSetting groupSetting;
        if (TextUtils.isEmpty(extension)) {
            groupSetting = new GroupSetting();
        } else {
            groupSetting = GsonUtil.getInstance().fromJson(extension, GroupSetting.class);
        }
        groupSetting.setAllowInvite(isAllow);
        mProvider.changeGroupSetting(groupSetting, new HandleListener<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                mView.showLoading(false);
                mView.updateGroupSetting(mGroup);
            }

            @Override
            public void onFail() {
                mView.showLoading(false);
                mView.updateGroupSetting(mGroup);
                mView.showMessage(R.string.setting_fail);
            }
        });
    }

    @Override
    public boolean isAllowInvite() {
        return mProvider.isAllowInvite();
    }

    /**
     * 消息免打扰
     */
    @Override
    public void messageSilence(boolean isChecked) {
        String conversationId = mGroup.getGroupId();
        String conversation = mGroup.getGroupName();

        IConvSTService convSTServices = CoreZygote.getConvSTServices();
        if (convSTServices != null) {
            if (isChecked) {
                convSTServices.makeConversationSilence(conversationId, conversation);
            } else {
                convSTServices.makeConversationActive(conversationId, conversation);
            }
        }
    }

    @Override
    public void leaveGroup() {
        mView.showConfirmDialog(R.string.exit_group_hint, dialog -> {
            mView.showLoading(true);
            mProvider.leaveGroup(new HandleListener<Boolean>() {
                @Override
                public void onSuccess(Boolean aBoolean) {
                    mView.showLoading(false);
                    mView.finish();
                }

                @Override
                public void onFail() {
                    mView.showLoading(false);
                    mView.showMessage(R.string.Exit_the_group_chat_failure);
                }
            });
        });
    }

    @Override
    public void delGroup() {
        mView.showConfirmDialog(R.string.dissolution_group_hint, dialog -> {
            mView.showLoading(true);
            mProvider.destroyGroup(new HandleListener<Boolean>() {
                @Override
                public void onSuccess(Boolean aBoolean) {
                    mView.showLoading(false);
                    EventBus.getDefault().post(new EMChatEvent.GroupDestroyed(groupId, false));
                    mView.finish();
                }

                @Override
                public void onFail() {
                    mView.showLoading(false);
                    mView.showMessage(R.string.Dissolve_group_chat_tofail);
                }
            });
        });
    }

    @Override
    public void addContract(List<AddressBook> addressBooks) {
        if (CommonUtil.isEmptyList(addressBooks)) {
            return;
        }
        mView.showLoading(true);
        mProvider.addContract(addressBooks, new HandleListener<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                mView.updateGroupSetting(mGroup);
                mView.showLoading(false);
            }

            @Override
            public void onFail() {
                mView.showMessage(R.string.Add_group_members_fail);
                mView.showLoading(false);
            }
        });
    }

    @Override
    public void getAllUserForServer() {
        mView.showLoading(true);
        mProvider.loadGroupUser(mGroup.getMemberCount(), new HandleListener<List<String>>() {
            @Override
            public void onSuccess(List<String> memberUser) {
                mView.showLoading(false);
                mView.openAddUserActivity(memberUser);
            }

            @Override
            public void onFail() {
                mView.showLoading(false);
                FEToast.showMessage(R.string.load_user_fail);
            }
        });

    }

}
