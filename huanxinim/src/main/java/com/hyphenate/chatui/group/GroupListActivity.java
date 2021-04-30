
package com.hyphenate.chatui.group;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.SimpleItemAnimator;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.base.component.FEListActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.common.DataKeeper;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.DevicesUtil;
import cn.flyrise.feep.core.dialog.FELoadingDialog;
import cn.flyrise.feep.core.dialog.FEMaterialDialog.Builder;
import cn.flyrise.feep.core.services.model.AddressBook;
import cn.squirtlez.frouter.FRouter;
import com.hyphenate.EMGroupChangeListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chatui.R;
import com.hyphenate.chatui.group.adapter.GroupListAdapter;
import com.hyphenate.chatui.group.contract.GroupListContract;
import com.hyphenate.chatui.group.persenter.GroupListPresenter;
import com.hyphenate.chatui.ui.ChatActivity;
import com.hyphenate.easeui.EaseUiK;
import com.hyphenate.easeui.busevent.EMMessageEvent;
import com.hyphenate.easeui.listener.SimpleGroupChangeListener;
import java.util.Arrays;
import java.util.List;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


/**
 * @author KLC 群聊列表
 * @since 2017-03-20 11:30
 */
public class GroupListActivity extends FEListActivity<EMGroup> implements GroupListContract.IView {

	public static final int CODE_ADD_CONTACTS = 8088;   // 选择联系人
	public static final String TAG = "GroupsActivity";
	protected GroupListAdapter groupAdapter;
	private GroupListContract.IPresenter mPresenter;
	private String mUserId;

	private FELoadingDialog mLoadingDialog;

	private EditText mEtSearch;
	private ImageView ivClear;
	private String lastSearch;
	private LinearLayout mLayoutSearchHint;//搜索提示

