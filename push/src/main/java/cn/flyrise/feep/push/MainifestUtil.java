package cn.flyrise.feep.push;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;

/**
 * 新建：陈冕;
 * 日期： 2018-6-25-15:34.
 */
public class MainifestUtil {

	public static String getXiaoMiAppid(Context context) {
		return getMetaData(context, "XMPUSH_APPID");
	}

	public static String getXiaoMiAppKey(Context context) {
		return getMetaData(context, "XMPUSH_APPKEY");
	}

	public static String getHuaWeiAppid(Context context) {
		String text = getMetaData(context, "com.huawei.hms.client.appid");
		return !TextUtils.isEmpty(text) && text.contains("=") ? text.split("=")[1] : text;
	}

	private static String getMetaData(Context context, String key) {
		Bundle bundle;
		try {
			bundle = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA).metaData;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
			return "";
		}
		String dataKey = bundle.get(key).toString();
		if (TextUtils.isEmpty(dataKey)) return dataKey;
		String replaceData = "";
		if (dataKey.contains("MI-")) {
			replaceData = dataKey.replace("MI-", "");
		}
		if (replaceData.contains(" ")) {
			return replaceData.replace(" ", "");
		}
		return TextUtils.isEmpty(replaceData) ? dataKey : replaceData;
	}

}