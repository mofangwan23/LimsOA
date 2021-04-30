package cn.flyrise.feep.knowledge;

import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import cn.flyrise.feep.R;
import cn.flyrise.feep.knowledge.adpater.KnowledgeListBaseAdapter;
import cn.flyrise.feep.knowledge.adpater.PublishedListAdapter;
import cn.flyrise.feep.knowledge.model.PubAndRecFile;
import cn.flyrise.feep.knowledge.presenter.PubAndRecListPresenterImpl;
import cn.flyrise.feep.knowledge.util.KnowKeyValue;
import cn.flyrise.feep.core.base.views.FEToolbar;

/**
 * Created by KLC on 2016/12/7.
 */
public class PubFileListActivity extends PubAndRecBaseListActivity<PubAndRecFile> {

    private LinearLayout cancelPublishLayout;
    private PublishedListAdapter mAdapter;

    private Animation mShowAnimation;
    private Animation mHideAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.knowledge_pubished_file_list);
    }


    @Override
    protected void toolBar(FEToolbar toolbar) {
        super.toolBar(toolbar);
        toolbar.setTitle(getString(R.string.know_published));
    }

    @Override
    public void bindView() {
        super.bindView();
        findViewById(R.id.down_layout).setVisibility(View.GONE);
        findViewById(R.id.share_layout).setVisibility(View.GONE);
        findViewById(R.id.rename_layout).setVisibility(View.GONE);
        findViewById(R.id.move_layout).setVisibility(View.GONE);
        findViewById(R.id.delete_layout).setVisibility(View.GONE);
        cancelPublishLayout = (LinearLayout) findViewById(R.id.cancel_publish_layout);
        cancelPublishLayout.setVisibility(View.VISIBLE);
        mShowAnimation = AnimationUtils.loadAnimation(this, R.anim.push_bottom_in);
        mHideAnimation = AnimationUtils.loadAnimation(this, R.anim.push_bottom_out);
    }

    @Override
    public void bindData() {
        super.bindData();
        mAdapter = new PublishedListAdapter(this);
        mListView.setAdapter(mAdapter);
        setBaseAdapter(mAdapter);
        PubAndRecListPresenterImpl<PubAndRecFile> presenter = new PubAndRecListPresenterImpl<>(KnowKeyValue.PUBLISTTYPE, this);
        setPresenter(presenter);
        mHandler.postDelayed(presenter::refreshList, 500);
    }

    @Override
    public void bindListener() {
        super.bindListener();
        cancelPublishLayout.setOnClickListener(v -> mPresenter.cancelPublish(mAdapter.getDataList()));
        mAdapter.setOnItemLongClickListener((view, object) -> PubFileListActivity.this.onItemLongClick());
        mAdapter.setSelectAllOrNotListener(new KnowledgeListBaseAdapter.SelectAllOrNotListener() {
            @Override
            public void selectAll() {
                mToolbar.setRightText(R.string.know_unSelectAll);
            }

            @Override
            public void notAll() {
                mToolbar.setRightText(R.string.know_selectAll);
            }
        });
        mAdapter.setChoiceListener(choiceCount -> {
            if (choiceCount == 0) {
                cancelPublishLayout.setAlpha(0.3f);
                cancelPublishLayout.setEnabled(false);
            }
            else {
                cancelPublishLayout.setAlpha(1);
                cancelPublishLayout.setEnabled(true);
            }
        });
        mAdapter.setOnItemClickListener((view, object) -> FileDetailActivity.startFileDetailActivity(PubFileListActivity.this, (PubAndRecFile) object));
        mToolbar.setNavigationOnClickListener(v -> onBackPressed());
        mToolbar.setRightTextClickListener(v -> mAdapter.selectAllOrNoOne());
    }

    private void onItemLongClick() {
        if (mAdapter.isCanChoice()) {
            mAdapter.setCanChoice(false);
            mToolbar.showNavigationIcon();
            mToolbar.setRightTextVisbility(View.GONE);
            showBottomMenu(false);
            mListView.setCanRefresh(true);
        }
        else {
            mAdapter.setCanChoice(true);
            mToolbar.setRightText(R.string.know_selectAll);
            showBottomMenu(true);
            mListView.setCanRefresh(false);
        }
    }

    public void showBottomMenu(boolean show) {
        if (show) {
            mBottomMenu.setVisibility(View.VISIBLE);
            mBottomMenu.startAnimation(mShowAnimation);
        }
        else {
            mBottomMenu.setVisibility(View.GONE);
            mBottomMenu.startAnimation(mHideAnimation);
        }
    }

    @Override
    public void onBackPressed() {
        if (mAdapter.isCanChoice())
            onItemLongClick();
        else
            finish();
    }
}
