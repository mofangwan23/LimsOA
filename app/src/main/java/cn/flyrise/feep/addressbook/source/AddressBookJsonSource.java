package cn.flyrise.feep.addressbook.source;

import android.database.Cursor;
import android.text.TextUtils;

import cn.flyrise.feep.R;
import cn.flyrise.feep.core.CoreZygote;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.structure.database.AndroidDatabase;

import java.util.ArrayList;
import java.util.List;

import cn.flyrise.feep.addressbook.model.ContactInfo;
import cn.flyrise.feep.addressbook.model.Department;
import cn.flyrise.feep.addressbook.model.Position;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.services.model.AddressBook;
import cn.flyrise.feep.dbmodul.database.FeepOADataBase;
import rx.Observable;

/**
 * @author ZYP
 * @since 2017-01-09 14:50
 */
public class AddressBookJsonSource implements IAddressBookDataSource {

	private AndroidDatabase mDataBase;

	public AddressBookJsonSource() {
		try {
			mDataBase = (AndroidDatabase) FlowManager.getDatabase(FeepOADataBase.NAME).getWritableDatabase();
		} catch (Exception ex) {
			mDataBase = null;
		}
	}

	@Override
	public List<AddressBook> obtainAllAddressBooks() {
		checkBeforeExecute();
		Cursor cursor = null;
		List<AddressBook> addressBooks = null;
		String sql = "select userID, name, imageHref, queenForeignKeyContainer_deptId, imid, departmentName "
				+ "from AddressBookTable group by userID";
		try {
			cursor = mDataBase.rawQuery(sql, null);
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
			cursor = mDataBase.rawQuery(sql, null);
			if (cursor.moveToNext()) {
				headCompany = new Department(cursor.getString(cursor.getColumnIndex("deptId")),
						cursor.getString(cursor.getColumnIndex("name")),
						cursor.getInt(cursor.getColumnIndex("level")),
						cursor.getString(cursor.getColumnIndex("fatherId")));
				headCompany.grade = cursor.getString(cursor.getColumnIndex("unitcode"));
			}
		} catch (Exception exp) {
			exp.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
		return headCompany;
	}

	@Override
	public List<Department> obtainAllSubCompany() {
		checkBeforeExecute();
		Cursor cursor = null;
		List<Department> companies = null;
		try {
			cursor = mDataBase.rawQuery("select deptId, name, unitcode from DepartmentTable where fatherId = " +
					"(select deptId from DepartmentTable where fatherId = ?) ", new String[]{"0"});
			companies = new ArrayList<>();
			while (cursor.moveToNext()) {
				Department department = new Department(cursor.getString(cursor.getColumnIndex("deptId")),
						cursor.getString(cursor.getColumnIndex("name")));
				department.grade = cursor.getString(cursor.getColumnIndex("unitcode"));
				companies.add(department);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
		return companies;
	}

	@Override
	public List<Department> obtainPartTimeDepartment(String userId) {
		this.checkBeforeExecute();
		Cursor cursor = null;
		List<Department> departments = null;
		try {
			cursor = mDataBase.rawQuery("select * from DepartmentTable where " +
							"deptId in (select queenForeignKeyContainer_deptId from AddressBookTable where userId = ?) " +
							"and name != (select departmentName from AddressBookTable where userId = ? group by userId) "
					, new String[]{userId, userId});
			departments = new ArrayList<>();
			while (cursor.moveToNext()) {
				Department department = new Department(cursor.getString(cursor.getColumnIndex("deptId")),
						cursor.getString(cursor.getColumnIndex("name")),
						cursor.getInt(cursor.getColumnIndex("level")),
						cursor.getString(cursor.getColumnIndex("fatherId")));
				department.grade = cursor.getString(cursor.getColumnIndex("unitcode"));
				departments.add(department);
			}
		} catch (Exception exp) {
			exp.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
		return departments;
	}

	@Override
	public List<String> obtainDepartmentIdsWhereUserIn(String userId) {
		checkBeforeExecute();
		Cursor cursor = null;
		List<String> ids = null;
		try {
			StringBuilder sqlBuilder = new StringBuilder();
			sqlBuilder.append("select deptId from DepartmentTable ")
					.append("where deptId in (select queenForeignKeyContainer_deptId from AddressBookTable where userID = ?) ")
					.append("group by deptId");
			cursor = mDataBase.rawQuery(sqlBuilder.toString(), new String[]{userId});
			ids = new ArrayList<>();
			while (cursor.moveToNext()) {
				ids.add(cursor.getString(0));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
		return ids;
	}

	@Override
	public Department obtainDepartmentByDeptId(String deptId) {
		this.checkBeforeExecute();
		if (TextUtils.isEmpty(deptId) || TextUtils.equals(deptId, "0")) {
			return null;
		}
		Cursor cursor = null;
		Department department = null;
		try {
			cursor = mDataBase.rawQuery("select * from DepartmentTable where deptId = ?", new String[]{deptId});
			if (cursor.moveToNext()) {
				department = new Department(cursor.getString(cursor.getColumnIndex("deptId")),
						cursor.getString(cursor.getColumnIndex("name")),
						cursor.getInt(cursor.getColumnIndex("level")),
						cursor.getString(cursor.getColumnIndex("fatherId")));
				department.grade = cursor.getString(cursor.getColumnIndex("unitcode"));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
		return department;
	}

	@Override
	public Department obtainDepartmentWhereUserIn(String userId) {
		this.checkBeforeExecute();
		Cursor cursor = null;
		Department department = null;
		try {
			cursor = mDataBase.rawQuery("select * from ContactsDeptTable", null);
			if (cursor.moveToNext()) {
				department = new Department(cursor.getString(cursor.getColumnIndex("deptId")),
						cursor.getString(cursor.getColumnIndex("name")),
						cursor.getInt(cursor.getColumnIndex("level")),
						cursor.getString(cursor.getColumnIndex("fatherId")));
				department.grade = cursor.getString(cursor.getColumnIndex("unitcode"));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
		return department;
	}

	@Override
	public List<Department> obtainSubDepartments(String parentDepartmentId) {
		this.checkBeforeExecute();
		Cursor cursor = null;
		List<Department> subDepartments = null;
		try {
			cursor = mDataBase.rawQuery("select * from DepartmentTable where fatherId = ? ",
					new String[]{parentDepartmentId});
			subDepartments = new ArrayList<>();
			while (cursor.moveToNext()) {
				Department department = new Department(cursor.getString(cursor.getColumnIndex("deptId")),
						cursor.getString(cursor.getColumnIndex("name")),
						cursor.getInt(cursor.getColumnIndex("level")));
				department.grade = cursor.getString(cursor.getColumnIndex("unitcode"));
				subDepartments.add(department);

			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
		return subDepartments;
	}

	@Override
	public List<String> obtainSubDepartmentIds(String deptId) {
		this.checkBeforeExecute();
		Cursor cursor = null;
		List<String> deptIds = null;
		try {
			cursor = mDataBase.rawQuery("select deptId from DepartmentTable where fatherId = ?", new String[]{deptId});
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

	@Override
	public List<AddressBook> obtainStaff(List<String> deptIds, String position) {
		this.checkBeforeExecute();
		StringBuilder querySql = new StringBuilder();
		querySql.append("select userID, name, imageHref, queenForeignKeyContainer_deptId, ")
				.append("ifnull(pinyin, '#') as pinyin, ")
				.append("position, imid, departmentName ")
				.append("from AddressBookTable ")
				.append("where queenForeignKeyContainer_deptId in(");
		final int size = deptIds.size() - 1;
		for (int i = 0; i < size; i++) {
			querySql.append("'").append(deptIds.get(i)).append("',");
		}
		querySql.append("'").append(deptIds.get(size)).append("') ");

		if (!TextUtils.isEmpty(position)) {
			querySql.append("and position = '").append(position).append("' ");
		}
		querySql.append("order by lower(substr(pinyin, 1, 1)), substr(name, 1, 1) ");

		String sql = querySql.toString();
		FELog.i("obtainStaff : " + sql);

		Cursor cursor = null;
		List<AddressBook> addressBooks = null;
		try {
			cursor = mDataBase.rawQuery(sql, null);
			addressBooks = new ArrayList<>();
			while (cursor.moveToNext()) {
				AddressBook addressBook = new AddressBook();
				addressBook.userId = cursor.getString(0);
				addressBook.name = cursor.getString(1);
				addressBook.imageHref = cursor.getString(2);
				addressBook.deptId = cursor.getString(3);
				addressBook.pinyin = cursor.getString(4);
				addressBook.position = cursor.getString(5);
				addressBook.imid = cursor.getString(6);
				addressBook.deptName = cursor.getString(7);
				if (TextUtils.isEmpty(addressBook.position)) continue;  // 没岗位的牲口，不要显示出来丢人现眼。
				if (addressBooks.contains(addressBook)) continue;        // 代码去重
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
	public List<AddressBook> obtainStaffBySortNo(List<String> deptIds) {
		this.checkBeforeExecute();
		StringBuilder querySql = new StringBuilder();
		querySql.append(
				"select userID, name, imageHref, queenForeignKeyContainer_deptId, ifnull(pinyin, '#') as pinyin, position, imid, departmentName ")
				.append("from AddressBookTable ")
				.append("where queenForeignKeyContainer_deptId in(");
		final int size = deptIds.size() - 1;
		for (int i = 0; i < size; i++) {
			querySql.append("'").append(deptIds.get(i)).append("',");
		}
		querySql.append("'").append(deptIds.get(size)).append("') ");

		String sql = querySql.toString();
		FELog.i("obtainStaff : " + sql);

		Cursor cursor = null;
		List<AddressBook> addressBooks = null;
		try {
			cursor = mDataBase.rawQuery(sql, null);
			addressBooks = new ArrayList<>();
			while (cursor.moveToNext()) {
				AddressBook addressBook = new AddressBook();
				addressBook.userId = cursor.getString(0);
				addressBook.name = cursor.getString(1);
				addressBook.imageHref = cursor.getString(2);
				addressBook.deptId = cursor.getString(3);
				addressBook.pinyin = cursor.getString(4);
				addressBook.position = cursor.getString(5);
				addressBook.imid = cursor.getString(6);
				addressBook.deptName = cursor.getString(7);
				if (TextUtils.isEmpty(addressBook.position)) continue;  // 没岗位的牲口，不要显示出来丢人现眼。
				if (addressBooks.contains(addressBook)) continue;        // 代码去重
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
	public int obtainContactCountByNameLike(String nameLike) {
		this.checkBeforeExecute();
		Cursor cursor = null;
		int totalCount = 0;
		try {
			StringBuilder querySql = new StringBuilder();
			querySql.append("select count(*) from (")
					.append("select name, userId from AddressBookTable ")
					.append("where name like ").append("'%").append(nameLike).append("%' ")
					.append("and queenForeignKeyContainer_deptId is not null ")
					.append("group by name)");

			String sql = querySql.toString();
			FELog.i("obtainContactCountByNameLike : " + sql);
			cursor = mDataBase.rawQuery(sql, null);
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

	@Override
	public List<AddressBook> obtainContactByNameLike(String nameLike, int offset) {
		this.checkBeforeExecute();
		Cursor cursor = null;
		List<AddressBook> contacts = null;
		try {
			StringBuilder querySql = new StringBuilder();
			querySql.append("select userID, name, imageHref, position, ifnull(pinyin, '#') as pinyin, ")
					.append("queenForeignKeyContainer_deptId, departmentName, imid, departmentName ")
					.append("from AddressBookTable ")
					.append("where name like '%").append(nameLike).append("%' and queenForeignKeyContainer_deptId is not null ")
					.append("group by userId ")
					.append("order by lower(substr(pinyin, 1, 1)), substr(name, 1, 1) ")
					.append("limit ").append(AddressBookRepository.PAGE_MAX_COUNT).append(" ")
					.append("offset ").append(offset);

			String sql = querySql.toString();
			FELog.i("ontainContactByNameLike = " + sql);
			cursor = mDataBase.rawQuery(sql, null);
			contacts = new ArrayList<>();
			while (cursor.moveToNext()) {
				AddressBook addressBook = new AddressBook();
				addressBook.userId = cursor.getString(0);
				addressBook.name = cursor.getString(1);
				addressBook.imageHref = cursor.getString(2);
				addressBook.position = cursor.getString(3);
				addressBook.pinyin = cursor.getString(4);
				addressBook.deptId = cursor.getString(5);
				addressBook.deptName = cursor.getString(6);
				addressBook.imid = cursor.getString(7);
				addressBook.deptName = cursor.getString(8);

				if (TextUtils.isEmpty(addressBook.position)) continue;  // 没岗位的牲口，不要显示出来丢人现眼。
				contacts.add(addressBook);
			}
		} catch (Exception exp) {
			exp.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
		return contacts;
	}

	@Override
	public List<Position> obtainPositionsInDepartment(List<String> deptIds) {
		this.checkBeforeExecute();
		StringBuilder querySql = new StringBuilder();
		querySql.append("select position from AddressBookTable ");
		querySql.append("where queenForeignKeyContainer_deptId in (");
		final int size = deptIds.size() - 1;
		for (int i = 0; i < size; i++) {
			querySql.append("'").append(deptIds.get(i)).append("',");
		}
		querySql.append("'").append(deptIds.get(size)).append("') ");
		querySql.append("group by position ");

		String sql = querySql.toString();
		FELog.i("obtainPositionInDepartment : " + sql);

		Cursor cursor = null;
		List<Position> positions = null;
		try {
			cursor = mDataBase.rawQuery(sql, null);
			positions = new ArrayList<>(cursor.getCount());
			while (cursor.moveToNext()) {   // 这个版本是没有 岗位 id 的。
				String position = cursor.getString(cursor.getColumnIndex("position"));
				if (TextUtils.isEmpty(position)) continue;
				positions.add(new Position("1024", position));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
		return positions;
	}

	@Override
	public Position obtainPositionWhichUserIs(String userId) {
		this.checkBeforeExecute();
		if (TextUtils.isEmpty(userId)) {
			return null;
		}
		Cursor cursor = null;
		Position position = null;
		StringBuilder querySql = new StringBuilder();
		querySql.append("select position from AddressBookTable ");
		querySql.append("where userID = ? ");
		querySql.append("and queenForeignKeyContainer_deptId is not null");
		String sql = querySql.toString();
		FELog.i("obtainPositionWhichUserIs : " + sql);
		try {
			cursor = mDataBase.rawQuery(sql, new String[]{userId});
			if (cursor.moveToNext()) {
				String p = cursor.getString(cursor.getColumnIndex("position"));
				if (TextUtils.isEmpty(p)) {
					p = CoreZygote.getContext().getString(R.string.addressbook_no_post);
				}
				position = new Position("1024", p);
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

		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder
				.append("select userID, name, imageHref, queenForeignKeyContainer_deptId, ")
				.append("ifnull(pinyin, '#') as pinyin, position, imid, departmentName ")
				.append("from AddressBookTable where userID in (");

		int len = userIds.size() - 1;
		for (int i = 0; i < len; i++) {
			sqlBuilder.append(userIds.get(i)).append(",");
		}
		sqlBuilder.append(userIds.get(len)).append(") ");
		sqlBuilder.append("order lower(substr(pinyin, 1, 1)), substr(name, 1, 1) ");

		String querySql = sqlBuilder.toString();
		FELog.i("obtainUserByIds = " + querySql);

		try {
			cursor = mDataBase.rawQuery(querySql, null);
			addressBooks = new ArrayList<>();
			while (cursor.moveToNext()) {
				AddressBook addressBook = new AddressBook();
				addressBook.userId = cursor.getString(0);
				addressBook.name = cursor.getString(1);
				addressBook.imageHref = cursor.getString(2);
				addressBook.deptId = cursor.getString(3);
				addressBook.pinyin = cursor.getString(4);
				addressBook.position = cursor.getString(5);
				addressBook.imid = cursor.getString(6);
				addressBook.deptName = cursor.getString(7);
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
		this.checkBeforeExecute();
		return Observable.create(f -> {
			Cursor cursor = null;
			List<AddressBook> addressBooks = null;
			StringBuilder sqlBuilder = new StringBuilder(
					"select userID, name, imageHref, position, pinyin, departmentName ");
			sqlBuilder.append("from AddressBookTable ");
			sqlBuilder.append("where queenForeignKeyContactsTable_type = ? group by userID ");

			String querySql = sqlBuilder.toString();
			FELog.i("obtainCommonUser : " + querySql);
			String params = type == TYPE_COMMON_USERS ? "common" : "tag";
			try {
				cursor = mDataBase.rawQuery(querySql, new String[]{params});
				addressBooks = new ArrayList<>();
				while (cursor.moveToNext()) {
					AddressBook addressBook = new AddressBook();
					addressBook.userId = cursor.getString(0);
					addressBook.name = cursor.getString(1);
					addressBook.imageHref = cursor.getString(2);
					addressBook.position = cursor.getString(3);
					addressBook.pinyin = cursor.getString(4);
					addressBook.deptName = cursor.getString(5);
					addressBooks.add(addressBook);
				}
			} catch (Exception exp) {
				exp.printStackTrace();
			} finally {
				closeCursor(cursor);
			}

			f.onNext(addressBooks);
			f.onCompleted();
		});
	}

	@Override
	public Observable<List<AddressBook>> obtainSubordinates() {
		return null;
	}

	@Override
	public Observable<ContactInfo> obtainUserDetailInfo(String userId) {
		return obtainUserDetailInfo(userId, null);
	}

	@Override
	public Observable<ContactInfo> obtainUserDetailInfo(String userId, String depId) {
		return Observable.create(f -> {
			Cursor cursor = null;
			ContactInfo contactInfo = null;

			StringBuilder builder = new StringBuilder();
			String[] value = null;
			if (TextUtils.isEmpty(depId)) {
				builder.append("select userID, name, queenForeignKeyContainer_deptId, departmentName, ");
				builder.append("imageHref, position, pinyin, imid, address, email, tel, phone, phone1, phone2 ");
				builder.append("from AddressBookTable where userID = ? ");
				builder.append("group by userID");
				value = new String[]{userId};
			}
			else {
				builder.append("select a.userID, a.name, b.deptId, b.name, ")
						.append("a.imageHref, a.position, a.pinyin, ")
						.append("a.imid, a.address, a.email, a.tel, ")
						.append("a.phone, a.phone1, a.phone2 ")
						.append("from AddressBookTable as a, DepartmentTable as b ")
						.append("where a.userID = ? and a.queenForeignKeyContainer_deptId = ? ")
						.append("and b.deptId = a.queenForeignKeyContainer_deptId");
				value = new String[]{userId, depId};
			}
			String sql = builder.toString();
			FELog.i("obtainUserDetailInfo : " + sql);
			try {
				cursor = mDataBase.rawQuery(sql, value);
				contactInfo = new ContactInfo();
				if (cursor.moveToNext()) {
					contactInfo.userId = cursor.getString(0);
					contactInfo.name = cursor.getString(1);
					contactInfo.deptId = cursor.getString(2);
					contactInfo.deptName = cursor.getString(3);
					contactInfo.imageHref = cursor.getString(4);
					contactInfo.position = cursor.getString(5);
					contactInfo.pinyin = cursor.getString(6);
					contactInfo.imid = cursor.getString(7);
					contactInfo.address = cursor.getString(8);
					contactInfo.email = cursor.getString(9);
					contactInfo.tel = cursor.getString(10);
					contactInfo.phone = cursor.getString(11);
					contactInfo.phone1 = cursor.getString(12);
					contactInfo.phone2 = cursor.getString(13);
				}
			} catch (Exception exp) {
				exp.printStackTrace();
			} finally {
				closeCursor(cursor);
			}
			f.onNext(contactInfo);
			f.onCompleted();
		});
	}

	@Override
	public AddressBook obtainUserBaseInfo(String userId) {
		this.checkBeforeExecute();
		AddressBook addressBook = null;
		Cursor cursor = null;
		String sql = "select userID, name, imageHref, queenForeignKeyContainer_deptId, pinyin, "
				+ "position, imid, departmentName from "
				+ "AddressBookTable where userID = ? group by userID ";
		try {
			cursor = mDataBase.rawQuery(sql, new String[]{userId});
			if (cursor.moveToNext()) {
				addressBook = new AddressBook();
				addressBook.userId = cursor.getString(0);
				addressBook.name = cursor.getString(1);
				addressBook.imageHref = cursor.getString(2);
				addressBook.deptId = cursor.getString(3);
				addressBook.pinyin = cursor.getString(4);
				addressBook.position = cursor.getString(5);
				addressBook.imid = cursor.getString(6);
				addressBook.deptName = cursor.getString(7);
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
		String sql = "update AddressBookTable set imageHref = '" + userImageHref + "' where userID = '" + userId + "'";
		try {
			mDataBase.execSQL(sql);
		} catch (Exception exp) {
			FELog.e("Update user image href in json source...");
			exp.printStackTrace();
		}
	}

	private void checkBeforeExecute() {
		if (mDataBase == null) {
			FELog.i("The AndroidDataBase object is null, try to init again.");
			mDataBase = (AndroidDatabase) FlowManager.getDatabase(FeepOADataBase.NAME).getWritableDatabase();
		}
	}

	private void closeCursor(Cursor cursor) {
		if (cursor != null) {
			cursor.close();
		}
	}
}
