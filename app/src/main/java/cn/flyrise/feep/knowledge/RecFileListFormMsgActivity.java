package cn.flyrise.feep.knowledge;

import android.os.Bundle;
import android.view.View;

import cn.flyrise.feep.R;
import cn.flyrise.feep.knowledge.adpater.RecFileListFormMsgAdapter;
import cn.flyrise.feep.knowledge.model.FileDetail;
import cn.flyrise.feep.knowledge.presenter.PubAndRecListPresenterImpl;
import cn.flyrise.feep.knowledge.util.KnowKeyValue;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.squirtlez.frouter.annotations.Route;


/**
 * Created by KLC on 2016/12/7.
 */
@Route("/knowledge/native/RecFileFromMsg")
public class RecFileListFormMsgActivity extends PubAndRecBaseListActivity<FileDetail> {

	private RecFileListFormMsgAdapter mAdapter;

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
		mAdapter = new RecFileListFormMsgAdapter(this);
		mListView.setAdapter(mAdapter);
		setBaseAdapter(mAdapter);
		String msgID = getIntent().getStringExtra(KnowKeyValue.EXTRA_RECEIVERMSAID);
		PubAndRecListPresenterImpl<FileDetail> presenter = new PubAndRecListPresenterImpl<>(msgID, this);
		setPresenter(presenter);
		mHandler.postDelayed(presenter::refreshList, 500);
	}

	@Override
	public void bindListener() {
		super.bindListener();
		mAdapter.setOnItemClickListener((view, object) -> FileDetailActivity.startFileDetailActivity(RecFileListFormMsgActivity.this, (FileDetail) object));
	}


}
