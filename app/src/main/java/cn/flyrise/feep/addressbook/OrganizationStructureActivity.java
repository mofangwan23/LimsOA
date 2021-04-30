package cn.flyrise.feep.addressbook;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SimpleItemAnimator;
import android.text.TextUtils;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ListView;
import cn.flyrise.feep.K;
import cn.flyrise.feep.K.addressBook;
import cn.flyrise.feep.R;
import cn.flyrise.feep.addressbook.adapter.OrganizationDepartmentTreeAdapter;
import cn.flyrise.feep.addressbook.adapter.OrganizationStructureRightAdapter;
import cn.flyrise.feep.addressbook.adapter.OrganizationStructureRightAdapter.OnItemClickListener;
import cn.flyrise.feep.addressbook.model.ContactQueryVO;
import cn.flyrise.feep.addressbook.model.Department;
import cn.flyrise.feep.addressbook.model.DepartmentNode;
import cn.flyrise.feep.addressbook.processor.AddressBookProcessor;
import cn.flyrise.feep.addressbook.source.AddressBookRepository;
import cn.flyrise.feep.addressbook.utils.AddressBookExceptionInvoker;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.base.views.LoadMoreRecyclerView;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.dialog.FELoadingDialog;
import cn.flyrise.feep.core.services.ILoginUserServices;
import cn.flyrise.feep.core.watermark.WMAddressDecoration;
import cn.flyrise.feep.core.watermark.WMStamp;
import java.util.ArrayList;
import java.util.List;
import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author ZYP
 * @since 2017-02-16 15:11 全新组织架构...
 */
public class OrganizationStructureActivity extends BaseActivity {

	private final DepartmentNode mRootNode = new DepartmentNode();   // 根节点
	private DepartmentNode mPreClickNode;                             // 上一次点击的 Node
	private String mDefaultNodeId;                                   // 默认选中用户所在的公司
	private FELoadingDialog mLoadingDialog;


	private ListView mDepartmentListView;
	private FrameLayout mLayoutDepartment;
	private OrganizationDepartmentTreeAdapter mDepartmentAdapter;

