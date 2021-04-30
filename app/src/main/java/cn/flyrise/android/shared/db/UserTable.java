//
// UserTable.java
// feep
//
// Created by LuTH on 2011-12-17.
// Copyright 2011 flyrise. All rights reserved.
//
package cn.flyrise.android.shared.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

import cn.flyrise.android.library.utility.encryption.AESUtils;
import cn.flyrise.android.library.utility.encryption.Base64Utils;
import cn.flyrise.android.shared.bean.UserBean;

/**
 * 用户表操作类
 *
 * @author LuTH
 */
public class UserTable {
    private static String TAG = "UserTable";

    private final SQLiteDatabase db;
    private final SQLiteHelper helper;                 // 辅助类名
    private final String tableName = "user";

    public UserTable(Context context) {
        helper = new SQLiteHelper(context);

        // 从辅助类获得数据库对象
        db = helper.getWritableDatabase();
    }

    /**
     * 当你完成了对数据库的操作（例如你的 Activity 已经关闭），需要调用 SQLiteDatabase 的 Close() 方法来释放掉数据库连接。
     */
    public void close() {
        helper.close();
    }

    public long insert(UserBean user) {
        long rowId = -1;
        try {
            final ContentValues cv = new ContentValues();
            cv.put("userID", user.getUserID());
            cv.put("loginname", user.getLoginName());
            cv.put("username", user.getUserName());

            // 保存密码时加密
            String encodePsw = "";

            try {
                final byte[] bps = AESUtils.encrypt(user.getPassword().getBytes());
                encodePsw = Base64Utils.encode(bps);
            } catch (final Exception e) {
                e.printStackTrace();
            }
            cv.put("password", encodePsw);

            cv.put("isSavePassword", user.isSavePassword());
            cv.put("isAutoLogin", user.isAutoLogin());
            cv.put("isHttps", user.isHttps());
            cv.put("serverAddress", user.getServerAddress());
            cv.put("serverPort", user.getServerPort());
            cv.put("httpsPort", user.getHttpsPort());
            cv.put("isVpn", user.isVPN());
            cv.put("vpnAddress", user.getVpnAddress());
            cv.put("vpnPort", user.getVpnPort());
            cv.put("vpnUsername", user.getVpnUsername());
            cv.put("vpnPassword", user.getVpnPassword());
            rowId = db.insert(tableName, "", cv);
        } catch (final Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return rowId;
    }

    /**
     * 删除失败返回0，成功则返回删除的条数
     *
     * @param id
     * @return
     */
    public int delete(int id) {
        int rowId = -1;
        try {
            rowId = db.delete(tableName, "_id = " + id, null);
        } catch (final Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return rowId;
    }

    public int update(UserBean user, int id) {
        int rowId = -1;
        try {
            final ContentValues cv = new ContentValues();
            cv.put("userID", user.getUserID());
            cv.put("loginname", user.getLoginName());
            cv.put("username", user.getUserName());

            // 保存密码时加密
            String encodePsw = "";
            try {
                if (!TextUtils.isEmpty(user.getPassword())) {
                    final byte[] bps = AESUtils.encrypt(user.getPassword().getBytes());
                    encodePsw = Base64Utils.encode(bps);
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }
            cv.put("password", encodePsw);

            cv.put("isSavePassword", user.isSavePassword());
            cv.put("isAutoLogin", user.isAutoLogin());
            cv.put("isHttps", user.isHttps());
            cv.put("serverAddress", user.getServerAddress());
            cv.put("serverPort", user.getServerPort());
            final Date date = new Date();
            final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            final String time = df.format(date);
            cv.put("time", time);
            cv.put("httpsPort", user.getHttpsPort());
            cv.put("isVpn", user.isVPN());
            cv.put("vpnAddress", user.getVpnAddress());
            cv.put("vpnPort", user.getVpnPort());
            cv.put("vpnUsername", user.getVpnUsername());
            cv.put("vpnPassword", user.getVpnPassword());
            rowId = db.update(tableName, cv, "_id = " + id, null);
        } catch (final Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return rowId;
    }

    public UserBean find(int id) {
        final UserBean user = new UserBean();
        Cursor c = null;
        try {
            c = db.rawQuery("SELECT * from " + tableName + " WHERE _id = " + id, null);
            if (c.moveToFirst()) {
                user.set_id(c.getString(c.getColumnIndex("_id")));
                user.setUserID(c.getString(c.getColumnIndex("userID")));
                user.setLoginName(c.getString(c.getColumnIndex("loginname")));
                user.setUserName(c.getString(c.getColumnIndex("username")));

                // 获取密码时解密
                String decodePsw = "";
                try {
                    final byte[] bps = AESUtils.decrypt(Base64Utils.decode(c.getString(c.getColumnIndex("password"))));
                    decodePsw = new String(bps);
                } catch (final Exception e) {
                    e.printStackTrace();
                }
                user.setPassword(decodePsw);
                final int isSavePassword = c.getInt(c.getColumnIndex("isSavePassword"));
                final boolean b = isSavePassword == 1;
                user.setSavePassword(b);

                final int isAutoLogin = c.getInt(c.getColumnIndex("isAutoLogin"));
                final boolean b2 = isAutoLogin == 1;
                user.setAutoLogin(b2);

                final int isHttps = c.getInt(c.getColumnIndex("isHttps"));
                final boolean b3 = isHttps == 1;
                user.setHttps(b3);

                final int isVpn = c.getInt(c.getColumnIndex("isVpn"));
                final boolean b4 = isVpn == 1;
                user.setVPN(b4);

                user.setVpnAddress(c.getString(c.getColumnIndex("vpnAddress")));
                user.setVpnPort(c.getString(c.getColumnIndex("vpnPort")));
                user.setVpnUsername(c.getString(c.getColumnIndex("vpnUsername")));
                user.setVpnPassword(c.getString(c.getColumnIndex("vpnPassword")));

                user.setServerAddress(c.getString(c.getColumnIndex("serverAddress")));
                user.setServerPort(c.getString(c.getColumnIndex("serverPort")));
                user.setTime(c.getString(c.getColumnIndex("time")));
                user.setHttpsPort(c.getString(c.getColumnIndex("httpsPort")));
            }
        } catch (final Exception e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            if (c != null) {
                c.close(); // 释放游标资源
            }
        }

        return user;
    }

    /*
     * public List<UserBean> select() { List<UserBean> userArray = new ArrayList<UserBean>(); UserBean user; Cursor c = db.query(tableName, null, null, null, null, null, null); // 循环显示 for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) { user = new UserBean(); user.set_id(c.getString(c.getColumnIndex("_id"))); user.setUserID(c.getString(c.getColumnIndex("userID")));
     * user.setLoginName(c.getString(c.getColumnIndex("loginname"))); user.setUserName(c.getString(c.getColumnIndex("username"))); user.setVpnPassword(c.getString(c.getColumnIndex("password"))); int isSavePassword = c.getInt(c.getColumnIndex("isSavePassword")); boolean b = isSavePassword == 1 ? true : false; user.setSavePassword(b); int isAutoLogin = c.getInt(c.getColumnIndex("isAutoLogin")); boolean
     * b2 = isAutoLogin == 1 ? true : false; user.setSavePassword(b2); int isHttps = c.getInt(c.getColumnIndex("isHttps")); boolean b3 = isHttps == 1 ? true : false; user.setHttps(b3); user.setServerAddress(c.getString(c.getColumnIndex("serverAddress"))); user.setServerPort(c.getString(c.getColumnIndex("serverPort"))); user.setTime(c.getString(c.getColumnIndex("time"))); userArray.add(user); }
     * c.close(); // 释放游标资源 return userArray; }
     */

    public long getCount() {
        long count = 0;
        Cursor c = null;
        try {
            c = db.rawQuery("SELECT COUNT(*) count from " + tableName, null);
            c.moveToFirst();
            count = c.getLong(c.getColumnIndex("count"));
        } catch (final Exception e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            if (c != null) {
                c.close(); // 释放游标资源
            }
        }
        return count;
    }

    /**
     * 判断数据库是否打开
     */
    public boolean isOpen() {
        return db != null && db.isOpen();
    }

}
