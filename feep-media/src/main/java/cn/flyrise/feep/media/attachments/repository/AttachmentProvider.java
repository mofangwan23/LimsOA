package cn.flyrise.feep.media.attachments.repository;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import java.io.File;

/**
 * @author ZYP
 * @since 2017-10-26 17:20
 * 使用 ContentProvider 实现与主模块数据库操作之间的解耦（还有这种操作?
 */
public class AttachmentProvider extends ContentProvider {

	/**
	 * 保存附件下载进度的表
	 */
	private static final String TABLE_DOWNLOAD_TASK = "DownloadInfoTable";

	/**
	 * 附件真实名字与 MD5 后名字的映射表
	 */
	private static final String TABLE_ATTACHMENT_NAME_MAP = "DownloadFileNameTable";

	private static final String AUTHORITY = "com.flyrise.study.media.attachment.provider";

	public static final int DOWNLOAD_TASK_CODE = 1;
	public static final int ATTACHMENT_NAME_MAP_CODE = 2;
	public static final Uri DOWNLOAD_TASK_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_DOWNLOAD_TASK);
	public static final Uri ATTACHMENT_NAME_MAP_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_ATTACHMENT_NAME_MAP);
	private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

	static {
		sUriMatcher.addURI(AUTHORITY, TABLE_DOWNLOAD_TASK, DOWNLOAD_TASK_CODE);
		sUriMatcher.addURI(AUTHORITY, TABLE_ATTACHMENT_NAME_MAP, ATTACHMENT_NAME_MAP_CODE);
	}

	private SQLiteDatabase mSQLiteDatabase;

	@Override public boolean onCreate() {
		try {
			File database = getContext().getDatabasePath("FeepDB.db");
			mSQLiteDatabase = SQLiteDatabase.openDatabase(database.getPath(), null, 0);
		} catch (Exception exp) {
			exp.printStackTrace();
		}
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		if(mSQLiteDatabase == null) {
			File database = getContext().getDatabasePath("FeepDB.db");
			mSQLiteDatabase = SQLiteDatabase.openDatabase(database.getPath(), null, 0);
		}

		String tableName = getTableName(uri);
		if (tableName == null) {
			throw new IllegalArgumentException("Unsupported URI : " + uri);
		}
		return mSQLiteDatabase.query(tableName, projection, selection, selectionArgs, null, null, sortOrder, null);
	}

	@Override public Uri insert(Uri uri, ContentValues values) {
		if(mSQLiteDatabase == null) {
			File database = getContext().getDatabasePath("FeepDB.db");
			mSQLiteDatabase = SQLiteDatabase.openDatabase(database.getPath(), null, 0);
		}
		String tableName = getTableName(uri);
		if (tableName == null) {
			throw new IllegalArgumentException("Unsupported URI : " + uri);
		}
		mSQLiteDatabase.insert(tableName, null, values);
		return uri;
	}

	@Override public int delete(Uri uri, String selection, String[] selectionArgs) {
		if(mSQLiteDatabase == null) {
			File database = getContext().getDatabasePath("FeepDB.db");
			mSQLiteDatabase = SQLiteDatabase.openDatabase(database.getPath(), null, 0);
		}
		String tableName = getTableName(uri);
		if (tableName == null) {
			throw new IllegalArgumentException("Unsupported URI : " + uri);
		}
		return mSQLiteDatabase.delete(tableName, selection, selectionArgs);
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		if(mSQLiteDatabase == null) {
			File database = getContext().getDatabasePath("FeepDB.db");
			mSQLiteDatabase = SQLiteDatabase.openDatabase(database.getPath(), null, 0);
		}
		String tableName = getTableName(uri);
		if (tableName == null) {
			throw new IllegalArgumentException("Unsupported URI : " + uri);
		}
		return mSQLiteDatabase.update(tableName, values, selection, selectionArgs);
	}

	@Override public String getType(Uri uri) {
		return null;
	}

	private String getTableName(Uri uri) {
		String tableName = null;
		switch (sUriMatcher.match(uri)) {
			case DOWNLOAD_TASK_CODE:
				tableName = TABLE_DOWNLOAD_TASK;
				break;
			case ATTACHMENT_NAME_MAP_CODE:
				tableName = TABLE_ATTACHMENT_NAME_MAP;
				break;
		}
		return tableName;
	}

}
