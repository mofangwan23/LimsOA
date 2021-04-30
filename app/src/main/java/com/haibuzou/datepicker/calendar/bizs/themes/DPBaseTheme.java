package com.haibuzou.datepicker.calendar.bizs.themes;

import android.graphics.Color;

/**
 * 主题的默认实现类
 *
 * The default implement of theme
 * @author AigeStudio 2015-06-17
 */
public class DPBaseTheme extends DPTheme {
    @Override
    public int colorBG() {
        return 0xFFFFFFFF;
    }

    @Override
    public int colorBGCircle() {
        return Color.parseColor("#28B9FF");
    }

    @Override
    public int colorTitleBG() {
        return 0xFFF37B7A;
    }

    @Override
    public int colorTitle() {
        return 0xEEFFFFFF;
    }

    @Override
    public int colorToday() {
        return 0xFFc9d8df;
    }

    @Override
    public int colorG() {
        return Color.parseColor("#000000");
    }

    @Override
    public int colorF() {
        return 0xEEC08AA4;
    }

    @Override
    public int colorWeekend() {
        return Color.parseColor("#CDCDCD");
    }

    @Override
    public int colorHoliday() {
        return 0x80FED6D6;
    }

    @Override
    public int colorTodayText() {
        return 0xFFFFFFFF;
    }

    @Override
    public int colorChooseText() {
        return 0x00000000;
    }
}
