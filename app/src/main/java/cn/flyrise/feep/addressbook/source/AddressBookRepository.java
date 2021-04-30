package cn.flyrise.feep.addressbook.source;

import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import cn.flyrise.android.protocol.entity.CommonTagResponse;
import cn.flyrise.feep.addressbook.model.ContactInfo;
import cn.flyrise.feep.addressbook.model.ContactQueryVO;
import cn.flyrise.feep.addressbook.model.Department;
import cn.flyrise.feep.addressbook.model.Position;
import cn.flyrise.feep.addressbook.processor.AddressBookProcessor;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.function.FunctionManager;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.core.network.request.RemoteRequest;
import cn.flyrise.feep.core.services.model.AddressBook;
import cn.flyrise.feep.utils.Patches;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import rx.Observable;

/**
 * @author ZYP
 * @since 2016-12-06 10:38
 * 通讯录数据仓库
 */
public class AddressBookRepository {

	public static final int PAGE_MAX_COUNT = 50;
	private IAddressBookDataSource mDataSources = null;
	private AdvanceAddressBookDataSource mAdvanceAddressBookDataSource;
	private AddressBookLostUsersSource mAddressBookLostUsersSource;

	private AddressBookRepository() {
	}

	private final static class Singleton {

		private final static AddressBookRepository sInstance = new AddressBookRepository();
	}

	public static AddressBookRepository get() {
		return Singleton.sInstance;
	}

	public void initDataSource(int sourceType) {
		if (sourceType == AddressBookProcessor.ADDRESS_BOOK_SOURCE_JSON) {
			if (mDataSources != null) mDataSources = null;
			mDataSources = new AddressBookJsonSource();
			FELog.e("initDataSource--new AddressBookJsonSource()");
		}
		else if (sourceType == AddressBookProcessor.ADDRESS_BOOK_SOURCE_DB) {
			if (mDataSources != null) {
				if (mDataSources instanceof AddressBookDatabaseSource) ((AddressBookDatabaseSource) mDataSources).closeDb();
				mDataSources = null;
			}
			mDataSources = new AddressBookDatabaseSource();
			SQLiteDatabase sqLiteDatabase = ((AddressBookDatabaseSource) mDataSources).getSQLiteDatabase();
			mAdvanceAddressBookDataSource = new AdvanceAddressBookDataSource(sqLiteDatabase);
			mAddressBookLostUsersSource = new AddressBookLostUsersSource(sqLiteDatabase);
			FELog.e("initDataSource--new AddressBookDatabaseSource()");
		}

		if (mDataSources == null) {
			throw new NullPointerException("You must pass a wrong source type in this method, current source type is $=" + sourceType);
		}
	}

	/**
	 * 查询所有用户数据
	 */
	public List<AddressBook> queryAllAddressBooks() {
		if (isDataSourcePrepared()) {
			return mDataSources.obtainAllAddressBooks();
		}
		return null;
	}

	/**
	 * 查询最顶层公司
	 * @return 返回 fatherId = 0 的部门信息
	 */
	public Department queryHeadCompany() {
		if (isDataSourcePrepared()) {
			return mDataSources.obtainHeadCompany();
		}
		return null;
	}

	/**
	 * 查找全部公司
	 */
	public List<Department> queryAllCompany() {
		isDataSourcePrepared();
		return mDataSources.obtainAllSubCompany();
	}

	/**
	 * 查询指定用户所在的公司，用户可能兼职。
	 */
	public List<Department> queryCompanyWhereUserIn(String userId) {
		if (isDataSourcePrepared()) {
			if (mDataSources == null) {
				return null;
			}
			List<String> deptIds = mDataSources.obtainDepartmentIdsWhereUserIn(userId);
			if (CommonUtil.isEmptyList(deptIds)) return null;
			List<Department> companies = new ArrayList<>();
			for (String deptId : deptIds) {
				Department department = queryCompanyWhereDepartmentIn(deptId);
				if (companies.contains(department)) {
					continue;
				}
				companies.add(department);
			}
			return companies;
		}
		return null;
	}

	/**
	 * 查找当前公司下的子部门，不包含子部门的子部门。
	 * @param companyId 当前公司 id ，其实就是 deptId 日~
	 */
	public List<Department> queryDepartmentByCompany(String companyId) {
		if (isDataSourcePrepared()) {
			return mDataSources.obtainSubDepartments(companyId);
		}
		return null;
	}

