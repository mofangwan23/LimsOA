package com.hyphenate.chatui.db;

import static com.hyphenate.chatui.db.DBKey.MSG_DELETE_MSG;
import static com.hyphenate.chatui.db.DBKey.MSG_NOTIFY;
import static com.hyphenate.chatui.db.DBKey.MSG_RECEIVE_MSG;
import static com.hyphenate.chatui.db.DBKey.MSG_SETTING_TABLE;
import static com.hyphenate.chatui.db.DBKey.MSG_SILENCE_ET;
import static com.hyphenate.chatui.db.DBKey.MSG_SILENCE_MODE;
import static com.hyphenate.chatui.db.DBKey.MSG_SILENCE_ST;
import static com.hyphenate.chatui.db.DBKey.MSG_SOUND;
import static com.hyphenate.chatui.db.DBKey.MSG_SPEAKER_ON;
import static com.hyphenate.chatui.db.DBKey.MSG_USER_ID;
import static com.hyphenate.chatui.db.DBKey.MSG_VIBRATE;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hyphenate.chatui.domain.MessageSetting;

/**
 * @author ZYP
 * @since 2017-08-13 16:09
 */
public final class MessageSettingManager {

    private DbOpenHelper mDBHelper;

    public MessageSettingManager() {
        mDBHelper = DbOpenHelper.getInstance();
    }

    public long insert(MessageSetting st) {
        SQLiteDatabase database = mDBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(MSG_USER_ID, st.userId);
        values.put(MSG_RECEIVE_MSG, st.receiveMsg ? 1 : 0);
        values.put(MSG_NOTIFY, st.notify ? 1 : 0);
        values.put(MSG_SOUND, st.sound ? 1 : 0);
        values.put(MSG_VIBRATE, st.vibrate ? 1 : 0);
        values.put(MSG_SILENCE_MODE, st.silence ? 1 : 0);
        values.put(MSG_SILENCE_ST, st.silenceST);
        values.put(MSG_SILENCE_ET, st.silenceET);
        values.put(MSG_DELETE_MSG, st.deleteMsg);
        values.put(MSG_SPEAKER_ON, st.speakerOn);
        return database.insert(MSG_SETTING_TABLE, null, values);
    }

    public int update(MessageSetting st) {
        SQLiteDatabase database = mDBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(MSG_RECEIVE_MSG, st.receiveMsg ? 1 : 0);
        values.put(MSG_NOTIFY, st.notify ? 1 : 0);
        values.put(MSG_SOUND, st.sound ? 1 : 0);
        values.put(MSG_VIBRATE, st.vibrate ? 1 : 0);
        values.put(MSG_SILENCE_MODE, st.silence ? 1 : 0);
        values.put(MSG_SILENCE_ST, st.silenceST);
        values.put(MSG_SILENCE_ET, st.silenceET);
        values.put(MSG_DELETE_MSG, st.deleteMsg);
        values.put(MSG_SPEAKER_ON, st.speakerOn);
        return database.update(MSG_SETTING_TABLE, values, MSG_USER_ID + " = ?", new String[]{st.userId});
    }

    public int delete(String userId) {
        SQLiteDatabase database = mDBHelper.getWritableDatabase();
        return database.delete(MSG_SETTING_TABLE, MSG_USER_ID + " = ?", new String[]{userId});
    }

    public MessageSetting query(String userId) {
        SQLiteDatabase database = mDBHelper.getReadableDatabase();
        MessageSetting msgSetting = null;
        Cursor cursor = null;
        String sql = "select * from " + MSG_SETTING_TABLE + " where " + MSG_USER_ID + " = ?";
        try {
            cursor = database.rawQuery(sql, new String[]{userId});
            if (cursor.moveToNext()) {
                msgSetting = new MessageSetting();
                msgSetting.userId = cursor.getString(cursor.getColumnIndex(MSG_USER_ID));
                msgSetting.silenceST = cursor.getString(cursor.getColumnIndex(MSG_SILENCE_ST));
                msgSetting.silenceET = cursor.getString(cursor.getColumnIndex(MSG_SILENCE_ET));
                msgSetting.receiveMsg = cursor.getInt(cursor.getColumnIndex(MSG_RECEIVE_MSG)) == 1;
                msgSetting.notify = cursor.getInt(cursor.getColumnIndex(MSG_NOTIFY)) == 1;
                msgSetting.sound = cursor.getInt(cursor.getColumnIndex(MSG_SOUND)) == 1;
                msgSetting.vibrate = cursor.getInt(cursor.getColumnIndex(MSG_VIBRATE)) == 1;
                msgSetting.silence = cursor.getInt(cursor.getColumnIndex(MSG_SILENCE_MODE)) == 1;
                msgSetting.deleteMsg = cursor.getInt(cursor.getColumnIndex(MSG_DELETE_MSG)) == 1;
                msgSetting.speakerOn = cursor.getInt(cursor.getColumnIndex(MSG_SPEAKER_ON)) == 1;
            }
        } catch (Exception exp) {
            exp.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        if (msgSetting == null) {                                                       // 数据库里不存在记录，使用默认记录
            msgSetting = MessageSetting.defaultSetting();
        }
        return msgSetting;
    }
}
