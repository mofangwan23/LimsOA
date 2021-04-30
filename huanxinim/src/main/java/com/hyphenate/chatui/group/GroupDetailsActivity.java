/**
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hyphenate.chatui.group;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.GridLayoutManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.base.views.LoadMoreRecyclerView;
import cn.flyrise.feep.core.base.views.UISwitchButton;
import cn.flyrise.feep.core.common.DataKeeper;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.DevicesUtil;
import cn.flyrise.feep.core.dialog.FELoadingDialog;
import cn.flyrise.feep.core.dialog.FEMaterialDialog;
import cn.flyrise.feep.core.dialog.FEMaterialEditTextDialog;
import cn.flyrise.feep.core.services.IConvSTService;
import cn.flyrise.feep.core.services.model.AddressBook;
import cn.squirtlez.frouter.FRouter;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chatui.R;
import com.hyphenate.chatui.group.adapter.GroupDetailUserAdapter;
import com.hyphenate.chatui.group.contract.GroupDetailContract;
import com.hyphenate.chatui.group.persenter.GroupDetailPresenter;
import com.hyphenate.chatui.ui.ChatRecordSearchActivity;
import com.hyphenate.easeui.EaseUiK;
import com.hyphenate.easeui.busevent.EMMessageEvent;
import com.hyphenate.easeui.listener.SimpleGroupChangeListener;
import java.util.List;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class GroupDetailsActivity extends BaseActivity implements GroupDetailContract.IView {

	private static final int CODE_REQUEST_ADD_CONTACTS = 10028;

	private LoadMoreRecyclerView mUserGridView;
	private TextView tv_showAll;
	private RelativeLayout mLayoutChangeName;
	private RelativeLayout mLayoutChangeOwner;
	private Button exitBtn;
	private Button deleteBtn;
	private UISwitchButton mSilenceModeBtn;
	private UISwitchButton mBtTop;
	private FEToolbar mToolbar;
	private TextView tvUserCount;
	private RelativeLayout mLayoutAllowInvite;
	private UISwitchButton mAllowInvite;

	private GroupDetailUserAdapter adapter;
	private FELoadingDialog mLoadingDialog;
	private String mGroupId;
	private GroupDetailPresenter mPresenter;
	private EMGroup mGroup;

	private final int REFRESH_CODE = 100;
	private final int REFRESH_DELAY = 500;
	private GroupChangeListener mGroupChangeListener;

	private boolean isPause;
	private boolean needUpdate;

	@SuppressLint("HandlerLeak") private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
				case REFRESH_CODE:
					if (isPause) {
						needUpdate = true;
					}
					else {
						mPresenter.loadGroup();
					}
					break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.em_activity_group_details);
		EventBus.getDefault().register(this);
	}

	@Override
	protected void toolBar(FEToolbar toolbar) {
		this.mToolbar = toolbar;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && !DevicesUtil.isSpecialDevice()) {
			int statusBarHeight = DevicesUtil.getStatusBarHeight(this);
			this.mToolbar.setPadding(0, statusBarHeight, 0, 0);
		}
		mToolbar.setNavigationOnClickListener(v -> finish());
	}

	@Override
	public void bindView() {
		super.bindView();
		mUserGridView = findViewById(R.id.gridview);
		mUserGridView.setLayoutManager(new GridLayoutManager(this, 5));
		mUserGridView.setNestedScrollingEnabled(false);
		tv_showAll = findViewById(R.id.tv_showAll);
		tvUserCount = findViewById(R.id.usercount);
		mLayoutChangeName = findViewById(R.id.rl_change_group_name);
		mLayoutChangeOwner = findViewById(R.id.rl_changeowner);
		mSilenceModeBtn = findViewById(R.id.switch_btn);
		mBtTop = findViewById(R.id.btTop);
		exitBtn = findViewById(R.id.btn_exit_grp);
		deleteBtn = findViewById(R.id.btn_exitdel_grp);
		mLayoutAllowInvite = findViewById(R.id.lyAllowInvite);
		mAllowInvite = findViewById(R.id.btAllowInvite);
		mLayoutAllowInvite.setVisibility(View.GONE);
		mSilenceModeBtn.setChecked(false);
	}

	@Override
	public void bindData() {
		super.bindData();
		mGroupId = getIntent().getStringExtra("groupId");
		mPresenter = new GroupDetailPresenter(this, mGroupId);
		adapter = new GroupDetailUserAdapter(this);
		mUserGridView.setAdapter(adapter);
		mPresenter.loadGroup();
		mGroupChangeListener = new GroupChangeListener();
		EMClient.getInstance().groupManager().addGroupChangeListener(mGroupChangeListener);
		EMConversation conversation = EMClient.getInstance().chatManager().getConversation(mGroupId);
		mBtTop.setChecked(!TextUtils.isEmpty(conversation.getExtField()));
	}

	@Override
	public void bindListener() {
		super.bindListener();
		findViewById(R.id.clear_all_history).setOnClickListener(v -> mPresenter.clearHistory());
		findViewById(R.id.rl_change_group_name).setOnClickListener(v -> mPresenter.changeGroupName());
		findViewById(R.id.rl_search).setOnClickListener(
				v -> startActivity(
						new Intent(GroupDetailsActivity.this, ChatRecordSearchActivity.class).putExtra("conversationId", mGroupId)));
		mSilenceModeBtn.setOnClickListener(v -> mPresenter.messageSilence(mSilenceModeBtn.isChecked()));

		exitBtn.setOnClickListener(v -> mPresenter.leaveGroup());
		deleteBtn.setOnClickListener(v -> mPresenter.delGroup());
		tv_showAll.setOnClickListener(v -> {
			Intent intent = new Intent(GroupDetailsActivity.this, GroupAllUserActivity.class);
			intent.putExtra(EaseUiK.EmUserList.emGroupID, mGroupId);
			startActivity(intent);
		});
		mLayoutChangeOwner.setOnClickListener(v -> {
			Intent intent = new Intent(GroupDetailsActivity.this, GroupUserManageListActivity.class);
			intent.putExtra(EaseUiK.EmUserList.emGroupID, mGroupId);
			intent.putExtra(EaseUiK.EmUserList.emUserListType, EaseUiK.EmUserList.em_userList_changeOwner_code);
			intent.putExtra(EaseUiK.EmUserList.emUserListTitle, getString(R.string.change_owner));
			startActivityForResult(intent, EaseUiK.EmUserList.em_userList_changeOwner_code);
		});
		adapter.setOnItemClickListener((view, object) -> {
			String userID = (String) object;
			switch (userID) {
				case EaseUiK.EmUserList.em_userList_addUser:
					mPresenter.getAllUserForServer();
					break;
				case EaseUiK.EmUserList.em_userList_removeUser:
					Intent intent = new Intent(GroupDetailsActivity.this, GroupUserManageListActivity.class);
					intent.putExtra(EaseUiK.EmUserList.emGroupID, mGroupId);
					intent.putExtra(EaseUiK.EmUserList.emUserListType, EaseUiK.EmUserList.em_userList_delete_code);
					intent.putExtra(EaseUiK.EmUserList.emUserListTitle, getString(R.string.remove_group_user));
					startActivity(intent);
					break;
				default:
					FRouter.build(GroupDetailsActivity.this, "/addressBook/detail")
							.withString("user_id", userID)
							.go();
					break;
			}
		});
		mBtTop.setOnCheckedChangeListener((buttonView, isChecked) -> {
			EMConversation conversation = EMClient.getInstance().chatManager().getConversation(mGroupId);
			conversation.setExtField(isChecked ? "1" : "");
		});
		mAllowInvite.setOnCheckedChangeListener((buttonView, isChecked) -> mPresenter.changeInvite(isChecked));
	}

	private class GroupChangeListener extends SimpleGroupChangeListener {

		@Override
		public void onMemberJoined(String groupID, String member) {
			if (groupID.equals(mGroupId)) {
				if (mHandler.hasMessages(REFRESH_CODE)) {
					return;
				}
				mHandler.sendEmptyMessageDelayed(REFRESH_CODE, REFRESH_DELAY);
			}
		}

		@Override
		public void onMemberExited(String groupID, String member) {
			if (groupID.equals(mGroupId)) {
				if (mHandler.hasMessages(REFRESH_CODE)) {
					return;
				}
				mHandler.sendEmptyMessageDelayed(REFRESH_CODE, REFRESH_DELAY);
			}
		}

		@Override
		public void onOwnerChanged(String groupId, String newOwner, String oldOwner) {
			if (groupId.equals(mGroupId)) {
				if (mHandler.hasMessages(REFRESH_CODE)) {
					return;
				}
				mHandler.sendEmptyMessageDelayed(REFRESH_CODE, REFRESH_DELAY);
			}
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onCmdGroupNameChange(EMMessageEvent.CmdChangeGroupName event) {
		EMGroup emGroup = event.group;
		if (emGroup.getGroupId().equals(mGroup.getGroupId())) {
			mGroup = emGroup;
			mToolbar.setTitle(mGroup.getGroupName());
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onCmdGroupSettingUpdate(EMMessageEvent.CmdGroupSettingUpdate event) {
		if (mGroupId.equals(event.groupID)) {
			if (mHandler.hasMessages(REFRESH_CODE)) {
				return;
			}
			mHandler.sendEmptyMessageDelayed(REFRESH_CODE, REFRESH_DELAY);
		}
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (CODE_REQUEST_ADD_CONTACTS == requestCode && resultCode == 2048) {
			List<AddressBook> addressBooks = (List<AddressBook>) DataKeeper.getInstance().getKeepDatas(CODE_REQUEST_ADD_CONTACTS);
			mPresenter.addContract(addressBooks);
		}
		else if (requestCode == EaseUiK.EmUserList.em_userList_changeOwner_code && resultCode == RESULT_OK) {
			mPresenter.loadGroup();
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
		}
		else {
			if (mLoadingDialog != null) {
				mLoadingDialog.hide();
				mLoadingDialog = null;
			}
		}
	}

	@Override
	public void updateGroupSetting(EMGroup mGroup) {
		this.mGroup = mGroup;
		mToolbar.setTitle(mGroup.getGroupName());
		tvUserCount.setText(mGroup.getMemberCount() + "/" + mGroup.getMaxUserCount());
		if (EMClient.getInstance().getCurrentUser().equals(mGroup.getOwner())) {
			exitBtn.setVisibility(View.GONE);
			deleteBtn.setVisibility(View.VISIBLE);
			mLayoutChangeOwner.setVisibility(View.VISIBLE);
			mLayoutChangeName.setVisibility(View.VISIBLE);
			mAllowInvite.setChecked(mPresenter.isAllowInvite());
			mLayoutAllowInvite.setVisibility(View.VISIBLE);
		}
		else {
			exitBtn.setVisibility(View.VISIBLE);
			deleteBtn.setVisibility(View.GONE);
			mLayoutChangeOwner.setVisibility(View.GONE);
			mLayoutChangeName.setVisibility(View.GONE);
		}

		IConvSTService cstServer = CoreZygote.getConvSTServices();
		mSilenceModeBtn.setChecked(cstServer != null && cstServer.isSilence(mGroupId));
	}


	@Override
	public void showGroupUser(List<String> userList) {
		adapter.setDataList(userList);
	}

	@Override
	public void showMoreLayout(boolean show) {
		tv_showAll.setVisibility(show ? View.VISIBLE : View.GONE);
	}

	@Override
	public void showMessage(int resourceID) {
		FEToast.showMessage(getString(resourceID));
	}

	@Override
	public void showConfirmDialog(int resourceID, FEMaterialDialog.OnClickListener clickListener) {
		new FEMaterialDialog.Builder(this).setMessage(getString(resourceID)).setNegativeButton(null, null)
				.setPositiveButton(null, clickListener).build().show();
	}

	@Override
	public void showInputDialog(int resourceID, FEMaterialEditTextDialog.OnClickListener clickListener) {
		new FEMaterialEditTextDialog.Builder(this).setHint(getString(resourceID)).setNegativeButton(null, null)
				.setPositiveButton(null, clickListener)
				.setDefaultText(mGroup.getGroupName()).setMaxSize(20).build().show();
	}

	@Override
	public void openAddUserActivity(List<String> allUser) {
		List<AddressBook> mSelectedPersons = CoreZygote.getAddressBookServices().queryUserIds(allUser);
		if (CommonUtil.nonEmptyList(mSelectedPersons)) {
			DataKeeper.getInstance().keepDatas(CODE_REQUEST_ADD_CONTACTS, mSelectedPersons);
		}
		FRouter.build(GroupDetailsActivity.this, "/addressBook/list")
				.withBool("select_mode", true)
				.withBool("except_selected", true)
				.withString("address_title", CommonUtil.getString(R.string.em_txt_new_group))
				.withInt("data_keep", CODE_REQUEST_ADD_CONTACTS)
				.requestCode(CODE_REQUEST_ADD_CONTACTS)
				.go();
	}

	@Override
	protected void onResume() {
		super.onResume();
		isPause = false;
		if (needUpdate) {
			mPresenter.loadGroup();
			needUpdate = false;
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		isPause = true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		EMClient.getInstance().groupManager().removeGroupChangeListener(mGroupChangeListener);
		EventBus.getDefault().unregister(this);
	}
}
