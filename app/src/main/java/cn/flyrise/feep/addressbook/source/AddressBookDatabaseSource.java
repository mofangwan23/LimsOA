package cn.flyrise.feep.addressbook.source;

import static cn.flyrise.feep.addressbook.utils.AddressBookUtils.isColumnExist;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import cn.flyrise.android.protocol.entity.CommonTagResponse;
import cn.flyrise.android.protocol.entity.ContactDetailResponse;
import cn.flyrise.android.protocol.entity.LocationLocusRequest;
import cn.flyrise.android.protocol.entity.LocationLocusResponse;
import cn.flyrise.feep.K;
import cn.flyrise.feep.addressbook.model.ContactInfo;
import cn.flyrise.feep.addressbook.model.Department;
import cn.flyrise.feep.addressbook.model.Position;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.SpUtil;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.core.network.request.RemoteRequest;
import cn.flyrise.feep.core.services.model.AddressBook;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import rx.Observable;

/**
 * @author ZYP
 * @since 2016-12-03 10:45
 */
public class AddressBookDatabaseSource implements IAddressBookDataSource {

	private String mDataBasePath;
	private SQLiteDatabase mSQLiteDatabase;

	AddressBookDatabaseSource() {
		String dbName = SpUtil.get(K.preferences.address_book_version, "");
		mDataBasePath = CoreZygote.getPathServices().getAddressBookPath() + File.separator + dbName;
//		mDataBasePath = CoreZygote.getPathServices().getAddressBookPath() + File.separator + "77413.db";
		FELog.i("AddressBook Database Source & database path is : " + mDataBasePath);
		File dbFile = new File(mDataBasePath);
		if (!dbFile.exists()) {
			throw new NullPointerException("Cannot found the database file, may be need to re-login.");
		}

		try {
			// 优先创建一个可读可写的数据库。在某些情况下，由于数据库被锁定，无法打开，这里会报异常
			mSQLiteDatabase = SQLiteDatabase.openDatabase(mDataBasePath, null, SQLiteDatabase.OPEN_READWRITE);
		} catch (Exception exp) {
			exp.printStackTrace();
			mSQLiteDatabase = null;
		}

		// 创建可读写的数据库失败，只好退而求其次，创建一个只读的数据库
		if (mSQLiteDatabase == null) {
			mSQLiteDatabase = SQLiteDatabase.openDatabase(mDataBasePath, null, SQLiteDatabase.OPEN_READONLY);
		}
	}

	@Override
	public List<AddressBook> obtainAllAddressBooks() {
		checkBeforeExecute();
		Cursor cursor = null;
		List<AddressBook> addressBooks = null;
		String sql = "select userId, name, imageHref, deptId, imid, department from AddressBookTable where isPartTime = 0";
		try {
			cursor = mSQLiteDatabase.rawQuery(sql, null);
			addressBooks = new ArrayList<>();
			while (cursor.moveToNext()) {
				AddressBook addressBook = new AddressBook();
				addressBook.userId = cursor.getString(0);
				addressBook.name = cursor.getString(1);
				addressBook.imageHref = cursor.getString(2);
				addressBook.deptId = cursor.getString(3);
				addressBook.imid = cursor.getString(4);
				addressBook.deptName = cursor.getString(5);
				addressBooks.add(addressBook);
			}
		} catch (Exception exp) {
			exp.printStackTrace();
		} finally {
			closeCursor(cursor);
		}

		return addressBooks;
	}

