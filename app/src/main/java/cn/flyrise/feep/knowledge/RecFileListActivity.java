package cn.flyrise.feep.knowledge;

import android.os.Bundle;
import android.view.View;

import java.util.List;

import cn.flyrise.feep.R;
import cn.flyrise.feep.knowledge.adpater.ReceiverFileListAdapter;
import cn.flyrise.feep.knowledge.model.PubAndRecFile;
import cn.flyrise.feep.knowledge.presenter.PubAndRecListPresenterImpl;
import cn.flyrise.feep.knowledge.util.KnowKeyValue;
import cn.flyrise.feep.core.base.views.FEToolbar;


/**
 * Created by KLC on 2016/12/7.
 */
public class RecFileListActivity extends PubAndRecBaseListActivity<PubAndRecFile> {

    private ReceiverFileListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.knowledge_pubished_file_list);
    }

    @Override
    protected void toolBar(FEToolbar toolbar) {
        super.toolBar(toolbar);
        toolbar.setTitle(getString(R.string.know_receive));
    }

    @Override
    public void bindView() {
        super.bindView();
        mBottomMenu.setVisibility(View.GONE);
    }

    @Override
    public void bindData() {
        super.bindData();
        mAdapter = new ReceiverFileListAdapter(this);
        mListView.setAdapter(mAdapter);
        setBaseAdapter(mAdapter);
        PubAndRecListPresenterImpl<PubAndRecFile> presenter = new PubAndRecListPresenterImpl<>(KnowKeyValue.RECLISTTYPE, this);
        setPresenter(presenter);
        mHandler.postDelayed(presenter::refreshList, 500);
    }

    @Override
    public void bindListener() {
        super.bindListener();
        mAdapter.setOnItemClickListener((view, object) -> FileDetailActivity.startFileDetailActivity(RecFileListActivity.this, (PubAndRecFile) object));
    }

    @Override
    public void refreshListData(List<PubAndRecFile> dataList) {
        super.refreshListData(dataList);
        mListView.scroll2Top();
    }
}
