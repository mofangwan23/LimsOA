package cn.flyrise.feep.knowledge.view;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import cn.flyrise.android.library.utility.LoadingHint;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.base.views.PullAndLoadMoreRecyclerView;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.dialog.FEMaterialDialog;
import cn.flyrise.feep.core.dialog.FEMaterialEditTextDialog;
import cn.flyrise.feep.knowledge.adpater.FolderFileListAdapter;
import cn.flyrise.feep.knowledge.contract.FolderFragmentContract;
import cn.flyrise.feep.knowledge.contract.RenameCreateContract;
import cn.flyrise.feep.knowledge.model.FileAndFolder;
import cn.flyrise.feep.knowledge.model.FolderManager;
import cn.flyrise.feep.knowledge.model.KnowledgeListEvent;
import cn.flyrise.feep.knowledge.presenter.FolderFragmentPresenterImpl;
import cn.flyrise.feep.meeting7.ui.component.StatusView;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import java.util.List;
import org.greenrobot.eventbus.EventBus;

/**
 * Created by KLC on 2016/12/7.
 * UpDate by mofangwan on 2018/07/25
 * 知识中心主页（文件夹列表）
 */

public class FolderFragment extends Fragment implements FolderFragmentContract.View, RenameCreateContract.View {

	private PullAndLoadMoreRecyclerView mListView;
	private StatusView mStatusView;
	private LinearLayout mBottomMenu;
	private LinearLayout mRenameLayout;
	private LinearLayout mMoveLayout;
	private LinearLayout mDeleteLayout;
	private LinearLayout mCollectLayout;
	private FEToolbar mToolBar;
	private FloatingActionsMenu mDetailMenu;
	private boolean isVisibleToUser;



	private FolderFragmentContract.Presenter mPresenter;
	private FolderManager folderManager;
	private FolderFileListAdapter mAdapter;
	private Handler mHandler;


	public static FolderFragment newInstance() {
		Bundle args = new Bundle();
		FolderFragment fragment = new FolderFragment();
		fragment.setArguments(args);
		return fragment;
	}

