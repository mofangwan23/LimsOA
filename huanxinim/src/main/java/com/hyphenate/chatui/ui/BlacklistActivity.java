package com.hyphenate.chatui.ui;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import cn.flyrise.feep.core.base.component.FEListActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.base.views.adapter.FEListAdapter;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.dialog.FEMaterialDialog.Builder;
import com.hyphenate.chatui.R;
import com.hyphenate.chatui.contract.BlackUserListContract;
import com.hyphenate.chatui.presenter.BlackListPresenter;
import com.hyphenate.easeui.utils.EaseUserUtils;

/**
 * Blacklist screen
 * 环信黑名单
 */
public class BlacklistActivity extends FEListActivity<String> implements BlackUserListContract.IView {

	private BlackListPresenter mPresenter;

	@Override
	protected void toolBar(FEToolbar toolbar) {
		super.toolBar(toolbar);
		toolbar.setTitle(R.string.blacklist);
	}

	@Override
	public void bindView() {
		super.bindView();
		layoutSearch.setVisibility(View.GONE);
	}

	@Override
	public void bindData() {
		super.bindData();
		setAdapter(new BlacklistAdapter());
		mPresenter = new BlackListPresenter(this, this);
		setPresenter(mPresenter);
		mPresenter.onStart();
	}

	@Override public void removeSuccess() {
		startLoadData();
	}

	@Override public void removeFail() {
		FEToast.showMessage("移除失败");
	}

	/**
	 * adapter
	 */
	private class BlacklistAdapter extends FEListAdapter<String> {

		@Override
		public void onChildBindViewHolder(RecyclerView.ViewHolder holder, int position) {
			ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
			String userID = dataList.get(position);
			EaseUserUtils.setUserAvatar(BlacklistActivity.this, userID, itemViewHolder.avatar);
			String userNick = EaseUserUtils.getUserNick(userID);
			itemViewHolder.name.setText(userNick);
			itemViewHolder.itemView.setOnLongClickListener(v -> {
				new Builder(BlacklistActivity.this)
						.setMessage(R.string.unblock_user)
						.setNegativeButton(null, null)
						.setPositiveButton(null, dialog -> mPresenter.removeOutBlacklist(userID)).build().show();
				return false;
			});
		}

		@Override
		public RecyclerView.ViewHolder onChildCreateViewHolder(ViewGroup parent, int viewType) {
			View convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.ease_row_contact, parent, false);
			return new ItemViewHolder(convertView);
		}

		class ItemViewHolder extends RecyclerView.ViewHolder {

			TextView name;
			ImageView avatar;

			ItemViewHolder(View itemView) {
				super(itemView);
				name =  itemView.findViewById(R.id.name);
				avatar =  itemView.findViewById(R.id.avatar);
			}
		}
	}
}
