package cn.flyrise.feep.robot.analysis;

import cn.flyrise.feep.core.common.FELog;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import cn.flyrise.feep.core.common.utils.GsonUtil;
import cn.flyrise.feep.robot.entity.WeatherResultData;

/**
 * 新建：陈冕;
 * 日期： 2017-12-28-17:26.
 * 天气
 */

public class AnalysisResultWeather {

    public static List<WeatherResultData> analysis(JSONObject jsonObject) throws JSONException {
        if (jsonObject.has("data") && jsonObject.get("data") instanceof JSONObject) {
            JSONObject wetherJsonObject = jsonObject.getJSONObject("data");
            if (wetherJsonObject.has("result") && wetherJsonObject.get("result") instanceof JSONArray) {
                JSONArray wetherJsonObjectJSONArray = wetherJsonObject.getJSONArray("result");
                if (wetherJsonObjectJSONArray == null) {
                    return null;
                }
                return GsonUtil.getInstance().fromJson(wetherJsonObjectJSONArray.toString(), new TypeToken<List<WeatherResultData>>() {
                }.getType());
            }
        }
        return null;
    }

}
