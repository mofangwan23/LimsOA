package cn.flyrise.android.shared.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.flyrise.android.shared.bean.AddressBookBean;

public class AddressBookTable {

    private static String TAG = "UserTable";

    private final SQLiteDatabase db;
    private final SQLiteHelper helper;                   // 辅助类名
    private final String tableName = "addressbook";

    private HashMap<String, Integer> LetterIndexer;            // 存放存在的汉语拼音首字母和与之对应的列表位置
    private HashMap<String, List<String>> letterlist;               // 保存字母对应姓氏队列

    public AddressBookTable (Context context) {
        helper = new SQLiteHelper (context);
        // 从辅助类获得数据库对象
        db = helper.getWritableDatabase ();
    }

    public long getCount () {
        long count = 0;
        Cursor c = null;
        try {
            c = db.rawQuery ("SELECT COUNT(*) count from " + tableName, null);
            c.moveToFirst ();
            count = c.getLong (c.getColumnIndex ("count"));
        } catch (final Exception e) {
            Log.e (TAG, e.getMessage (), e);
        } finally {
            if (c != null) {
                c.close (); // 释放游标资源
            }
        }
        return count;
    }

    public List<AddressBookBean> select () {
        final List<AddressBookBean> addressbookArray = new ArrayList<> (); // 全部数据
        final List<AddressBookBean> illegality = new ArrayList<> (); // 保存非法字符
        AddressBookBean addressbook;

        final Cursor c = db.query (tableName, null, null, null, null, null, null);
        if (c.getCount () == 0) {
            LetterIndexer = null;
            letterlist = null;
            return null;
        }
        // 循环显示
        int i = 0;
        String letter = null; // 记录显示是什么字母
        LetterIndexer = new HashMap<> (); // 字母、姓氏对应的位置
        letterlist = new HashMap<> (); // 字母对应的姓氏
        List<String> letlist = new ArrayList<> ();
        final List<String> illega = new ArrayList<> ();
        for (c.moveToFirst (); !c.isAfterLast (); c.moveToNext ()) {
            addressbook = new AddressBookBean ();
            addressbook.setId (c.getString (c.getColumnIndex ("id")));
            addressbook.setName (c.getString (c.getColumnIndex ("name")));
            addressbook.setDepartmentName (c.getString (c.getColumnIndex ("departmentName")));
            addressbook.setImageHref (c.getString (c.getColumnIndex ("imageHref")));
            addressbook.setPosition (c.getString (c.getColumnIndex ("commonGroup")));
            addressbook.setTel (c.getString (c.getColumnIndex ("tel")));
            addressbook.setPhone (c.getString (c.getColumnIndex ("phone")));
            addressbook.setEmail (c.getString (c.getColumnIndex ("email")));
            addressbook.setIsChar (false);
            if (!isLetter (c.getString (c.getColumnIndex ("charType")))) {
                // 非字母字符
                if (!illega.contains (c.getString (c.getColumnIndex ("name")).substring (0, 1))) {
                    illega.add (c.getString (c.getColumnIndex ("name")).substring (0, 1));
                }
                addressbook.setCharType ("#");
                illegality.add (addressbook);
                continue;
            }
            final String chartype = c.getString (c.getColumnIndex ("charType"));
            if (letter == null || !letter.equals (chartype)) {
                if (letter != null && !letter.equals (chartype)) {
                    letterlist.put (letter.toUpperCase (), letlist);
                }
                letter = chartype;
                LetterIndexer.put (letter.toUpperCase (), i++);
                final AddressBookBean letterben = new AddressBookBean ();
                letterben.setCharType (letter.toUpperCase ());
                letterben.setIsChar (true);
                addressbookArray.add (letterben);
                // 刷新存放字母对应姓氏的队列
                letlist = new ArrayList<> ();
            }
            // 存放姓氏
            if (!letlist.contains (c.getString (c.getColumnIndex ("name")).substring (0, 1))) {
                letlist.add (c.getString (c.getColumnIndex ("name")).substring (0, 1));
            }
            addressbook.setCharType (letter);
            // 保存姓氏的位置
            if (!LetterIndexer.containsKey (c.getString (c.getColumnIndex ("name")).substring (0, 1))) {
                LetterIndexer.put (c.getString (c.getColumnIndex ("name")).substring (0, 1), i++);
            } else {
                i++;
            }
            addressbookArray.add (addressbook);
        }
        if (illegality.size () != 0) {
            LetterIndexer.put ("#", i++);
            for (final AddressBookBean illaddressbook : illegality) {
                if (!LetterIndexer.containsKey (illaddressbook.getName ().substring (0, 1))) {
                    LetterIndexer.put (illaddressbook.getName ().substring (0, 1), i++);
                } else {
                    i++;
                }
            }
            final AddressBookBean lastLet = new AddressBookBean ();
            lastLet.setIsChar (true);
            lastLet.setName ("#");
            lastLet.setCharType ("#");
            illegality.add (0, lastLet);
        }

        letterlist.put (letter.toUpperCase (), letlist);
        letterlist.put ("#", illega);
        c.close (); // 释放游标资源
        addressbookArray.addAll (illegality);
        return addressbookArray;
    }

    public boolean isLetter (String str) {
        final Pattern pattern = Pattern.compile ("[a-zA-Z]+");
        final Matcher m = pattern.matcher (str);
        return m.matches ();
    }

    public void delete () {
        db.delete (tableName, null, null);
    }

    /**
     * 当你完成了对数据库的操作（例如你的 Activity 已经关闭），需要调用 SQLiteDatabase 的 Close() 方法来释放掉数据库连接。
     */
    public void close () {
        helper.close ();
    }

    /**
     * 判断数据库是否打开
     */
    public boolean isOpen () {
        return db != null && db.isOpen ();
    }

}