	public void setActivityView(FolderManager folderManager, FEToolbar toolbar, FloatingActionsMenu detailMenu) {
		this.folderManager = folderManager;
		this.mToolBar = toolbar;
		this.mDetailMenu = detailMenu;
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_knowledge_list, container, false);
		findView(view);
		bindData();
		setListener();
		return view;
	}

	private void findView(View view) {
		mListView = (PullAndLoadMoreRecyclerView) view.findViewById(R.id.listview);
		mStatusView = (StatusView) view.findViewById(R.id.knowledge_list_statusview);
		mBottomMenu = (LinearLayout) view.findViewById(R.id.llBottomMenu);
		mRenameLayout = (LinearLayout) view.findViewById(R.id.rename_layout);
		mMoveLayout = (LinearLayout) view.findViewById(R.id.move_layout);
		mDeleteLayout = (LinearLayout) view.findViewById(R.id.delete_layout);
		mCollectLayout = view.findViewById(R.id.collect_layout);
		view.findViewById(R.id.down_layout).setVisibility(View.GONE);
		view.findViewById(R.id.cancel_publish_layout).setVisibility(View.GONE);
		view.findViewById(R.id.share_layout).setVisibility(View.GONE);
	}

	private void bindData() {
		mHandler = new Handler();
		mAdapter = new FolderFileListAdapter(getActivity(),true);
		mListView.setAdapter(mAdapter);
		mPresenter = new FolderFragmentPresenterImpl(folderManager, this, this);
		mHandler.postDelayed(() -> mPresenter.refreshListData(), 500);
	}

	public void setListener() {
		mListView.setRefreshListener(() -> mPresenter.refreshListData());
		mListView.setLoadMoreListener(() -> mPresenter.loadMore());
		mRenameLayout
				.setOnClickListener(v -> mPresenter.renameFolder(CoreZygote.getLoginUserServices().getUserId(), mAdapter.getDataList()));
		mDeleteLayout.setOnClickListener(v -> mPresenter.deleteFolder(mAdapter.getDataList()));
		mMoveLayout.setOnClickListener(v -> mPresenter.moveFolderAndFile(getActivity(), mAdapter.getDataList()));
		mAdapter.setChoiceListener((choiceCount, clickFolderList, clickFiles) -> mPresenter.setPermission(choiceCount, clickFolderList));
		mAdapter.setOnItemClickListener(new FolderFileListAdapter.onItemClickListener() {
			@Override
			public void onClickListener(FileAndFolder fileAndFolder) {
				if(!mAdapter.isEditStand){
					mPresenter.openFolder(getActivity(), fileAndFolder);
				}
			}
		});

		mAdapter.setOnItemLongClickListener((view, object) -> FolderFragment.this.onItemLongClick());


	}

	@Override
	public void showRefreshLoading(boolean show) {
		if (show) {
			mListView.setRefreshing(true);
		}
		else {
			mHandler.postDelayed(() -> mListView.setRefreshing(false), 500);
		}
	}

	@Override
	public void refreshListData(List<FileAndFolder> dataList) {
		if (getActivity() == null) return;
		mAdapter.refreshData(dataList);
		setEmptyView();
		mListView.scroll2Top();
		dealComplete();
	}

	@Override
	public void setEmptyView() {
		if (mAdapter.getItemCount() == 0) {
			mStatusView.setVisibility(View.VISIBLE);
			mStatusView.setText("这是个空文件夹");
		}
		else {
			mStatusView.setVisibility(View.GONE);
		}
	}

	@Override
	public void loadMoreListData(List<FileAndFolder> dataList) {
		mAdapter.addData(dataList);
	}

	@Override
	public void loadMoreListFail() {
		mListView.scrollLastItem2Bottom();
	}

	@Override
	public void showConfirmDialog(int resourceID, FEMaterialDialog.OnClickListener onClickListener) {
		new FEMaterialDialog.Builder(getActivity()).setMessage(getString(resourceID))
				.setPositiveButton(null, onClickListener)
				.setNegativeButton(null, null)
				.build()
				.show();
	}

	@Override
	public void setBottomEnable(boolean canRename, boolean canMove, boolean canDelete, boolean canCollection) {
		setEnable(mRenameLayout, canRename);
		setEnable(mMoveLayout, canMove);
		setEnable(mDeleteLayout, canDelete);
		setEnable(mCollectLayout, canCollection);
	}

	@Override
	public void setFloatEnable(boolean canCreate) {
		mDetailMenu.collapse();
		if (this.isVisibleToUser) {
			setEnable(mDetailMenu, canCreate);
		}
	}

	@Override
	public void setTableEnable(boolean enable) {
		EventBus.getDefault().post(new KnowledgeListEvent(KnowledgeListEvent.SETTABLAYOUTENABLE, enable));
		mListView.setCanRefresh(enable);
//		showActionButton(enable);
	}

	public void showActionButton(boolean show) {
		if (show) {
			mDetailMenu.setVisibility(View.VISIBLE);
		}
		else {
			mDetailMenu.setVisibility(View.GONE);
		}
	}

	private void setEnable(View view, boolean enable) {
		if (enable) {
			view.setAlpha(1);
			view.setEnabled(true);
		}
		else {
			view.setAlpha(0.3f);
			view.setEnabled(false);
		}
	}

	@Override
	public void showInputDialog(int titleResourceID, int hintResourceID, String checkBoxText, FEMaterialEditTextDialog.OnClickListener onClickListener) {
		new FEMaterialEditTextDialog.Builder(getActivity())
				.setTitle(getString(titleResourceID))
				.setHint(getString(hintResourceID))
				.setPositiveButton("确定", onClickListener)
				.setNegativeButton("", null)
				.build()
				.show();
	}

	@Override
	public void refreshList() {
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void refreshListByNet() {
		mPresenter.refreshListData();
	}

	@Override
	public void dealComplete() {
		mAdapter.restoreOriginalState(false);
		showBottomMenu(false);
        EventBus.getDefault().post(new KnowledgeListEvent(KnowledgeListEvent.SETTABLAYOUTENABLE, true));
		mListView.setCanRefresh(true);
		showActionButton(true);
		mToolBar.setRightText(R.string.know_more_control);
		mPresenter.setPermission();
	}

	@Override public void showErrorMessage(String errorMessage) {
		FEToast.showMessage(errorMessage);
	}

	@Override
	public void showDealLoading(boolean show) {
		if (show) {
			LoadingHint.show(getActivity());
		}
		else {
			LoadingHint.hide();
		}
	}

	@Override
	public void showMessage(int resourceID) {
		FEToast.showMessage(getString(resourceID));
	}

	@Override
	public void setCanPullUp(boolean hasMore) {
		if (hasMore) {
			mListView.addFootView();
		}
		else {
			mListView.removeFootView();
		}
	}

	public void showBottomMenu(boolean show) {
		if (show) {
			mBottomMenu.setVisibility(View.VISIBLE);
			mToolBar.setRightText(getString(R.string.cancel));
		}
		else {
			mBottomMenu.setVisibility(View.GONE);
		}
	}


	public void onBackPressed() {
		if (mAdapter.isEditStand) {
			selectCancel();
		}
		else {
			getActivity().finish();
		}
	}

	public void selectCancel() {
		mAdapter.restoreOriginalState(false);
		showBottomMenu(false);
		mToolBar.setRightText(R.string.know_more_control);
		setTableEnable(true);
		mPresenter.setPermission();

	}

	public void createFolder() {
		mPresenter.createFolder(CoreZygote.getLoginUserServices().getUserId());
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		this.isVisibleToUser = isVisibleToUser;
		if (isVisibleToUser && mPresenter != null) {
			mPresenter.setPermission();
		}
	}

	private void onItemLongClick() {
		if(mAdapter.isEditStand){
			mAdapter.setCanChoice(false);
			setTableEnable(true);
			mAdapter.isEditStand = false;
			mListView.setCanRefresh(true);
			showBottomMenu(false);
			showActionButton(true);
			EventBus.getDefault().post(new KnowledgeListEvent(KnowledgeListEvent.SETTABLAYOUTENABLE, true));
		}else {
			mAdapter.setCanChoice(true);
			setTableEnable(false);
			mAdapter.isEditStand = true;
			mListView.setCanRefresh(false);
			showBottomMenu(true);
			showActionButton(false);
			EventBus.getDefault().post(new KnowledgeListEvent(KnowledgeListEvent.SETTABLAYOUTENABLE, false));
		}

//		if (mAdapter.isCanChoice()) {
//			mAdapter.setCanChoice(false);
//			mToolBar.showNavigationIcon();
//			mToolBar.showRightIcon();
//			showBottomMenu(false);
//			showActionButton(true);
//			EventBus.getDefault().post(new KnowledgeListEvent(KnowledgeListEvent.SETTABLAYOUTENABLE, true));
//			mListView.setCanRefresh(true);
//		}
//		else {
//			mAdapter.setCanChoice(true);
//			mToolBar.setRightText(R.string.know_selectAll);
//			mToolBar.showLeftText();
//			showBottomMenu(true);
//			showActionButton(false);
//			EventBus.getDefault().post(new KnowledgeListEvent(KnowledgeListEvent.SETTABLAYOUTENABLE, false));
//			mListView.setCanRefresh(false);
//		}
	}

}
