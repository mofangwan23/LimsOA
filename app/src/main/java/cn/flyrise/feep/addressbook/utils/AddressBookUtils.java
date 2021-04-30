package cn.flyrise.feep.addressbook.utils;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * @author ZYP
 * @since 2017-11-28 15:00
 */
public class AddressBookUtils {

	public static boolean isColumnExist(SQLiteDatabase database, String tableName, String columnName) {
		boolean isColumnExist = false;
		if (database == null) {
			return isColumnExist;
		}

		Cursor cursor = null;
		try {
			String sql = "select * from sqlite_master where name = '"
					+ tableName + "' and sql like '%" + columnName + "%'";
			cursor = database.rawQuery(sql, null);
			isColumnExist = cursor != null && cursor.moveToNext();
		} catch (Exception exp) {
			isColumnExist = false;
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return isColumnExist;
	}

}
