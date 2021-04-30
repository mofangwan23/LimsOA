package cn.flyrise.feep.commonality.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import cn.flyrise.feep.core.CoreZygote;

/**
 * Created by Administrator on 2016-8-19.
 */
public class SystemManagerUtils {
    /**
     * 获取应用当前版本
     */
    public static String getVersion() {
        final PackageManager pm = CoreZygote.getContext().getPackageManager();
        try {
            final PackageInfo info = pm.getPackageInfo(CoreZygote.getContext().getPackageName(), 0);
            return info.versionName;
        } catch (final PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取应用当前版本
     */
    public static int getVersionCode() {
        final PackageManager pm = CoreZygote.getContext().getPackageManager();
        try {
            final PackageInfo info = pm.getPackageInfo(CoreZygote.getContext().getPackageName(), 0);
            return info.versionCode;
        } catch (final PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 显示软键盘
     */
    public static void shwoInputMethod(EditText mEditText) {
        setInputMethod(true, mEditText);
    }

    /**
     * 关闭软键盘
     */
    public static void hideInputMethod(EditText mEditText) {
        setInputMethod(false, mEditText);
    }

    private static void setInputMethod(boolean isShow, EditText mEditText) {
        if (mEditText == null) {
            return;
        }
        InputMethodManager inputManager = (InputMethodManager) mEditText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (isShow) {
            inputManager.showSoftInput(mEditText, 0);
        }
        else {
            inputManager.hideSoftInputFromWindow(mEditText.getWindowToken(), 0); //强制隐藏键盘
        }

    }
}
