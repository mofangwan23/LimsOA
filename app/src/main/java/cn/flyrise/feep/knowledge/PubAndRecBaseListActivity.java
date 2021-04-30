package cn.flyrise.feep.knowledge;

import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.List;

import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.android.library.utility.LoadingHint;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.base.views.PullAndLoadMoreRecyclerView;
import cn.flyrise.feep.knowledge.adpater.KnowledgeListBaseAdapter;
import cn.flyrise.feep.knowledge.contract.PubAndRecListContract;
import cn.flyrise.feep.knowledge.presenter.PubAndRecListPresenterImpl;
import cn.flyrise.feep.core.dialog.FEMaterialDialog;
import cn.flyrise.feep.core.base.views.FEToolbar;

/**
 * Created by KLC on 2016/12/8.
 */

public class PubAndRecBaseListActivity<T> extends BaseActivity implements PubAndRecListContract.View<T> {

    protected FEToolbar mToolbar;
    protected PullAndLoadMoreRecyclerView mListView;
    protected View mIv_empty;
    protected LinearLayout mBottomMenu;

    protected Handler mHandler;
    protected PubAndRecListContract.Presenter mPresenter;
    private KnowledgeListBaseAdapter<T> baseAdapter;

    @Override
    protected void toolBar(FEToolbar toolbar) {
        super.toolBar(toolbar);
        this.mToolbar = toolbar;
        toolbar.showNavigationIcon();
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    @Override
    public void bindView() {
        super.bindView();
        mListView = (PullAndLoadMoreRecyclerView) findViewById(R.id.listview);
        mIv_empty =  findViewById(R.id.error_layout);
        mBottomMenu = (LinearLayout) findViewById(R.id.llBottomMenu);
    }

    public void setBaseAdapter(KnowledgeListBaseAdapter<T> baseAdapter) {
        this.baseAdapter = baseAdapter;
    }

    public void setPresenter(PubAndRecListPresenterImpl mPresenter) {
        this.mPresenter = mPresenter;
    }

    @Override
    public void bindData() {
        super.bindData();
        mHandler = new Handler();
    }

    @Override
    public void bindListener() {
        super.bindListener();
        mListView.setRefreshListener(() -> mPresenter.refreshList());
        mListView.setLoadMoreListener(() -> mPresenter.loadMoreData());
    }


    @Override
    public void showDealLoading(boolean show) {
        if (show)
            LoadingHint.show(this);
        else
            LoadingHint.hide();
    }

    @Override
    public void showMessage(int resourceID) {
        FEToast.showMessage(getString(resourceID));
    }

    @Override
    public void showRefreshLoading(boolean show) {
        if (show)
            mListView.setRefreshing(true);
        else
            mHandler.postDelayed(() -> mListView.setRefreshing(false), 500);
    }


    @Override
    public void refreshListData(List<T> dataList) {
        baseAdapter.refreshData(dataList);
        setEmptyView();
    }

    @Override
    public void setEmptyView() {
        if (baseAdapter.getItemCount() == 0) {
            mIv_empty.setVisibility(View.VISIBLE);
        }
        else {
            mIv_empty.setVisibility(View.GONE);
        }
    }

    @Override
    public void loadMoreListData(List<T> dataList) {
        baseAdapter.addData(dataList);
    }

    @Override
    public void loadMoreListFail() {
        mListView.scrollLastItem2Bottom();
    }

    @Override
    public void dealComplete() {
        onBackPressed();
        mPresenter.refreshList();
    }

    @Override
    public void showConfirmDialog(int resourceID, FEMaterialDialog.OnClickListener onClickListener) {
        new FEMaterialDialog.Builder(this).setMessage(getString(resourceID))
                .setPositiveButton(null, onClickListener)
                .setNegativeButton(null, null)
                .build()
                .show();
    }

    @Override
    public void setCanPullUp(boolean hasMore) {
        if (hasMore)
            mListView.addFootView();
        else
            mListView.removeFootView();
    }
}
