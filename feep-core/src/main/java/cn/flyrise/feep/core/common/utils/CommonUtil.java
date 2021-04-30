package cn.flyrise.feep.core.common.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.util.Base64;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collection;

import cn.flyrise.feep.core.CoreZygote;

/**
 * @author ZYP
 * @since 2017-02-08 13:02
 */
public class CommonUtil {

	public static <E> boolean isEmptyList(Collection<E> list) {
		return list == null || list.isEmpty();
	}

	public static boolean isEmptyList(Object[] array) {
		return array == null || array.length == 0;
	}

	public static <E> boolean nonEmptyList(Collection<E> list) {
		return list != null && list.size() > 0;
	}

	public static boolean isBlankText(String text) {
		return text == null || text.trim().length() == 0;
	}

	public static boolean isNumber(String str) {
		for (int i = 0; i < str.length(); i++) {
			if (!Character.isDigit(str.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	public static String getString(@StringRes int res) {
		return CoreZygote.getContext().getResources().getString(res);
	}

	public static int parseInt(String str) {
		int value;
		try {
			value = Integer.parseInt(str);
		} catch (Exception exp) {
			value = -99;
			exp.printStackTrace();
		}
		return value;
	}

	public static boolean isInteger(String str) {
		boolean isInteger = false;
		try {
			Integer.parseInt(str);
			isInteger = true;
		} catch (Exception exp) {
			exp.printStackTrace();
			isInteger = false;
		}
		return isInteger;
	}

	public static float parseFloat(String str) {
		float value = 0.0F;
		try {
			value = Float.parseFloat(str);
		} catch (Exception exp) {
			value = 0.0F;
		}
		return value;
	}

	public static long parseLong(String str) {
		long value = 0;
		try {
			value = Long.parseLong(str);
		} catch (Exception exp) {
			value = 0;
		}
		return value;
	}

	public static String toBase64(String str) {
		String base64 = "";
		try {
			base64 = Base64.encodeToString(str.getBytes("utf-8"), Base64.DEFAULT);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return base64;
	}

	public static String toBase64Password(String str) {
		String base64 = "";
		try {
			base64 = Base64.encodeToString(str.getBytes("utf-8"), Base64.NO_WRAP);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return base64;
	}

	public static boolean checkArray(String a, String[] array) {
		boolean b = false;
		if (a != null) {
			for (final String element : array) {
				if (a.equalsIgnoreCase(element)) {
					b = true;
				}
			}
		}
		return b;
	}

	public static boolean containsArray(String a, String[] array) {
		boolean b = false;
		if (!TextUtils.isEmpty(a)) {
			for (final String element : array) {
				if (a.contains(element)) {
					b = true;
				}
			}
		}
		return b;
	}

	public static boolean equalsArray(String a, String[] array) {
		boolean b = false;
		if (!TextUtils.isEmpty(a)) {
			for (final String element : array) {
				if (TextUtils.equals(a, element)) {
					b = true;
				}
			}
		}
		return b;
	}

	public static boolean isPhoneNumber(String url) {
		String title = subUrlTitle(url);
		return !TextUtils.isEmpty(title) && "tel".equals(title);
	}

	public static boolean isSendSms(String url) {
		String title = subUrlTitle(url);
		return !TextUtils.isEmpty(title) && "sms".equals(title);
	}

	public static boolean isSendEmail(String url) {
		String title = subUrlTitle(url);
		return !TextUtils.isEmpty(title) && "Mailto".equals(title);
	}

	public static String subUrlNumber(String url) {
		if (TextUtils.isEmpty(url)) {
			return null;
		}
		StringBuffer sb = new StringBuffer(url);
		if (sb.indexOf(":") < 0) {
			return null;
		}
		int index = sb.indexOf("?");
		String phoneNumber = "";
		if (index <= 0) {//邮箱和短信后面会有字段
			phoneNumber = sb.substring(sb.indexOf(":"), sb.length());
		}
		else {
			phoneNumber = sb.substring(sb.indexOf(":"), index);
		}
		if (!TextUtils.isEmpty(phoneNumber)) {
			return phoneNumber;
		}
		return null;
	}

	private static String subUrlTitle(String url) {
		if (TextUtils.isEmpty(url)) {
			return null;
		}
		StringBuffer sb = new StringBuffer(url);
		if (sb.indexOf(":") < 0) {
			return null;
		}
		String title = sb.substring(0, sb.indexOf(":"));
		if (!TextUtils.isEmpty(title)) {
			return title;
		}
		return null;
	}

	public static Bitmap scaleBitmap(Resources resources, int resId, int requestWidth, int requestHeight) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(resources, resId, options);

		int realWidth = options.outWidth;
		int realHeight = options.outHeight;
		int inSampleSize = 1;

		if (realWidth > requestWidth || realHeight > requestHeight) {
			int halfWidth = requestWidth / 2;
			int halfHeight = requestHeight / 2;
			while ((halfWidth / inSampleSize) > requestWidth
					&& (halfHeight / inSampleSize) > requestHeight) {
				inSampleSize *= 2;
			}
		}

		options.inJustDecodeBounds = false;
		options.inSampleSize = inSampleSize;
		return BitmapFactory.decodeResource(resources, resId, options);
	}

	/**
	 * 半角转换为全角
	 * @return 全角字符
	 */
	public static String toDBC(String input) {
		final char[] c = input.toCharArray();
		for (int i = 0; i < c.length; i++) {
			if (c[i] == 32) {
				c[i] = (char) 12288;
				continue;
			}
			if (c[i] < 127) {
				c[i] = (char) (c[i] + 65248);
			}
		}
		return new String(c);
	}

	/**
	 * 将字符串转换为MD5
	 */
	public static String getMD5(final String s) {
		char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
		try {
			byte[] btInput = s.getBytes();
			MessageDigest mdInst = MessageDigest.getInstance("MD5");
			mdInst.update(btInput);
			byte[] md = mdInst.digest();
			int j = md.length;
			char str[] = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++) {
				byte byte0 = md[i];
				str[k++] = hexDigits[byte0 >>> 4 & 0xf];
				str[k++] = hexDigits[byte0 & 0xf];
			}
			return new String(str);
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	public static boolean serviceIsWorking(Context context, Class<?> service) {
		final ActivityManager myManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		final ArrayList<ActivityManager.RunningServiceInfo> runningService = (ArrayList<ActivityManager.RunningServiceInfo>) myManager
				.getRunningServices(50);
		for (int i = 0; i < runningService.size(); i++) {
			String serviceName = runningService.get(i).service.getClassName();
			if (service.getName().equals(serviceName)) {
				return true;
			}
		}
		return false;
	}
}
