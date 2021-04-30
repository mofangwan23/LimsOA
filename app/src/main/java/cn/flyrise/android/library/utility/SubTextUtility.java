package cn.flyrise.android.library.utility;

import android.text.TextUtils;

/**
 * cm
 * Created by Administrator on 2015-12-17.
 */
public class SubTextUtility {
    /**
     * 文本中是否存在需要截取的书引号（开始和结束）
     */
    public static boolean isTextBook (String title) {
        if (TextUtils.isEmpty (title)) {
            return false;
        }
        StringBuilder buffer = new StringBuilder (title);
        boolean isBook = false;
        int start = buffer.indexOf ("《");
        int end = buffer.indexOf ("》");
        isBook = start == 0 && end == (buffer.length() - 1);
        return isBook;
    }

    /**
     * 截取出存在于书名号中的标题
     */
    public static String subTextString (String title) {
        StringBuilder buffer = new StringBuilder (title);
        String titles = null;
        int start = buffer.indexOf ("《");
        int end = buffer.indexOf ("》");
        if (start > -1 && end > -1) {
            titles = buffer.substring (start + 1, end);
        } else {
            titles = title;
        }
        return titles;
    }
}
