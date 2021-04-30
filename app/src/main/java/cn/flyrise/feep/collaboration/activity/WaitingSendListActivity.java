package cn.flyrise.feep.collaboration.activity;

import android.content.Intent;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;

import cn.flyrise.feep.core.dialog.FEMaterialDialog.Builder;
import java.util.ArrayList;
import java.util.List;

import cn.flyrise.android.protocol.entity.BooleanResponse;
import cn.flyrise.android.protocol.entity.WaitSendDeleteRequest;
import cn.flyrise.feep.R;
import cn.flyrise.feep.collaboration.adapter.WaitingSendListAdapter;
import cn.flyrise.feep.collaboration.model.WaitingSend;
import cn.flyrise.feep.collaboration.presenter.WaitingSendListPresenter;
import cn.flyrise.feep.core.base.component.FEListActivity;
import cn.flyrise.feep.core.base.component.FEListContract;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.dialog.FELoadingDialog;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;

/**
 * @author ZYP
 * @since 2017-04-26 09:39 待发列表 UI update By Klc 2017-04-26 15:00
 */
public class WaitingSendListActivity extends FEListActivity<WaitingSend> {

	private WaitingSendListAdapter mAdapter;
	private FELoadingDialog mLoadingDialog;
	private List<WaitingSend> checkList;

	@Override
	protected void toolBar(FEToolbar toolbar) {
		this.mToolBar = toolbar;
		toolbar.setTitle(R.string.committed);
		toolbar.setRightText(R.string.delete);
		toolbar.setRightTextVisbility(View.GONE);
		toolbar.setRightTextClickListener(v -> {
			String deleteIDs = getDeleteIDs();
			if (TextUtils.isEmpty(deleteIDs)) {
				FEToast.showMessage(getString(R.string.no_select_item));
			}
			else {
				new Builder(WaitingSendListActivity.this)
						.setMessage(R.string.whether_delete).setCancelable(true)
						.setNegativeButton(null, null)
						.setPositiveButton(null, dialog -> delete(deleteIDs))
						.build().show();
			}
		});
	}

	@Override
	public void bindView() {
		super.bindView();
		layoutSearch.setVisibility(View.GONE);
	}

	@Override
	public void bindData() {
		super.bindData();
		mAdapter = new WaitingSendListAdapter();
		setAdapter(mAdapter);
		setPresenter(new WaitingSendListPresenter(this));
		startLoadData();
	}

	@Override
	public void bindListener() {
		super.bindListener();
		mAdapter.setOnItemClickListener((view, object) -> {
			WaitingSend waitingSend = (WaitingSend) object;
			Intent intentNewCollaboration = new Intent(WaitingSendListActivity.this, NewCollaborationActivity.class);
			intentNewCollaboration.putExtra("fromType", 101);
			intentNewCollaboration.putExtra("collaborationId", waitingSend.id);
			startActivity(intentNewCollaboration);
		});
		mAdapter.setOnItemLongClickListener((view, object) -> {
			if (mToolBar.getRightTextView().getVisibility() == View.VISIBLE) {
				mToolBar.setRightTextVisbility(View.GONE);
			}
			else {
				mToolBar.setRightTextVisbility(View.VISIBLE);
			}
		});
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mAdapter.isCheckState()) {
				mAdapter.setCheckState(false);
				mToolBar.setRightTextVisbility(View.GONE);
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	private String getDeleteIDs() {
		List<WaitingSend> dateList = mAdapter.getDataList();
		checkList = new ArrayList<>();
		StringBuilder ids = new StringBuilder();
		for (WaitingSend waitingSend : dateList) {
			if (waitingSend.isCheck) {
				checkList.add(waitingSend);
				ids.append(waitingSend.id).append(",");
			}
		}
		if (ids.length() > 0) {
			ids.deleteCharAt(ids.length() - 1);
		}
		return ids.toString();
	}

	private void delete(String deleteIds) {
		showLoading();
		FEHttpClient.getInstance().post(new WaitSendDeleteRequest(deleteIds), new ResponseCallback<BooleanResponse>() {
			@Override
			public void onCompleted(BooleanResponse response) {
				if ("0".equals(response.getErrorCode()) && response.isSuccess) {
					hideLoading();
					mToolBar.setRightTextVisbility(View.GONE);
					mAdapter.getDataList().removeAll(checkList);
					mAdapter.setCheckState(false);
					FEToast.showMessage(R.string.delete_success);
				}
				else {
					onFailure(null);
				}
			}

			@Override
			public void onFailure(RepositoryException repositoryException) {
				super.onFailure(repositoryException);
				hideLoading();
				FEToast.showMessage(R.string.delete_fail);
			}
		});
	}

	public void showLoading() {
		hideLoading();
		mLoadingDialog = new FELoadingDialog.Builder(this)
				.setCancelable(false)
				.create();
		mLoadingDialog.show();
	}

	public void hideLoading() {
		if (mLoadingDialog != null) {
			mLoadingDialog.hide();
		}
		mLoadingDialog = null;
	}

}
