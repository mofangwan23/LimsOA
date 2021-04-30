//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.hyphenate.easeui.utils;

import android.annotation.SuppressLint;

public class DateUtils {
    public static boolean isCloseEnough(long var0, long var2) {
        long var4 = var0 - var2;
        if (var4 < 0L) {
            var4 = -var4;
        }
        return var4 < 300000L;
    }

    @SuppressLint({"DefaultLocale"})
    public static String toTime(int var0) {
        var0 /= 1000;
        int var1 = var0 / 60;
        if (var1 >= 60) {
            var1 %= 60;
        }

        int var3 = var0 % 60;
        return String.format("%02d:%02d", new Object[]{Integer.valueOf(var1), Integer.valueOf(var3)});
    }

}