	/**
	 * 查询用户的岗位信息
	 * @param userId 用户 id
	 */
	public Position queryPositionWhichUserIs(String userId) {
		if (isDataSourcePrepared()) {
			return mDataSources.obtainPositionWhichUserIs(userId);
		}
		return null;
	}

	/**
	 * 查找指定部门所在的公司信息
	 * @param deptId 部门 id
	 * 现有的数据库表结构找不出可以优化的空间 2017-11-28
	 */
	public Department queryCompanyWhereDepartmentIn(String deptId) {
		return queryTopDepartmentEndWithFatherId(deptId, "0");
	}

	/**
	 * 根据指定的部门 id，往上迭代，直到找到 fatherId 对应的部门位置。
	 * @param deptId 某一具体部门 id
	 * @param endFatherId 终止迭代的父部门 id。
	 * 现有的数据库表结构，找不出可以优化的空间 2017-11-28
	 */
	public Department queryTopDepartmentEndWithFatherId(String deptId, String endFatherId) {
		if (isDataSourcePrepared()) {
			Department targetDepartment = null;
			Department department = mDataSources.obtainDepartmentByDeptId(deptId);

			if (department != null && TextUtils.equals(department.fatherId, endFatherId)) {
				return department;
			}

			while (department != null && !TextUtils.equals(department.fatherId, endFatherId)) {
				targetDepartment = department;
				department = mDataSources.obtainDepartmentByDeptId(department.fatherId);
			}
			return targetDepartment;
		}
		return null;
	}

	/**
	 * 查找指定用户所在的部门
	 * @param userId 用户 id
	 */
	public Department queryDepartmentWhereUserIn(String userId) {
		if (isDataSourcePrepared()) {
			return mDataSources.obtainDepartmentWhereUserIn(userId);
		}
		return null;
	}

	/**
	 * 查询指定部门下的子部门，不含子部门的子部门
	 * @param deptId 指定部门 id
	 */
	public List<Department> querySubDepartmentInDepartment(String deptId) {
		if (isDataSourcePrepared()) {
			return mDataSources.obtainSubDepartments(deptId);
		}
		return null;
	}

	/**
	 * 查找当前部门下的所有岗位，包含子部门的所有岗位
	 * 使用 {@link AdvanceAddressBookDataSource } 进行优化
	 * @param deptId 指定部门 id
	 */
	public List<Position> queryPositionByDeptId(String deptId) {
		if (!isDataSourcePrepared()) {
			return null;
		}

		if (mAdvanceAddressBookDataSource != null) {
			List<Position> positions = mAdvanceAddressBookDataSource.obtainPositionsInDepartment(deptId);
			if (positions != null) {                // 双重保险，一旦 AdvanceAddressBookDataSource 返回空，则使用旧的逻辑，继续往下走
				return positions;
			}
		}

		List<String> deptIds = queryAllSubDepartmentByDeptId(deptId);
		if (CommonUtil.isEmptyList(deptIds)) {  // 当前部门不存在子部门
			return mDataSources.obtainPositionsInDepartment(Arrays.asList(deptId));
		}
		return mDataSources.obtainPositionsInDepartment(deptIds);
	}

	/**
	 * 查询指定部门下的相关人员（包括子部门），按 pinyin 进行排序
	 * 使用 {@link AdvanceAddressBookDataSource } 进行优化
	 * @param deptId 部门 id，需要先迭代出旗下所有子部门
	 * @param positionName 岗位名称，可以为空
	 */
	public List<AddressBook> queryStaffByCondition(String deptId, String positionName) {
		if (!isDataSourcePrepared()) {
			return null;
		}

		if (mAdvanceAddressBookDataSource != null) {
			List<AddressBook> addressBooks = mAdvanceAddressBookDataSource.queryStaffByCondition(deptId, positionName);
			if (addressBooks != null) {
				return addressBooks;
			}
		}

		List<String> deptIds = queryAllSubDepartmentByDeptId(deptId);
		return mDataSources.obtainStaff(CommonUtil.isEmptyList(deptIds) ? Arrays.asList(deptId) : deptIds, positionName);
	}

	/**
	 * 查询指定部门下的相关人员（包括子部门），按 deptGrade 和 sortNo 进行排序
	 * 使用 {@link AdvanceAddressBookDataSource } 进行优化
	 */
	public List<AddressBook> queryStaffBySortNo(String deptId) {
		if (!isDataSourcePrepared()) {
			return null;
		}

		if (mAdvanceAddressBookDataSource != null) {
			List<AddressBook> addressBooks = mAdvanceAddressBookDataSource.queryStaffBySortNo(deptId);
			if (addressBooks != null) {
				return addressBooks;
			}
		}

		List<String> deptIds = queryAllSubDepartmentByDeptId(deptId);
		return mDataSources.obtainStaffBySortNo(CommonUtil.isEmptyList(deptIds) ? Arrays.asList(deptId) : deptIds);
	}

