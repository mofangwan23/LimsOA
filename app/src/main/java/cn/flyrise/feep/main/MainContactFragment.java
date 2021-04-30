package cn.flyrise.feep.main;

import static cn.flyrise.feep.addressbook.processor.AddressBookProcessor.ADDRESS_BOOK_DOWNLOADING;
import static cn.flyrise.feep.addressbook.processor.AddressBookProcessor.ADDRESS_BOOK_DOWNLOADING_UPDATE;
import static cn.flyrise.feep.addressbook.processor.AddressBookProcessor.ADDRESS_BOOK_DOWNLOAD_ACTION;
import static cn.flyrise.feep.addressbook.processor.AddressBookProcessor.ADDRESS_BOOK_DOWNLOAD_FAILED;
import static cn.flyrise.feep.addressbook.processor.AddressBookProcessor.ADDRESS_BOOK_INIT_SUCCESS;
import static cn.flyrise.feep.addressbook.processor.AddressBookProcessor.ADDRESS_BOOK_UPDATE_FAILED;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import cn.flyrise.android.library.utility.LoadingHint;
import cn.flyrise.android.shared.utility.FEUmengCfg;
import cn.flyrise.feep.K;
import cn.flyrise.feep.K.addressBook;
import cn.flyrise.feep.R;
import cn.flyrise.feep.addressbook.AddressBookDetailActivity;
import cn.flyrise.feep.addressbook.MineAttentionActivity;
import cn.flyrise.feep.addressbook.MineDepartmentActivity;
import cn.flyrise.feep.addressbook.OrganizationStructureActivity;
import cn.flyrise.feep.addressbook.SubordinatesActivity;
import cn.flyrise.feep.addressbook.model.Department;
import cn.flyrise.feep.addressbook.processor.AddressBookDownloadServices;
import cn.flyrise.feep.addressbook.source.AddressBookRepository;
import cn.flyrise.feep.addressbook.utils.ContactsIntent;
import cn.flyrise.feep.commonality.TheContactPersonSearchActivity;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.DevicesUtil;
import cn.flyrise.feep.core.common.utils.GsonUtil;
import cn.flyrise.feep.core.function.FunctionManager;
import cn.flyrise.feep.core.premission.FePermissions;
import cn.flyrise.feep.core.premission.PermissionCode;
import cn.flyrise.feep.core.premission.PermissionSettingDialog;
import cn.flyrise.feep.event.CompanyChangeEvent;
import cn.flyrise.feep.main.adapter.MainContactAdapter;
import cn.flyrise.feep.main.adapter.MainContactModel;
import cn.flyrise.feep.main.modules.Sasigay;
import cn.flyrise.feep.utils.Patches;
import com.hyphenate.chatui.utils.IMHuanXinHelper;
import java.util.ArrayList;
import java.util.List;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author ZYP
 * @since 2017-02-13 10:22
 */
public class MainContactFragment extends Fragment {

	private AddressBookActionReceiver mActionReceiver;
	private ViewGroup mWaterMarkContainer;
	private RecyclerView mRecyclerView;
	private MainContactAdapter mAdapter;

