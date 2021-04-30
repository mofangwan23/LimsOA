package cn.flyrise.feep.robot.analysis;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.flyrise.feep.robot.entity.RobotHolidayItem;

/**
 * 新建：陈冕;
 * 日期： 2017-12-28-17:16.
 * 节假日查询
 */

public class AnalysisResultHoliday {

    public static List<RobotHolidayItem> analysis(JSONArray results) throws JSONException {
        List<RobotHolidayItem> robotResultItems = new ArrayList<>();
        RobotHolidayItem robotHolidayItem;
        for (int i = 0; i < results.length(); i++) {
            robotHolidayItem = new RobotHolidayItem();
            JSONObject result = results.getJSONObject(i);
            if (result.has("name")) {
                robotHolidayItem.name = result.getString("name");
            }
            if (result.has("duration")) {
                robotHolidayItem.duration = result.getString("duration");
            }
            if (result.has("holidayStartDate")) {
                robotHolidayItem.startDate = result.getString("holidayStartDate");
            }
            if (result.has("holidayEndDate")) {
                robotHolidayItem.endDate = result.getString("holidayEndDate");
            }
            if (result.has("workDay")) {
                robotHolidayItem.workDay = result.getString("workDay");
            }
            robotResultItems.add(robotHolidayItem);
        }
        return robotResultItems;
    }
}