	@Override
	public Department obtainHeadCompany() {
		checkBeforeExecute();
		Cursor cursor = null;
		Department headCompany = null;
		String sql = "select * from DepartmentTable where fatherId = 0";
		try {
			cursor = mSQLiteDatabase.rawQuery(sql, null);
			if (cursor.moveToNext()) {
				headCompany = new Department(cursor.getString(cursor.getColumnIndex("deptId")),
						cursor.getString(cursor.getColumnIndex("name")),
						cursor.getInt(cursor.getColumnIndex("level")),
						cursor.getString(cursor.getColumnIndex("fatherId")));

				int gradeIndex = cursor.getColumnIndex("grade");
				if (gradeIndex != -1) {
					headCompany.grade = cursor.getString(gradeIndex);
				}
			}
		} catch (Exception exp) {
			exp.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
		return headCompany;
	}

	/**
	 * 查询当前集团下所有子公司~
	 */
	@Override
	public List<Department> obtainAllSubCompany() {
		this.checkBeforeExecute();
		Cursor cursor = null;
		List<Department> companies = null;
		try {
			boolean isGradeExist = isColumnExist(mSQLiteDatabase, "DepartmentTable", "grade");
			boolean isSortNoExist = isColumnExist(mSQLiteDatabase, "DepartmentTable", "sortNo");

			StringBuilder builder = new StringBuilder();
			builder.append("select deptId, name ");
			if (isGradeExist) {
				builder.append(", grade ");
			}

			// 无论如何都得查出顶级集团呀... 所以这个没差...
			builder.append("from DepartmentTable where fatherId = (select deptId from DepartmentTable where fatherId = ?)");

			if (isSortNoExist) {
				builder.append("order by sortNo ");
			}
			else if (isGradeExist) {
				builder.append("order by grade ");
			}

			cursor = mSQLiteDatabase.rawQuery(builder.toString(), new String[]{"0"});
			companies = new ArrayList<>();
			while (cursor.moveToNext()) {
				Department department = new Department(cursor.getString(cursor.getColumnIndex("deptId")),
						cursor.getString(cursor.getColumnIndex("name")));

				int gradeIndex = cursor.getColumnIndex("grade");
				if (gradeIndex != -1) {
					department.grade = cursor.getString(gradeIndex);
				}

				companies.add(department);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
		return companies;
	}

	/**
	 * 查询指定用户的兼职部门
	 * @param userId 指定用于
	 */
	@Override
	public List<Department> obtainPartTimeDepartment(String userId) {
		this.checkBeforeExecute();
		Cursor cursor = null;
		List<Department> departments = null;
		try {
			boolean isGradeExist = isColumnExist(mSQLiteDatabase, "DepartmentTable", "grade");
			boolean isSortNoExist = isColumnExist(mSQLiteDatabase, "DepartmentTable", "sortNo");
			StringBuilder builder = new StringBuilder();
			builder.append("select * from DepartmentTable ")
					.append("where deptId in ")
					.append("(select deptId from UsersTable where userId = ? and isPartTime = ?) ");

			if (isSortNoExist) {
				builder.append("order by sortNo ");
			}
			else if (isGradeExist) {
				builder.append("order by grade ");
			}

			cursor = mSQLiteDatabase.rawQuery(builder.toString(), new String[]{userId, "1"});
			departments = new ArrayList<>();
			while (cursor.moveToNext()) {
				Department department = new Department(cursor.getString(cursor.getColumnIndex("deptId")),
						cursor.getString(cursor.getColumnIndex("name")),
						cursor.getInt(cursor.getColumnIndex("level")),
						cursor.getString(cursor.getColumnIndex("fatherId")));

				int gradeIndex = cursor.getColumnIndex("grade");
				if (gradeIndex != -1) {
					department.grade = cursor.getString(gradeIndex);
				}

				departments.add(department);
			}
		} catch (Exception exp) {
			exp.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
		return departments;
	}

	/**
	 * 查询指定用户任职的部门
	 * @param userId 用户 id
	 */
	@Override
	public List<String> obtainDepartmentIdsWhereUserIn(String userId) {
		checkBeforeExecute();
		Cursor cursor = null;
		List<String> deptIds = null;
		try {
			String sql = "select deptId from AddressBookTable where userId = ?";
			cursor = mSQLiteDatabase.rawQuery(sql, new String[]{userId});
			deptIds = new ArrayList<>();
			while (cursor.moveToNext()) {
				deptIds.add(cursor.getString(0));
			}
		} catch (Exception exp) {
			exp.printStackTrace();
		} finally {
			closeCursor(cursor);
		}

		return deptIds;
	}

	/**
	 * 根据指定的部门 id 查找部门详细信息
	 * @param deptId 部门 id
	 */
	@Override
	public Department obtainDepartmentByDeptId(String deptId) {
		this.checkBeforeExecute();
		if (TextUtils.isEmpty(deptId) || TextUtils.equals(deptId, "0")) {
			return null;
		}

		Cursor cursor = null;
		Department department = null;
		try {
			cursor = mSQLiteDatabase.rawQuery("select * from DepartmentTable where deptId = ?", new String[]{deptId});
			if (cursor.moveToNext()) {
				department = new Department(cursor.getString(cursor.getColumnIndex("deptId")),
						cursor.getString(cursor.getColumnIndex("name")),
						cursor.getInt(cursor.getColumnIndex("level")),
						cursor.getString(cursor.getColumnIndex("fatherId")));

				int gradeIndex = cursor.getColumnIndex("grade");
				if (gradeIndex != -1) {
					department.grade = cursor.getString(gradeIndex);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
		return department;
	}

	/**
	 * 查询指定用户所在的部门信息，用户主职部门
	 * @param userId 用户 id
	 */
	@Override
	public Department obtainDepartmentWhereUserIn(String userId) {
		this.checkBeforeExecute();
		Cursor cursor = null;
		Department department = null;
		try {
			cursor = mSQLiteDatabase.rawQuery("select * from DepartmentTable where deptId = " +
					"(select deptId from AddressBookTable where userId = ? and isPartTime = ?)", new String[]{userId, "0"});
			if (cursor.moveToNext()) {
				department = new Department(cursor.getString(cursor.getColumnIndex("deptId")),
						cursor.getString(cursor.getColumnIndex("name")),
						cursor.getInt(cursor.getColumnIndex("level")),
						cursor.getString(cursor.getColumnIndex("fatherId")));

				int gradeIndex = cursor.getColumnIndex("grade");
				if (gradeIndex != -1) {
					department.grade = cursor.getString(gradeIndex);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
		return department;
	}

	/**
	 * 根据父部门的 id 查询其下子部门，不含子部门的子部门。
	 * @param parentDepartmentId 父部门 id
	 */
	@Override
	public List<Department> obtainSubDepartments(String parentDepartmentId) {
		this.checkBeforeExecute();
		Cursor cursor = null;
		List<Department> departments = null;
		try {
			boolean isGradeExist = isColumnExist(mSQLiteDatabase, "DepartmentTable", "grade");
			boolean isSortNoExist = isColumnExist(mSQLiteDatabase, "DepartmentTable", "sortNo");

			StringBuilder builder = new StringBuilder();
			builder.append("select * from DepartmentTable where fatherId = ? ");
			if (isSortNoExist) {
				builder.append("order by sortNo ");
			}
			else if (isGradeExist) {
				builder.append("order by grade");
			}

			cursor = mSQLiteDatabase.rawQuery(builder.toString(), new String[]{parentDepartmentId});
			departments = new ArrayList<>();
			while (cursor.moveToNext()) {
				Department department = new Department(cursor.getString(cursor.getColumnIndex("deptId")),
						cursor.getString(cursor.getColumnIndex("name")),
						cursor.getInt(cursor.getColumnIndex("level")));

				int gradeIndex = cursor.getColumnIndex("grade");
				if (gradeIndex != -1) {
					department.grade = cursor.getString(gradeIndex);
				}

				departments.add(department);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
		return departments;
	}

	/**
	 * 查找指定部门下的所有子部门(id)，不包含子部门的子部门
	 * @param deptId 当前部门 id
	 */
	@Override
	public List<String> obtainSubDepartmentIds(String deptId) {
		this.checkBeforeExecute();
		Cursor cursor = null;
		List<String> deptIds = null;
		try {
			cursor = mSQLiteDatabase.rawQuery("select deptId from DepartmentTable where fatherId = ?", new String[]{deptId});
			deptIds = new ArrayList<>();
			while (cursor.moveToNext()) {
				deptIds.add(cursor.getString(0));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
		return deptIds;
	}

	/**
	 * 根据条件查询联系人信息，按拼音排序
	 * @param deptIds 部门 id，可以多个
	 * @param position 岗位名称，可以没有
	 */
	@Override
	public List<AddressBook> obtainStaff(List<String> deptIds, String position) {
		this.checkBeforeExecute();

		boolean isDeptGradeExist = isColumnExist(mSQLiteDatabase, "AddressBookTable", "deptGrade");
		boolean isSortNoExist = isColumnExist(mSQLiteDatabase, "AddressBookTable", "sortNo");

		StringBuilder querySql = new StringBuilder();
		querySql.append("select userId, name, imageHref, deptId, position, ifnull(pinyin, '#') as pinyin, imid, department ");

		if (isDeptGradeExist) {
			querySql.append(", deptGrade ");
		}

		if (isSortNoExist) {
			querySql.append(", sortNo ");
		}

		querySql.append("from AddressBookTable ");
		querySql.append("where deptId in(");
		final int size = deptIds.size() - 1;
		for (int i = 0; i < size; i++) {
			querySql.append("'").append(deptIds.get(i)).append("',");
		}
		querySql.append("'").append(deptIds.get(size)).append("') ");
//        querySql.append("and isPartTime = 0 ");

		if (!TextUtils.isEmpty(position)) {
			querySql.append("and position = '").append(position).append("' ");
		}

		querySql.append("order by lower(substr(pinyin, 1, 1)), substr(name, 1, 1) ");
		if (isDeptGradeExist) {
			querySql.append(", deptGrade ");
		}

		if (isSortNoExist) {
			querySql.append(", sortNo ");
		}

		String sql = querySql.toString();
		FELog.i("obtainStaff : " + sql);

		Cursor cursor = null;
		List<AddressBook> addressBooks = null;
		try {
			cursor = mSQLiteDatabase.rawQuery(sql, null);
			addressBooks = new ArrayList<>();
			while (cursor.moveToNext()) {
				AddressBook addressBook = AddressBook.build(cursor);
				if (addressBooks.contains(addressBook)) {
					continue;
				}
				addressBooks.add(addressBook);
			}
		} catch (Exception exp) {
			exp.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
		return addressBooks;
	}

	/**
	 * 根据条件查询通讯录，按人员级别（deptGrade, sortNo）排序
	 */
	@Override
	public List<AddressBook> obtainStaffBySortNo(List<String> deptIds) {
		this.checkBeforeExecute();

		boolean isDeptGradeExist = isColumnExist(mSQLiteDatabase, "AddressBookTable", "deptGrade");
		boolean isSortNoExist = isColumnExist(mSQLiteDatabase, "AddressBookTable", "sortNo");

		StringBuilder querySql = new StringBuilder();
		querySql.append("select userId, name, imageHref, deptId, position, ifnull(pinyin, '#') as pinyin, imid, department ");

		if (isDeptGradeExist) {
			querySql.append(", deptGrade ");
		}

		if (isSortNoExist) {
			querySql.append(", sortNo ");
		}

		querySql.append("from AddressBookTable ");
		querySql.append("where deptId in(");
		final int size = deptIds.size() - 1;
		for (int i = 0; i < size; i++) {
			querySql.append("'").append(deptIds.get(i)).append("',");
		}
		querySql.append("'").append(deptIds.get(size)).append("') ");

		if (isDeptGradeExist) {
			querySql.append("order by deptGrade ");

			if (isSortNoExist) {
				querySql.append(", sortNo ");
			}
		}

		String sql = querySql.toString();
		FELog.i("obtainStaffBySortNo : " + sql);

		Cursor cursor = null;
		List<AddressBook> addressBooks = null;
		try {
			cursor = mSQLiteDatabase.rawQuery(sql, null);
			addressBooks = new ArrayList<>();
			while (cursor.moveToNext()) {
				AddressBook addressBook = AddressBook.build(cursor);
				if (addressBooks.contains(addressBook)) continue;
				addressBooks.add(addressBook);
			}
		} catch (Exception exp) {
			exp.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
		return addressBooks;
	}

	/**
	 * 联系人查找页面，根据用户数据的用户名，查找匹配的总数。
	 * @param nameLike 模糊查询的名字
	 */
	@Override
	public int obtainContactCountByNameLike(String nameLike) {
		this.checkBeforeExecute();
		Cursor cursor = null;
		int totalCount = 0;
		try {
			String sql = "select count(*) from AddressBookTable "
					+ "where (name like '%" + nameLike + "%' "
					+ "or pinyin like '" + nameLike + "%') "
					+ "and deptId is not null ";
			FELog.i("obtainContactCountByNameLike : " + sql);
			cursor = mSQLiteDatabase.rawQuery(sql, null);
			if (cursor.moveToNext()) {
				totalCount = cursor.getInt(0);
			}
		} catch (Exception exp) {
			exp.printStackTrace();
		} finally {
			closeCursor(cursor);
		}

		return totalCount;
	}

	/**
	 * 模糊搜索联系人信息
	 * @param nameLike 联系人名字
	 * @param offset 偏移量
	 */
	@Override
	public List<AddressBook> obtainContactByNameLike(String nameLike, int offset) {
		this.checkBeforeExecute();
		Cursor cursor = null;
		List<AddressBook> contacts = null;
		try {

			boolean isDeptGradeExist = isColumnExist(mSQLiteDatabase, "AddressBookTable", "deptGrade");
			boolean isSortNoExist = isColumnExist(mSQLiteDatabase, "AddressBookTable", "sortNo");

			StringBuilder querySql = new StringBuilder();
			querySql.append("select a.userId as userId, ")
					.append("a.name as userName, ")
					.append("a.imageHref as imageHref, ")
					.append("a.position as position, ")
					.append("a.deptId as deptId, ")
					.append("ifnull(a.pinyin, '#') as pinyin, ")
					.append("d.name as deptName, ")
					.append("a.imid as imid ");

			if (isDeptGradeExist) {
				querySql.append(", a.deptGrade as deptGrade ");
			}

			if (isSortNoExist) {
				querySql.append(", a.sortNo as sortNo ");
			}

			querySql.append("from AddressBookTable as a ")
					.append("left join DepartmentTable as d ")
					.append("where (userName like '%").append(nameLike).append("%' ")
					.append("or pinyin like '").append(nameLike).append("%') ")
					.append("and a.deptId is not null ")
					.append("and a.deptId = d.deptId ")
					.append("and isPartTime = 0 ")
					.append("group by a.userId ")
					.append("order by lower(substr(a.pinyin, 1, 1)), substr(a.name, 1, 1) ");

			if (isDeptGradeExist) {
				querySql.append(", a.deptGrade ");
			}

			if (isSortNoExist) {
				querySql.append(", a.sortNo ");
			}

			querySql.append("limit ").append(AddressBookRepository.PAGE_MAX_COUNT).append(" ")
					.append("offset ").append(offset);

			String sql = querySql.toString();
			FELog.i("ontainContactByNameLike = " + sql);
			cursor = mSQLiteDatabase.rawQuery(sql, null);
			contacts = new ArrayList<>();
			while (cursor.moveToNext()) {
				contacts.add(AddressBook.buildWithDepartment(cursor));
			}
		} catch (Exception exp) {
			exp.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
		return contacts;
	}

	/**
	 * 获取指定部门下的所有岗位
	 * @param deptIds 指定部门 ids
	 */
	@Override
	public List<Position> obtainPositionsInDepartment(List<String> deptIds) {
		this.checkBeforeExecute();

		boolean isSortNoExist = isColumnExist(mSQLiteDatabase, "PositionTable", "sortNo");

		StringBuilder querySql = new StringBuilder();
		querySql.append("select p.posId, p.position from PositionTable as p ");
		querySql.append("left join (");
		querySql.append("select posId from UsersTable ");
		querySql.append("where deptId in (");
		final int size = deptIds.size() - 1;
		for (int i = 0; i < size; i++) {
			querySql.append("'").append(deptIds.get(i)).append("',");
		}
		querySql.append("'").append(deptIds.get(size)).append("')) ");
		querySql.append("as t ");
		querySql.append("where p.posId = t.posId ");
		querySql.append("group by p.position ");
		if (isSortNoExist) {
			querySql.append(", order by p.sortNo ");
		}

		String sql = querySql.toString();
		FELog.i("obtainPositionInDepartment : " + sql);

		Cursor cursor = null;
		List<Position> positions = null;
		try {
			cursor = mSQLiteDatabase.rawQuery(sql, null);
			positions = new ArrayList<>(cursor.getCount());
			while (cursor.moveToNext()) {
				positions.add(new Position(cursor.getString(cursor.getColumnIndex("posId")),
						cursor.getString(cursor.getColumnIndex("position"))));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
		return positions;
	}

	/**
	 * 查询指定用户的岗位信息
	 * @param userId 指定用户 id
	 */
	@Override
	public Position obtainPositionWhichUserIs(String userId) {
		this.checkBeforeExecute();
		Position position = null;
		Cursor cursor = null;
		try {
			cursor = mSQLiteDatabase.rawQuery("select u.posId, p.position from UsersTable as u " +
					"left join PositionTable as p " +
					"where u.userId = ? and u.isPartTime = 0 and u.posId = p.posId", new String[]{userId});
			if (cursor.moveToNext()) {
				position = new Position(cursor.getString(cursor.getColumnIndex("posId")),
						cursor.getString(cursor.getColumnIndex("position")));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
		return position;
	}

	@Override
	public List<AddressBook> obtainUserByIds(List<String> userIds) {
		this.checkBeforeExecute();
		if (CommonUtil.isEmptyList(userIds)) {
			return null;
		}

		Cursor cursor = null;
		List<AddressBook> addressBooks = null;

		boolean isDeptGradeExist = isColumnExist(mSQLiteDatabase, "AddressBookTable", "deptGrade");
		boolean isSortNoExist = isColumnExist(mSQLiteDatabase, "AddressBookTable", "sortNo");

		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("select userId, name, imageHref, deptId, position, ifnull(pinyin, '#') as pinyin, imid, department ");

		if (isDeptGradeExist) {
			sqlBuilder.append(", deptGrade ");
		}

		if (isSortNoExist) {
			sqlBuilder.append(", sortNo ");
		}

		sqlBuilder.append("from AddressBookTable ");
		sqlBuilder.append("where userId in (");
		int len = userIds.size() - 1;
		for (int i = 0; i < len; i++) {
			sqlBuilder.append(userIds.get(i)).append(",");
		}
		sqlBuilder.append(userIds.get(len)).append(") ");
		sqlBuilder.append("and isPartTime = 0 ");

		if (isDeptGradeExist) {
			sqlBuilder.append("order by deptGrade ");
			if (isSortNoExist) {
				sqlBuilder.append(", sortNo ");
			}
		}

		String querySql = sqlBuilder.toString();
		FELog.i("obtainUserByIds = " + querySql);

		try {
			cursor = mSQLiteDatabase.rawQuery(querySql, null);
			addressBooks = new ArrayList<>();
			while (cursor.moveToNext()) {
				AddressBook addressBook = AddressBook.build(cursor);
				addressBooks.add(addressBook);
			}
		} catch (Exception exp) {
			exp.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
		return addressBooks;
	}

	@Override
	public Observable<List<AddressBook>> obtainUserByType(int type) {
		return Observable.unsafeCreate(f -> {
			String method = type == TYPE_COMMON_USERS
					? RemoteRequest.METHOD_GET_COMMON_PERSONS
					: RemoteRequest.METHOD_GET_TAG_PERSONS;
			RemoteRequest request = RemoteRequest.buildRequest(method);
			FEHttpClient.getInstance().post(request, new ResponseCallback<CommonTagResponse>() {
				@Override
				public void onCompleted(CommonTagResponse response) {
					if (response.getErrorCode().equals("0")) {
						List<String> userIds = response.result;
						List<AddressBook> dbUsers = obtainUserByIds(userIds);
						if (CommonUtil.isEmptyList(userIds) || CommonUtil.isEmptyList(dbUsers)) {
							f.onError(new RuntimeException("Query user by type failed."));
							f.onCompleted();
							return;
						}
						if (type == TYPE_COMMON_USERS) {
							for (int i = 0; i < userIds.size(); i++) {
								for (int j = i; j < dbUsers.size(); j++) {
									if (dbUsers.get(j).userId.equals(userIds.get(i))) {
										Collections.swap(dbUsers, i, j);
										break;
									}
								}
							}
						}
						f.onNext(dbUsers);
						f.onCompleted();
					}
					else {
						f.onError(new Throwable("get data error"));
						f.onCompleted();
					}
				}

				@Override
				public void onFailure(RepositoryException repositoryException) {
					f.onError(repositoryException != null && repositoryException.exception() != null
							? repositoryException.exception()
							: new RuntimeException("Query user by type failed."));
					f.onCompleted();
				}
			});
		});
	}

	@Override
	public Observable<List<AddressBook>> obtainSubordinates() {
		return Observable.unsafeCreate(f -> {
			LocationLocusRequest request = new LocationLocusRequest();
			request.setRequestType("1");
			FEHttpClient.getInstance().post(request, new ResponseCallback<LocationLocusResponse>() {
				@Override
				public void onCompleted(LocationLocusResponse response) {
					if (response.getErrorCode().equals("0")) {
						List<String> departmentList = response.departmentList;
						List<AddressBook> dbUser = obtainUserByIds(departmentList);
						f.onNext(dbUser);
						f.onCompleted();
					}
					else {
						f.onError(new Throwable("get data error"));
						f.onCompleted();
					}
				}

				@Override
				public void onFailure(RepositoryException repositoryException) {
					f.onError(repositoryException != null && repositoryException.exception() != null
							? repositoryException.exception()
							: new RuntimeException("Query user by type failed."));
					f.onCompleted();
				}
			});
		});
	}

	@Override
	public Observable<ContactInfo> obtainUserDetailInfo(String userId) {
		return obtainUserDetailInfo(userId, null);
	}

	@Override
	public Observable<ContactInfo> obtainUserDetailInfo(String userId, String depId) {
		this.checkBeforeExecute();
		return Observable.unsafeCreate(f -> {
			Cursor cursor = null;
			final ContactInfo contactInfo = new ContactInfo();
			boolean hasException = false;

			String[] value;
			StringBuilder sqlBuilder = new StringBuilder();
			sqlBuilder.append("select userId, name, imageHref, position, pinyin, deptId, department, imid ");
			sqlBuilder.append("from AddressBookTable where userId = ? ");
			if (TextUtils.isEmpty(depId)) {
				sqlBuilder.append("and isPartTime = 0");
				value = new String[]{userId};
			}
			else {
				sqlBuilder.append("and deptId = ?");
				value = new String[]{userId, depId};
			}
			String sql = sqlBuilder.toString();
			FELog.i("obtainUserDetailInfo : " + sql);
			try {
				cursor = mSQLiteDatabase.rawQuery(sql, value);
				if (cursor.moveToNext()) {
					contactInfo.userId = cursor.getString(0);
					contactInfo.name = cursor.getString(1);
					contactInfo.imageHref = cursor.getString(2);
					contactInfo.position = cursor.getString(3);
					contactInfo.pinyin = cursor.getString(4);
					contactInfo.deptId = cursor.getString(5);
					contactInfo.deptName = cursor.getString(6);
					contactInfo.imid = cursor.getString(7);
				}
			} catch (Exception exp) {
				hasException = true;
				exp.printStackTrace();
			} finally {
				closeCursor(cursor);
			}

			if (hasException) {
				f.onError(new RuntimeException("Query contact info from local database failed."));
				f.onCompleted();
				return;
			}

			RemoteRequest request = RemoteRequest.buildUserDetailInfoRequest(userId);
			FEHttpClient.getInstance().post(request, new ResponseCallback<ContactDetailResponse>() {
				@Override
				public void onCompleted(ContactDetailResponse response) {
					if (response == null || !TextUtils.equals(response.getErrorCode(), "0")) {
						f.onError(new RuntimeException("Query contact info from server failed."));
						f.onCompleted();
						return;
					}
					ContactInfo result = response.result;
					contactInfo.tel = result.tel;
					contactInfo.address = result.address;
					contactInfo.email = result.email;
					contactInfo.phone = result.phone;
					contactInfo.phone1 = result.phone1;
					contactInfo.phone2 = result.phone2;

					f.onNext(contactInfo);
					f.onCompleted();
				}

				@Override
				public void onFailure(RepositoryException repositoryException) {
					f.onError(repositoryException != null
							? repositoryException.exception()
							: new RuntimeException("Query user detail failed."));
					f.onCompleted();
				}
			});
		});
	}

	@Override
	public AddressBook obtainUserBaseInfo(String userId) {
		this.checkBeforeExecute();
		AddressBook addressBook = null;
		Cursor cursor = null;
		String sql = "select userId, name, imageHref, deptId, position, pinyin, "
				+ "imid, department from AddressBookTable "
				+ "where userId = ? and isPartTime = 0";
		try {
			cursor = mSQLiteDatabase.rawQuery(sql, new String[]{userId});
			if (cursor.moveToNext()) {
				addressBook = AddressBook.build(cursor);
			}
		} catch (Exception exp) {
			exp.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
		return addressBook;
	}

	@Override
	public void updateUserImageHref(String userId, String userImageHref) {
		this.checkBeforeExecute();
		String sql = "update UsersTable set imageHref = '" + userImageHref + "' where userId = '" + userId + "'";
		try {
			if (mSQLiteDatabase.isReadOnly()) {
				SQLiteDatabase sqLiteDatabase = SQLiteDatabase.openDatabase(mDataBasePath, null, SQLiteDatabase.OPEN_READWRITE);
				if (sqLiteDatabase != null) {
					mSQLiteDatabase.execSQL(sql);
				}
				if (sqLiteDatabase != null) sqLiteDatabase.close();
			}
			else {
				mSQLiteDatabase.execSQL(sql);
			}
		} catch (Exception exp) {
			FELog.e("Update user image href in database source...");
			exp.printStackTrace();
		}
	}

	private void checkBeforeExecute() {
		if (mSQLiteDatabase == null) {
			FELog.i("The SQLiteDatabase object is null, try to init it.");
			File addressBookDatabase = new File(mDataBasePath);
			if (!addressBookDatabase.exists()) {    // 直接异常往外怼 = = ...
				throw new RuntimeException("Can't find the address book database in sdcard, must login again now~");
			}
			mSQLiteDatabase = SQLiteDatabase.openDatabase(mDataBasePath, null, 0);
		}
	}

	private void closeCursor(Cursor cursor) {
		if (cursor != null) {
			cursor.close();
		}
	}

	public SQLiteDatabase getSQLiteDatabase() {
		this.checkBeforeExecute();
		return mSQLiteDatabase;
	}

	public void closeDb() {
		if (mSQLiteDatabase != null) mSQLiteDatabase.close();
	}
}
