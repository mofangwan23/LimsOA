package cn.flyrise.feep.robot.entity;

import java.util.List;

/**
 * 义理解的数据
 */

public class RobotResultData {

    public int inputType;//输入类型，语音、文本

    public String service; //返回的服务类型

    public String text;//一般为用户输入语句

    public String query;  //一般为用户输入语句

    public String answerText; //提示语

    public MoreResults moreResults; //新建日程使用

    public FeepOperationEntry operationEntry;//feoa的操作实体

    public List<SemanticParsenr> semantic;

    public List<WeatherResultData> weatherDatas;

    public List<RobotResultItem> results; //诗词、笑话、故事

    public List<RobotTrainItem> trainItems;//火车票

    public List<RobotHolidayItem> holidayItems;//节假日

}
