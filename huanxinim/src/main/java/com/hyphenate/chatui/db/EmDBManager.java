package com.hyphenate.chatui.db;


public class EmDBManager {

    static private EmDBManager dbMgr = new EmDBManager();
    private DbOpenHelper dbHelper;

    private EmDBManager() {
        dbHelper = DbOpenHelper.getInstance();
    }

    public static synchronized EmDBManager getInstance() {
        if (dbMgr == null) {
            dbMgr = new EmDBManager();
        }
        return dbMgr;
    }

    synchronized public void closeDB() {
        if (dbHelper != null) {
            dbHelper.closeDB();
        }
        dbMgr = null;
    }

}
