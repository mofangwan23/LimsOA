package cn.flyrise.feep.event;

/**
 * Created by Administrator on 2016-6-30.
 * 当收到极光推动的消息时，刷新主菜单气泡
 */
public class EventJPushRefreshNoticeMessageMenu {
    public String type = "-9";
    public String totalNums = "0";
    public String circleNums = "0";

    public EventJPushRefreshNoticeMessageMenu() {
    }

    public EventJPushRefreshNoticeMessageMenu(String type, String totalNums, String circleNums) {
        this.type = type;
        this.totalNums = totalNums;
        this.circleNums = circleNums;
    }
}
