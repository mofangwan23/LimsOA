package cn.flyrise.feep.knowledge.util;

/**
 * Created by klc
 */

public class SizeUtil {

    public static String formatSize(String kb) {
        float fileSize = Float.parseFloat(kb);
        if (fileSize < 1000) {
            return String.format("%.2fK", fileSize);
        }
        else if (fileSize > 1000) {
            return String.format("%.2fM", fileSize / 1024);
        }
        return "";
    }

}
