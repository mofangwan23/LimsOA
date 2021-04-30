package cn.flyrise.feep.robot.util;

import android.text.TextUtils;

/**
 * 新建：陈冕;
 * 日期： 2017-11-28-15:13
 * 过滤特殊字符.
 */

public class RobotFilterChar {

    private final static String[] filterChars = {"[k0]", "[k3]", "[h0]", "[h1]", "[h2]", "[n0]", "[n1]", "[n2]", " ~ "};

    private final static String[] replaceChars = {"\\[k0\\]", "\\[k3\\]", "\\[h0\\]"
            , "\\[h1\\]", "\\[h2\\]", "\\[n0\\]", "\\[n1\\]", "\\[n2\\]", " ~ "};

    private final static String chars = "～";

    public static String filterChars(String text) {
        if (TextUtils.isEmpty(text)) {
            return text;
        }
        for (int i = 0; i < filterChars.length; i++) {
            if (!text.contains(filterChars[i])) {
                continue;
            }
            if (TextUtils.equals(" ~ ", filterChars[i])) {
                text = text.replaceAll(replaceChars[i], chars);
            } else {
                text = text.replaceAll(replaceChars[i], "");
            }
        }
        return text;
    }

    public static String getPoetryContentList(String poetryContent) {
        if (TextUtils.isEmpty(poetryContent)) {
            return poetryContent;
        }

        StringBuilder sb = new StringBuilder();
        if (poetryContent.contains("。")) {
            poetryContent = poetryContent.replaceAll("。", "。|");
        }
        if (poetryContent.contains("？")) {
            poetryContent = poetryContent.replaceAll("？", "？|");
        }
        if (poetryContent.contains("！")) {
            poetryContent = poetryContent.replaceAll("！", "！|");
        }
        if (!poetryContent.contains("|")) {
            return poetryContent;
        }
        String[] textList = poetryContent.split("\\|");
        for (String item : textList) {
            sb.append(item);
            sb.append("\n\n");
        }
        return sb.toString();
    }

    // 根据Unicode编码完美的判断中文汉字（不包含符号）
    private static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT) {
            return true;
        }
        return false;
    }

    // 完整的判断中文汉字和符号,有一个非中文字符排除
    public static boolean isChinese(String strName) {
        char[] ch = strName.toCharArray();
        for (int i = 0; i < ch.length; i++) {
            char c = ch[i];
            if (!isChinese(c)) {
                return false;
            }
        }
        return true;
    }

}
