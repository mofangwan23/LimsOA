package cn.flyrise.feep.core.common.utils;

import android.content.Context;
import android.content.SharedPreferences;

import cn.flyrise.feep.core.CoreZygote;

/**
 * @author ZYP
 * @since 2017-02-07 17:18
 *
 * SharedPreferences 工具类
 */
public class SpUtil {

    private static final String SP_FILE_NAME = "User_Preferences";
    private static SharedPreferences sPreferences;

    @SuppressWarnings("all")
    public static void put(String key, Object value) {
        SharedPreferences preferences = getPreferences();
        SharedPreferences.Editor editor = preferences.edit();
        if (value instanceof Integer) {
            editor.putInt(key, (Integer) value);
        }
        else if (value instanceof String) {
            editor.putString(key, (String) value);
        }
        else if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
        }
        else if (value instanceof Long) {
            editor.putLong(key, (Long) value);
        }
        editor.commit();
    }

    @SuppressWarnings("all")
    public static <T> T get(String key, T defaultValue) {
        SharedPreferences preferences = getPreferences();
        if (defaultValue instanceof Integer) {
            return (T) new Integer(preferences.getInt(key, (Integer) defaultValue));
        }
        else if (defaultValue instanceof String) {
            return (T) preferences.getString(key, (String) defaultValue);
        }
        else if (defaultValue instanceof Boolean) {
            return (T) new Boolean(preferences.getBoolean(key, (Boolean) defaultValue));
        }
        else if (defaultValue instanceof Long) {
            return (T) new Long(preferences.getLong(key, (Long) defaultValue));
        }
        return null;
    }

    public static boolean contains(String key) {
        SharedPreferences preferences = getPreferences();
        return preferences.contains(key);
    }

    private static SharedPreferences getPreferences() {
        if (sPreferences == null) {
            sPreferences = CoreZygote.getContext().getSharedPreferences(SP_FILE_NAME, Context.MODE_PRIVATE);
        }
        return sPreferences;
    }

}
