package cn.flyrise.feep.addressbook.utils;

import android.text.TextUtils;

import java.io.File;
import java.io.IOException;

/**
 * @author ZYP
 * @since 2017-07-21 17:39
 * 通讯录缓存清除工具
 */
public class AddressBookCacheInvoker {

    /**
     * 创建通讯录的清除标记
     */
    public static void createClearMark(String path, String mark) {
        if (TextUtils.isEmpty(path)) {
            return;
        }

        File file = new File(path + File.separator + mark);
        if (file.exists()) {
            return;
        }

        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 检查指定路径下是否存在清除标记
     */
    public static boolean hasClearMark(String path, String mark) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }

        boolean hasClearMark;
        try {
            File file = new File(path + File.separator + mark);
            hasClearMark = file.exists();
        } catch (Exception exp) {
            hasClearMark = false;
        }
        return hasClearMark;
    }

}