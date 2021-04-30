/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2012-9-10 下午2:58:48
 */

package cn.flyrise.feep.form;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;

import cn.flyrise.feep.core.common.X.Func;
import cn.flyrise.feep.core.function.AppMenu;
import cn.flyrise.feep.core.function.FunctionManager;
import cn.flyrise.feep.core.function.Module;
import cn.flyrise.feep.form.contract.FormListContract;
import cn.flyrise.feep.form.presenter.FormListPresenter;
import cn.flyrise.feep.form.view.FormSearchActivity;
import cn.squirtlez.frouter.FRouter;
import java.util.List;

import cn.flyrise.android.protocol.model.FormTypeItem;
import cn.flyrise.android.shared.utility.FEUmengCfg;
import cn.flyrise.feep.K;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.base.component.FEListActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.form.adapter.FormListAdapter;
import cn.squirtlez.frouter.annotations.Route;

/**
 * 类功能描述：</br>
 * @author 钟永健 cm
 * @version 2.0</   br> 修改时间：2017-1-17</br> 修改备注：</br>
 */
@Route("/flow/list")
public class FormListActivity extends FEListActivity<FormTypeItem> implements FormListContract.View {

	private FormListAdapter mAdapter;
	private FormListPresenter mPresenter;

	@Override public void onAttachedToWindow() {
		super.onAttachedToWindow();
		boolean fromIM = getIntent().getBooleanExtra("fromIM", false);
		if (fromIM) {
			Module module = FunctionManager.findModule(Func.NewForm);
			if (module != null && !TextUtils.isEmpty(module.url)) {
				FRouter.build(this, "/x5/browser")
						.withString("appointURL", module.url+"?FE_IMMEDIATELY_BACK=true")
						.withInt("moduleId", Func.Default)
						.go();
			}
		}
	}

	@Override public void bindView() {
		super.bindView();
		layoutSearch.setVisibility(View.VISIBLE);
	}

	@Override
	public void bindData() {
		super.bindData();
		this.mToolBar.setTitle(getString(R.string.form_list_title));
		mAdapter = new FormListAdapter(this);
		mPresenter = new FormListPresenter(this);
		setAdapter(mAdapter);
		setPresenter(mPresenter);
		startLoadData();
	}

	@Override
	protected void toolBar(FEToolbar toolbar) {
		super.toolBar(toolbar);
		toolbar.setNavigationOnClickListener(v -> mPresenter.onBackToParent());
	}

	@Override
	public void bindListener() {
		super.bindListener();
		layoutSearch.setOnClickListener(v -> startActivity(new Intent(FormListActivity.this, FormSearchActivity.class)));
		mAdapter.setOnItemClickListener((view, object) -> {
			final FormTypeItem typeItem = (FormTypeItem) object;
			if ("0".equals(typeItem.getFormType())) {
				mPresenter.getListDataForFormID(typeItem.getFormListId());
			}
			else {
				final Intent intent = new Intent(FormListActivity.this, NewFormActivity.class);
				intent.putExtra(K.form.TITLE_DATA_KEY, typeItem.getFormName());
				intent.putExtra(K.form.URL_DATA_KEY, typeItem.getFormUrl());
				startActivity(intent);
			}
		});
	}

	@Override
	public void loadMoreListData(List<FormTypeItem> dataList) {
		mAdapter.notifyDataSetChanged();
		setCanPullUp(mPresenter.hasMoreData());
	}

	@Override
	public void onBackPressed() {
		mPresenter.onBackToParent();
	}

	@Override
	protected void onResume() {
		super.onResume();
		FEUmengCfg.onActivityPauseUMeng(this, FEUmengCfg.FormList);
	}

	@Override
	protected void onPause() {
		super.onPause();
		FEUmengCfg.onActivityPauseUMeng(this, FEUmengCfg.FormList);
	}

}
