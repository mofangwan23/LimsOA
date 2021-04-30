package cn.flyrise.feep.dbmodul.utils;

import cn.flyrise.feep.dbmodul.table.ContactsVerionsTable;
import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.SQLite;

/**
 * Created by Administrator on 2016-11-17.
 */

public class ContactsVersionUtils {

	public static void insert(ContactsVerionsTable table) {
		if (table != null) {
			if (getCountContacts() > 0) {
				deleteContacts();
			}
			table.save();
		}
	}

	public static void deleteContacts() {
		Delete.table(ContactsVerionsTable.class);
	}

	public static long getCountContacts() {
		return SQLite.selectCountOf().from(ContactsVerionsTable.class).count();
	}

	/**
	 * 搜索联系人数据基本信息
	 */
	public static ContactsVerionsTable select() {
		ContactsVerionsTable table = SQLite.select().from(ContactsVerionsTable.class).querySingle();
		if (table == null) {
			return null;
		}
		return table;
	}
}
