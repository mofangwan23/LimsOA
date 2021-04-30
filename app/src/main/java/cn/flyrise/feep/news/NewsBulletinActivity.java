package cn.flyrise.feep.news;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import cn.flyrise.feep.FEApplication;
import cn.flyrise.feep.R;
import cn.flyrise.feep.commonality.MessageSearchActivity;
import cn.flyrise.feep.commonality.bean.FEListItem;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.base.views.LoadMoreRecyclerView;
import cn.flyrise.feep.core.function.Module;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.particular.ParticularActivity;
import cn.flyrise.feep.particular.ParticularIntent;
import cn.flyrise.feep.particular.presenter.ParticularPresenter;
import com.dk.view.badge.BadgeUtil;
import java.util.List;

/**
 * @author ZYP
 * @since 2016-11-09 13:17
 * 新闻 and 公告
 */
public abstract class NewsBulletinActivity extends BaseActivity implements NewsBulletinContract.IView {

	private boolean isLoading;
	private Handler mHandler = new Handler(Looper.getMainLooper());
	private LoadMoreRecyclerView mRecyclerView;
	private SwipeRefreshLayout mSwipeRefreshLayout;
	private NewsBulletinPresenter mNewsBulletinPresenter;
	private NewsBulletinAdapter mNewsBulletinAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mNewsBulletinPresenter = new NewsBulletinPresenter(this, menuInfo().getModuleId());
		setContentView(R.layout.activity_news_bulletin);
		mNewsBulletinPresenter.start();
	}

	@Override
	protected void toolBar(FEToolbar toolbar) {
		toolbar.setTitle(menuInfo().name);
	}

	public abstract Module menuInfo();

	@Override
	public void bindView() {
		mRecyclerView = (LoadMoreRecyclerView) findViewById(R.id.recyclerView);
		mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
	}

	@Override
	public void bindData() {
		mSwipeRefreshLayout.setColorSchemeResources(R.color.login_btn_defulit);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
		mRecyclerView.setItemAnimator(new DefaultItemAnimator());
		mRecyclerView.setAdapter(mNewsBulletinAdapter = new NewsBulletinAdapter());
		mNewsBulletinAdapter.setEmptyView(findViewById(R.id.ivErrorView));
	}

	@Override
	public void bindListener() {
		findViewById(R.id.layoutSearch).setOnClickListener(view -> {
			Intent intent = new Intent(NewsBulletinActivity.this, MessageSearchActivity.class);
			intent.putExtra(MessageSearchActivity.REQUESTTYPE, menuInfo().getModuleId());
			intent.putExtra(MessageSearchActivity.REQUESTNAME, menuInfo().name);
			startActivity(intent);
		});

		mRecyclerView.setOnLoadMoreListener(() -> {                         // 滑动到底部，触发加载更多事件
			if (!isLoading && mNewsBulletinPresenter.hasMoreData()) {
				isLoading = true;
				mNewsBulletinPresenter.request(mNewsBulletinPresenter.getCurrentPage() + 1);
			}
			else {
				mNewsBulletinAdapter.removeFooterView();
			}
		});

		mSwipeRefreshLayout.setOnRefreshListener(mNewsBulletinPresenter::refresh);  // 下拉刷新事件
		mNewsBulletinAdapter.setOnItemClickListener(this::openParticularActivity);  // item 点击事件
	}

	/**
	 * 开始加载
	 */
	@Override
	public void showLoading() {
		mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(true));
	}

	/**
	 * 获取数据成功
	 * @param feListItems 展示的列表
	 * @param hasMoreData 是否还有更多列表
	 */
	@Override
	public void fetchDataSuccess(List<FEListItem> feListItems, boolean hasMoreData) {
		isLoading = false;
		mHandler.postDelayed(() -> mSwipeRefreshLayout.setRefreshing(false), 1000);
		if (mNewsBulletinPresenter.getCurrentPage() == 1)
			mNewsBulletinAdapter.setListItems(feListItems);
		else mNewsBulletinAdapter.addFEListItem(feListItems);

		if (hasMoreData)
			mNewsBulletinAdapter.setFooterView(cn.flyrise.feep.core.R.layout.core_refresh_bottom_loading);
		else mNewsBulletinAdapter.removeFooterView();
	}

	/**
	 * 获取数据失败
	 */
	@Override
	public void fetchDataError(RepositoryException exception) {
		isLoading = false;
		mHandler.postDelayed(() -> mSwipeRefreshLayout.setRefreshing(false), 1000);
		int currentPage = mNewsBulletinPresenter.getCurrentPage();
		if (currentPage > 1) {
			mNewsBulletinPresenter.setCurrentPage(currentPage--);
			LinearLayoutManager layoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
			int lastVisibleItem = layoutManager.findLastVisibleItemPosition();
			if (lastVisibleItem == mNewsBulletinAdapter.getItemCount() - 1) {
				View footView = layoutManager.findViewByPosition(lastVisibleItem);
				if (footView != null) {
					int itemHeight = layoutManager.findViewByPosition(lastVisibleItem - 1).getHeight();
					int listViewHeight = mRecyclerView.getHeight();
					int itemCount = listViewHeight / itemHeight;
					int offest = listViewHeight % itemHeight;
					layoutManager.scrollToPositionWithOffset(mNewsBulletinAdapter.getDataSourceCount() - itemCount, offest);
				}
			}
		}
	}

	private void openParticularActivity(FEListItem feListItem) {
		if (feListItem == null) {
			return;
		}
		if (feListItem.isNews()) {
			FEApplication feApplication = (FEApplication) this.getApplicationContext();
			int num = feApplication.getCornerNum() - 1;
			BadgeUtil.setBadgeCount(this, num);//角标
			feApplication.setCornerNum(num);
		}
		new ParticularIntent.Builder(this)
				.setTargetClass(ParticularActivity.class)
				.setParticularType(
						menuInfo().getModuleId() == 5 ? ParticularPresenter.PARTICULAR_NEWS : ParticularPresenter.PARTICULAR_ANNOUNCEMENT)
				.setBusinessId(feListItem.getId())
				.setFEListItem(feListItem)
				.setListRequestType(menuInfo().getModuleId())
				.create()
				.start();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mNewsBulletinPresenter.onDestroy();
	}
}