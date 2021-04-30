package cn.flyrise.feep.robot.analysis;

import android.text.TextUtils;
import cn.flyrise.feep.robot.contract.RobotEntityContractKt;
import cn.flyrise.feep.robot.entity.RobotResultItem;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 新建：陈冕;
 * 日期： 2017-12-28-17:19.
 * 解析返回mp3的数据
 * 诗歌、故事、笑话、听新闻
 */

public class AnalysisResultMp3 {

	public static List<RobotResultItem> analysis(final String service, JSONArray results) throws JSONException {
		List<RobotResultItem> robotResultItems = new ArrayList<>();
		RobotResultItem robotResultItem;
		for (int i = 0; i < results.length(); i++) {
			robotResultItem = new RobotResultItem();
			JSONObject result = results.getJSONObject(i);
			if (result == null) {
				continue;
			}
			if (result.has("mp3Url")) {//笑话
				robotResultItem.urlMp3 = result.getString("mp3Url");
			}
			else if (result.has("playUrl")) {//故事
				robotResultItem.urlMp3 = result.getString("playUrl");
			}
			else if (result.has("audiopath")) {//音乐
				robotResultItem.urlMp3 = result.getString("audiopath");
			}
			else if (result.has("url")) {//音乐
				robotResultItem.urlMp3 = result.getString("url");
			}
			if (isMp3NoNullService(service) && TextUtils.isEmpty(robotResultItem.urlMp3)) {
				continue;
			}

			if (result.has("title")) {
				robotResultItem.title = result.getString("title");
			}
			else if (result.has("name")) {
				robotResultItem.title = result.getString("name");
			}
			else if (result.has("songname")) {
				robotResultItem.title = result.getString("songname");
			}
			else if (result.has("translation")) { //学英语中文翻译
				robotResultItem.title = result.getString("translation");
			}

			if (result.has("author")) {
				robotResultItem.author = result.getString("author");
			}
			if (result.has("dynasty")) {
				robotResultItem.dynasty = result.getString("dynasty");
			}
			if (result.has("content")) {
				robotResultItem.content = result.getString("content");
			}

			if (result.has("answer")) {
				robotResultItem.riddleAnswer = result.getString("answer");
			}

			if (result.has("imgUrl")) {
				robotResultItem.imgUrl = result.getString("imgUrl");
			}

			if (result.has("note")) {
				robotResultItem.note = result.getString("note");
			}
			robotResultItems.add(robotResultItem);
		}
		return robotResultItems;
	}

	private static boolean isMp3NoNullService(String service) {
		return TextUtils.equals(service, RobotEntityContractKt.joke)
				|| TextUtils.equals(service, RobotEntityContractKt.news)
				|| TextUtils.equals(service, RobotEntityContractKt.englishEveryday)
				|| TextUtils.equals(service, RobotEntityContractKt.story);
	}
}