	public ContactQueryVO queryDepartmentStaff(String deptId, int offset) {
		if (!isDataSourcePrepared()) {
			return null;
		}

		ContactQueryVO contactQueryVO = new ContactQueryVO();
		if (offset == 0) {
			int totalCount = mAdvanceAddressBookDataSource.queryDepartmentStaffCount(deptId);
			contactQueryVO.totalCount = totalCount;
			contactQueryVO.totalPage = totalCount % PAGE_MAX_COUNT == 0 ? totalCount / PAGE_MAX_COUNT : (totalCount / PAGE_MAX_COUNT) + 1;
		}
		contactQueryVO.contacts = mAdvanceAddressBookDataSource.queryDepartmentStaff(deptId, offset);
		return contactQueryVO;
	}

	/**
	 * 根据输入的用户名查询，支持模糊查询
	 * @param nameLike 用户输入的名称
	 * @return {@link ContactQueryVO}
	 */
	public ContactQueryVO queryContactByNameLike(String nameLike, int offset) {
		if (isDataSourcePrepared()) {
			ContactQueryVO contactQueryVO = new ContactQueryVO();
			int totalCount = mDataSources.obtainContactCountByNameLike(nameLike);
			contactQueryVO.totalCount = totalCount;
			contactQueryVO.totalPage = totalCount % PAGE_MAX_COUNT == 0 ? totalCount / PAGE_MAX_COUNT : (totalCount / PAGE_MAX_COUNT) + 1;
			contactQueryVO.contacts = mDataSources.obtainContactByNameLike(nameLike, offset);
			return contactQueryVO;
		}
		return null;
	}

	/**
	 * 查询指定部门下的所有子部门 id
	 * @param deptId 部门 id
	 * 使用 {@link AdvanceAddressBookDataSource } 进行优化
	 */
	private List<String> queryAllSubDepartmentByDeptId(String deptId) {
		if (mAdvanceAddressBookDataSource != null) {
			List<String> deptIds = mAdvanceAddressBookDataSource.queryAllSubDepartmentByDeptId(deptId);
			if (deptIds != null) {
				return deptIds;
			}
		}

		List<String> deptIds = hasSubDepartment(deptId);
		if (CommonUtil.isEmptyList(deptIds)) {
			return null;
		}

		List<String> subDeptIds = new ArrayList<>();
		subDeptIds.add(deptId);
		for (String did : deptIds) {
			List<String> strings = queryAllSubDepartmentByDeptId(did);
			if (!CommonUtil.isEmptyList(strings)) {
				subDeptIds.addAll(strings);
			}
			else {
				subDeptIds.add(did);
			}
		}

		return subDeptIds;
	}

	/**
	 * 指定部门是否含有子部门
	 * @param deptId 指定部门
	 */
	public List<String> hasSubDepartment(String deptId) {
		if (isDataSourcePrepared()) {
			return mDataSources.obtainSubDepartmentIds(deptId);
		}
		return null;
	}

	/**
	 * 查找当前登录用户的常用联系人
	 */
	public Observable<List<AddressBook>> queryCommonUsers() {
		return new AddressBookCommonManager(mDataSources).loadCommonAddress();
	}

	/**
	 * 查找当前登录用户的关注列表
	 */
	public Observable<List<AddressBook>> queryMineAttentionUsers() {
		if (isDataSourcePrepared()) {
			return mDataSources.obtainUserByType(IAddressBookDataSource.TYPE_MIME_ATTENTION);
		}
		return Observable.unsafeCreate(f -> {
			f.onError(new RuntimeException("Query mine attention failed."));
			f.onCompleted();
		});
	}

	/**
	 * 查找当前登录用户的下属联系人
	 */
	public Observable<List<AddressBook>> querySubordinatesUsers() {
		if (isDataSourcePrepared()) {
			return mDataSources.obtainSubordinates();
		}
		return Observable.unsafeCreate(f -> {
			f.onError(new RuntimeException("Query common users failed."));
			f.onCompleted();
		});
	}

