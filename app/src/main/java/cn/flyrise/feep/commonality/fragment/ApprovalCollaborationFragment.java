package cn.flyrise.feep.commonality.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import cn.flyrise.android.protocol.entity.ListRequest;
import cn.flyrise.android.protocol.entity.ListResponse;
import cn.flyrise.android.protocol.model.ListDataItem;
import cn.flyrise.android.protocol.model.ListTable;
import cn.flyrise.feep.FEApplication;
import cn.flyrise.feep.R;
import cn.flyrise.feep.commonality.adapter.ApprovalCollaborationAdapter;
import cn.flyrise.feep.commonality.bean.FEListItem;
import cn.flyrise.feep.core.base.views.LoadMoreRecyclerView;
import cn.flyrise.feep.core.base.views.adapter.BaseMessageRecyclerAdapter;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.X.RequestType;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.function.AppSubMenu;
import cn.flyrise.feep.core.function.FunctionManager;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.core.watermark.WMAddressDecoration;
import cn.flyrise.feep.core.watermark.WMStamp;
import cn.flyrise.feep.event.EventMessageDisposeSuccess;
import cn.flyrise.feep.notification.NotificationController;
import cn.flyrise.feep.particular.ParticularActivity;
import cn.flyrise.feep.particular.ParticularIntent;
import cn.flyrise.feep.particular.presenter.ParticularPresenter;
import cn.flyrise.feep.utils.Patches;
import com.dk.view.badge.BadgeUtil;
import java.util.ArrayList;
import java.util.List;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * @author ZYP
 * @since 2016/8/16 11:23
 */
public class ApprovalCollaborationFragment extends Fragment {

	private static final int PAGE_SIZE = 20;
	private AppSubMenu mMenuInfo;
	private View mEmptyView;
	private LinearLayoutManager mLayoutManager;
	private LoadMoreRecyclerView mRecyclerView;
	private SwipeRefreshLayout mSwipeRefreshLayout;
	private ApprovalCollaborationAdapter mApprovalAdapter;

	private int mCurrentPage;
	private boolean mIsLoading;
	private int mTotalApprovalSize;
	private ListRequest mApprovalListRequest;

	private String messageId;
	private String lastMessageId;
	private OnApprovalListener mListener;

	public static ApprovalCollaborationFragment newInstance(AppSubMenu menuInfo) {
		if (menuInfo == null || menuInfo.menuId == -1) {
			throw new NullPointerException("The MenuInfo can not be null.");
		}
		ApprovalCollaborationFragment fragment = new ApprovalCollaborationFragment();
		fragment.setMenuInf(menuInfo);
		return fragment;
	}

	public void setListener(OnApprovalListener listener){
		mListener=listener;
	}

	public void setMenuInf(AppSubMenu menuInfo) {
		this.mMenuInfo = menuInfo;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		EventBus.getDefault().register(this);
		View view = inflater.inflate(R.layout.fragment_approval_collaboration, container, false);
		mRecyclerView = view.findViewById(R.id.recyclerView);
		mSwipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
		mEmptyView = view.findViewById(R.id.ivEmptyView);
		bindData();
		setListener();
		return view;
	}

	private void bindData() {
		mSwipeRefreshLayout.setColorSchemeResources(R.color.defaultColorAccent);
		mRecyclerView.setItemAnimator(new DefaultItemAnimator());
		mRecyclerView.setLayoutManager(mLayoutManager = new LinearLayoutManager(getActivity()));
		mApprovalAdapter = new ApprovalCollaborationAdapter();
		mApprovalAdapter.setHasStableIds(true);
		mRecyclerView.setAdapter(mApprovalAdapter);

		String watermark = WMStamp.getInstance().getWaterMarkText();
		mRecyclerView.addItemDecoration(new WMAddressDecoration(watermark));

		mApprovalAdapter.setEmptyView(mEmptyView);
	}

	private void setListener() {
		mSwipeRefreshLayout.setOnRefreshListener(() -> requestApprovalLists(mCurrentPage = 1, PAGE_SIZE + "", true));

		mRecyclerView.setOnLoadMoreListener(() -> {
			if (!mIsLoading && isLoaderMore()) {
				mIsLoading = true;
				mApprovalAdapter.setLoadState(BaseMessageRecyclerAdapter.LOADING);
				requestApprovalLists(++mCurrentPage, PAGE_SIZE + "", false);
			}
			else {
				mApprovalAdapter.setLoadState(BaseMessageRecyclerAdapter.LOADING_END);
			}
		});

		mApprovalAdapter.setOnApprovalItemClickListener(feListItem -> {
			if (mMenuInfo.menuId == RequestType.ToDo
					&& TextUtils.isEmpty(feListItem.getLevel())
					&& mApprovalAdapter.hasLevelItem()) {
				FEToast.showMessage(getString(R.string.collaboration_higher_task_hint));
			}
			if (feListItem == null) return;
			messageId = feListItem.getId();
			if (feListItem.isNews()) {
				NotificationController.messageReaded(getActivity(), feListItem.getId());
				FEApplication feApplication = (FEApplication) getActivity().getApplicationContext();
				int num = feApplication.getCornerNum() - 1;
				BadgeUtil.setBadgeCount(getActivity(), num);//角标
				feApplication.setCornerNum(num);
			}
			new ParticularIntent.Builder(getActivity())
					.setParticularType(ParticularPresenter.PARTICULAR_COLLABORATION)
					.setTargetClass(ParticularActivity.class)
					.setBusinessId(feListItem.getId())
					.setListRequestType(mMenuInfo.menuId)
					.create()
					.start();
		});

		mRecyclerView.addOnScrollListener(new OnScrollListener() {

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
	}

	private boolean isLoaderMore() {
		if (FunctionManager.hasPatch(Patches.PATCH_NO_REFRESH_LIST) && mMenuInfo.menuId == RequestType.ToDo) {
			return mTotalApprovalSize > PAGE_SIZE;
		}
		return mApprovalAdapter.needAddFooter(mTotalApprovalSize);
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		startRefreshList();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!FunctionManager.hasPatch(Patches.PATCH_NO_REFRESH_LIST)) {
			startRefreshList();
		}
	}

	public void startRefreshList() {
		mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(true));
		requestApprovalLists(mCurrentPage = 1, PAGE_SIZE + "", true);
	}

