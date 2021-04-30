package cn.flyrise.android.shared.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Base64;

import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.android.library.utility.encryption.AESUtils;
import cn.flyrise.android.library.utility.encryption.Base64Utils;

/**
 * 初始化数据库操作类
 *
 * @author LuTH
 */
public class SQLiteHelper extends SQLiteOpenHelper {

    private static final int mVersion = 6;
    public static final String DATABASE_NAME = "feep";
    public static final String TABLE_DOWNLOAD_INFO = "downloadinfo";
    public static final String TABLE_ADDRESS_BOOK = "addressbook";

    private final StringBuilder mCrtUsrSQL;
    private final StringBuilder mCrtDwSQL;
    private final StringBuilder mCrtAddSQL;

    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, mVersion);

        mCrtUsrSQL = new StringBuilder("CREATE TABLE user (");
        mCrtUsrSQL.append("_id INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL,");
        mCrtUsrSQL.append("userID VARCHAR,");
        mCrtUsrSQL.append("loginname VARCHAR,");
        mCrtUsrSQL.append("username VARCHAR,");
        mCrtUsrSQL.append("password VARCHAR,");
        mCrtUsrSQL.append("isSavePassword BOOL DEFAULT 0,");
        mCrtUsrSQL.append("isAutoLogin BOOL DEFAULT 0,");
        mCrtUsrSQL.append("isHttps BOOL DEFAULT 0,");
        mCrtUsrSQL.append("serverAddress VARCHAR,");
        mCrtUsrSQL.append("serverPort VARCHAR,");
        mCrtUsrSQL.append("httpsPort VARCHAR,");
        mCrtUsrSQL.append("time DATETIME DEFAULT (CURRENT_TIMESTAMP),");
        mCrtUsrSQL.append("isVpn BOOL DEFAULT 0,");
        mCrtUsrSQL.append("vpnAddress VARCHAR,");
        mCrtUsrSQL.append("vpnPort VARCHAR,");
        mCrtUsrSQL.append("vpnUsername VARCHAR,");
        mCrtUsrSQL.append("vpnPassword VARCHAR");
        mCrtUsrSQL.append(")");

        mCrtDwSQL = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
        mCrtDwSQL.append(TABLE_DOWNLOAD_INFO).append(" ( ");
        mCrtDwSQL.append("id INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL,");
        mCrtDwSQL.append("userID VARCHAR,");
        mCrtDwSQL.append("taskID VARCHAR,");
        mCrtDwSQL.append("url VARCHAR,");
        mCrtDwSQL.append("filePath VARCHAR,");
        mCrtDwSQL.append("fileName VARCHAR,");
        mCrtDwSQL.append("fileSize VARCHAR,");
        mCrtDwSQL.append("downLoadSize VARCHAR )");

        mCrtAddSQL = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
        mCrtAddSQL.append(TABLE_ADDRESS_BOOK).append(" ( ");
        mCrtAddSQL.append("id INTEGER,");
        mCrtAddSQL.append("name VARCHAR,");
        mCrtAddSQL.append("departmentName VARCHAR,");
        mCrtAddSQL.append("imageHref VARCHAR,");
        mCrtAddSQL.append("commonGroup VARCHAR,");
        mCrtAddSQL.append("tel VARCHAR,");
        mCrtAddSQL.append("phone VARCHAR,");
        mCrtAddSQL.append("email VARCHAR,");
        mCrtAddSQL.append("charType VARCHAR )");
    }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL(mCrtUsrSQL.toString());
        db.execSQL(mCrtDwSQL.toString());
        db.execSQL(mCrtAddSQL.toString());
    }

    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 1) {
            db.execSQL(mCrtDwSQL.toString());
        }

        db.execSQL(mCrtAddSQL.toString());

        if (oldVersion == 1 && newVersion >= 5) {// 从第一版升级到最新版本时，密码解密
            updateVersion(db);
        }

        // 新增用户表中的https加密端口
        if (!checkColumnExists(db, "user", "httpsPort")) {// 不存在httpsPort字段
            final String updateSql = "ALTER TABLE user ADD COLUMN httpsPort VARCHAR";
            db.execSQL(updateSql);
        }

        // 新增用户表中的vpn字段   --- MD 破数据库不支持批量添加字段
        if (!checkColumnExists(db, "user", "isVpn")) {
            final String updateSql = "ALTER TABLE user ADD COLUMN isVpn BOOL";
            db.execSQL(updateSql);
        }

        if (!checkColumnExists(db, "user", "vpnAddress")) {
            final String updateSql = "ALTER TABLE user ADD COLUMN vpnAddress VARCHAR";
            db.execSQL(updateSql);
        }

        if (!checkColumnExists(db, "user", "vpnPort")) {
            final String updateSql = "ALTER TABLE user ADD COLUMN vpnPort VARCHAR";
            db.execSQL(updateSql);
        }

        if (!checkColumnExists(db, "user", "vpnUsername")) {
            final String updateSql = "ALTER TABLE user ADD COLUMN vpnUsername VARCHAR";
            db.execSQL(updateSql);
        }

        if (!checkColumnExists(db, "user", "vpnPassword")) {
            final String updateSql = "ALTER TABLE user ADD COLUMN vpnPassword VARCHAR";
            db.execSQL(updateSql);
        }
    }

    /**
     * 更新一代数据
     */
    private void updateVersion(SQLiteDatabase db) {

        // 将密码加密从Base64升级到AES加密
        Cursor c = null;
        String encodePsw = null;
        try {
            c = db.rawQuery("SELECT * from user WHERE _id = 1", null);
            if (c.moveToFirst()) {
                // 获取密码时解密（先用base64解密数据，然后使用AES加密数据，再用base64加密数据）
                final String mima = new String(Base64.decode(c.getString(c.getColumnIndex("password")), Base64.DEFAULT), "utf-8");
                final byte[] bps = AESUtils.encrypt(mima.getBytes());
                encodePsw = Base64Utils.encode(bps);

                final ContentValues cv = new ContentValues();
                cv.put("password", encodePsw);
                db.update("user", cv, "_id = 1", null);
            }
        } catch (final Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close(); // 释放游标资源
            }
        }

    }

    /**
     * 判断某个字段是否存在
     */
    private boolean checkColumnExists(SQLiteDatabase db, String tableName, String columnName) {
        boolean result = false;
        Cursor cursor = null;

        try {
            cursor = db.rawQuery("select * from sqlite_master where name = ? and sql like ?", new String[]{tableName, "%" + columnName + "%"});
            result = null != cursor && cursor.moveToFirst();
        } catch (final Exception e) {
            FELog.e("DB", "checkColumnExists2..." + e.getMessage());
        } finally {
            if (null != cursor && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return result;
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }
}
