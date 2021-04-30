package cn.flyrise.feep.addressbook;

import static cn.flyrise.feep.addressbook.adapter.BaseContactAdapter.CODE_INVERT_SELECT_ALL;
import static cn.flyrise.feep.addressbook.adapter.BaseContactAdapter.CODE_SELECT_ALL;
import static cn.flyrise.feep.addressbook.view.DepartmentFilterFragment.DEFAULT_DEPARTMENT;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;
import cn.flyrise.android.protocol.model.CommonGroup;
import cn.flyrise.feep.K;
import cn.flyrise.feep.K.ChatContanct;
import cn.flyrise.feep.K.addressBook;
import cn.flyrise.feep.R;
import cn.flyrise.feep.addressbook.adapter.BaseContactAdapter;
import cn.flyrise.feep.addressbook.adapter.ContactAdapter;
import cn.flyrise.feep.addressbook.adapter.SurnameAdapter;
import cn.flyrise.feep.addressbook.model.CommonGroupEvent;
import cn.flyrise.feep.addressbook.model.CompanyEvent;
import cn.flyrise.feep.addressbook.model.Department;
import cn.flyrise.feep.addressbook.model.DepartmentEvent;
import cn.flyrise.feep.addressbook.model.DismissEvent;
import cn.flyrise.feep.addressbook.model.Position;
import cn.flyrise.feep.addressbook.model.PositionEvent;
import cn.flyrise.feep.addressbook.model.SubDepartmentEvent;
import cn.flyrise.feep.addressbook.processor.AddressBookProcessor;
import cn.flyrise.feep.addressbook.utils.AddressBookExceptionInvoker;
import cn.flyrise.feep.addressbook.view.AddressBookFilterView;
import cn.flyrise.feep.addressbook.view.CommonGroupFilterFragment;
import cn.flyrise.feep.addressbook.view.CompanyFilterFragment;
import cn.flyrise.feep.addressbook.view.DepartmentFilterFragment;
import cn.flyrise.feep.addressbook.view.LetterFloatingView;
import cn.flyrise.feep.addressbook.view.PositionFilterFragment;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.base.views.FELetterListView;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.common.DataKeeper;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.utils.PixelUtil;
import cn.flyrise.feep.core.dialog.FELoadingDialog;
import cn.flyrise.feep.core.services.model.AddressBook;
import cn.flyrise.feep.core.watermark.WMAddressDecoration;
import cn.flyrise.feep.core.watermark.WMStamp;
import cn.squirtlez.frouter.annotations.Route;
import java.util.List;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * @author ZYP
 * @since 2016-12-05 16:15
 */
@Route("/addressBook/list")
public class AddressBookActivity extends BaseContactActivity implements AddressBookContract.IView {

	private FEToolbar mToolbar;
	private RecyclerView mRecyclerView;
	private FELetterListView mLetterView;
	private AddressBookFilterView mAddressBookFilterView;

	private boolean isOnlyOneCompany;
	private Position mPosition;                                 // 岗位
	private Department mCompany, mDepartment, mSubDepartment;   // 公司、一级部门、二级部门
	private CommonGroup mCommonGroup;                           // 选中的常用组，有可能是不存在的

	private ViewGroup mFilterContainer;
	private CompanyFilterFragment mCompanyView;
	private DepartmentFilterFragment mDepartmentView;
	private PositionFilterFragment mPositionView;
	private CommonGroupFilterFragment mCommonGroupView;
	private int mMaxFilterViewHeight;

	private View mLetterFloatingView;                           // 特么的字母、姓氏索引列表
	private TextView mTvLetterView;
	private SurnameAdapter mSurnameAdapter;

