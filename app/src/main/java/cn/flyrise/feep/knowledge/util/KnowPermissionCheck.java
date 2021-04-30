package cn.flyrise.feep.knowledge.util;

/**
 * Created by KLC on 2016/12/1.
 */
public class KnowPermissionCheck {

    public static boolean canUpload(int num) {
        return Math.floor((num % 4) / 2) == 1;
    }

    public static boolean canMove(int num) {
        return Math.floor((num % 4) / 2) == 1;
    }

    public static boolean canDownLoad(int num) {
        return Math.floor((num % 8) / 4) == 1;
    }

    public static boolean canRename(int num) {
        return Math.floor((num % 16) / 8) == 1;
    }

    public static boolean canPublish(int num) {
        return Math.floor((num % 32) / 16) == 1;
    }

    public static boolean canDelete(int num) {return Math.floor((num % 128) / 64) == 1;
    }

}