	private EMGroupChangeListener mGroupChangeListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EventBus.getDefault().register(this);
	}

	@Override
	protected void toolBar(FEToolbar toolbar) {
		super.toolBar(toolbar);
		toolbar.setTitle(getResources().getString(R.string.group_chat));
		toolbar.setRightIcon(R.drawable.add_btn);
		toolbar.setRightImageClickListener(v -> createGroup());
	}

	@Override
	public void bindView() {
		super.bindView();
		layoutSearch.setVisibility(View.GONE);
		findViewById(R.id.search_inside_bar).setVisibility(View.VISIBLE);
		mEtSearch = findViewById(R.id.etSearch);
		mLayoutSearchHint = findViewById(R.id.hint_layout);
		ivClear = findViewById(R.id.ivDeleteText);
		((SimpleItemAnimator) listView.getLoadMoreRecyclerView().getItemAnimator()).setSupportsChangeAnimations(false);
	}

	@Override
	public void bindData() {
		super.bindData();
		lastSearch = "";
		mPresenter = new GroupListPresenter(this, this);
		setPresenter(mPresenter);
		groupAdapter = new GroupListAdapter(this);
		groupAdapter.setHasStableIds(true);
		setAdapter(groupAdapter);
		mUserId = CoreZygote.getLoginUserServices().getUserId();
		startLoadData();
	}

	@Override
	public void bindListener() {
		super.bindListener();
		groupAdapter.setOnItemClickListener((view, object) -> {
			EMGroup emGroup = (EMGroup) object;
			String forwardMsgId = getIntent().getStringExtra("forward_msg_id"); //╮(╯▽╰)╭ 转发聊天内容。
			if (!TextUtils.isEmpty(forwardMsgId)) {
				new Builder(this).setNegativeButton(null, null)
						.setMessage(getString(R.string.whether_forward_to) + emGroup.getGroupName())
						.setPositiveButton(null, dialog -> openGroupChat(emGroup.getGroupId(), forwardMsgId))
						.build()
						.show();
			}
			else {
				openGroupChat(emGroup.getGroupId(), forwardMsgId);
			}
		});
		ivClear.setOnClickListener(v -> {
			mEtSearch.setText("");
			clickeSearchView(false);
		});
		mLayoutSearchHint.setOnClickListener(v -> {
			clickeSearchView(true);
			mEtSearch.setFocusable(true);
			mEtSearch.setFocusableInTouchMode(true);
		});
		mEtSearch.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				String input = s.toString().trim();
				if (TextUtils.isEmpty(input)) {
					ivClear.setVisibility(View.GONE);
				}
				else {
					ivClear.setVisibility(View.VISIBLE);
				}
				if (!lastSearch.equals(input)) {
					mPresenter.searchGroup(input);
				}
				lastSearch = input;
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
		mGroupChangeListener = new SimpleGroupChangeListener() {
			@Override
			public void onAutoAcceptInvitationFromGroup(String s, String s1, String s2) {
				mPresenter.refreshListData();
			}

			@Override
			public void onUserRemoved(String s, String s1) {
				mPresenter.refreshListData();
			}

			@Override
			public void onGroupDestroyed(String s, String s1) {
				mPresenter.refreshListData();
			}
		};
	}

	private void clickeSearchView(boolean isStartSearch) {
		mLayoutSearchHint.setVisibility(isStartSearch ? View.GONE : View.VISIBLE);
		if (isStartSearch)
			DevicesUtil.showKeyboard(mEtSearch);
		else
			DevicesUtil.hideKeyboard(mEtSearch);
	}

	private void openGroupChat(String groupID, String msgID) {
		Intent intent = new Intent(GroupListActivity.this, ChatActivity.class);
		intent.putExtra(EaseUiK.EmChatContent.emChatType, EaseUiK.EmChatContent.em_chatType_group);
		intent.putExtra(EaseUiK.EmChatContent.emChatID, groupID);
		if (!TextUtils.isEmpty(msgID)) {
			intent.putExtra("forward_msg_id", msgID);
		}
		startActivity(intent);
	}

	private void createGroup() {
		List<AddressBook> addressBooks = CoreZygote.getAddressBookServices().queryUserIds(Arrays.asList(mUserId));
		DataKeeper.getInstance().keepDatas(GroupListActivity.this.hashCode(), addressBooks);
		FRouter.build(GroupListActivity.this, "/addressBook/list")
				.withBool("select_mode", true)
				.withBool("except_selected", true)
				.withInt("data_keep", GroupListActivity.this.hashCode())
				.withString("address_title", getString(R.string.em_title_select_contact))
				.requestCode(CODE_ADD_CONTACTS)
				.go();
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onCmdGroupNameChange(EMMessageEvent.CmdChangeGroupName event) {
		mPresenter.refreshListData();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == CODE_ADD_CONTACTS && resultCode == 2048) {
			List<AddressBook> addressBooks = (List<AddressBook>) DataKeeper.getInstance().getKeepDatas(GroupListActivity.this.hashCode());
			if (CommonUtil.nonEmptyList(addressBooks)) {
				mPresenter.createNewGroup(addressBooks);
			}
		}
	}

	@Override
	public void showLoading() {
		hideLoading();
		mLoadingDialog = new FELoadingDialog.Builder(this)
				.setLoadingLabel(getResources().getString(R.string.core_loading_wait))
				.setCancelable(false)
				.create();
		mLoadingDialog.show();
	}

	@Override
	public void startChatActivity(EMGroup emGroup) {
		Intent intent = new Intent(GroupListActivity.this, ChatActivity.class);
		intent.putExtra(EaseUiK.EmChatContent.emChatID, emGroup.getGroupId());
		intent.putExtra(EaseUiK.EmChatContent.emChatType, EaseUiK.EmChatContent.em_chatType_group);
		startActivity(intent);
	}

	@Override
	public String getSearchKey() {
		return mEtSearch.getText().toString().trim();
	}

	@Override
	public void hideLoading() {
		if (mLoadingDialog != null) {
			mLoadingDialog.hide();
			mLoadingDialog = null;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		mPresenter.refreshListData();
		EMClient.getInstance().groupManager().addGroupChangeListener(mGroupChangeListener);
	}

	@Override
	protected void onPause() {
		super.onPause();
		EMClient.getInstance().groupManager().removeGroupChangeListener(mGroupChangeListener);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mGroupChangeListener = null;
		EventBus.getDefault().unregister(this);
	}
}
