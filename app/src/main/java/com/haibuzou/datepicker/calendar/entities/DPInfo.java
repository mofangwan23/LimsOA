package com.haibuzou.datepicker.calendar.entities;

/**
 * 日历数据实体
 * 封装日历绘制时需要的数据
 * 
 * Entity of calendar
 *
 * @author AigeStudio 2015-03-26
 */
public class DPInfo {
    public String strG, strF;
    public boolean isHoliday;
    public boolean isChoosed;
    public boolean isToday, isWeekend;
    public boolean isSolarTerms, isFestival, isDeferred;
    public boolean isDecorBG;
    public boolean isDecorTL, isDecorT, isDecorTR, isDecorL, isDecorR;

    // add by yj 2016-6-22 16:17:10
    public boolean isLastDate; // 是否是上个月的日期
    public boolean isNextDate; // 是否是下个月的日期

}