package cn.flyrise.feep.utils;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.RawContacts.Data;
import android.text.TextUtils;

import java.util.Arrays;
import java.util.List;

public class InitDuHelper {

    private static final String DU_DU_NAME = "嘟一下电话会议";
    private final List<String> sContacts;
    private final Context mContext;

    public InitDuHelper(Context context) {
        this.mContext = context;
        sContacts = Arrays.asList("18046240588", "18060248088", "18060248288", "18046275088",
                "18046242299", "18046240988", "13375909998", "13385002265");
    }

    public void insertToContacts() {
        if (hasInsertContactBefore()) {
            return;
        }

        ContentValues values = new ContentValues();
        Uri rawContactUri = mContext.getContentResolver().insert(ContactsContract.RawContacts.CONTENT_URI, values);
        long rawContactId = ContentUris.parseId(rawContactUri);

        values.put(Data.RAW_CONTACT_ID, rawContactId);
        values.put(Data.MIMETYPE, CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
        values.put(CommonDataKinds.StructuredName.GIVEN_NAME, DU_DU_NAME);
        mContext.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);

        for (String phone : sContacts) {
            if (!TextUtils.isEmpty(phone)) {
                values.clear();
                values.put(Data.RAW_CONTACT_ID, rawContactId);
                values.put(Data.MIMETYPE, CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
                values.put(CommonDataKinds.Phone.NUMBER, phone);
                values.put(CommonDataKinds.Phone.TYPE, 7);
                mContext.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);
            }
        }
    }

    private boolean hasInsertContactBefore() {
        final String[] projection = {BaseColumns._ID};
        final Cursor cursor = mContext.getContentResolver().query(CommonDataKinds.Phone.CONTENT_URI,
                projection, ContactsContract.Contacts.DISPLAY_NAME + " = '" + DU_DU_NAME + "'", null, null);
        if (cursor == null || !cursor.moveToFirst()) {  // 不存在联系人
            return false;
        }

        cursor.close();
        return true;
    }
}
