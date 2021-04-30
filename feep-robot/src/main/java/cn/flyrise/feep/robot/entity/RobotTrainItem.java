package cn.flyrise.feep.robot.entity;

import java.util.List;

/**
 * 新建：陈冕;
 * 日期： 2017-11-29-15:54.
 */

public class RobotTrainItem {

    public String originStation;    //	出发车站

    public String terminalStation;  //终点站

    public String startTime;        //火车预定出发时间

    public String arrivalTime;      //火车预定到达时间

    public String trainNo;          //车次编号

    public String runTime;           //路途时长

    public String trainType;        //火车车型

    public List<RobotTrainPrice> prices; //价格
}
