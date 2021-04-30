package com.hyphenate.chatui.group;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.common.DataKeeper;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.dialog.FELoadingDialog;
import cn.flyrise.feep.core.services.model.AddressBook;
import cn.squirtlez.frouter.FRouter;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chatui.R;
import com.hyphenate.chatui.group.adapter.GroupDetailUserAdapter;
import com.hyphenate.chatui.group.contract.GroupAllUserContract;
import com.hyphenate.easeui.busevent.EMMessageEvent;
import com.hyphenate.easeui.listener.SimpleGroupChangeListener;
import com.hyphenate.chatui.group.persenter.GroupAllUserPresenter;
import com.hyphenate.easeui.EaseUiK;
import java.util.List;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by klc on 2017/3/31.
 */

public class GroupAllUserActivity extends BaseActivity implements GroupAllUserContract.IView {

	private final int CODE_REQUEST_ADD_CONTACTS = 10028;

	private final int REFRESH_CODE = 100;
	private final int REFRESH_DELAY = 1000;

	private RecyclerView mGridView;
	private EditText mEtQuery;
	private ImageView ivClear;
	private FELoadingDialog mLoadingDialog;

	private List<AddressBook> mSelectedPersons;
	private String mGroupId;
	private GroupDetailUserAdapter adapter;
	private String lastSearch;
	private GroupAllUserContract.IPresenter mPresenter;

	private GroupChangeListener mGroupChangeListener;

	private boolean isPause;
	private boolean needUpdate;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
				case REFRESH_CODE:
					if (!isPause) {
						mPresenter.loadGroup();
					}
					else {
						needUpdate = true;
					}
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.em_activity_group_usergrid);
		EventBus.getDefault().register(this);
	}

	@Override
	protected void toolBar(FEToolbar toolbar) {
		super.toolBar(toolbar);
		toolbar.setTitle(R.string.group_user);
	}

	@Override
	public void bindView() {
		super.bindView();
		mGridView = (RecyclerView) findViewById(R.id.listview);
		mEtQuery = (EditText) findViewById(R.id.etSearch);
		ivClear = (ImageView) findViewById(R.id.ivDeleteText);
		mGridView.setLayoutManager(new GridLayoutManager(this, 5));
		findViewById(R.id.btnSearchCancle).setVisibility(View.GONE);
		findViewById(R.id.search_line).setVisibility(View.GONE);
	}

	@Override
	public void bindData() {
		super.bindData();
		lastSearch = "";
		mGroupId = getIntent().getStringExtra(EaseUiK.EmUserList.emGroupID);
		adapter = new GroupDetailUserAdapter(this);
		mGridView.setAdapter(adapter);
		mPresenter = new GroupAllUserPresenter(mGroupId, this);
		mPresenter.loadGroup();
	}

	@Override
	public void bindListener() {
		super.bindListener();
		adapter.setOnItemClickListener((view, object) -> {
			String userID = (String) object;
			switch (userID) {
				case "action_add_user":
					mSelectedPersons = CoreZygote.getAddressBookServices().queryUserIds(mPresenter.getAllUserList());
					if (CommonUtil.nonEmptyList(mSelectedPersons)) {
						DataKeeper.getInstance().keepDatas(CODE_REQUEST_ADD_CONTACTS, mSelectedPersons);
					}
					FRouter.build(GroupAllUserActivity.this, "/addressBook/list")
							.withBool("select_mode", true)
							.withBool("except_selected", true)
							.withString("address_title", CommonUtil.getString(R.string.em_txt_new_group))
							.withInt("data_keep", CODE_REQUEST_ADD_CONTACTS)
							.requestCode(CODE_REQUEST_ADD_CONTACTS)
							.go();
					break;
				case "action_remove_user":
					Intent intent = new Intent(GroupAllUserActivity.this, GroupUserManageListActivity.class);
					intent.putExtra(EaseUiK.EmUserList.emGroupID, mGroupId);
					intent.putExtra(EaseUiK.EmUserList.emUserListType, EaseUiK.EmUserList.em_userList_delete_code);
					intent.putExtra(EaseUiK.EmUserList.emUserListTitle, getString(R.string.remove_group_user));
					startActivity(intent);
					break;
				default:
					FRouter.build(GroupAllUserActivity.this, "/addressBook/detail")
							.withString("user_id", userID)
							.go();
					break;
			}
		});
		ivClear.setOnClickListener(v -> mEtQuery.setText(""));
		mEtQuery.addTextChangedListener(new TextWatcher() {
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
					mPresenter.queryContacts(input);
				}
				lastSearch = input;
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
		mGroupChangeListener = new GroupChangeListener();
		EMClient.getInstance().groupManager().addGroupChangeListener(mGroupChangeListener);
	}

	private class GroupChangeListener extends SimpleGroupChangeListener {

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
		public void onMemberJoined(String groupID, String s1) {
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
	}

	@Override
	public void refreshListData() {
	}

	@Override
	public void refreshListData(List<String> dataList) {
		adapter.setDataList(dataList);
	}

	public void hideLoading() {
		if (mLoadingDialog != null) {
			mLoadingDialog.hide();
			mLoadingDialog = null;
		}
	}

	public void showLoading() {
		hideLoading();
		mLoadingDialog = new FELoadingDialog.Builder(GroupAllUserActivity.this)
				.setLoadingLabel(getResources().getString(R.string.core_loading_wait))
				.setCancelable(false)
				.create();
		mLoadingDialog.show();
	}

	@Override
	protected void onResume() {
		super.onResume();
		isPause = false;
		if (needUpdate) {
			mPresenter.getAllUserForServer();
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
