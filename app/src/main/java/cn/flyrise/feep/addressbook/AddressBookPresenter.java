package cn.flyrise.feep.addressbook;

import static cn.flyrise.feep.addressbook.source.AddressBookRepository.get;

import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import cn.flyrise.android.protocol.entity.CommonGroupResponse;
import cn.flyrise.android.protocol.entity.CommonGroupUsersResponse;
import cn.flyrise.android.protocol.entity.DefineUserGroupRequest;
import cn.flyrise.android.protocol.model.CommonGroup;
import cn.flyrise.feep.K;
import cn.flyrise.feep.R;
import cn.flyrise.feep.addressbook.model.Department;
import cn.flyrise.feep.addressbook.model.Position;
import cn.flyrise.feep.addressbook.source.AddressBookRepository;
import cn.flyrise.feep.addressbook.view.DepartmentFilterFragment;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.function.FunctionManager;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.core.services.model.AddressBook;
import cn.flyrise.feep.main.modules.Sasigay;
import cn.flyrise.feep.utils.Patches;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ZYP
 * @since 2016-12-12 13:11
 */
public class AddressBookPresenter implements AddressBookContract.IPresenter {

	private AddressBookContract.IView mAddressBookView;
	private Handler mHandler;
	private final List<CommonGroup> mCommonGroups = new ArrayList<>();
	private final Map<String, List<String>> mCommonGroupMap = new HashMap<>();

	public AddressBookPresenter(AddressBookContract.IView view, Handler handler) {
		this.mAddressBookView = view;
		this.mHandler = handler;
	}

	@Override
	public void queryContacts(Department company, Department department, Department subDepartment, Position position) {
		mAddressBookView.showLoading();
		new Thread(() -> {
			String companyName = company == null ? CommonUtil.getString(R.string.unknown_company) : company.name;
			String departmentName = department == null ? CommonUtil.getString(R.string.unknown_department1) : department.name;
			String subDepartmentName = subDepartment == null ? CommonUtil.getString(R.string.unknown_department1) : subDepartment.name;
			String positionName = position == null ? CommonUtil.getString(R.string.unknown_position) : position.position;
			FELog.i("ObtainAddressBookByCondition # thread " + Thread.currentThread().getName() + " : " + companyName + " - "
					+ departmentName + " - " + subDepartmentName + " - " + positionName);

			try {
				String deptId = null;
				boolean accessDeptId = false;
				if (subDepartment != null) {
					deptId = subDepartment.deptId;
					accessDeptId = !TextUtils.equals(deptId, DepartmentFilterFragment.DEFAULT_SUB_DEPARTMENT.deptId);
				}

				if (!accessDeptId && department != null) {
					deptId = department.deptId;
					accessDeptId = true;
				}

				if (!accessDeptId) {
					deptId = company.deptId;
				}

				positionName = (position == null || TextUtils.equals(position.posId, "-10086")) ? null : position.position;
				List<AddressBook> addressBooks = get().queryStaffByCondition(deptId, positionName);

				if (CommonUtil.isEmptyList(addressBooks)) {
					mHandler.post(() -> {
						mAddressBookView.hideLoading();
						mAddressBookView.showContacts(null);
					});
					return;
				}
				mHandler.post(() -> {
					mAddressBookView.hideLoading();
					mAddressBookView.showContacts(addressBooks);
				});
			} catch (Exception exp) {
				mHandler.post(() -> {
					mAddressBookView.hideLoading();
					mAddressBookView.showContacts(null);
				});
			}
		}).start();
	}

