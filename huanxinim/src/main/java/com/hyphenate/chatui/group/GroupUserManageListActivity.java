package com.hyphenate.chatui.group;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.base.views.PullAndLoadMoreRecyclerView;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.dialog.FELoadingDialog;
import cn.flyrise.feep.core.dialog.FEMaterialDialog;
import cn.squirtlez.frouter.FRouter;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chatui.R;
import com.hyphenate.chatui.group.adapter.GroupUserManageListAdapter;
import com.hyphenate.chatui.group.contract.GroupUserManageContract;
import com.hyphenate.chatui.group.model.EmUserItem;
import com.hyphenate.chatui.group.persenter.GroupUserManagePresenter;
import com.hyphenate.easeui.EaseUiK;
import com.hyphenate.easeui.utils.EaseUserUtils;
import java.util.List;
import java.util.Locale;

/**
 * Created by klc on 2017/3/15.
 * 群组用户管理列表。
 */

public class GroupUserManageListActivity extends BaseActivity implements GroupUserManageContract.IView {

	protected PullAndLoadMoreRecyclerView mListView;
	private EditText mEtQuery;
	private ImageView ivClear;
	private FEToolbar mToolBar;
	private FELoadingDialog mLoadingDialog;

	private GroupUserManagePresenter mPresenter;
	private GroupUserManageListAdapter mAdapter;
	private String lastSearch;

	private int mActionType;
	private EMGroup mGroup;
	private Handler mHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.em_activity_group_userlist);
	}

	@Override
	protected void toolBar(FEToolbar toolbar) {
		super.toolBar(toolbar);
		this.mToolBar = toolbar;
	}

	@Override
	public void bindView() {
		super.bindView();
		mListView = findViewById(R.id.listview);
		ivClear = findViewById(R.id.ivDeleteText);
		mEtQuery = findViewById(R.id.etSearch);
		mListView.setCanRefresh(false);
		findViewById(R.id.btnSearchCancle).setVisibility(View.GONE);
		findViewById(R.id.search_line).setVisibility(View.GONE);
		findViewById(R.id.top).setBackgroundColor(Color.parseColor("#f5f5f5"));
	}

	@Override
	public void bindData() {
		super.bindData();
		this.lastSearch = "";
		String mGroupID = getIntent().getStringExtra(EaseUiK.EmUserList.emGroupID);
		mGroup = EMClient.getInstance().groupManager().getGroup(mGroupID);
		mActionType = getIntent().getIntExtra(EaseUiK.EmUserList.emUserListType, EaseUiK.EmUserList.em_userList_delete_code);
		mToolBar.setTitle(getIntent().getStringExtra(EaseUiK.EmUserList.emUserListTitle));
		mAdapter = new GroupUserManageListAdapter(this, mGroupID);
		mAdapter.setListType(mActionType);
		mListView.setAdapter(mAdapter);
		mHandler = new Handler();
		mPresenter = new GroupUserManagePresenter(this, mGroupID);
		switch (mActionType) {
			case EaseUiK.EmUserList.em_userList_delete_code:
				setDeleteCount(0);
				mHandler.postDelayed(() -> mPresenter.getAllUser(), 500);
				break;
			case EaseUiK.EmUserList.em_userList_showAll_code:
			case EaseUiK.EmUserList.em_userList_changeOwner_code:
				mHandler.postDelayed(() -> mPresenter.getAllUser(), 500);
				break;
			case EaseUiK.EmUserList.em_userList_manage_code:
				mHandler.postDelayed(() -> mPresenter.getAllUser(), 500);
				break;
			default:
		}
	}

	@Override
	public void bindListener() {
		super.bindListener();
		if (mActionType == EaseUiK.EmUserList.em_userList_delete_code) {
			mToolBar.setRightTextClickListener(v -> {
				if (!CommonUtil.isEmptyList(mAdapter.getSelectUser())) {
					new FEMaterialDialog.Builder(GroupUserManageListActivity.this)
							.setCancelable(true)
							.setMessage("是否删除选中人员")
							.setPositiveButton(null, dialog -> mPresenter.deleteUser(mAdapter.getSelectUser()))
							.setNegativeButton(null, null)
							.build()
							.show();
				}
			});
		}
		mAdapter.setOnItemClickListener((view, object) -> {
			EmUserItem clickItem = (EmUserItem) object;
			if (view instanceof ImageView) {
				FRouter.build(GroupUserManageListActivity.this, "/addressBook/detail")
						.withString("user_id", clickItem.userId)
						.go();
				return;
			}
			switch (mActionType) {
				case EaseUiK.EmUserList.em_userList_delete_code:
					setDeleteCount(mAdapter.getSelectUser().size());
					break;
				case EaseUiK.EmUserList.em_userList_showAll_code:
					break;
				case EaseUiK.EmUserList.em_userList_changeOwner_code:
					if (clickItem.userId.equals(mGroup.getOwner())) return;
					String hint = getString(R.string.em_hint_change_owner) + EaseUserUtils.getUserNick(clickItem.userId);
					showConfirmDialog(hint, dialog -> mPresenter.changeOwner(clickItem.userId));
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
				ivClear.setVisibility(TextUtils.isEmpty(input) ? View.GONE : View.VISIBLE);
				if (!lastSearch.equals(input)) {
					mPresenter.queryContacts(input);
				}
				lastSearch = input;
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
	}


	@Override
	public void showRefresh(boolean show) {
		if (show) {
			mListView.setRefreshing(true);
		}
		else {
			mHandler.postDelayed(() -> mListView.setRefreshing(false), 500);
		}
	}

	@Override
	public void refreshListData() {
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void refreshListData(List<EmUserItem> dataList) {
		mAdapter.setDataList(dataList);
	}

	@Override
	public void showConfirmDialog(String msg, FEMaterialDialog.OnClickListener clickListener) {
		new FEMaterialDialog.Builder(this).setMessage(msg).setNegativeButton(null, null)
				.setPositiveButton(null, clickListener).build().show();
	}

	@Override
	public void hideLoading() {
		if (mLoadingDialog != null) {
			mLoadingDialog.hide();
			mLoadingDialog = null;
		}
	}

	@Override
	public void showLoading() {
		hideLoading();
		mLoadingDialog = new FELoadingDialog.Builder(GroupUserManageListActivity.this)
				.setLoadingLabel(getResources().getString(R.string.core_loading_wait))
				.setCancelable(false)
				.create();
		mLoadingDialog.show();
	}

	@Override
	public void showMessage(int resourceID) {
		FEToast.showMessage(getString(resourceID));
	}

	@Override
	public void setDeleteCount(int count) {
		mToolBar.setRightText(String.format(Locale.getDefault(), getString(R.string.has_delete), mAdapter.getSelectUser().size()));
		if (count == 0) {
			mToolBar.setRightText(R.string.delete);
			mToolBar.setRightTextColor(Color.parseColor("#666667"));
			mToolBar.getRightTextView().setEnabled(false);
			mAdapter.getSelectUser().clear();
		}
		else {
			mToolBar.setRightText(String.format(Locale.getDefault(), getString(R.string.has_delete), mAdapter.getSelectUser().size()));
			mToolBar.setRightTextColor(Color.parseColor("#565656"));
			mToolBar.getRightTextView().setEnabled(true);
		}
	}
}
