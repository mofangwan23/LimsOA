package cn.flyrise.feep.cordova.utils;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import cn.flyrise.feep.cordova.view.FECordovaActivity;
import cn.flyrise.feep.cordova.view.ParticularCordovaActivity;
import cn.flyrise.feep.core.common.X.Func;
import cn.flyrise.feep.core.function.FunctionManager;
import cn.flyrise.feep.core.function.Module;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.flyrise.feep.core.common.CordovaShowInfo;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.request.NoticesManageRequest;

public class CordovaShowUtils {

	public static final String CORDOVA_SHOW_INFO = "cordova_show_info";

	public static final String FINISH_CORDOVA = "finish_cordova_activity";
	public static final String PAGEID = "pageid";
	public static final String MSGID = "meesage_id";
	public static final String ID = "id";
	public static final String ADD_SCHEDULE = "schedule";
	private static final String baseUrl = "file:///android_asset/wechat/html/";
	private static CordovaShowUtils cordovaShowUtils;
	private static final String FILE = "file:";
	private static final String CALL_NUMBERS = "&callNumbers=";
	private static final String NEEDACK = "&needback=0";
	private static final String PAGE_ID = "&pageId=1";
	private static final String HTTP = "http:";
	private static final String HTTPS = "https:";
	private static final String URL_COAD = "?1=1";

	private HashMap<Integer, String> urlMap;

	private HashMap<Integer, String> markMap;

	public static CordovaShowUtils getInstance() {
		if (cordovaShowUtils == null) {
			cordovaShowUtils = new CordovaShowUtils();
		}
		return cordovaShowUtils;
	}

	private CordovaShowUtils() {
		urlMap = new HashMap<>();       // url集
		markMap = new HashMap<>();      // 书签集
		initUrlMap();
	}

	private void initUrlMap() {
		urlMap.put(Func.Knowledge, CordovaShowUtils.baseUrl + "knowledge/km.html");
		urlMap.put(Func.Vote, CordovaShowUtils.baseUrl + "vote/vote-page.html");
		urlMap.put(Func.Schedule, CordovaShowUtils.baseUrl + "schedule/schedule.html");
		urlMap.put(Func.Activity, CordovaShowUtils.baseUrl + "activity/activity_list.html");
		urlMap.put(Func.CRM, "/mdp/html/CRM/index.html");
		urlMap.put(Func.Associate, "/mdp/html/BLOG/listUI.html");
		urlMap.put(Func.Dudu, "/common/dudu/callMeeting.html?");
		urlMap.put(Func.Headline, "/mdp/html/yunger/listUI.html");
		markMap.put(Func.Activity, "#editActivity");
		markMap.put(Func.Vote, "#pageVoteDetail");
		markMap.put(Func.Schedule, "#pageother");
	}

	public String getCordovaWebViewUrl(final CordovaShowInfo mShowInfo) {
		if (mShowInfo == null) {
			return "";
		}
		String urlFilter = "";
		if (!TextUtils.isEmpty(mShowInfo.duduData)) {
			urlFilter = CALL_NUMBERS + mShowInfo.duduData + NEEDACK;
		}
		if (!TextUtils.isEmpty(mShowInfo.pageid)) {
			urlFilter += PAGE_ID;
		}
		if (mShowInfo.type <= 0) {
			return "";
		}
		String url = getUrl(mShowInfo.type, mShowInfo);
		try {
			String urlPath;
			if (Func.Dudu == mShowInfo.type) {
				return url + urlFilter;
			}
			if (!TextUtils.isEmpty(mShowInfo.msgId)) {
				Module module = FunctionManager.findModule(mShowInfo.type);
				if (module != null && !TextUtils.isEmpty(module.url)) {
					// 服务器上的版本
					urlPath = url + "?activeid=" + mShowInfo.id + urlFilter;
				}
				else {
					if (Func.Knowledge == mShowInfo.type && TextUtils.isEmpty(mShowInfo.id)) {
						urlPath = url + "?" + CordovaShowUtils.ID + "=" + mShowInfo.id + urlFilter;
					}
					else {
						// 巨他妈旧的版本
						urlPath = url + (TextUtils.isEmpty(markMap.get(mShowInfo.type))
								? ""
								: markMap.get(mShowInfo.type)) + "?" + CordovaShowUtils.ID + "=" + mShowInfo.id + urlFilter;
					}
				}
				messageReaded(mShowInfo.msgId);// 标记消息已读
			}
			else if (mShowInfo.type == Func.Default && !TextUtils.isEmpty(mShowInfo.url)) {
				urlPath = url;
			}
			else {
				urlPath = TextUtils.isEmpty(urlFilter) ? url : url + URL_COAD + urlFilter;
			}
			FELog.i("fecordova", "-->>>>cordovaurUrl:" + urlPath);
			return urlPath;
		} catch (Exception e) {
		}
		return "";
	}

	private String getUrl(int type, CordovaShowInfo mShowInfo) {

		if (type == Func.Default && !TextUtils.isEmpty(mShowInfo.url)) {
			return initUrl(mShowInfo.url);
		}

		Module module = FunctionManager.findModule(type);
		if (module != null && !TextUtils.isEmpty(module.url) && Func.Dudu != type) {
			return initUrl(module.url);
		}

		String url = urlMap.get(type);
		if (url.startsWith(FILE)) {
			return url;
		}
		else {
			final String feurl = CoreZygote.getLoginUserServices().getServerAddress();
			return feurl + url;
		}
	}

	private String initUrl(String serviceUrl) {
		if (!TextUtils.isEmpty(serviceUrl)) {
			if (serviceUrl.startsWith(HTTP) || serviceUrl.startsWith(HTTPS)) {
				return serviceUrl;
			}
			else {
				final String feurl = CoreZygote.getLoginUserServices().getServerAddress();
				return feurl + serviceUrl;
			}
		}
		return "";
	}


	/**
	 * 标记消息已读
	 */
	private void messageReaded(String id) {
		if (TextUtils.isEmpty(id)) {
			return;
		}
		final List<String> ids = new ArrayList<>();
		final NoticesManageRequest reqContent = new NoticesManageRequest();
		ids.add(id);
		reqContent.setMsgIds(ids);
		reqContent.setUserId(CoreZygote.getLoginUserServices().getUserId());
		FEHttpClient.getInstance().post(reqContent, null);
	}

	public static Intent getCordovaActivity(Context context, int modeValue) {
		Intent intent;
		Module module = FunctionManager.findModule(modeValue);
		if (module != null && !TextUtils.isEmpty(module.url)) {
			intent = new Intent(context, ParticularCordovaActivity.class);
		}
		else {
			intent = new Intent(context, FECordovaActivity.class);
		}
		return intent;
	}
}
