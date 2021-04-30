package cn.flyrise.feep.addressbook.source;

import static cn.flyrise.feep.addressbook.utils.AddressBookUtils.isColumnExist;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import cn.flyrise.feep.addressbook.model.Position;
import cn.flyrise.feep.core.services.model.AddressBook;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ZYP
 * @since 2017-11-28 13:53
 */
public class AdvanceAddressBookDataSource {

	private SQLiteDatabase mSQLiteDatabase;

	public AdvanceAddressBookDataSource(SQLiteDatabase database) {
		this.mSQLiteDatabase = database;
	}

	/**
	 * 查询指定部门下的所有岗位
	 */
	public List<Position> obtainPositionsInDepartment(String deptId) {
		if (mSQLiteDatabase == null) {
			return null;
		}

		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("select u.posId, t.position from UsersTable as u ")
				.append("left join PositionTable as t ")
				.append("where u.deptId in (select deptId from DepartmentTable where grade like ")
				.append("(select grade from DepartmentTable where deptId = ?) || '%') ")
				.append("and u.posId = t.posId ")
				.append("group by t.position ");

		Cursor cursor = null;
		List<Position> positions = null;
		try {
			cursor = mSQLiteDatabase.rawQuery(sqlBuilder.toString(), new String[]{deptId});
			positions = new ArrayList<>(cursor.getCount());
			while (cursor.moveToNext()) {
				positions.add(new Position(cursor.getString(cursor.getColumnIndex("posId")),
						cursor.getString(cursor.getColumnIndex("position"))));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		return positions;
	}

	/**
	 * 根据条件查询员工，按拼音、 deptGrade、sortNo 进行排序
	 */
	public List<AddressBook> queryStaffByCondition(String deptId, String positionName) {
		return queryStaff(deptId, positionName, true);
	}


	public int queryDepartmentStaffCount(String deptId) {
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("select count(*) from AddressBookTable ")
				.append("where deptId in ")
				.append("(select deptId from DepartmentTable where grade like ")
				.append("(select grade from DepartmentTable where deptId = ?) || '%')");

		Cursor cursor = null;
		int totalCount = 0;
		try {
			cursor = mSQLiteDatabase.rawQuery(sqlBuilder.toString(), new String[]{deptId});
			if (cursor.moveToNext()) {
				totalCount = cursor.getInt(0);
			}
		} catch (Exception exp) {
			exp.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return totalCount;
	}

	public List<AddressBook> queryDepartmentStaff(String deptId, int offset) {
		if (mSQLiteDatabase == null) {
			return null;
		}

		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("select userId, name, imageHref, position ");
		sqlBuilder.append("from AddressBookTable ")
				.append("where deptId in ")
				.append("(select deptId from DepartmentTable where grade like ")
				.append("(select grade from DepartmentTable where deptId = ?) || '%') ");
		sqlBuilder.append("group by userId order by deptGrade,sortNo ");
		sqlBuilder.append("limit ").append(AddressBookRepository.PAGE_MAX_COUNT).append(" ")
				.append("offset ").append(offset);

		Cursor cursor = null;
		List<AddressBook> addressBooks = null;
		try {
			cursor = mSQLiteDatabase.rawQuery(sqlBuilder.toString(), new String[]{deptId});
			addressBooks = new ArrayList<>();
			while (cursor.moveToNext()) {
				addressBooks.add(AddressBook.cloneFromZygote(cursor));
			}
		} catch (Exception exp) {
			exp.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		return addressBooks;
	}

	/**
	 * 根据条件查询员工，按 deptGrade、sortNo 进行排序， 这里不能拼音排
	 */
	public List<AddressBook> queryStaffBySortNo(String deptId) {
		return queryStaff(deptId, null, false);
	}

	private List<AddressBook> queryStaff(String deptId, String positionName, boolean usePinYinSort) {
		if (mSQLiteDatabase == null) {
			return null;
		}

		boolean isDeptGradeExist = isColumnExist(mSQLiteDatabase, "AddressBookTable", "deptGrade");
		boolean isSortNoExist = isColumnExist(mSQLiteDatabase, "AddressBookTable", "sortNo");

		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("select userId, name, imageHref, deptId, position, ifnull(pinyin, '#') as pinyin, imid, department, min(isPartTime) ");

		if (isDeptGradeExist) sqlBuilder.append(", deptGrade ");
		if (isSortNoExist) sqlBuilder.append(", sortNo ");

		sqlBuilder.append("from AddressBookTable ")
				.append("where deptId in ")
				.append("(select deptId from DepartmentTable where grade like ")
				.append("(select grade from DepartmentTable where deptId = ?) || '%') ");
		if (!TextUtils.isEmpty(positionName)) sqlBuilder.append("and position = ? ");
		sqlBuilder.append("group by userId ");

		if (usePinYinSort) {
			sqlBuilder.append("order by lower(substr(pinyin, 1, 1)), substr(name, 1, 1) ");
			if (isDeptGradeExist) sqlBuilder.append(", deptGrade ");
			if (isSortNoExist) sqlBuilder.append(", sortNo ");
		}
		else {
			if (isDeptGradeExist) {
				sqlBuilder.append("order by deptGrade ");
				if (isSortNoExist) {
					sqlBuilder.append(", sortNo ");
				}
				sqlBuilder.append(" lower(substr(pinyin, 1, 1)), substr(name, 1, 1) ");
			}
		}

		Cursor cursor = null;
		List<AddressBook> addressBooks = null;
		try {
			String[] selectionArgs = (TextUtils.isEmpty(positionName))
					? new String[]{deptId}
					: new String[]{deptId, positionName};
			cursor = mSQLiteDatabase.rawQuery(sqlBuilder.toString(), selectionArgs);
			addressBooks = new ArrayList<>();
			while (cursor.moveToNext()) {
				addressBooks.add(AddressBook.build(cursor));
			}
		} catch (Exception exp) {
			exp.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		return addressBooks;
	}

	/**
	 * 查询指定部门下的，所有子部门 id
	 * @param deptId 制定部门 id
	 * @return 包括自己在内的所有子部门 id 集合
	 */
	public List<String> queryAllSubDepartmentByDeptId(String deptId) {
		if (mSQLiteDatabase == null) {
			return null;
		}

		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("select deptId from DepartmentTable ")
				.append("where grade like ")
				.append("(select grade from DepartmentTable where deptId = ?) || '%' ");

		Cursor cursor = null;
		List<String> deptIds = null;
		try {
			cursor = mSQLiteDatabase.rawQuery(sqlBuilder.toString(), new String[]{deptId});
			deptIds = new ArrayList<>();
			while (cursor.moveToNext()) {
				deptIds.add(cursor.getString(0));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return deptIds;
	}

	/**
	 * 使用模糊查询，查询出符合条件的用户信息，支持分页查找
	 * @param nameLike 可以是名字、拼音
	 * @param offset 偏移量
	 */
	public List<AddressBook> queryContactByNameLike(String nameLike, int offset) {
		if (mSQLiteDatabase == null) {
			return null;
		}

		boolean isDeptGradeExist = isColumnExist(mSQLiteDatabase, "AddressBookTable", "deptGrade");
		boolean isSortNoExist = isColumnExist(mSQLiteDatabase, "AddressBookTable", "sortNo");

		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("select a.userId as userId, ")
				.append("a.name as userName, ")
				.append("a.imageHref as imageHref, ")
				.append("a.position as position, ")
				.append("a.deptId as deptId, ")
				.append("ifnull(a.pinyin, '#') as pinyin as pinyin, ")
				.append("d.name as deptName, ")
				.append("fd.name as fDeptName, ")
				.append("a.imid as imid ");

		if (isDeptGradeExist) {
			sqlBuilder.append(", a.deptGrade as deptGrade ");
		}

		if (isSortNoExist) {
			sqlBuilder.append(", a.sortNo as sortNo ");
		}

		sqlBuilder.append("from AddressBookTable as a ")
				.append("left join DepartmentTable as d on a.deptId = d.deptId ")
				.append("left join DepartmentTable as fd on fd.deptId = d.fatherId ")
				.append("where isPartTime = 0 ")
				.append("and a.deptId is not null ")
				.append("and (userName like '%").append(nameLike).append("%' ")
				.append("or pinyin like '").append(nameLike).append("%') ")
				.append("gourp by userId ");

		if (isDeptGradeExist) {
			sqlBuilder.append("order by a.deptGrade ");
			if (isSortNoExist) {
				sqlBuilder.append(", a.sortNo ");
			}
		}

		sqlBuilder.append("limit ").append(AddressBookRepository.PAGE_MAX_COUNT).append(" ")
				.append("offset ").append(offset);

		List<AddressBook> addressBooks = null;
		Cursor cursor = null;

		try {
			addressBooks = new ArrayList<>();
			while (cursor.moveToNext()) {
				addressBooks.add(AddressBook.buildWithDepartment(cursor));
			}
		} catch (Exception exp) {
			exp.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return addressBooks;
	}
}