	private WindowManager mWindowManager;
	private Runnable mLetterFloatingRunnable;
	private AddressBookContract.IPresenter mPresenter;
	private FELoadingDialog mLoadingDialog;
	private boolean withSelect;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPresenter = new AddressBookPresenter(this, mHandler);
		withSelect = getIntent().getBooleanExtra(K.addressBook.select_mode, false);
		setContentView(R.layout.activity_address_book);
		EventBus.getDefault().register(this);
	}

	@Override
	protected void toolBar(FEToolbar toolbar) {
		this.mToolbar = toolbar;
		this.mToolbar.setLineVisibility(View.GONE);
		String title = getIntent().getStringExtra(K.addressBook.address_title);
		if (TextUtils.isEmpty(title)) {
			title = getResources().getString(R.string.all_contact);
		}
		else {
			this.mToolbar.setRightIcon(R.drawable.icon_search);
			this.mToolbar.setRightImageClickListener(view -> {
				// 已经选择的数据
				int hashCode = AddressBookActivity.class.hashCode();
				DataKeeper.getInstance().keepDatas(hashCode, mContactAdapter.getSelectedContacts());

				Intent intent = new Intent(AddressBookActivity.this, ContactSearchActivity.class);
				intent.putExtra(K.addressBook.select_mode, withSelect);
				intent.putExtra(K.addressBook.data_keep, hashCode);
				intent.putExtra(K.addressBook.start_chat, getIntent().getBooleanExtra(K.addressBook.start_chat, false));
				intent.putExtra(K.addressBook.except_self, getIntent().getBooleanExtra(addressBook.except_self, false));
				intent.putExtra(K.addressBook.single_select, getIntent().getBooleanExtra(K.addressBook.single_select, false));
				intent.putExtra(K.addressBook.except_own_select, getIntent().getBooleanExtra(K.addressBook.except_own_select, false));

				String msgID = getIntent().getStringExtra(ChatContanct.EXTRA_FORWARD_MSG_ID);
				if (!TextUtils.isEmpty(msgID)) {
					intent.putExtra(ChatContanct.EXTRA_FORWARD_MSG_ID, msgID);
				}
				boolean exceptSelected = getIntent().getBooleanExtra(addressBook.except_selected, false);
				if (exceptSelected) {
					intent.putExtra(addressBook.except_selected, exceptSelected);

					// 禁止选择的数据
					int dataKeepKey = getIntent().getIntExtra(K.addressBook.data_keep, -1);
					List<AddressBook> cannotSelected = (List<AddressBook>) DataKeeper.getInstance().getKeepDatas(dataKeepKey);
					DataKeeper.getInstance().keepDatas(dataKeepKey, cannotSelected);
					intent.putExtra(addressBook.cannot_selected, dataKeepKey);
				}

				startActivityForResult(intent, 1024);
			});
		}
		this.mToolbar.setTitle(title);

		boolean isSingleSelect = getIntent().getBooleanExtra(addressBook.single_select, false);
		if (withSelect & !isSingleSelect) {
			this.mToolbar.setRightTextClickListener(view -> {
				// 设置
				int tag = (int) view.getTag();
				if (tag == CODE_INVERT_SELECT_ALL) {
					mContactAdapter.executeSelect(false);
					view.setTag(CODE_SELECT_ALL);
					mToolbar.getRightTextView().setVisibility(View.GONE);
				}
				else if (tag == CODE_SELECT_ALL) {
					mContactAdapter.executeSelect(true);
					mToolbar.getRightTextView().setTag(CODE_INVERT_SELECT_ALL);
					mToolbar.setRightTextWithImage("全不选");

				}
				updateSelectedCount();
			});
		}
	}

	@Override
	public void bindView() {
		super.bindView();
		mAddressBookFilterView =  findViewById(R.id.addressBookFilterView);
		if (!withSelect) {
			mAddressBookFilterView.hideCommonlyGroup();
		}

		mLetterView =  findViewById(R.id.letterListView);
		mFilterContainer =  findViewById(R.id.layoutFilterContainer);
		mRecyclerView =  findViewById(R.id.recyclerView);

		mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
		mRecyclerView.setItemAnimator(new DefaultItemAnimator());

		String watermark = WMStamp.getInstance().getWaterMarkText();
		mRecyclerView.addItemDecoration(new WMAddressDecoration(watermark));

		mContactAdapter = new ContactAdapter(this);
		mContactAdapter.setEmptyView(findViewById(R.id.ivEmptyView));
		boolean isFromMetting = "选择参会人员".equals(mToolbar.getTitle().toString()) ? true : false;
		mContactAdapter.setAddressFromMetting(isFromMetting);
		mRecyclerView.setAdapter(mContactAdapter);
	}

	@Override
	public void bindData() {
		if (CoreZygote.getLoginUserServices() == null) return;
		int addressBookState = CoreZygote.getLoginUserServices().getAddressBookState();
		if (addressBookState == AddressBookProcessor.ADDRESS_BOOK_DOWNLOAD_FAILED) {    // 通讯录下载失败
			AddressBookExceptionInvoker.showAddressBookExceptionDialog(this);
			return;
		}

		Intent intent = getIntent();
		int dataKeepKey = intent.getIntExtra(K.addressBook.data_keep, -1);
		List<AddressBook> selectedContacts = (List<AddressBook>) DataKeeper.getInstance().getKeepDatas(dataKeepKey);
		if (intent.getBooleanExtra(K.addressBook.except_selected, false)) {
			mContactAdapter.setCannotSelectContacts(selectedContacts);
		}
		else {
			mContactAdapter.setSelectedContacts(selectedContacts);
		}

		if (intent.getBooleanExtra(addressBook.except_self, false)) {
			String userId = CoreZygote.getLoginUserServices().getUserId();
			AddressBook self = CoreZygote.getAddressBookServices().queryUserInfo(userId);
			mContactAdapter.addCannotSelectContacts(self);
		}
		mContactAdapter.isExceptOwn(getIntent().getBooleanExtra(K.addressBook.except_own_select, false));
		super.bindData();

		final String userId = CoreZygote.getLoginUserServices().getUserId();
		this.mPresenter.queryUserDepartmentInfos(userId, intent);       // 初始化部门信息
		if (withSelect) {
			// 如果是选择状态，看一下是否能拿到常用组，如果能拿到，就显示常用组。不然就直接隐藏常用组
			this.mPresenter.queryCommonlyGroup(userId);
		}
		this.bindLetterFloatingView();
	}

	@Override
	protected int getPreviewMaxHeight() {
		return getTopAndHeight()[1] - getResources().getDimensionPixelSize(R.dimen.mdp_4);
	}

	@Override
	protected int getPreviewMarginTop() {
		return getTopAndHeight()[0] - getResources().getDimensionPixelSize(R.dimen.mdp_46);
	}

	@Override
	public void bindListener() {
		mAddressBookFilterView.setCompanyClickListener(view -> {
			hideWindow();
			if (isFilterViewVisibility(mCompanyView)) {
				hideAllFiltersViews();
				return;
			}

			if (mCompanyView == null) {
				boolean onlyUserCompany = getIntent().getBooleanExtra(K.addressBook.only_user_company, false);
				String userId = null;
				if (onlyUserCompany) {
					userId = CoreZygote.getLoginUserServices().getUserId();
				}
				mCompanyView = CompanyFilterFragment.newInstance(mCompany, userId, isOnlyOneCompany);
				mCompanyView.setMaxHeight(getMaxFilterViewHeight());
				getSupportFragmentManager().beginTransaction().add(R.id.layoutFilterContainer, mCompanyView).commit();
				hideAllFilterViewAndShowThis(mCompanyView);
				return;
			}
			mCompanyView.setDefaultCompany(mCompany);
			hideAllFilterViewAndShowThis(mCompanyView);
		});

		mAddressBookFilterView.setDepartmentClickListener(view -> {
			hideWindow();
			if (isFilterViewVisibility(mDepartmentView)) {
				hideAllFiltersViews();
				return;
			}
			if (mDepartmentView == null) {
				mDepartmentView = DepartmentFilterFragment.newInstance(mCompany, mDepartment, mSubDepartment);
				mDepartmentView.setMaxHeight(getMaxFilterViewHeight());
				getSupportFragmentManager().beginTransaction().add(R.id.layoutFilterContainer, mDepartmentView).commit();
				hideAllFilterViewAndShowThis(mDepartmentView);
				return;
			}
			mDepartmentView.setDefaultDepartment(mCompany, mDepartment, mSubDepartment);
			hideAllFilterViewAndShowThis(mDepartmentView);
		});

		mAddressBookFilterView.setPositionClickListener(view -> {
			hideWindow();
			if (isFilterViewVisibility(mPositionView)) {
				hideAllFiltersViews();
				return;
			}
			if (mPositionView == null) {
				mPositionView = PositionFilterFragment.newInstance(mCompany, mDepartment, mSubDepartment, mPosition);
				mPositionView.setMaxHeight(getMaxFilterViewHeight());
				getSupportFragmentManager().beginTransaction().add(R.id.layoutFilterContainer, mPositionView).commit();
				hideAllFilterViewAndShowThis(mPositionView);
				return;
			}

			mPositionView.setDefaultPosition(mCompany, mDepartment, mSubDepartment, mPosition);
			hideAllFilterViewAndShowThis(mPositionView);
		});

		boolean isSingleSelect = getIntent().getBooleanExtra(addressBook.single_select, false);
		if (withSelect && !isSingleSelect) {
			setOnAddressBookCheckChangeListener(addressBook -> updateToolBarRightText());
		}

		mLetterFloatingRunnable = this::hideWindow;
		mLetterView.setOnTouchingLetterChangedListener(letter -> {                  // 右侧字母索引
			if (mContactAdapter != null) {
				int selection = letter.toLowerCase().charAt(0);

				int position = ((ContactAdapter) mContactAdapter).getPositionBySelection(selection);
				if (position != -1) {
					((LinearLayoutManager) mRecyclerView.getLayoutManager()).scrollToPositionWithOffset(position, 0);
				}

				List<String> surnames = ((ContactAdapter) mContactAdapter).getSurnameBySelection(selection);
				mTvLetterView.setText(letter);
				mSurnameAdapter.notifyChange(surnames);
				showWindow();
				mHandler.removeCallbacks(mLetterFloatingRunnable);
				mHandler.postDelayed(mLetterFloatingRunnable, 3000);
			}
		});

		mContactAdapter.setOnContactItemClickListener(this::onItemClick);
	}

	private void updateToolBarRightText() {
		int state = mContactAdapter.selectState();
		if (state == BaseContactAdapter.CODE_HIDE_SELECT) {
			mToolbar.getRightTextView().setVisibility(View.GONE);
		}
		else if (state == CODE_INVERT_SELECT_ALL) {
			mToolbar.setRightTextWithImage("全不选");
		}
		else {
			mToolbar.setRightTextWithImage("全选");
		}
		mToolbar.getRightTextView().setTag(state);
	}

	private void bindLetterFloatingView() {
		mLetterFloatingView = new LetterFloatingView(this);
		mTvLetterView = mLetterFloatingView.findViewById(R.id.overlaytext);
		ListView mSurnameListView = mLetterFloatingView.findViewById(R.id.overlaylist);
		mSurnameListView.setAdapter(mSurnameAdapter = new SurnameAdapter());

		mLetterFloatingView.setOnKeyListener((v, keyCode, event) -> {
			FELog.i("AddressBookActivity key listener : " + keyCode);
			if (event.getKeyCode() == KeyEvent.KEYCODE_BACK || event.getKeyCode() == KeyEvent.KEYCODE_SETTINGS) {
				if (mLetterFloatingView.getVisibility() == View.VISIBLE) {
					hideWindow();
					finish();
				}
			}
			return false;
		});

		mSurnameListView.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				mHandler.removeCallbacks(mLetterFloatingRunnable);
				mHandler.postDelayed(mLetterFloatingRunnable, 2000);
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			}
		});

		mSurnameListView.setOnItemClickListener((parent, view, position, id) -> {
			mHandler.removeCallbacks(mLetterFloatingRunnable);
			mHandler.postDelayed(mLetterFloatingRunnable, 2000);
			String surname = (String) mSurnameAdapter.getItem(position);
			int surnameAscii = surname.charAt(0);
			int surnamePosition = ((ContactAdapter) mContactAdapter).getPositionBySurname(surnameAscii);
			if (surnamePosition != -1) {
				((LinearLayoutManager) mRecyclerView.getLayoutManager()).scrollToPositionWithOffset(surnamePosition, 0);
			}
		});
		mWindowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
	}

	//显示小窗口
	private void showWindow() {
		if (mLetterFloatingView == null || mWindowManager == null) return;
		if (mLetterFloatingView.getVisibility() != View.VISIBLE) {
			mLetterFloatingView.setVisibility(View.VISIBLE);
		}
		if (mLetterFloatingView.getParent() == null) {
			mWindowManager.addView(mLetterFloatingView, windowParams());
		}
		else {
			mWindowManager.updateViewLayout(mLetterFloatingView, windowParams());
		}
	}

	//隐藏小窗口
	private void hideWindow() {
		if (mLetterFloatingView == null || mWindowManager == null) return;
		if (mLetterFloatingView.getParent() != null) {
			mWindowManager.removeView(mLetterFloatingView);
		}
	}

	private WindowManager.LayoutParams windowParams() {
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, PixelUtil.dipToPx(300),
				WindowManager.LayoutParams.TYPE_APPLICATION,
				WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
				PixelFormat.TRANSLUCENT);
		lp.gravity = Gravity.TOP | Gravity.RIGHT;
		lp.x = PixelUtil.dipToPx(40);
		lp.y = PixelUtil.dipToPx(128);
		return lp;
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onCompanySelected(CompanyEvent event) {
		this.mFilterContainer.setVisibility(View.GONE);
		this.mCompany = event.company;
		if (event.hasChange || mCommonGroup != null) {
			mAddressBookFilterView.setCompanyName(mCompany.name);
			mDepartment = null;
			mSubDepartment = null;
			mPosition = null;
			mCommonGroup = null;
			mAddressBookFilterView.setDepartmentName(getString(R.string.all_department));
			mAddressBookFilterView.setPositionName(getString(R.string.all_position));
			mAddressBookFilterView.setCommonlyName("常用组");
			mPresenter.queryContacts(mCompany, mDepartment, mSubDepartment, mPosition);
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onDepartmentSelected(DepartmentEvent event) {
		this.mDepartment = TextUtils.equals(event.department.deptId, DEFAULT_DEPARTMENT.deptId) ? null : event.department;
		if (event.hasChange || mCommonGroup != null) {          // 一级部门发生了改变
			mPosition = null;
			mSubDepartment = null;
			mCommonGroup = null;
			mAddressBookFilterView.setDepartmentName(mDepartment == null ? DEFAULT_DEPARTMENT.name : mDepartment.name);
			mAddressBookFilterView.setPositionName(getString(R.string.all_position));
			mAddressBookFilterView.setCommonlyName("常用组");
			if (event.refresh) {
				this.mFilterContainer.setVisibility(View.GONE);
				mPresenter.queryContacts(mCompany, mDepartment, mSubDepartment, mPosition);
			}
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onSubDepartmentSelected(SubDepartmentEvent event) {
		this.mFilterContainer.setVisibility(View.GONE);
		this.mSubDepartment = event.subDepartment;

		if (event.hasChange || mCommonGroup != null) {
			mPosition = null;
			mCommonGroup = null;
			boolean isAllDepartment = TextUtils.equals(mSubDepartment.deptId, DepartmentFilterFragment.DEFAULT_SUB_DEPARTMENT.deptId);
			mAddressBookFilterView.setDepartmentName(isAllDepartment ? mDepartment.name : mSubDepartment.name);
			mAddressBookFilterView.setPositionName(getString(R.string.all_position));
			mAddressBookFilterView.setCommonlyName("常用组");
			mPresenter.queryContacts(mCompany, mDepartment, mSubDepartment, mPosition);
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onPositionSelected(PositionEvent event) {
		this.mFilterContainer.setVisibility(View.GONE);
		this.mPosition = event.position;
		if (event.hasChange || mCommonGroup != null) {
			mCommonGroup = null;
			mAddressBookFilterView.setPositionName(event.position.position);
			mAddressBookFilterView.setCommonlyName("常用组");
			mPresenter.queryContacts(mCompany, mDepartment, mSubDepartment, mPosition);
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onCommonGroupSelected(CommonGroupEvent event) {
		this.mFilterContainer.setVisibility(View.GONE);
		this.mCommonGroup = event.commonGroup;
		if (event.hasChange) {
			mAddressBookFilterView.setCommonlyName(event.commonGroup.groupName);
			mPresenter.queryCommonlyUserByGroupId(event.commonGroup.groupId);
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onFilterViewDismiss(DismissEvent dismissEvent) {
		if (mFilterContainer.getVisibility() == View.VISIBLE) {
			hideAllFilterViewAndShowThis(null);
			mFilterContainer.setVisibility(View.GONE);
		}
	}

	private int getMaxFilterViewHeight() {
		if (mMaxFilterViewHeight == 0) {
			int[] location = new int[2];
			mAddressBookFilterView.getLocationOnScreen(location);
			int screenHeight = getResources().getDisplayMetrics().heightPixels;
			mMaxFilterViewHeight = screenHeight - location[1] - mAddressBookFilterView.getMeasuredHeight();
		}
		return mMaxFilterViewHeight;
	}

	private int[] getTopAndHeight() {
//		if (mTopAndHeight == null) {
		mTopAndHeight = new int[2];
		int[] location = new int[2];
		mAddressBookFilterView.getLocationOnScreen(location);
		mTopAndHeight[0] = location[1] + mAddressBookFilterView.getMeasuredHeight() / 2;

		int addressBookFilterViewLocation = location[1];
		mContactsConfirmView.getLocationOnScreen(location);
		int confirmViewLocation = location[1];

//			int screenHeight = getResources().getDisplayMetrics().heightPixels;
//			mTopAndHeight[1] = screenHeight - location[1] - mAddressBookFilterView.getMeasuredHeight();
		mTopAndHeight[1] = confirmViewLocation - addressBookFilterViewLocation + getResources().getDimensionPixelSize(R.dimen.mdp_5);
//		}
		return mTopAndHeight;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == K.addressBook.search_result_code) {
			int hashCode = AddressBookActivity.class.hashCode();
			List<AddressBook> selectedContacts = (List<AddressBook>) DataKeeper.getInstance().getKeepDatas(hashCode);
			mContactAdapter.addSelectedContacts(selectedContacts);
			updateSelectedCount();
			mContactAdapter.notifyDataSetChanged();
		}
		else if (resultCode == Activity.RESULT_OK) {
			boolean singleChoose = getIntent().getBooleanExtra(K.addressBook.single_select, false);
			if (singleChoose) {
				String userId = data.getStringExtra(addressBook.user_id);
				String name = data.getStringExtra(addressBook.user_name);
				Intent r = new Intent();
				r.putExtra(K.addressBook.user_id, userId);
				r.putExtra(K.addressBook.user_name, name);
				setResult(Activity.RESULT_OK, r);
				finish();
			}
		}
	}

	@Override
	public void showInitialization(Department company, Department department,
			Department subDepartment, Position position, boolean isOnlyOneCompany) {
		this.mCompany = company;
		this.mDepartment = department;
		this.mSubDepartment = subDepartment;
		this.mPosition = position;
		this.isOnlyOneCompany = isOnlyOneCompany;

		mAddressBookFilterView.setCompanyName(mCompany != null ? mCompany.name : getString(R.string.unknown));
		mAddressBookFilterView.setDepartmentName(mSubDepartment != null ? mSubDepartment.name
				: (mDepartment != null ? mDepartment.name : getString(R.string.all_department)));

		mAddressBookFilterView.setPositionName(mPosition != null ? mPosition.position : getString(R.string.all_position));
		mPresenter.queryContacts(mCompany, mDepartment, mSubDepartment, mPosition);
	}

	@Override
	public void showContacts(List<AddressBook> addressBooks) {
		this.mContactAdapter.setContacts(addressBooks);
		((ContactAdapter) mContactAdapter).buildSelection(addressBooks);
		List<String> letter = ((ContactAdapter) mContactAdapter).getLetterList();
		mLetterView.setShowLetters(letter);

		boolean isSingleSelect = getIntent().getBooleanExtra(addressBook.single_select, false);
		if (withSelect && !isSingleSelect) {
			updateToolBarRightText();
		}
	}

	@Override
	public void showLoading() {
		if (mLoadingDialog != null) {
			mLoadingDialog.hide();
			mLoadingDialog = null;
		}

		mLoadingDialog = new FELoadingDialog.Builder(this)
				.setLoadingLabel(getResources().getString(R.string.core_loading_wait))
				.setCancelable(true)
				.setOnDismissListener(this::finish)
				.create();
		mLoadingDialog.show();
	}

	@Override
	public void hideLoading() {
		if (mLoadingDialog != null) {
			mLoadingDialog.removeDismissListener();
			mLoadingDialog.hide();
			mLoadingDialog = null;
		}
	}

	@Override
	public void showCommonGroups(List<CommonGroup> commonGroups) {
		mAddressBookFilterView.showCommonlyGroup();                 // 显示常用组
		mAddressBookFilterView.setCommonlyName("常用组");
		mAddressBookFilterView.setCommonlyClickListener(view -> {
			if (isFilterViewVisibility(mCommonGroupView)) {
				hideAllFiltersViews();
				return;
			}
			if (mCommonGroupView == null) {
				mCommonGroupView = CommonGroupFilterFragment.newInstance(commonGroups);
				mCommonGroupView.setMaxHeight(getMaxFilterViewHeight());
				getSupportFragmentManager().beginTransaction().add(R.id.layoutFilterContainer, mCommonGroupView).commit();
				hideAllFilterViewAndShowThis(mCommonGroupView);
				return;
			}

			mCommonGroupView.setDefaultCommonGroup(mCommonGroup);
			hideAllFilterViewAndShowThis(mCommonGroupView);
		});
	}

	@Override
	public void showCommonUsers(List<AddressBook> addressBooks) {
		this.hideLoading();
		this.mContactAdapter.setContacts(addressBooks);
		((ContactAdapter) mContactAdapter).buildSelection(addressBooks);
		List<String> letter = ((ContactAdapter) mContactAdapter).getLetterList();
		mLetterView.setShowLetters(letter);

//		mContactAdapter.executeSelect(true);        // 还他妈得更新一下傻逼完毕
		updateToolBarRightText();
		updateSelectedCount();
	}

	@Override
	public void noneCommonGroups() {
		mAddressBookFilterView.hideCommonlyGroup();
	}

	@Override
	protected void onPause() {
		super.onPause();
		hideWindow();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		hideLoading();
		EventBus.getDefault().unregister(this);
		mHandler.removeCallbacksAndMessages(null);
		hideWindow();
//		WMStamp.getInstance().clearWaterMark(this);
	}

	private void hideAllFilterViewAndShowThis(Fragment targetFragment) {
		mFilterContainer.setVisibility(View.VISIBLE);
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		if (mCompanyView != null) {
			transaction.hide(mCompanyView);
		}

		if (mDepartmentView != null) {
			transaction.hide(mDepartmentView);
		}

		if (mPositionView != null) {
			transaction.hide(mPositionView);
		}

		if (targetFragment != null) {
			transaction.show(targetFragment);
		}
		transaction.commit();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (KeyEvent.KEYCODE_BACK == keyCode) {
			if (mFilterContainer.getVisibility() == View.VISIBLE) {
				hideAllFilterViewAndShowThis(null);
				mFilterContainer.setVisibility(View.GONE);
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	public boolean isFilterViewVisibility(Fragment targetFragment) {
		return mFilterContainer.getVisibility() == View.VISIBLE && targetFragment != null && targetFragment.isVisible();
	}

	private void hideAllFiltersViews() {
		hideAllFilterViewAndShowThis(null);
		mFilterContainer.setVisibility(View.GONE);
	}

}
