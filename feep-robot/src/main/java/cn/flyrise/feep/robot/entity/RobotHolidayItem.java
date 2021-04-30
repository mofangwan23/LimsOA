package cn.flyrise.feep.robot.entity;

import android.support.annotation.NonNull;

/**
 * 新建：陈冕;
 * 日期： 2018-1-4-16:23.
 * 节假日查询
 */

public class RobotHolidayItem implements Comparable<RobotHolidayItem> {

    public String name; //节日名称

    public String duration; //节日时长

    public String startDate; //节日开始时间

    public String endDate; //节日结束时间

    public String workDay; //节日描述

    @Override
    public int compareTo(@NonNull RobotHolidayItem robotHolidayItem) {
        return this.startDate.compareTo(robotHolidayItem.startDate);
    }
}
