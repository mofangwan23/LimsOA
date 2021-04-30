package cn.flyrise.feep.main.message.task;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import cn.flyrise.android.protocol.entity.ListRequest;
import cn.flyrise.android.protocol.entity.ListResponse;
import cn.flyrise.android.protocol.model.ListDataItem;
import cn.flyrise.android.protocol.model.ListTable;
import cn.flyrise.feep.FEApplication;
import cn.flyrise.feep.commonality.bean.FEListItem;
import cn.flyrise.feep.core.base.views.adapter.BaseMessageRecyclerAdapter;
import cn.flyrise.feep.core.common.X.RequestType;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.function.AppSubMenu;
import cn.flyrise.feep.core.function.FunctionManager;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.event.EventMessageDisposeSuccess;
import cn.flyrise.feep.main.message.BaseMessageAdapter;
import cn.flyrise.feep.main.message.MessageFragment;
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
 * @since 2017-03-30 16:46
 */
public class TaskMessageFragment extends MessageFragment<FEListItem> {

	private static final int PAGE_SIZE = 20;
	private ListRequest mApprovalListRequest;
	private AppSubMenu mMenuInfo;

	private String messageId;
	private String lastMessageId;

	public static TaskMessageFragment newInstance(AppSubMenu menuInfo) {
		if (menuInfo == null || menuInfo.menuId == -1) {
			throw new NullPointerException("The MenuInfo can not be null.");
		}
		TaskMessageFragment fragment = new TaskMessageFragment();
		fragment.setMenuInf(menuInfo);
		return fragment;
	}

	public void setMenuInf(AppSubMenu menuInfo) {
		this.mMenuInfo = menuInfo;
	}