	/**
	 * 分几种：Company only, not_position
	 */
	@Override
	public void queryUserDepartmentInfos(String userId, Intent intent) {
		mAddressBookView.showLoading();
		new Thread(() -> {
			// 默认的部门 ID
			String defaultDepartmentId = intent.getStringExtra(K.addressBook.default_department);

			// 是否只显示公司
			boolean isCompanyOnly = intent.getBooleanExtra(K.addressBook.company_only, false);

			// 查询用户所在的部门
			Department userIn = TextUtils.isEmpty(defaultDepartmentId)
					? get().queryDepartmentWhereUserIn(userId)
					: get().queryDepartmentById(defaultDepartmentId);

			// 查询用户所在的公司
			Department _company = get().queryCompanyWhereDepartmentIn(userIn.deptId);

			// 是否只有一个公司
			boolean _isOnlyOneCompany = false;
			List<Department> allCompanies = get().queryAllCompany();
			if (CommonUtil.nonEmptyList(allCompanies) && allCompanies.size() == 1) {
				_isOnlyOneCompany = true;
			}

			// 这段代码今天(2018-08-02 19:25)只有老子知道什么意思，明天就只有上帝知道什么意思了~
//			Department o$_$o = null;
			Department department1 = null;//一级公司
			Department department2 = null;//二级部门
			Department department3 = null;//三级部门-岗位

			boolean isVersionIn7 = FunctionManager.hasPatch(Patches.PATCH_NEW_APPLICATION);
			if (isVersionIn7) {
				department1 = Sasigay.INSTANCE.getSelectedCompany();
//				if (department1 != null) {
//					List<Department> allPartTimeDepartments = get().queryPartTimeDepartment(userId);
//					for (Department d : allPartTimeDepartments) {
//						Department company = get().queryCompanyWhereDepartmentIn(d.deptId);
//						if (company != null && TextUtils.equals(company.deptId, department1.deptId)) {
////							o$_$o = d;
//							department2 = get().queryTopDepartmentEndWithFatherId(d.deptId, department1.fatherId);
//
//							if (department2 != null) {
//								department3 = get().queryTopDepartmentEndWithFatherId(d.deptId, department2.fatherId);
//							}
//							break;
//						}
//					}
//				}
				if (department1 != null) {
					Department userInDepartment = get().queryDepartmentWhereUserIn(userId);   // 查找的是主岗
					Department company = get().queryCompanyWhereDepartmentIn(userInDepartment.deptId);
					if (company != null && TextUtils.equals(company.deptId, department1.deptId)) {
						department2 = get().queryTopDepartmentEndWithFatherId(userInDepartment.deptId, department1.fatherId);
						if (department2 != null) {
							department3 = get().queryTopDepartmentEndWithFatherId(userInDepartment.deptId, department2.fatherId);
						}
					}
				}
			}

			// 只显示公司
			boolean isOnlyOneCompany = _isOnlyOneCompany;
			if (isCompanyOnly) {
				final Department xCompany = _company;
				final Department yCompany = department1;
				mHandler.post(() -> {
					mAddressBookView.hideLoading();
					if (isVersionIn7 && yCompany != null) {
						mAddressBookView.showInitialization(yCompany, null, null, null, isOnlyOneCompany);
					}
					else {
						mAddressBookView.showInitialization(xCompany, null, null, null, isOnlyOneCompany);
					}
				});
				return;
			}

			// 如果只有一个公司的话，部门提前一级显示
			Department company = _isOnlyOneCompany
					? get().queryTopDepartmentEndWithFatherId(userIn.deptId, _company.fatherId)
					: _company;

			Department _department = get().queryTopDepartmentEndWithFatherId(userIn.deptId, company.fatherId);
			Department department = (_department != null && TextUtils.equals(company.deptId, _department.deptId))
					? null
					: _department;

			Department subDepartment = department == null
					? null : get().queryTopDepartmentEndWithFatherId(userIn.deptId, department.fatherId);

			final Department sDepartment1 = department1;
			final Department sDepartment2 = department2;
			final Department sDepartment3 = department3;

			boolean hasPosition = intent.getBooleanExtra(K.addressBook.with_position, false);
			if (!hasPosition) {

				mHandler.post(() -> {
					mAddressBookView.hideLoading();
					if (isVersionIn7 && sDepartment1 != null) {
						mAddressBookView.showInitialization(sDepartment1, sDepartment2, sDepartment3, null, isOnlyOneCompany);
					}
					else {
						mAddressBookView.showInitialization(company, department, subDepartment, null, isOnlyOneCompany);
					}
				});
				return;
			}

			Position ________ = get().queryPositionWhichUserIs(userId);
			mHandler.post(() -> {
				mAddressBookView.hideLoading();
				if (isVersionIn7 && sDepartment1 != null) {
					mAddressBookView.showInitialization(sDepartment1, sDepartment2, sDepartment3, ________, isOnlyOneCompany);
				}
				else {
					mAddressBookView.showInitialization(company, department, subDepartment, ________, isOnlyOneCompany);
				}
			});
		}).start();
	}

	/**
	 * 查询常用组
	 */
	@Override public void queryCommonlyGroup(String userId) {
		if (CommonUtil.nonEmptyList(mCommonGroups)) {
			mAddressBookView.showCommonGroups(mCommonGroups);
			return;
		}

		FEHttpClient.getInstance().post(DefineUserGroupRequest.requestGroup(), new ResponseCallback<CommonGroupResponse>() {
			@Override public void onCompleted(CommonGroupResponse resposne) {
				if (resposne != null && CommonUtil.nonEmptyList(resposne.results)) {
					mAddressBookView.showCommonGroups(resposne.results);
				}
				else {
					mAddressBookView.noneCommonGroups();
				}
			}

			@Override public void onFailure(RepositoryException repositoryException) {
				mAddressBookView.noneCommonGroups();
			}
		});
	}

	/**
	 * 根据 Group Id 查询相关的用户
	 */
	@Override public void queryCommonlyUserByGroupId(String groupId) {
		mAddressBookView.showLoading();
		if (mCommonGroupMap.containsKey(groupId)) {
			List<String> results = mCommonGroupMap.get(groupId);
			if (CommonUtil.isEmptyList(results)) {
				mAddressBookView.showCommonUsers(null);
				return;
			}

			new Thread(() -> {
				final List<AddressBook> addressBooks = get().queryUsersByUserIds(results);
				mHandler.post(() -> mAddressBookView.showCommonUsers(addressBooks));
			}).start();
			return;
		}

		DefineUserGroupRequest request = DefineUserGroupRequest.requestUsersInGroup(groupId);
		FEHttpClient.getInstance().post(request, new ResponseCallback<CommonGroupUsersResponse>() {
			@Override public void onCompleted(CommonGroupUsersResponse response) {
				if (response == null || !TextUtils.equals(response.getErrorCode(), "0")) {
					FEToast.showMessage("获取常用列表失败，请稍后重试");
					mAddressBookView.hideLoading();
					return;
				}

				final List<String> userIds = parseUserIds(response.results);
				mCommonGroupMap.put(groupId, userIds);

				if (CommonUtil.isEmptyList(userIds)) {
					mAddressBookView.showCommonUsers(null);
					return;
				}

				new Thread(() -> {
					final List<AddressBook> addressBooks = get().queryUsersByUserIds(userIds);
					mHandler.post(() -> mAddressBookView.showCommonUsers(addressBooks));
				}).start();
			}

			@Override public void onFailure(RepositoryException repositoryException) {
				FEToast.showMessage("获取常用列表失败，请稍后重试");
				mAddressBookView.hideLoading();
			}
		});
	}

	private static List<String> parseUserIds(String results) {
		if (TextUtils.isEmpty(results)) {
			return null;
		}

		List<String> userIds = null;
		try {
			userIds = Arrays.asList(results.split(","));
		} catch (Exception exp) {
			exp.printStackTrace();
			userIds = null;
		}

		return userIds;
	}
}
