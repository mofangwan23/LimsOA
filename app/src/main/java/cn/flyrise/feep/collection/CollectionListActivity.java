package cn.flyrise.feep.collection;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.text.TextUtils;
import android.view.View;

import cn.flyrise.android.library.utility.LoadingHint;
import cn.flyrise.feep.R;
import cn.flyrise.feep.collection.adapter.CollectionListAdapter;
import cn.flyrise.feep.collection.bean.CollectionEvent;
import cn.flyrise.feep.collection.bean.Favorite;
import cn.flyrise.feep.collection.protocol.CollectionCheckRequest;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.base.views.LoadMoreRecyclerView;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.X.Func;
import cn.flyrise.feep.core.common.X.RequestType;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.function.FunctionManager;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.core.network.request.ResponseContent;
import cn.flyrise.feep.media.attachments.AttachmentViewer;
import cn.flyrise.feep.media.attachments.bean.TaskInfo;
import cn.flyrise.feep.media.attachments.downloader.DownloadConfiguration;
import cn.flyrise.feep.media.attachments.listener.SimpleAttachmentViewerListener;
import cn.flyrise.feep.media.attachments.repository.AttachmentDataSource;
import cn.flyrise.feep.meeting7.ui.component.StatusView;
import cn.flyrise.feep.particular.ParticularActivity;
import cn.flyrise.feep.particular.ParticularIntent;
import cn.flyrise.feep.particular.presenter.ParticularPresenter;
import cn.flyrise.feep.utils.Patches;
import cn.flyrise.feep.workplan7.PlanDetailActivity;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author ZYP
 * @since 2018-05-21 11:34
 * 收藏夹列表
 */
public class CollectionListActivity extends BaseActivity implements CollectionListAdapter.OnCheckBoxClickListener,CollectionListAdapter.OnChoiceListener {

	private FEToolbar mToolbar;
	private LoadMoreRecyclerView mRecyclerView;
	private CollectionListAdapter mAdapter;

	private LinearLayoutManager mLayoutManager;
	private SwipeRefreshLayout mSwipeRefreshLayout;