	@Nullable @Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		if (!EventBus.getDefault().isRegistered(this)) {//加上判断) {
			EventBus.getDefault().register(this);
		}
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		startRefreshList();
	}

	public void startRefreshList() {
		mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(true));
		requestMessage(mCurrentPage = 1, true);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!FunctionManager.hasPatch(Patches.PATCH_NO_REFRESH_LIST)) {
			startRefreshList();
		}
	}

	@Override
	public BaseMessageAdapter<FEListItem> getMessageAdapter() {
		return new TaskMessageAdapter(mMenuInfo);
	}

	@Override
	protected void bindListener() {
		super.bindListener();
		mAdapter.setOnMessageClickListener((feListItem, position) -> {
			messageId = feListItem.getId();
			if (feListItem.isNews()) {
				FEApplication feApplication = (FEApplication) getActivity().getApplicationContext();
				int num = feApplication.getCornerNum() - 1;
				BadgeUtil.setBadgeCount(getActivity(), num);//角标
				feApplication.setCornerNum(num);
				if (mAdapter instanceof TaskMessageAdapter) ((TaskMessageAdapter) mAdapter).markupMessageRead(messageId);
			}
			new ParticularIntent.Builder(getActivity())
					.setParticularType(ParticularPresenter.PARTICULAR_COLLABORATION)
					.setTargetClass(ParticularActivity.class)
					.setBusinessId(feListItem.getId())
					.setListRequestType(mMenuInfo.menuId)
					.create().start();
		});
	}

	@Override
	public String getMessageTitle(Object clickObject) {
		return ((FEListItem) clickObject).getTitle();
	}

	@Override
	public void requestMessage(int pageNumber, boolean isRefresh) {
		requestMessage(pageNumber, PAGE_SIZE + "", isRefresh);
	}

	private String getMessageId(boolean isRefresh) {
		if (!FunctionManager.hasPatch(Patches.PATCH_NO_REFRESH_LIST)) return "";
		if (mMenuInfo.menuId != RequestType.ToDo) return "";
		return isRefresh ? "" : lastMessageId;
	}

	public void requestMessage(int pageNumber, String pageNum, boolean isRefresh) {
		if (mApprovalListRequest == null) {
			mApprovalListRequest = new ListRequest();
			mApprovalListRequest.setSearchKey("");
			mApprovalListRequest.setPerPageNums(PAGE_SIZE + "");
			mApprovalListRequest.setRequestType(mMenuInfo.menuId);
		}
		mApprovalListRequest.setPage(pageNumber + "");
		mApprovalListRequest.setLastMessageId(getMessageId(isRefresh));
		mApprovalListRequest.setPerPageNums(pageNum);

		FEHttpClient.getInstance().post(mApprovalListRequest, new ResponseCallback<ListResponse>(getActivity()) {
			@Override
			public void onCompleted(ListResponse listResponse) {
				if (isRefresh) {
					mSwipeRefreshLayout.postDelayed(() -> mSwipeRefreshLayout.setRefreshing(false), 1000);
				}

				mIsLoading = false;
				mTotalSize = CommonUtil.parseInt(listResponse.getTotalNums());
				mAdapter.setLoadState(BaseMessageRecyclerAdapter.LOADING_COMPLETE);
				List<FEListItem> listItems = convertToFEListItems(listResponse.getTable());
				if (!CommonUtil.isEmptyList(listItems) && listItems.get(listItems.size() - 1) != null) {
					lastMessageId = listItems.get(listItems.size() - 1).getId();
				}
				if (isRefresh) {
					mAdapter.setDataSource(listItems);
					mRecyclerView.scrollToPosition(0);
				}
				else {
					mAdapter.addDataSource(listItems);
				}

				if (mAdapter.needAddFooter(mTotalSize)) {
					mAdapter.setFooterView(cn.flyrise.feep.core.R.layout.core_refresh_bottom_loading);
				}
			}

			@Override
			public void onFailure(RepositoryException repositoryException) {
				mIsLoading = false;
				mSwipeRefreshLayout.postDelayed(() -> mSwipeRefreshLayout.setRefreshing(false), 1000);
				if (mCurrentPage > 1) {
					mCurrentPage--;
				}
				mRecyclerView.scrollLastItem2Bottom(mAdapter);
			}
		});
	}

	@Override
	public boolean isLoaderMore() {
		if (FunctionManager.hasPatch(Patches.PATCH_NO_REFRESH_LIST)
				&& mMenuInfo.menuId == RequestType.ToDo) {
			return mTotalSize > PAGE_SIZE;
		}
		return mAdapter.needAddFooter(mTotalSize);
	}

	private List<FEListItem> convertToFEListItems(ListTable table) {
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
				else if ("sendUserImg".equals(listDataItem.getName())) {
					item.setImageHerf(listDataItem.getValue());
				}
				else if ("isNews".equals(listDataItem.getName())) {
					item.setNews(TextUtils.equals(listDataItem.getValue(), "true"));
				}
				else if ("important".equals(listDataItem.getName())
						&& mMenuInfo.menuId == RequestType.ToDo) {
					item.setImportant(listDataItem.getValue());
				}
				else if ("level".equals(listDataItem.getName())) {
					item.setLevel(listDataItem.getValue());
				}
				else if ("sendUserId".equals(listDataItem.getName())) {
					item.setSendUserId(listDataItem.getValue());
				}
//				else if(""){
//
//				}
			}
			listItems.add(item);
		}
		return listItems;
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void eventRefreshList(EventMessageDisposeSuccess disposeSuccess) {
		if (disposeSuccess.isRefresh) {
			startRefreshList();
			return;
		}
		if (!FunctionManager.hasPatch(Patches.PATCH_NO_REFRESH_LIST)) return;
		if (mMenuInfo.menuId != RequestType.ToDo
				|| TextUtils.isEmpty(messageId) || !(mAdapter instanceof TaskMessageAdapter)) return;
		((TaskMessageAdapter) mAdapter).removeMessage(messageId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (EventBus.getDefault().isRegistered(this)) {//加上判断) {
			EventBus.getDefault().unregister(this);
		}
	}
}
