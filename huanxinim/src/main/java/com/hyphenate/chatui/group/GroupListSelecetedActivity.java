package com.hyphenate.chatui.group;

import android.app.Activity;
import android.content.Intent;

import com.hyphenate.chat.EMGroup;
import com.hyphenate.chatui.R;

import cn.flyrise.feep.core.common.FEToast;

/**
 * 新建：陈冕;
 * 日期： 2018-3-26-9:34.
 * 选择需要导入的群聊
 */

public class GroupListSelecetedActivity extends GroupListActivity {

    @Override
    public void bindListener() {
        super.bindListener();
        groupAdapter.setOnItemClickListener((view, object) -> {
            EMGroup emGroup = (EMGroup) object;
            if (emGroup == null) {
                FEToast.showMessage(getResources().getString(R.string.get_chat_group_error));
                return;
            }
            Intent intent = new Intent();
            intent.putExtra("group_id", emGroup.getGroupId());
            setResult(Activity.RESULT_OK, intent);
            finish();
        });
    }
}