	/**
	 * 根据用户 Id 查找用户的详细信息
	 * @param userId 用户 id
	 */
	public Observable<ContactInfo> queryUserDetailInfo(String userId) {
		if (isDataSourcePrepared()) {
			return mDataSources.obtainUserDetailInfo(userId);
		}
		return Observable.unsafeCreate(f -> {
			f.onError(new RuntimeException("Query user detail info failed."));
			f.onCompleted();
		});
	}

	/**
	 * 根据用户 Id 及 部门Id 查找用户的详细信息
	 * @param userId 用户 id
	 * @param deptId 用户 deptId
	 */
	public Observable<ContactInfo> queryUserDetailInfo(String userId, String deptId) {
		if (isDataSourcePrepared()) {
			return mDataSources.obtainUserDetailInfo(userId, deptId);
		}
		return Observable.unsafeCreate(f -> {
			f.onError(new RuntimeException("Query user detail info failed."));
			f.onCompleted();
		});
	}

	/**
	 * 根据用户 Id 查找用户基本信息
	 * @param userId 用户 id
	 */
	public AddressBook queryUserBaseInfo(String userId) {
		if (isDataSourcePrepared()) {
			return mDataSources.obtainUserBaseInfo(userId);
		}
		return null;
	}

	//	根据用户id查询用户信息，异步查询丢失用户
	public Observable<AddressBook> queryUserDetail(String userId) {
		if (isDataSourcePrepared()) {
			AddressBook addressBook = mDataSources.obtainUserBaseInfo(userId);
			if (addressBook != null && !TextUtils.isEmpty(addressBook.userId)) {
				return Observable.unsafeCreate(f -> f.onNext(addressBook));
			}
			else if (mAddressBookLostUsersSource != null && FunctionManager.hasPatch(Patches.PATCH_REQUEST_USER_DETAIL)) {
				return mAddressBookLostUsersSource.requestUserDetail(userId);
			}
		}
		return Observable.unsafeCreate(f -> {
			f.onError(new RuntimeException("Query user detail info failed."));
			f.onCompleted();
		});
	}

	/**
	 * 根据传入的 ids 查询用户基本信息
	 * @param userIds 多个用户 id
	 */
	public List<AddressBook> queryUsersByUserIds(List<String> userIds) {
		if (isDataSourcePrepared()) {
			return mDataSources.obtainUserByIds(userIds);
		}
		return null;
	}

	/**
	 * 查询指定用户的兼职部门
	 */
	public List<Department> queryPartTimeDepartment(String userId) {
		if (isDataSourcePrepared()) {
			return mDataSources.obtainPartTimeDepartment(userId);
		}
		return null;
	}

	/**
	 * 查询指定公司下的兼职部门
	 */
	public List<Department> queryPartTimeDepartmentInCompany(String userId, String companyId) {
		if (isDataSourcePrepared()) {
			List<Department> allPartTimeDepartment = mDataSources.obtainPartTimeDepartment(userId);
			if (CommonUtil.isEmptyList(allPartTimeDepartment)) {
				return null;
			}

			List<Department> targetDepartment = new ArrayList<>();
			for (Department d : allPartTimeDepartment) {
				Department c = queryCompanyWhereDepartmentIn(d.deptId);
				if (TextUtils.equals(c.deptId, companyId)) {
					targetDepartment.add(d);
				}
			}
			return targetDepartment;
		}
		return null;
	}

	/**
	 * 查询指定部门信息
	 */
	public Department queryDepartmentById(String deptId) {
		if (isDataSourcePrepared()) {
			return mDataSources.obtainDepartmentByDeptId(deptId);
		}
		return null;
	}

	/**
	 * 数据源是否已经准备好
	 * @return true 已经准备好了
	 */
	private boolean isDataSourcePrepared() {
		if (mDataSources == null) {
			FELog.e("The IAddressBookDataSource object is null, "
					+ "you must call the #initDataSource() when contacts prepared.");
			return false;
		}
		return true;
	}

	public boolean updateUserImageHref(String userId, String userImageHref) {
		if (isDataSourcePrepared()) {
			mDataSources.updateUserImageHref(userId, userImageHref);
			return true;
		}
		return false;
	}

	/**
	 * 根据部门Id查找所在部门的联系人
	 */
	public List<AddressBook> queryContactsByDeptId(String deptId) {
		if (isDataSourcePrepared()) {
			return mDataSources.obtainStaff(Arrays.asList(deptId), null);
		}
		return null;
	}

	public List<AddressBook> queryContactsByDeptIds(List<String> deptIds) {
		if (isDataSourcePrepared()){
			return mDataSources.obtainStaff(deptIds, null);
		}
		return null;
	}
}