	private boolean isLoading = false;
	private int mCurrentPage, mTotalPage;
	private LoadMoreRecyclerView mPersonsListView;
	private OrganizationStructureRightAdapter mPersonsAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ILoginUserServices loginUserServices = CoreZygote.getLoginUserServices();
		if (loginUserServices == null) {
			finish();
			return;
		}
		setContentView(R.layout.activity_organization_structure);
	}

	@Override
	protected void toolBar(FEToolbar toolbar) {
		String departmentName = getIntent().getStringExtra(addressBook.department_name);
		if (TextUtils.isEmpty(departmentName)) {
			departmentName = getString(R.string.organizational_structure);
		}
		toolbar.setTitle(departmentName);
	}

	@Override
	public void bindView() {
		mLayoutDepartment = findViewById(R.id.layoutDepartment);
		mDepartmentListView = findViewById(R.id.listViewDepartment);
		mDepartmentListView.setAdapter(mDepartmentAdapter = new OrganizationDepartmentTreeAdapter());

		int maxWidth = getResources().getDisplayMetrics().widthPixels / 2;

		String host = CoreZygote.getLoginUserServices().getServerAddress();
		mPersonsListView = findViewById(R.id.listViewPersons);
		LayoutParams layoutParams = mPersonsListView.getLayoutParams();
		layoutParams.width = maxWidth;

		String watermark = WMStamp.getInstance().getWaterMarkText();
		mPersonsListView.addItemDecoration(new WMAddressDecoration(watermark));

		mPersonsListView.setLayoutParams(layoutParams);
		mPersonsListView.setHasFixedSize(true);
		mPersonsListView.setLayoutManager(new LinearLayoutManager(this));
		mPersonsListView.setAdapter(mPersonsAdapter = new OrganizationStructureRightAdapter(this, host));
		((SimpleItemAnimator) mPersonsListView.getItemAnimator()).setSupportsChangeAnimations(false);
	}

	@Override
	public void bindData() {
		int addressBookState = CoreZygote.getLoginUserServices().getAddressBookState();
		if (addressBookState == AddressBookProcessor.ADDRESS_BOOK_DOWNLOAD_FAILED) {
			AddressBookExceptionInvoker.showAddressBookExceptionDialog(this);
			return;
		}
		showLoading();
		Observable
				.create(f -> {
					String userId = CoreZygote.getLoginUserServices().getUserId();     // 首先怼出用户所在的公司
					Department departmentUserIn = AddressBookRepository.get().queryDepartmentWhereUserIn(userId);
					if (departmentUserIn != null) {
						Department company = AddressBookRepository.get().queryCompanyWhereDepartmentIn(departmentUserIn.deptId);
						mDefaultNodeId = company == null ? null : company.deptId;
					}
					Department headDepartment = AddressBookRepository.get().queryHeadCompany();
					mRootNode.addChild(DepartmentNode.build(headDepartment, true));
					List<Department> departments = AddressBookRepository.get().querySubDepartmentInDepartment(headDepartment.deptId);
					for (Department department : departments) {
						boolean isLeafNode = CommonUtil.isEmptyList(AddressBookRepository.get().hasSubDepartment(department.deptId));
						mRootNode.addChild(DepartmentNode.build(department, isLeafNode));
					}
					f.onNext(200);
					f.onCompleted();
				})
				.flatMap(code -> mapToList())
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(departmentNodes -> {
					hideLoading();

					// 展开
					DepartmentNode departmentUserInNode = null;
					List<DepartmentNode> children = mRootNode.getChildren();
					if (CommonUtil.nonEmptyList(children)) {
						for (DepartmentNode child : children) {
							if (child.value == null)
								continue;
							if (TextUtils.equals(child.value.deptId, mDefaultNodeId)) {
								departmentUserInNode = child;
								break;
							}
						}
					}

					if (departmentUserInNode != null) {
						List<Department> departments = AddressBookRepository.get()
								.querySubDepartmentInDepartment(departmentUserInNode.value.deptId);
						for (Department department : departments) {
							boolean isLeafNode = CommonUtil.isEmptyList(AddressBookRepository.get().hasSubDepartment(department.deptId));
							departmentUserInNode.addChild(DepartmentNode.build(department, isLeafNode));
						}
						departmentUserInNode.isExpand = true;
						mPreClickNode = departmentUserInNode;
					}
					mDepartmentAdapter.setDefaultNodeId(mDefaultNodeId);
					mapToList()                                                     // Refresh Department Tree
							.subscribeOn(Schedulers.io())
							.observeOn(AndroidSchedulers.mainThread())
							.subscribe(deptNodes -> {
								mDepartmentAdapter.setDepartmentNodes(deptNodes);
								WMStamp.getInstance().draw(mLayoutDepartment, mDepartmentListView);
							}, exception -> exception.printStackTrace());

					if (!TextUtils.isEmpty(mDefaultNodeId)) {
						queryAddressBooks(mDefaultNodeId);
					}
				}, exception -> {
					hideLoading();
					exception.printStackTrace();
					FEToast.showMessage(getResources().getString(R.string.message_operation_fail));
				});
	}

	@Override
	public void bindListener() {
		/**
		 * 部门的点击：
		 * 1. 如果是展开，根据 deptId 查找出新的部门，并挂到 RootNode 相应的节点上，并重新 mapToList 刷新界面
		 * 2. 如果是隐藏，找到相应的节点，把对应的 children remove 掉，同样重新 mapToList 刷新界面
		 */
		mDepartmentListView.setOnItemClickListener((parent, view, position, id) -> {
			mDepartmentAdapter.setDefaultNodeId(null);
			DepartmentNode departmentNode = (DepartmentNode) mDepartmentAdapter.getItem(position);
			if (departmentNode == null)
				return;
			if (departmentNode.value == null)
				return;                       // 根节点
			if (departmentNode.value.level == 0)
				return;                    // 根节点

			mDepartmentAdapter.setDefaultNodeId(departmentNode.value.deptId);
			if (departmentNode.isLeafNode()) {                              // 叶子节点
				if (isSameNode(departmentNode)) {
					return;
				}
				queryAddressBooks(departmentNode.value.deptId);
				mPreClickNode = departmentNode;
				mDepartmentAdapter.notifyDataSetChanged();
				return;
			}

			DepartmentNode targetNode = searchDepartmentNode(mRootNode, departmentNode.value.deptId);

			if (departmentNode.isExpand) {                                  // 当前节点展开，隐藏
				if (targetNode != null)
					targetNode.removeChildren();
			}
			else {                                                          // 当前节点隐藏，展开
				List<Department> departments = AddressBookRepository.get().querySubDepartmentInDepartment(departmentNode.value.deptId);
				for (Department department : departments) {
					boolean isLeafNode = CommonUtil.isEmptyList(AddressBookRepository.get().hasSubDepartment(department.deptId));
					targetNode.addChild(DepartmentNode.build(department, isLeafNode));
				}
			}
			targetNode.isExpand = !departmentNode.isExpand;

			mapToList()                                                     // Refresh Department Tree
					.subscribeOn(Schedulers.io())
					.observeOn(AndroidSchedulers.mainThread())
					.subscribe(departmentNodes -> {
						mDepartmentAdapter.setDepartmentNodes(departmentNodes);
						WMStamp.getInstance().draw(mLayoutDepartment, mDepartmentListView);
					}, exception -> exception.printStackTrace());

			if (isSameNode(departmentNode)) {
				return;
			}
			// Refresh AddressBook list in right.
			queryAddressBooks(departmentNode.value.deptId);
			mPreClickNode = departmentNode;
		});

		/**
		 * 右侧人员详情的点击
		 */
		mPersonsAdapter.setOnItemClickListener((OnItemClickListener) (position, addressBook) -> {
			Intent intent = new Intent(this, AddressBookDetailActivity.class);
			intent.putExtra(K.addressBook.user_id, addressBook.userId);
			intent.putExtra(K.addressBook.department_id, addressBook.deptId);
			startActivity(intent);
		});

		/**
		 * 分页处理：加载更多
		 */
		mPersonsListView.setOnLoadMoreListener(() -> {
			// 查询更多
			if (!isLoading && mCurrentPage < mTotalPage) {
				isLoading = true;
				mCurrentPage++;
				executeQueryAction(mDepartmentAdapter.getDefaultNodeId());
			}
			else if (mCurrentPage >= mTotalPage) {
				mPersonsAdapter.removeFooterView();
			}
		});
	}

	/**
	 * Convert the department node tree to List.
	 */
	private Observable<List<DepartmentNode>> mapToList() {
		return Observable.create(f -> {
			List<DepartmentNode> departmentNodes = new ArrayList<>();
			mapToList(mRootNode, departmentNodes);
			f.onNext(departmentNodes);
			f.onCompleted();
		});
	}

	private void queryAddressBooks(String deptId) {
		mCurrentPage = 0;
		showLoading();
		executeQueryAction(deptId);
	}

	private void executeQueryAction(String deptId) {
		Observable
				.create((OnSubscribe<? extends ContactQueryVO>) f -> {
					ContactQueryVO queryVO = AddressBookRepository.get()
							.queryDepartmentStaff(deptId, mCurrentPage * AddressBookRepository.PAGE_MAX_COUNT);
					f.onNext(queryVO);
					f.onCompleted();
				})
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(results -> {
					hideLoading();
					isLoading = false;
					if (mCurrentPage == 0) mTotalPage = results.totalPage;
					if (mTotalPage > 1) {
						if (mCurrentPage == 0) {
							mPersonsAdapter.setAddressBooks(results.contacts);
							mPersonsAdapter.setFooterView(cn.flyrise.feep.core.R.layout.core_refresh_bottom_loading);
						}
						else {
							mPersonsAdapter.addAddressBooks(results.contacts);
							if (mCurrentPage == mTotalPage) {
								mPersonsAdapter.removeFooterView();
							}
							else {
								mPersonsAdapter.setFooterView(cn.flyrise.feep.core.R.layout.core_refresh_bottom_loading);
							}
						}
					}
					else {
						mCurrentPage = mTotalPage;
						mPersonsAdapter.removeFooterView();
						mPersonsAdapter.setAddressBooks(results.contacts);
					}
				}, exception -> {
					hideLoading();
					exception.printStackTrace();
				});
	}