	private FavoriteRepository mRepository;
	private String mFavoriteId;
	private int mPage = 1;
	private boolean hasNextPage = false;
	protected boolean mIsLoading;
	private AttachmentViewer mViewer;
	private StatusView mStatusView;

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EventBus.getDefault().register(this);
		mRepository = new FavoriteRepository();
		DownloadConfiguration configuration = new DownloadConfiguration.Builder()
				.owner(CoreZygote.getLoginUserServices().getUserId())
				.downloadDir(CoreZygote.getPathServices().getKnowledgeCachePath())
				.encryptDir(CoreZygote.getPathServices().getSafeFilePath())
				.decryptDir(CoreZygote.getPathServices().getTempFilePath())
				.create();
		AttachmentDataSource dataSource = new AttachmentDataSource(CoreZygote.getContext());
		this.mViewer = new AttachmentViewer(dataSource, configuration);
		this.mViewer.setAttachmentViewerListener(new XSimpleAttachmentViewerListener());
		setContentView(R.layout.activity_collection_list);
	}

	@Override protected void toolBar(FEToolbar toolbar) {
		this.mToolbar = toolbar;
	}

	@Override public void bindView() {
		this.mSwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
		this.mRecyclerView = findViewById(R.id.recyclerView);
		this.mRecyclerView.setLayoutManager(mLayoutManager = new LinearLayoutManager(this));
		this.mRecyclerView.setItemAnimator(new DefaultItemAnimator());
		this.mRecyclerView.setAdapter(mAdapter = new CollectionListAdapter(this));

		this.mAdapter.setEmptyView(findViewById(R.id.layoutEmptyView));
        this.mStatusView = findViewById(R.id.list_statusview);
	}

	@Override public void bindData() {
		this.mSwipeRefreshLayout.setColorSchemeResources(R.color.defaultColorAccent);
		this.mSwipeRefreshLayout.setOnRefreshListener(() -> refreshFavoriteList(mPage = 1));

		this.mFavoriteId = getIntent().getStringExtra("favoriteId");
		String favoriteName = getIntent().getStringExtra("favoriteName");
		mToolbar.setTitle(favoriteName);
		mToolbar.setRightTextClickListener(v -> {
			onItemLongClick();
		});

		mToolbar.setRightTextViewLeftTextClickListener(v -> {
			StringBuilder businessIds = new StringBuilder();
			StringBuilder types = new StringBuilder();
			List<Favorite> favoriteList = mAdapter.getSelectionFavoriteList();
			if(favoriteList.size()==0){
				FEToast.showMessage("请选择删除项");
				return;
			}
			for (int i=0;i < favoriteList.size();i++){
				Favorite favorite = favoriteList.get(i);
				if(favoriteList.size()==1 || i == favoriteList.size()-1){
					businessIds.append(favorite.id);
					if (favorite.type!=null){
						types.append(favorite.type);
					}
				}else {
					businessIds.append(favorite.id).append(",");
					if(favorite.type!=null){
						types.append(favorite.type).append(",");
					}
				}

			}
			removeFromFavoriteFolder(mFavoriteId,businessIds.toString(),types.toString());
		});
		this.mRecyclerView.setOnLoadMoreListener(() -> {
			if (!mIsLoading && hasNextPage) {
				mIsLoading = true;
				refreshFavoriteList(++mPage);
			}
			else if (!hasNextPage) {
				mAdapter.removeFooterView();
			}
		});

		this.mRecyclerView.addOnScrollListener(new OnScrollListener() {
			@Override public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
				if (newState == RecyclerView.SCROLL_STATE_IDLE) {
					int firstVisibleItemPosition = mLayoutManager.findFirstVisibleItemPosition();
					if (firstVisibleItemPosition == 0 && !recyclerView.canScrollVertically(-1)) {
						mSwipeRefreshLayout.setEnabled(true);
					}
					else {
						mSwipeRefreshLayout.setEnabled(false);
					}
				}
			}
		});

		this.mAdapter.setOnItemClickListener(favorite -> {
			LoadingHint.show(CollectionListActivity.this);
			Observable
					.unsafeCreate(f -> {
						CollectionCheckRequest request = CollectionCheckRequest.request(favorite.id, favorite.type);
						FEHttpClient.getInstance().post(request, new ResponseCallback<ResponseContent>() {
							@Override public void onCompleted(ResponseContent response) {
								if (response == null) {
									onFailure(null);
									return;
								}

								if (!TextUtils.equals(response.getErrorCode(), "0")) {
									String errorMessage = response.getErrorMessage();
									if (TextUtils.isEmpty(errorMessage)) {
										errorMessage = getString(R.string.lbl_retry_operator);
									}
									f.onNext(errorMessage);
								}
								else {
									f.onNext(null);
								}
							}

							@Override public void onFailure(RepositoryException repository) {
								f.onError(repository.exception());
							}
						});
					})
					.subscribeOn(Schedulers.io())
					.observeOn(AndroidSchedulers.mainThread())
					.subscribe(result -> {
						LoadingHint.hide();
						if (result == null) {
							enterBusinessPage(favorite);
						}
						else {
							FEToast.showMessage(result.toString());
						}
					}, exception -> {
						LoadingHint.hide();
						exception.printStackTrace();
						FEToast.showMessage(getString(R.string.lbl_retry_operator));
					});
		});

		this.mAdapter.setOnItemLongClickListener((view, object) -> {
			onItemLongClick();
		});

		this.mAdapter.setOnCheckBoxClickListener(this);

		this.mAdapter.setOnChoiceListener(this);

		this.refreshFavoriteList(mPage = 1);
	}

	protected void stopRefreshing() {
		mSwipeRefreshLayout.postDelayed(() -> mSwipeRefreshLayout.setRefreshing(false), 1000);
	}

	private void refreshFavoriteList(int page) {
		mRepository.queryFavoriteFromFolder(mFavoriteId, page)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(favoriteData -> {

					this.mIsLoading = false;
					this.hasNextPage = favoriteData.hasNextPage;
					if (mPage == 1) {
						stopRefreshing();
						mAdapter.setDataSources(favoriteData.favorites);
						mRecyclerView.scrollToPosition(0);
					}
					else {
						mAdapter.appendDataSources(favoriteData.favorites);
					}

					if (hasNextPage) {
						mAdapter.setFooterView(cn.flyrise.feep.core.R.layout.core_refresh_bottom_loading);
					}

					if(favoriteData.favorites.size()==0){
						mStatusView.setVisibility(View.VISIBLE);
						mToolbar.setRightTextVisbility(View.GONE);
						mToolbar.setRightLeftTextVisbility(View.GONE);
					}else {
						mStatusView.setVisibility(View.GONE);
						mToolbar.setRightText("编辑");
						mAdapter.setCanChoice(true);
						onItemLongClick();
					}

				}, exception -> {
					if (mPage == 1) {
						stopRefreshing();
					}

					mIsLoading = false;
					if (mPage > 1) {
						mPage--;
					}
				});
	}

	private void enterBusinessPage(Favorite favorite) {
		int type = CommonUtil.parseInt(favorite.type);
		int particularType = 0;
		int requestType = -1;

		if (FunctionManager.hasPatch(Patches.PATCH_PLAN) && type == Func.Plan) {
			PlanDetailActivity.Companion.startActivity(CollectionListActivity.this, "", favorite.id);
			return;
		}

		if (type == Func.Knowledge) {
			openFileDetail(favorite);
			return;
		}

		switch (type) {
			case Func.Done:
				particularType = ParticularPresenter.PARTICULAR_COLLABORATION;
				requestType = RequestType.Done;
				break;
			case Func.Sended:
				particularType = ParticularPresenter.PARTICULAR_COLLABORATION;
				requestType = RequestType.Sended;
				break;
			case Func.News:
				particularType = ParticularPresenter.PARTICULAR_NEWS;
				requestType = RequestType.News;
				break;
			case Func.Announcement:
				particularType = ParticularPresenter.PARTICULAR_ANNOUNCEMENT;
				requestType = RequestType.Announcement;
				break;
			case Func.Plan:
				particularType = ParticularPresenter.PARTICULAR_ANNOUNCEMENT;
				requestType = RequestType.Announcement;
				break;
		}

        if(requestType == 0) return;
		new ParticularIntent.Builder(CollectionListActivity.this)
				.setParticularType(particularType)
				.setTargetClass(ParticularActivity.class)
				.setBusinessId(favorite.id)
				.setListRequestType(requestType)
				.create()
				.start();
	}

	private void openFileDetail(Favorite favorite) {
		LoadingHint.show(this);
		String url = FEHttpClient.getInstance().getHost() + FEHttpClient.KNOWLEDGE_DOWNLOAD_PATH + favorite.id;
		String taskId = favorite.id;
		String fileName = favorite.title;
		mViewer.openAttachment(url, taskId, fileName);

		LoadingHint.setOnKeyDownListener((keyCode, event) -> {
			TaskInfo taskInfo = mViewer.createTaskInfo(url, taskId, fileName);
			mViewer.getDownloader().deleteDownloadTask(taskInfo);
		});
	}

	private void onItemLongClick(){
	    if(mAdapter.canChoice()){
            mAdapter.setCanChoice(false);
			mToolbar.setRightText("编辑");
			mToolbar.setRightLeftTextVisbility(View.GONE);
		}else {
            mAdapter.setCanChoice(true);
			mToolbar.setRightText(R.string.cancel);
		}
    }


	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onCollectionChangeEvent(CollectionEvent event) {
		if (event.code == 200) {
			this.refreshFavoriteList(mPage = 1);
		}
	}

	@Override protected void onDestroy() {
		EventBus.getDefault().unregister(this);
		super.onDestroy();
	}

	@Override
	public void onCheckBoxClickListener(List<Favorite> favoriteList) {
		if(favoriteList.size()>0){
			mToolbar.setRightTextViewLeft(getString(R.string.delete));
		}else {
			mToolbar.setRightTextViewLeft("");
		}
	}

	@Override
	public void onChoiceListener(List<Favorite> favoriteList) {

	}

	private class XSimpleAttachmentViewerListener extends SimpleAttachmentViewerListener {

		@Override public void prepareOpenAttachment(Intent intent) {
			LoadingHint.hide();
			if (intent == null) {
				FEToast.showMessage("暂不支持查看此文件类型");
				return;
			}

			try {
				startActivity(intent);
			} catch (Exception exp) {
				FEToast.showMessage("无法打开，建议安装查看此类型文件的软件");
			}
		}

		@Override public void onDownloadFailed() {
			FEToast.showMessage(R.string.know_open_fail);
			LoadingHint.hide();
		}

		@Override public void onDownloadProgressChange(int progress) {
			LoadingHint.showProgress(progress, getString(R.string.know_opening));
		}

		@Override public void onDecryptFailed() {
			FEToast.showMessage(R.string.know_open_fail);
			LoadingHint.hide();
		}

		@Override public void onDecryptProgressChange(int progress) {
			LoadingHint.showProgress(progress, getString(R.string.know_decode_open));
		}
	}

	protected void removeFromFavoriteFolder(String favoriteId, String businessId, String removeType) {
		if (mRepository == null) {
			mRepository = new FavoriteRepository();
		}

		LoadingHint.show(this);
		mRepository.removeFromFolder(favoriteId, businessId, removeType)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(result -> {
					LoadingHint.hide();
					if (result.errorCode == 0) {
						FEToast.showMessage("删除成功");
						refreshFavoriteList(1);
						return;
					}
					FEToast.showMessage(result.errorMessage);
				}, exception -> {
					LoadingHint.hide();
					FEToast.showMessage("取消收藏失败，请稍后重试！");
				});
	}
}
