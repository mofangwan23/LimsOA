package com.hyphenate.chatui.group;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.hyphenate.chatui.R;
import com.hyphenate.chatui.group.contract.GroupDestroyContract;
import com.hyphenate.chatui.group.persenter.GroupDestroyPresenter;
import com.hyphenate.chatui.ui.ChatRecordSearchActivity;

import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.common.utils.DevicesUtil;
import cn.flyrise.feep.core.dialog.FELoadingDialog;

/**
 * 新建：陈冕;
 * 日期： 2018-3-2-14:15.
 * 群组注销后的操作界面
 */

public class GroupDestroySettingActivity extends BaseActivity implements GroupDestroyContract.IView {

    private String mGroupId;
    private String mTitle;
    private FEToolbar mToolbar;
    private FELoadingDialog mLoadingDialog = null;//加载提示框

    private GroupDestroyContract.IPresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.em_activity_group_destroy);
    }

    @Override
    protected void toolBar(FEToolbar toolbar) {
        super.toolBar(toolbar);
        mToolbar = toolbar;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && !DevicesUtil.isSpecialDevice()) {
            int statusBarHeight = DevicesUtil.getStatusBarHeight(this);
            this.mToolbar.setPadding(0, statusBarHeight, 0, 0);
        }
    }

    @Override
    public void bindData() {
        super.bindData();
        if (getIntent() != null) {
            mGroupId = getIntent().getStringExtra("groupId");
            mTitle = getIntent().getStringExtra("groupTitle");
        }
        mToolbar.setTitle(mTitle);
        mPresenter = new GroupDestroyPresenter(this, mGroupId, mTitle);
    }

    @Override
    public void bindListener() {
        super.bindListener();
        findViewById(R.id.rl_search).setOnClickListener(v ->
                startActivity(new Intent(GroupDestroySettingActivity.this
                        , ChatRecordSearchActivity.class).putExtra("conversationId", mGroupId)));
        findViewById(R.id.all_history_write_local).setOnClickListener(v -> mPresenter.allHistoryWriteLocal());
        findViewById(R.id.all_history_reader_new_goup).setOnClickListener(v -> mPresenter.allHistoryReaderNewGoup());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == SELECTED_GROUP_ID && data != null) {
            mPresenter.startReaderGroup(data.getStringExtra("group_id"));
        }
    }

    @Override
    public void showLoading(boolean show) {
        if (show) {
            if (mLoadingDialog != null) {
                mLoadingDialog.hide();
            }
            mLoadingDialog = new FELoadingDialog.Builder(this)
                    .setLoadingLabel(getResources().getString(R.string.core_loading_wait))
                    .setCancelable(false)
                    .create();
            mLoadingDialog.show();
        } else {
            if (mLoadingDialog != null) {
                mLoadingDialog.hide();
                mLoadingDialog = null;
            }
        }
    }

}