//	private void queryAddressBooks(String deptId) {
//		showLoading();
//		Observable
//				.create(f -> {
//					List<AddressBook> addressBooks = AddressBookRepository.get().queryStaffBySortNo(deptId);
//					f.onNext(addressBooks);
//					f.onCompleted();
//				})
//				.subscribeOn(Schedulers.io())
//				.observeOn(AndroidSchedulers.mainThread())
//				.subscribe(results -> {
//					hideLoading();
//					List<AddressBook> addressBooks = results == null ? null : (List<AddressBook>) results;
//					mPersonsAdapter.setAddressBooks(addressBooks);
//					mPersonsListView.scrollToPosition(0);
//				}, exception -> {
//					hideLoading();
//					exception.printStackTrace();
//				});
//	}

	private void mapToList(DepartmentNode departmentNode, List<DepartmentNode> departmentNodes) {
		if (departmentNode == null)
			return;
		if (departmentNode.isLeafNode()) {            // 不存子节点
			if (departmentNode.value != null) {
				departmentNodes.add(departmentNode);
			}
			return;
		}

		if (departmentNode.value != null) {             // 根节点 root 为空
			departmentNodes.add(departmentNode);
		}

		List<DepartmentNode> children = departmentNode.getChildren();
		if (CommonUtil.isEmptyList(children))
			return;
		for (DepartmentNode node : children) {
			mapToList(node, departmentNodes);
		}
	}

	private void showLoading() {
		if (mLoadingDialog != null) {
			mLoadingDialog.hide();
			mLoadingDialog = null;
		}

		mLoadingDialog = new FELoadingDialog.Builder(this)
				.setLoadingLabel(getResources().getString(R.string.core_loading_wait))
				.setCancelable(true)
				.create();
		mLoadingDialog.show();
	}

	private void hideLoading() {
		if (mLoadingDialog != null) {
			mLoadingDialog.removeDismissListener();
			mLoadingDialog.hide();
			mLoadingDialog = null;
		}
	}

	/**
	 * 查找指定 deptId 的部门节点
	 * @param departmentNode 部门节点
	 * @param deptId 目标节点的 id
	 */
	private DepartmentNode searchDepartmentNode(DepartmentNode departmentNode, String deptId) {
		if (departmentNode == null)
			return null;
		if (departmentNode.value != null) {
			if (TextUtils.equals(departmentNode.value.deptId, deptId)) {
				return departmentNode;
			}
		}

		List<DepartmentNode> children = departmentNode.getChildren();
		if (CommonUtil.isEmptyList(children)) {
			return null;
		}
		for (DepartmentNode childNode : children) {
			DepartmentNode searchResult = searchDepartmentNode(childNode, deptId);
			if (searchResult != null) {
				return searchResult;
			}
		}
		return null;
	}

	private boolean isSameNode(DepartmentNode departmentNode) {
		if (mPreClickNode == null) {
			return false;
		}
		boolean isSameNode = false;
		try {
			Department preDepartment = mPreClickNode.value;
			Department currentDepartment = departmentNode.value;
			isSameNode = TextUtils.equals(preDepartment.deptId, currentDepartment.deptId);
		} catch (Exception exp) {
			exp.printStackTrace();
			isSameNode = false;
		}
		return isSameNode;
	}

	@Override protected void onDestroy() {
		super.onDestroy();
		WMStamp.getInstance().clearWaterMark(mLayoutDepartment);
	}
}
