package cn.flyrise.feep.robot.entity;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2017-7-17.
 * 日程的日期
 */

public class UsedState {

    public String state;

    @SerializedName("datetime.time")
    public String datetime_time;

    @SerializedName("datetime.date")
    public String datetime_date;

    public String content;

}
