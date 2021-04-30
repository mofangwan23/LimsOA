package cn.flyrise.feep.schedule.utils;

import android.text.Html;
import android.text.TextUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.utils.SpUtil;

/**
 * Created by yj on 2016/7/27.
 * 用于记录月视图与周试图选择时的 i,j,year,month
 */
public class ScheduleUtil {

    // 上次选择的i，j,year,month
    public static int lastChoosedYear = -1;
    public static int lastChoosedMonth = -1;
    public static int lastChoosedI = -1;
    public static int lastChoosedJ = -1;

    // 有没选择过
    public static boolean isChoosed() {
        if (lastChoosedI != -1 && lastChoosedJ != -1 && lastChoosedYear != -1 && lastChoosedMonth != -1) {
            return true;
        }
        else {
            return false;
        }
    }

    public static void reset(){
	    lastChoosedYear = -1;
	    lastChoosedMonth = -1;
	    lastChoosedI = -1;
	    lastChoosedJ = -1;
    }

    /**
     * 转换日程事件提醒时间
     * @param movedUpTime string 类型的提醒时间
     */
    public static int getPromptMinute(String movedUpTime) {
        int promptMinute = 0;
        try {
            promptMinute = Integer.parseInt(movedUpTime);
        } catch (Exception e) {
            promptMinute = convertPromptTime(movedUpTime);
        } finally {
            return promptMinute;
        }
    }

    /**
     * 获取循环时间规则
     * @param rule      定义的规则
     * @param startDate 开始时间
     * @param endDate   结束时间
     */
    public static String getLoopRule(String rule, Date startDate, Date endDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-H-mm");
        String startDateStr = sdf.format(startDate);
        String endDateStr = sdf.format(endDate);

        String[] starts = startDateStr.split("-");
        String[] ends = endDateStr.split("-");
        String loopRule = null;
	    switch (rule) {
		    case "0":
		    case "永不":
			    return null;
		    case "1":
		    case "每日":
			    loopRule = "FREQ=DAILY;UNTIL=" + ends[0] + ends[1] + ends[2] + "T160000Z;WKST=SU";
			    break;
		    case "2":
		    case "每周":
			    String mdatastr = startDate.toString();
			    String week_date_str = mdatastr.substring(0, 2);
			    loopRule = "FREQ=WEEKLY;UNTIL=" + ends[0] + ends[1] + ends[2] + "T160000Z;WKST=SU;BYDAY=" + week_date_str;
			    break;
		    case "3":
		    case "每月":
			    loopRule = "FREQ=MONTHLY;UNTIL=" + ends[0] + ends[1] + ends[2] + "T160000Z;WKST=SU;BYMONTHDAY=" + starts[2];
			    break;
	    }
        return loopRule;
    }

    /**
     * 格式化提醒时间
     * @param promptTimeStr 提前通知时间
     */
    public static int convertPromptTime(String promptTimeStr) {
        int promptTime = 0;
        if ("无".equals(promptTimeStr)) {
            return 0;
        }
        else if (promptTimeStr.contains("分钟")) {
            promptTime = Integer.parseInt(promptTimeStr.replace("分钟", ""));
        }
        else if (promptTimeStr.contains("小时")) {
            promptTime = Integer.parseInt(promptTimeStr.replace("小时", "")) * 60;
        }
        else if (promptTimeStr.contains("天")) {
            promptTime = Integer.parseInt(promptTimeStr.replace("天", "")) * 24 * 60;
        }
        else if (promptTimeStr.contains("周")) {
            promptTime = Integer.parseInt(promptTimeStr.replace("周", "")) * 7 * 24 * 60;
        }
        return promptTime;
    }

    /**
     * 删除Html标签
     * @param html
     * @return
     */
    public static String removeHtmlTag(String html) {
        if (TextUtils.isEmpty(html)) return "";
        return Html.fromHtml(html).toString();
    }

    public static boolean hasUserScheduleSetting() {
        String userId = CoreZygote.getLoginUserServices().getUserId();
        if (SpUtil.contains(userId)) {
            return SpUtil.get(userId, false);
        }
        return false;
    }
}
