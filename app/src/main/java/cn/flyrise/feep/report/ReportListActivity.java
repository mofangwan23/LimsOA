/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2012-12-30 上午11:34:46
 */

package cn.flyrise.feep.report;

import android.content.Intent;
import android.view.View;


import cn.flyrise.feep.core.base.component.FEListActivity;
import cn.flyrise.feep.core.base.component.FEListContract;
import cn.flyrise.android.protocol.model.ReportListItem;
import cn.flyrise.android.shared.utility.FEUmengCfg;
import cn.flyrise.feep.K;
import cn.flyrise.feep.R;

/**
 * 类功能描述：</br>
 *
 * @author 钟永健
 * @version 1.0</br> 修改时间：2012-12-30</br> 修改备注：</br>
 * @author klc
 * @version 2.0</br> 修改时间：2017-2-7</br> 修改备注：</br>
 */
public class ReportListActivity extends FEListActivity<ReportListItem> {

    private ReportListAdapter listAdapter;

    @Override
    public void bindView() {
        super.bindView();
        this.layoutSearch.setVisibility(View.GONE);
    }

    @Override
    public void bindData() {
        super.bindData();
        this.mToolBar.setTitle(getString(R.string.report_list_title));
        listAdapter = new ReportListAdapter();
        setPresenter( new ReportListPresenter(this));
        setAdapter(listAdapter);
        startLoadData();
    }

    @Override
    public void bindListener() {
        super.bindListener();
        listAdapter.setOnItemClickListener((view, object) -> {
            final ReportListItem listItem = (ReportListItem) object;
            final Intent intent = new Intent(ReportListActivity.this, ReportDetailsActivity.class);
            intent.putExtra(K.form.TITLE_DATA_KEY, listItem.getReportName());
            intent.putExtra(K.form.URL_DATA_KEY, listItem.getSearchPageUrl());
            intent.putExtra(ReportDetailsActivity.REPORT_ITEM_KEY, listItem);
            startActivity(intent);
        });
    }


    @Override
    protected void onPause() {
        super.onPause();
        FEUmengCfg.onActivityPauseUMeng(this, FEUmengCfg.ReportList);
    }

    @Override
    protected void onResume() {
        super.onResume();
        FEUmengCfg.onActivityResumeUMeng(this, FEUmengCfg.ReportList);
    }
}
