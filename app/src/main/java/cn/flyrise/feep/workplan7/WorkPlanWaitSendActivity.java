package cn.flyrise.feep.workplan7;

import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import cn.flyrise.feep.K;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.base.component.FEListActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.dialog.FEMaterialDialog.Builder;
import cn.flyrise.feep.workplan7.adapter.WorkPlanWaitSendAdapter;
import cn.flyrise.feep.workplan7.model.WorkPlanWaitSend;
import cn.flyrise.feep.workplan7.presenter.WorkPlanWaitSendPresenter;
import java.util.List;

/**
 * author : klc
 * data on 2018/5/7 16:47
 * Msg : 新建计划的暂存列表
 */
public class WorkPlanWaitSendActivity extends FEListActivity<WorkPlanWaitSend> {

	private WorkPlanWaitSendAdapter mAdapter;
	private WorkPlanWaitSendPresenter mPresenter;
	private boolean isDelete = false;

	@Override
	protected void toolBar(FEToolbar toolbar) {
		this.mToolBar = toolbar;
		toolbar.setTitle(R.string.plan_wait_commit_title);
		setToolbarRightText(true);
		toolbar.setRightTextClickListener(v -> {
			if (!isDelete) {
				Plan7CreateActivity.Companion.startActivity(this, K.plan.PLAN_TYPE_DAY);
				return;
			}
			String deleteIDs = getDeleteIDs();
			if (TextUtils.isEmpty(deleteIDs)) {
				FEToast.showMessage(getString(R.string.no_select_item));
			}
			else {
				new Builder(WorkPlanWaitSendActivity.this)
						.setMessage(R.string.whether_delete).setCancelable(true)
						.setNegativeButton(null, null)
						.setPositiveButton(null, dialog -> mPresenter.delete(deleteIDs))
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
		mAdapter = new WorkPlanWaitSendAdapter();
		mPresenter = new WorkPlanWaitSendPresenter(this);
		setAdapter(mAdapter);
		setPresenter(mPresenter);
	}

	@Override
	public void bindListener() {
		super.bindListener();
		mAdapter.setOnItemClickListener(
				(view, object) -> Plan7CreateActivity.Companion
						.startActivity(WorkPlanWaitSendActivity.this, ((WorkPlanWaitSend) object).id));
		mAdapter.setOnItemLongClickListener((view, object) -> {
			isDelete = true;
			setToolbarRightText(false);
		});
	}

	private void setToolbarRightText(Boolean isShowCreate) {
		mToolBar.setRightText(isShowCreate ? R.string.alertdialog_workplan : R.string.delete);
	}

	@Override protected void onResume() {
		super.onResume();
		startLoadData();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mAdapter.isCheckState()) {
				mAdapter.setCheckState(false);
				isDelete = false;
				setToolbarRightText(true);
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	private String getDeleteIDs() {
		List<WorkPlanWaitSend> dateList = mAdapter.getDataList();
		StringBuilder ids = new StringBuilder();
		for (WorkPlanWaitSend waitingSend : dateList) {
			if (waitingSend.isCheck) {
				ids.append(waitingSend.id).append(",");
			}
		}
		if (ids.length() > 0) {
			ids.deleteCharAt(ids.length() - 1);
		}
		return ids.toString();
	}

}