	private void requestApprovalLists(int pageNumber, String PageNums, final boolean isRefresh) {
		if (mApprovalListRequest == null) {
			mApprovalListRequest = new ListRequest();
			mApprovalListRequest.setSearchKey("");
			mApprovalListRequest.setPerPageNums(PAGE_SIZE + "");
			mApprovalListRequest.setRequestType(mMenuInfo.menuId);
		}
		mApprovalListRequest.setPerPageNums(PageNums);
		mApprovalListRequest.setPage(pageNumber + "");
		mApprovalListRequest.setLastMessageId(getMessageId(isRefresh));

		FEHttpClient.getInstance().post(mApprovalListRequest, new ResponseCallback<ListResponse>(getActivity()) {
			@Override
			public void onCompleted(ListResponse listResponse) {
				if (isRefresh) stopRefreshing();
				mApprovalAdapter.setLoadState(BaseMessageRecyclerAdapter.LOADING_COMPLETE);
				mIsLoading = false;
				mTotalApprovalSize = CommonUtil.parseInt(listResponse.getTotalNums());

				if (mListener!=null) {
					mListener.messageSize(mTotalApprovalSize);
				}

				List<FEListItem> listItems = convertToFEListItems(listResponse.getTable(), getSearchType() == 0);
				if (!CommonUtil.isEmptyList(listItems) && listItems.get(listItems.size() - 1) != null) {
					lastMessageId = listItems.get(listItems.size() - 1).getId();
				}
				if (isRefresh) {
					mApprovalAdapter.setApprovalLists(listItems);
				}
				else {
					mApprovalAdapter.addApprovalLists(listItems);
				}

				if (mCurrentPage == 1) {
					mRecyclerView.scrollToPosition(0);
				}
			}

			@Override
			public void onFailure(RepositoryException repositoryException) {
				if (isRefresh) {
					stopRefreshing();
				}
				mApprovalAdapter.setLoadState(BaseMessageRecyclerAdapter.LOADING_COMPLETE);
				mIsLoading = false;
				if (mCurrentPage > 1) {
					mCurrentPage--;
				}
			}
		});
	}

	private String getMessageId(boolean isRefresh) {
		if (!FunctionManager.hasPatch(Patches.PATCH_NO_REFRESH_LIST)) return "";
		if (mMenuInfo.menuId != RequestType.ToDo) return "";
		return isRefresh ? "" : lastMessageId;
	}

	private void stopRefreshing() {
		mSwipeRefreshLayout.postDelayed(() -> mSwipeRefreshLayout.setRefreshing(false), 1000);
	}

	public static List<FEListItem> convertToFEListItems(ListTable table, boolean isToDo) {
		if (table == null) {
			return null;
		}

		List<List<ListDataItem>> listDataItems = table.getTableRows();
		if (CommonUtil.isEmptyList(listDataItems)) {
			return null;
		}
		final List<FEListItem> listItems = new ArrayList<>();
		for (final List<ListDataItem> listItem : listDataItems) {
			final FEListItem item = new FEListItem();
			for (final ListDataItem listDataItem : listItem) {
				if ("id".equals(listDataItem.getName())) {
					item.setId(listDataItem.getValue());
				}
				else if ("title".equals(listDataItem.getName())) {
					item.setTitle(listDataItem.getValue());
				}
				else if ("sendTime".equals(listDataItem.getName())) {
					item.setSendTime(listDataItem.getValue());
				}
				else if ("sendUser".equals(listDataItem.getName())) {
					item.setSendUser(listDataItem.getValue());
				}
				else if ("sendUserId".equals(listDataItem.getName())) {
					item.setSendUserId(listDataItem.getValue());
				}
				else if ("sendUserImg".equals(listDataItem.getName())) {
					item.setImageHerf(listDataItem.getValue());
				}
				else if ("important".equals(listDataItem.getName()) && isToDo) {
					item.setImportant(listDataItem.getValue());
				}
				else if ("level".equals(listDataItem.getName())) {
					item.setLevel(listDataItem.getValue());
				}
			}
			listItems.add(item);
		}
		return listItems;
	}

	public int getSearchType() {
		return this.mMenuInfo.menuId;
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void eventRefreshList(EventMessageDisposeSuccess disposeSuccess) {
		if (disposeSuccess.isRefresh) {
			startRefreshList();
			return;
		}
		if (!FunctionManager.hasPatch(Patches.PATCH_NO_REFRESH_LIST)) return;
		if (mMenuInfo.menuId != RequestType.ToDo || TextUtils.isEmpty(messageId)) return;
		if (mApprovalAdapter.removeMessage(messageId)) {
//			requestApprovalLists(++mCurrentPage, 1 + "", false);
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		EventBus.getDefault().unregister(this);
	}

	public interface OnApprovalListener{
		void messageSize(int size);
	}
}
