package cn.flyrise.feep.dbmodul.utils;

import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.dbmodul.database.FeepOADataBase;
import cn.flyrise.feep.dbmodul.table.AddressBookTable;
import cn.flyrise.feep.dbmodul.table.AddressBookTable_Table;
import cn.flyrise.feep.dbmodul.table.ContactsDeptTable;
import cn.flyrise.feep.dbmodul.table.ContactsPersonnelTable;
import cn.flyrise.feep.dbmodul.table.DepartmentTable;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;
import com.raizlabs.android.dbflow.structure.database.transaction.ITransaction;
import java.util.List;

/**
 * Created by Administrator on 2016-11-11.
 */

public class ContactsTableUtils {

	private static saveContactsListener listener;

	public static void insertDept(List<DepartmentTable> departmentTables) {
		if (departmentTables == null || departmentTables.size() <= 0) {
			return;
		}
		if (getCountDept() > 0 || getCountAddress() > 0) {
			deleteDept();
		}

		FlowManager.getDatabase(FeepOADataBase.NAME).beginTransactionAsync(new ITransaction() {
			@Override public void execute(DatabaseWrapper databaseWrapper) {
				for (DepartmentTable department : departmentTables) {
					department.save();
				}
			}
		}).execute();
	}

	public static void insertContactsPerson(List<ContactsPersonnelTable> tables) {
		if (tables == null || tables.size() <= 0) {
			return;
		}
		if (getCountContactsPerson() > 0) {
			deleteContactsPerson();
		}

		FlowManager.getDatabase(FeepOADataBase.NAME).beginTransactionAsync(wrapper -> {
			for (ContactsPersonnelTable persons : tables) {
				persons.save();
			}
			if (listener != null) {
				listener.onSuccess();
			}
		}).execute();
	}

	public static void insertContactsDept(ContactsDeptTable table) {
		if (table != null) {
			if (getCountContactsDept() > 0) {
				deleteContactsDept();
			}
			table.save();
		}
	}

	private static void deleteDept() {
		Delete.table(DepartmentTable.class);
		Delete.table(AddressBookTable.class);
	}

	private static void deleteContactsPerson() {
		Delete.table(ContactsPersonnelTable.class);
	}

	private static void deleteContactsDept() {
		Delete.table(ContactsDeptTable.class);
	}

	public static long getCountDept() {
		return SQLite.selectCountOf().from(DepartmentTable.class).count();
	}

	public static long getCountContactsDept() {
		return SQLite.selectCountOf().from(ContactsDeptTable.class).count();
	}

	public static long getCountContactsPerson() {
		return SQLite.selectCountOf().from(ContactsPersonnelTable.class).count();
	}

	/**
	 * 全部人员数量（包括部分重复）
	 */
	public static long getCountAddress() {
		return SQLite.selectCountOf().from(AddressBookTable.class).count();
	}

	/**
	 * 去除重复后的人员数
	 */
	public static long getCountDistinctAddress() {
		List<AddressBookTable> tables = SQLite.select(AddressBookTable_Table.userID).distinct().from(AddressBookTable.class).queryList();
		return CommonUtil.isEmptyList(tables) ? 0 : tables.size();
	}

	public interface saveContactsListener {

		void onSuccess();
	}

	public static void setSaveContactsListener(saveContactsListener listeners) {
		listener = listeners;
	}

}