	private View mLoadingView;
	private View mProgressView;
	private TextView mTvLoading;
	private ImageView mIvFailed;
	private Button mBtnRetryDownload;
	public FEToolbar mToolbar;
	private Department mMineDepartment;
	private List<MainContactModel> models;
	private List<MainContactModel> mPartTimeDepartmentModels;
	List<Department> departments = null;
	private boolean load_frequent_contacts_complete;
	private boolean isMajorCompany;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		EventBus.getDefault().register(this);
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View contentView = inflater.inflate(R.layout.fragment_main_contact, container, false);
		bindView(contentView);
		bindByAddressBookState();
		startInitReceiver();
		return contentView;
	}

	@Override public void onStart() {
		super.onStart();
		FePermissions.with(this)
				.permissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE})
				.rationaleMessage(getResources().getString(R.string.permission_msg_base_info))
				.requestCode(PermissionCode.BASE)
				.request();
	}

	private void bindByAddressBookState() {
		if (CoreZygote.getLoginUserServices() == null) return;
		int addressBookState = CoreZygote.getLoginUserServices().getAddressBookState();
		switch (addressBookState) {
			case ADDRESS_BOOK_DOWNLOADING:                                                      // 正在下载
				promptInfoChange(true);
				break;
			case ADDRESS_BOOK_DOWNLOAD_FAILED:                                                  // 下载失败，尝试重新下载
				promptInfoChange(false);
				mTvLoading.setOnClickListener(
						view -> AddressBookDownloadServices.start(getActivity()));
				break;
			case ADDRESS_BOOK_DOWNLOADING_UPDATE:                                               // 存在旧数据
				bindData();
				break;
			case ADDRESS_BOOK_UPDATE_FAILED:
			case ADDRESS_BOOK_INIT_SUCCESS:
				bindData();
				break;
		}
	}

	private void startInitReceiver() {
		if (getActivity() == null) return;
		mActionReceiver = new AddressBookActionReceiver();
		getActivity().registerReceiver(mActionReceiver, new IntentFilter(ADDRESS_BOOK_DOWNLOAD_ACTION));
	}

	public void bindView(View view) {
		String title = getResources().getString(R.string.top_contact);
		mToolbar = view.findViewById(R.id.toolBar);
		if (!TextUtils.isEmpty(title) && mToolbar != null) {
			mToolbar.setTitle(title);
			mToolbar.setNavigationVisibility(View.GONE);
			if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT && !DevicesUtil.isSpecialDevice()) {
				int statusBarHeight = DevicesUtil.getStatusBarHeight(getActivity());
				mToolbar.setPadding(0, statusBarHeight, 0, 0);
			}
		}

		view.findViewById(R.id.layoutContactSearch).setOnClickListener(v ->         // 设置点击事件
				startActivity(new Intent(getActivity(), TheContactPersonSearchActivity.class)));

		mWaterMarkContainer = view.findViewById(R.id.layoutContentView);
		mLoadingView = view.findViewById(R.id.layoutLoading);
		mTvLoading = view.findViewById(R.id.tvContactLoading);
		mIvFailed = view.findViewById(R.id.ivFailed);
		mBtnRetryDownload = view.findViewById(R.id.btnRetry);
		mProgressView = view.findViewById(R.id.progress_view);

		mRecyclerView = view.findViewById(R.id.recyclerView);
		mRecyclerView.setTag("FeiLaoMian");
		mRecyclerView.setItemAnimator(new DefaultItemAnimator());
		mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
		mRecyclerView.setAdapter(mAdapter = new MainContactAdapter(getContext()));
		mAdapter.setOnItemClickListener(this::onItemClick);
		mAdapter.setOnPartTimeDepartmentItemClickLister(this::OnItemPartTimeDepartmentClick);
		mAdapter.setContactTitleClicklistener(new MainContactAdapter.OnContactTitleClickListener() {
			@Override
			public void onAllContactClick() {
				new ContactsIntent(getActivity()).companyOnly().open();
			}

			@Override
			public void onGroupChatClick() {
				IMHuanXinHelper.getInstance().startGroupListActivity(getActivity());
			}

			@Override
			public void onSubordinatesClick() {
				getActivity().startActivity(new Intent(getActivity(), SubordinatesActivity.class));
			}

			@Override
			public void onFollowClick() {
				getActivity().startActivity(new Intent(getActivity(), MineAttentionActivity.class));
			}
		});
	}

	private void bindData() {
		models = new ArrayList<>();
		LoadingHint.show(getActivity());
		Observable
				.unsafeCreate((OnSubscribe<List<MainContactModel>>) f -> {
					Department headDepartment = AddressBookRepository.get().queryHeadCompany();// 查询总公司
					String headCompanyName = headDepartment == null ? "" : headDepartment.name;
					FEUmengCfg.sendDepertmentName(getActivity(), headCompanyName);              // 友盟数据统计

					// 查询用户所在部门
					String userId = CoreZygote.getLoginUserServices().getUserId();
					mMineDepartment = AddressBookRepository.get()
							.queryDepartmentWhereUserIn(userId);                               // 查找的是主岗
					// 所有人，群聊，下属，关注人
					models.add(new MainContactModel.Builder()
							.setTag("header").build());
					// 组织架构
					models.add(new MainContactModel.Builder()
							.setTag(getResources().getString(R.string.organizational_structure)).build());

					// 组织架构
					models.add(new MainContactModel.Builder()
							.setIconRes(R.drawable.icon_address_department)
							.setName(headCompanyName)
							.setType(MainContactAdapter.TYPE_COMPANY)
							.setSubName(getResources().getString(R.string.organizational_structure))
							.setArrowVisibility(true)
							.build());

					// 查找兼职部门
					departments = null;
					if (FunctionManager.hasPatch(Patches.PATCH_PART_TIME_DEPARTMENT)) {
						departments = AddressBookRepository.get().queryPartTimeDepartment(userId);
					}

					Department majorCompany = AddressBookRepository.get()
							.queryCompanyWhereDepartmentIn(mMineDepartment.deptId);    // 主岗所在的公司
					Department selectedCompany = Sasigay.INSTANCE.getSelectedCompany();                 // 选中的公司

					isMajorCompany = isMajorCompany(majorCompany, selectedCompany);
					if (isMajorCompany) {
						// 我的部门
						models.add(new MainContactModel.Builder()
								.setIconRes(R.drawable.the_contact_dept_iocn_vh)
								.setName(mMineDepartment.name)
								.setType(MainContactAdapter.TYPE_DEPARTMENT)
								.setSubName(getResources().getString(R.string.organizational_mine_department))
								.setArrowVisibility(true)
								.setDividerVisiblity(CommonUtil.isEmptyList(departments))
								.build());
					}

					if (CommonUtil.nonEmptyList(departments)) {
						int len = departments.size() - 1;
						Department lastPartTimeDepartment = departments.get(len);

						if (!isMajorCompany && departments.size() <= 2) {
							for (int i = 0; i < len; i++) {
								Department partTimeDepartment = departments.get(i);
								models.add(new MainContactModel.Builder()
										.setIconRes(R.drawable.the_contact_dept_iocn_vh)
										.setName(partTimeDepartment.name)
										.setType(MainContactAdapter.TYPE_DEPARTMENT)
										.setDepartmentId(partTimeDepartment.deptId)
										.setSubName(getResources().getString(R.string.organizational_part_time_department))
										.setArrowVisibility(true)
										.build());
							}
							models.add(new MainContactModel.Builder()
									.setIconRes(R.drawable.the_contact_dept_iocn_vh)
									.setName(lastPartTimeDepartment.name)
									.setType(MainContactAdapter.TYPE_DEPARTMENT)
									.setDepartmentId(lastPartTimeDepartment.deptId)
									.setSubName(getResources().getString(R.string.organizational_part_time_department))
									.setArrowVisibility(true)
									.setDividerVisiblity(true)
									.build());
						}
						else if (!isMajorCompany && departments.size() >= 3) {
							Department partTimeDepartment0 = departments.get(0);
							models.add(new MainContactModel.Builder()
									.setIconRes(R.drawable.the_contact_dept_iocn_vh)
									.setName(partTimeDepartment0.name)
									.setType(MainContactAdapter.TYPE_DEPARTMENT)
									.setDepartmentId(partTimeDepartment0.deptId)
									.setSubName(getResources().getString(R.string.organizational_part_time_department))
									.setArrowVisibility(true)
									.build());
							Department partTimeDepartment1 = departments.get(1);
							models.add(new MainContactModel.Builder()
									.setIconRes(R.drawable.the_contact_dept_iocn_vh)
									.setName(partTimeDepartment1.name)
									.setType(MainContactAdapter.TYPE_DEPARTMENT)
									.setDepartmentId(partTimeDepartment1.deptId)
									.setSubName(getResources().getString(R.string.organizational_part_time_department))
									.setArrowVisibility(true)
									.setDividerVisiblity(true)
									.setDepartmentsize(departments.size())
									.build());

							mPartTimeDepartmentModels = new ArrayList<>();
							for (int i = 2; i < len - 1; i++) {
								Department partTimeDepartment = departments.get(i);
								mPartTimeDepartmentModels.add(new MainContactModel.Builder()
										.setIconRes(R.drawable.the_contact_dept_iocn_vh)
										.setName(partTimeDepartment.name)
										.setType(MainContactAdapter.TYPE_DEPARTMENT)
										.setDepartmentId(partTimeDepartment.deptId)
										.setSubName(getResources().getString(R.string.organizational_part_time_department))
										.setArrowVisibility(false)
										.build());
							}

							mPartTimeDepartmentModels.add(new MainContactModel.Builder()
									.setIconRes(R.drawable.the_contact_dept_iocn_vh)
									.setName(lastPartTimeDepartment.name)
									.setType(MainContactAdapter.TYPE_DEPARTMENT)
									.setDepartmentId(lastPartTimeDepartment.deptId)
									.setSubName(getResources().getString(R.string.organizational_part_time_department))
									.setArrowVisibility(false)
									.setDividerVisiblity(!CommonUtil.isEmptyList(departments))
									.setDepartmentsize(departments.size() - 1)
									.build());


						}
						else if (isMajorCompany && departments.size() <= 1) {
							models.add(new MainContactModel.Builder()
									.setIconRes(R.drawable.the_contact_dept_iocn_vh)
									.setName(lastPartTimeDepartment.name)
									.setType(MainContactAdapter.TYPE_DEPARTMENT)
									.setDepartmentId(lastPartTimeDepartment.deptId)
									.setSubName(getResources().getString(R.string.organizational_part_time_department))
									.setArrowVisibility(true)
									.setDividerVisiblity(true)
									.build());
						}
						else {
							mPartTimeDepartmentModels = new ArrayList<>();
							Department firstPartTimeDepartment = departments.get(0);
							models.add(new MainContactModel.Builder()
									.setIconRes(R.drawable.the_contact_dept_iocn_vh)
									.setName(firstPartTimeDepartment.name)
									.setType(MainContactAdapter.TYPE_DEPARTMENT)
									.setDepartmentId(firstPartTimeDepartment.deptId)
									.setSubName(getResources().getString(R.string.organizational_part_time_department))
									.setArrowVisibility(true)
									.setDividerVisiblity(true)
									.setDepartmentsize(departments.size())
									.build());

							for (int i = 1; i < len; i++) {
								Department partTimeDepartment = departments.get(i);
								mPartTimeDepartmentModels.add(new MainContactModel.Builder()
										.setIconRes(R.drawable.the_contact_dept_iocn_vh)
										.setName(partTimeDepartment.name)
										.setType(MainContactAdapter.TYPE_DEPARTMENT)
										.setDepartmentId(partTimeDepartment.deptId)
										.setSubName(getResources().getString(R.string.organizational_part_time_department))
										.setArrowVisibility(false)
										.build());
							}

							mPartTimeDepartmentModels.add(new MainContactModel.Builder()
									.setIconRes(R.drawable.the_contact_dept_iocn_vh)
									.setName(lastPartTimeDepartment.name)
									.setType(MainContactAdapter.TYPE_DEPARTMENT)
									.setDepartmentId(lastPartTimeDepartment.deptId)
									.setSubName(getResources().getString(R.string.organizational_part_time_department))
									.setArrowVisibility(false)
									.setDividerVisiblity(!CommonUtil.isEmptyList(departments))
									.setDepartmentsize(departments.size())
									.build());
						}
					}

					// 根据是否含有 CRM 模块来判断是否有外部联系人这一选项
					boolean hasExternalContact = FunctionManager.hasModule(43);
					MainContactModel.Builder mineAttentionBuilder = new MainContactModel.Builder()
							.setIconRes(R.drawable.my_attention)
							.setName(getResources().getString(R.string.my_attention))
							.setType(MainContactAdapter.TYPE_ATTENTION)
							.setArrowVisibility(true);

					if (!hasExternalContact) {
						mineAttentionBuilder.setDividerVisiblity(true);
					}

					f.onNext(models);
					f.onCompleted();
				})
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(ms -> {
					LoadingHint.hide();
					mAdapter.setIsShowPartTimeDepartment(false);
					refreshModels(ms);

					// 组装常用联系人数据
					final String host = CoreZygote.getLoginUserServices().getServerAddress();
					AddressBookRepository.get().queryCommonUsers()
							.flatMap(Observable::from)
							.map(addressBook -> {
								String deptName = TextUtils.isEmpty(addressBook.deptName) ? "" : addressBook.deptName + "-";
								return new MainContactModel.Builder()
										.setArrowVisibility(false)
										.setType(MainContactAdapter.TYPE_COMMON_USE)
										.setName(addressBook.name)
										.setSubName(deptName + addressBook.position)
										.setIconUrl(host + addressBook.imageHref)
										.setUserId(addressBook.userId)
										.build();
							})
							.toList()
							.subscribeOn(Schedulers.io())
							.observeOn(AndroidSchedulers.mainThread())
							.subscribe(commonUserModels -> {
										if (CommonUtil.nonEmptyList(commonUserModels)) {
											commonUserModels.add(0, new MainContactModel.Builder()
													.setTag(getResources().getString(R.string.frequent_contacts))
													.build());
											MainContactModel model = commonUserModels.get(commonUserModels.size() - 1);
											if (model != null) {
												model.hasLongSpliteLine = true;
												commonUserModels.set(commonUserModels.size() - 1, model);
											}
											mAdapter.addMainContactModels(commonUserModels);
//									WMStamp.getInstance().draw(mWaterMarkContainer, mRecyclerView);
										}
										load_frequent_contacts_complete = true;
									},
									Throwable::printStackTrace);
				}, it -> {
					LoadingHint.hide();
					it.printStackTrace();
					promptInfoChange(false);
					FEToast.showMessage("通讯录加载失败，请重试！");
				});
	}

	public void refreshModels(List<MainContactModel> models) {
		FELog.i("-->>>module:" + GsonUtil.getInstance().toJson(models));
		mAdapter.setMainContactModels(models);
		mAdapter.notifyDataSetChanged();
//		WMStamp.getInstance().draw(mWaterMarkContainer, mRecyclerView);
		mLoadingView.setVisibility(View.GONE);
		mRecyclerView.setVisibility(View.VISIBLE);
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onCompanyChange(CompanyChangeEvent event) {
		bindData();
	}

	private void promptInfoChange(boolean isDownloading) {
		mLoadingView.setVisibility(View.VISIBLE);
		if (isDownloading) {
			mIvFailed.setVisibility(View.GONE);
			mBtnRetryDownload.setVisibility(View.GONE);
			mProgressView.setVisibility(View.VISIBLE);
			mTvLoading.setText(getResources().getString(R.string.contact_loading));
		}
		else {
			mProgressView.setVisibility(View.GONE);
			mIvFailed.setVisibility(View.VISIBLE);
			mTvLoading.setText("联系人加载失败");
			mBtnRetryDownload.setVisibility(View.VISIBLE);
			mBtnRetryDownload.setOnClickListener(view -> {
				AddressBookDownloadServices.start(getActivity());
				promptInfoChange(true);
			});
		}
	}

	public void onItemClick(MainContactModel model) {
		if (model.type == MainContactAdapter.TYPE_COMPANY) {
			Intent intent = new Intent(getActivity(), OrganizationStructureActivity.class);
			intent.putExtra(addressBook.department_name, model.name);
			getActivity().startActivity(intent);
		}
		else if (model.type == MainContactAdapter.TYPE_DEPARTMENT) {
			Intent intent = new Intent(getActivity(), MineDepartmentActivity.class);
			intent.putExtra(addressBook.department_name, model.name);
			intent.putExtra(addressBook.department_id, TextUtils.isEmpty(model.deptId) ? mMineDepartment.deptId : model.deptId);
			getActivity().startActivity(intent);
		}
		else if (model.type == MainContactAdapter.TYPE_ALL) {
			new ContactsIntent(getActivity()).companyOnly().open();
		}
		else if (model.type == MainContactAdapter.TYPE_ATTENTION) {
			getActivity().startActivity(new Intent(getActivity(), MineAttentionActivity.class));
		}
		else if (model.type == MainContactAdapter.TYPE_COMMON_USE) {
			Intent intent = new Intent(getActivity(), AddressBookDetailActivity.class);
			intent.putExtra(K.addressBook.user_id, model.userId);
			getActivity().startActivity(intent);
		}
		else if (model.type == MainContactAdapter.TYPE_GROUP_CHAT) {                                     // 新建群聊
			IMHuanXinHelper.getInstance().startGroupListActivity(getActivity());
		}
		else if (model.type == MainContactAdapter.TYPE_CUSTOM_CONTACT) {
			startActivity(new Intent(getActivity(), ExternalContactListActivity.class));
		}
	}

	public void OnItemPartTimeDepartmentClick(boolean isShowDepartment) {
//        if(!load_frequent_contacts_complete){
//            return;
//        }
		Department firstPartTimeDepartment = null;
		int extendsSize;

		if (mMineDepartment == null || !isMajorCompany) {//非主公司会少一个"我的部门"
			firstPartTimeDepartment = departments.get(1);
			extendsSize = departments.size() - 2;
		}
		else {
			firstPartTimeDepartment = departments.get(0);
			extendsSize = departments.size() - 1;
		}

		if (isShowDepartment) {
			models.set(4, new MainContactModel.Builder()
					.setIconRes(R.drawable.the_contact_dept_iocn_vh)
					.setName(firstPartTimeDepartment.name)
					.setType(MainContactAdapter.TYPE_DEPARTMENT)
					.setDepartmentId(firstPartTimeDepartment.deptId)
					.setSubName(getResources().getString(R.string.organizational_part_time_department))
					.setArrowVisibility(true)
					.setDividerVisiblity(false)
					.setDepartmentsize(0)
					.build());
			models.addAll(5, mPartTimeDepartmentModels);
			refreshModels(models);
		}
		else {
			models.set(4, new MainContactModel.Builder()
					.setIconRes(R.drawable.the_contact_dept_iocn_vh)
					.setName(firstPartTimeDepartment.name)
					.setType(MainContactAdapter.TYPE_DEPARTMENT)
					.setDepartmentId(firstPartTimeDepartment.deptId)
					.setSubName(getResources().getString(R.string.organizational_part_time_department))
					.setArrowVisibility(true)
					.setDividerVisiblity(true)
					.setDepartmentsize(extendsSize)
					.build());
			for (int i = 0; i < mPartTimeDepartmentModels.size(); i++) {
				models.remove(5);
			}
			refreshModels(models);
		}
	}

	private boolean isMajorCompany(Department _1, Department _2) {
		if (_1 == null) return false;
		if (_2 == null) return true;
		return TextUtils.equals(_1.deptId, _2.deptId);
	}

	private class AddressBookActionReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			bindByAddressBookState();
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		EventBus.getDefault().unregister(this);
//		WMStamp.getInstance().clearWaterMark(mWaterMarkContainer);
		if (mActionReceiver != null) {
			getActivity().unregisterReceiver(mActionReceiver);
			mActionReceiver = null;
		}
	}


	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		FePermissions.onRequestPermissionsResult(this, requestCode, permissions, grantResults,
				(requestCode1, permissions1, grantResults1, deniedMessage) -> new PermissionSettingDialog.Builder(getActivity())
						.setCancelable(false)
						.setMessage(getResources().getString(R.string.permission_msg_request_failed_storage))
						.setNeutralText(getResources().getString(R.string.permission_text_go_setting))
						.setPositiveText(getResources().getString(R.string.permission_text_i_know))
						.setPositiveListener(v -> getActivity().finish())
						.setNeutralListener(v -> {
							Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
							intent.setData(Uri.fromParts("package", getActivity().getPackageName(), null));
							startActivity(intent);
							getActivity().finish();
						})
						.build()
						.show());
	}

}
