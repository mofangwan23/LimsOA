package cn.flyrise.feep.robot.util;

import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;

/**
 * 新建：陈冕;
 * 日期： 2017-8-10-15:24.
 */

public class RobotTextModifyUtil {

    //兼容5.0以下的html样式
    public static Spanned fromHtml(String content) {
        Spanned spanned;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            spanned = Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY);
        } else {
            spanned = Html.fromHtml(content);
        }
        return spanned;
    }

    public static String getPoetryTitle(String defulatTitle,String text) {
        String poetryContent = getPoetryContent(text);
        if (TextUtils.isEmpty(poetryContent)) {
            return defulatTitle;
        }
        if (poetryContent.contains(",")) {
            int titleIndex = poetryContent.lastIndexOf(",");
            return poetryContent.substring(0, titleIndex);
        }
        return defulatTitle;
    }

    public static String getPoetryContentList(String text) {
        String poetryContent = getPoetryContent(text);
        if (TextUtils.isEmpty(poetryContent)) {
            return text;
        }

        String content = "";
        if (poetryContent.contains(",")) {
            int titleIndex = poetryContent.lastIndexOf(",");
            content = poetryContent.substring(titleIndex + 1, poetryContent.length());
        }
        if (TextUtils.isEmpty(content)) {
            return text;
        }
        if (content.contains("。") || content.contains("？")) {
            String[] textList = content.split("[。？]");
            StringBuilder sb = new StringBuilder();
            for (String item : textList) {
                sb.append(item);
                sb.append("\n\n");
            }
            return sb.toString();
        }
        return text;
    }

    private static String getPoetryContent(String text) {
        if (TextUtils.isEmpty(text)) {
            return "";
        }
        if (text.contains("]") && text.contains("[")) {
            int left = text.indexOf("]");
            int right = text.lastIndexOf("[");
            return text.substring(left + 1, right);
        }
        return "";
    }


}
