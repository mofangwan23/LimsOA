package cn.flyrise.feep.robot.analysis;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.flyrise.feep.robot.entity.RobotTrainItem;
import cn.flyrise.feep.robot.entity.RobotTrainPrice;

/**
 * 新建：陈冕;
 * 日期： 2017-12-28-17:16.
 * 火车
 */

public class AnalysisResultTrain {

    public static List<RobotTrainItem> analysis(JSONArray results) throws JSONException {
        List<RobotTrainItem> robotResultItems = new ArrayList<>();
        List<RobotTrainPrice> robotTrainPrices;
        RobotTrainItem robotTrainItem;
        RobotTrainPrice robotTrainPrice;
        for (int i = 0; i < results.length(); i++) {
            robotTrainItem = new RobotTrainItem();
            robotTrainPrices = new ArrayList<>();
            JSONObject result = results.getJSONObject(i);
            if (result.has("arrivalTime")) {
                robotTrainItem.arrivalTime = result.getString("arrivalTime");
            }
            if (result.has("originStation")) {
                robotTrainItem.originStation = result.getString("originStation");
            }
            if (result.has("runTime")) {
                robotTrainItem.runTime = result.getString("runTime");
            }
            if (result.has("startTime")) {
                robotTrainItem.startTime = result.getString("startTime");
            }
            if (result.has("terminalStation")) {
                robotTrainItem.terminalStation = result.getString("terminalStation");
            }
            if (result.has("trainNo")) {
                robotTrainItem.trainNo = result.getString("trainNo");
            }
            if (result.has("trainType")) {
                robotTrainItem.trainType = result.getString("trainType");
            }
            if (result.has("price") && result.get("price") instanceof JSONArray) {
                JSONArray priceArray = result.getJSONArray("price");
                if (priceArray != null) {
                    for (int j = 0; j < priceArray.length(); j++) {
                        JSONObject priceJson = priceArray.getJSONObject(j);
                        if (priceJson == null) {
                            continue;
                        }
                        robotTrainPrice = new RobotTrainPrice();
                        if (priceJson.has("name")) {
                            robotTrainPrice.name = priceJson.getString("name");
                        }
                        if (priceJson.has("value")) {
                            robotTrainPrice.vaule = priceJson.getString("value");
                        }
                        robotTrainPrices.add(robotTrainPrice);
                    }
                    robotTrainItem.prices = robotTrainPrices;
                }
            }
            robotResultItems.add(robotTrainItem);
        }
        return robotResultItems;
    }
}
