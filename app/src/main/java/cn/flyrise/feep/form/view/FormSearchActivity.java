package cn.flyrise.feep.form.view;

import android.content.Intent;

import cn.flyrise.feep.form.NewFormActivity;
import cn.flyrise.feep.form.contract.FormListContract;
import cn.flyrise.feep.form.presenter.FormListPresenter;
import java.util.List;

import cn.flyrise.android.protocol.model.FormTypeItem;
import cn.flyrise.feep.K;
import cn.flyrise.feep.core.base.component.FESearchListActivity;
import cn.flyrise.feep.form.adapter.FormListAdapter;

/**
 * 陈冕
 * 功能：流程搜索
 * Created by Administrator on 2016-3-17.
 * Update by klc on 2017-1-22.
 */
public class FormSearchActivity extends FESearchListActivity<FormTypeItem> implements FormListContract.View {

    private FormListAdapter mAdapter;
    private FormListPresenter mPresenter;

    @Override
    public void bindData() {
        super.bindData();
        mAdapter = new FormListAdapter(this);
        mPresenter = new FormListPresenter(this);
        setAdapter(mAdapter);
        setPresenter(mPresenter);
    }

    @Override
    public void bindListener() {
        super.bindListener();
        mAdapter.setOnItemClickListener((view, object) -> {
            final FormTypeItem typeItem = (FormTypeItem) object;
            final Intent intent = new Intent(FormSearchActivity.this, NewFormActivity.class);
            intent.putExtra(K.form.TITLE_DATA_KEY, typeItem.getFormName());
            intent.putExtra(K.form.URL_DATA_KEY, typeItem.getFormUrl());
            startActivity(intent);
        });
    }

    @Override
    public void searchData(String searchKey) {
        mPresenter.refreshListData(searchKey, null);
    }

    @Override
    public void loadMoreListData(List<FormTypeItem> dataList) {
        mAdapter.notifyDataSetChanged();
        setCanPullUp(mPresenter.hasMoreData());
    }
}
