package cn.flyrise.feep.addressbook.processor;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.text.TextUtils;
import cn.flyrise.feep.K;
import cn.flyrise.feep.addressbook.model.ExtractInfo;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.SpUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ZYP
 * @since 2017-02-09 11:16
 * 新机制 通讯录的更新
 */
public class SqlAddressBookProcessor extends AddressBookProcessor {

	@Override public void dispose(ExtractInfo extractInfo) {
		boolean updateResult = executeUpdateSQL(extractInfo);
		if (!updateResult) {                                         // 通讯录更新失败，然而能继续使用数据库
			FELog.w("execute update sql failed.");
			if (mDisposeListener != null) {
				mHandler.post(() -> mDisposeListener.onDisposeSuccess(ADDRESS_BOOK_INIT_SUCCESS, ADDRESS_BOOK_SOURCE_DB));
			}
			return;
		}

		// 修改通讯录文件名
		String oldDBName = SpUtil.get(K.preferences.address_book_version, "");  // 能执行到这里，说明 db 文件一定是存在
		String newDBName = extractInfo.name;

		if (newDBName.endsWith(".sql")) {
			newDBName = newDBName.replace("sql", "db");
		}
		boolean renameResult = renameDatabse(oldDBName, newDBName);
		if (renameResult) {                                                     // 重命名失败呢? 那就不管啊~
			SpUtil.put(K.preferences.address_book_version, newDBName);
		}

		if (mDisposeListener != null) {
			mHandler.post(() -> mDisposeListener.onDisposeSuccess(ADDRESS_BOOK_INIT_SUCCESS, ADDRESS_BOOK_SOURCE_DB));
		}
	}

	/**
	 * 执行 SQL 更新语句
	 */
	private boolean executeUpdateSQL(ExtractInfo extractInfo) {
		File sqlFile = new File(extractInfo.path);
		if (!sqlFile.exists()) {
			// sql 文件不存在
			FELog.e("The sql file was not exist.");
			return false;
		}

		// 首先找到 db 文件，还得保存一个 db name
		String dbName = SpUtil.get(K.preferences.address_book_version, "");
		if (TextUtils.isEmpty(dbName)) {
			sqlFile.delete();
			SpUtil.put(K.preferences.address_book_version, "");
			throw new RuntimeException("The database file was not found. may be delete by the sb user.");
		}

		List<String> sqlStatements = sqlStatements(sqlFile);
		if (CommonUtil.isEmptyList(sqlStatements)) {
			FELog.w("There has no sql to update addressbook.");
			return false;
		}

		boolean isUpdateSuccess = false;
		SQLiteDatabase database = null;
		try {
			String dbPath = CoreZygote.getPathServices().getAddressBookPath() + File.separator + dbName;
			database = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
			for (String sql : sqlStatements) {
				SQLiteStatement statement = database.compileStatement(sql);
				statement.executeUpdateDelete();
			}
			isUpdateSuccess = true;
			sqlFile.delete();
		} catch (Exception exp) {
			exp.printStackTrace();
			FELog.e("execute update sql failed. Message is : " + exp.getMessage());
			isUpdateSuccess = false;
		} finally {
			if (database != null) database.close();
		}
		return isUpdateSuccess;
	}


	/**
	 * 从 sql 文件中获取 sql 语句
	 */
	private List<String> sqlStatements(File sqlFile) {
		List<String> sqlLines = null;
		try {
			sqlLines = new ArrayList<>();
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(sqlFile)));
			String line = null;
			while ((line = reader.readLine()) != null) {
				sqlLines.add(line);
			}
			reader.close();
		} catch (Exception exp) {
			exp.printStackTrace();
			FELog.e("read sql statement from sql file failed. The sql files is = " + sqlFile.getAbsolutePath());
			sqlLines = null;
		}
		return sqlLines;
	}

	/**
	 * 执行完 sql 语句后，更新数据库文件名，sb.
	 */
	private boolean renameDatabse(String oldName, String newName) {
		boolean isRenameSuccess = false;
		try {
			String databasePath = CoreZygote.getPathServices().getAddressBookPath() + File.separator + oldName;
			File databaseFile = new File(databasePath);

			String newPath = CoreZygote.getPathServices().getAddressBookPath() + File.separator + newName;
			File newDatabaseFile = new File(newPath);
			isRenameSuccess = databaseFile.renameTo(newDatabaseFile);
		} catch (Exception exp) {
			exp.printStackTrace();
			isRenameSuccess = false;
		}
		return isRenameSuccess;
	}
}
