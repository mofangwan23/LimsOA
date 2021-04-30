package com.haibuzou.datepicker.calendar.utils;

import java.util.Calendar;

/**
 * 数组操作工具类
 *
 * Utils of data operation
 * @author AigeStudio 2015-07-22
 */
public final class DataUtils {

    private DataUtils() {}

    private final static Calendar sCalendar = Calendar.getInstance();

    public static String[][] arraysConvert(String[] src, int row, int column) {
        String[][] tmp = new String[row][column];
        for (int i = 0; i < row; i++) {
            tmp[i] = new String[column];
            System.arraycopy(src, i * column, tmp[i], 0, column);
        }
        return tmp;
    }
}
