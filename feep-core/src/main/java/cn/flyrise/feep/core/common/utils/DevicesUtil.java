package cn.flyrise.feep.core.common.utils;

import android.Manifest;
import android.Manifest.permission;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.FEStatusBar;
import java.lang.reflect.Field;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * @author ZYP
 * @since 2017-02-06 16:44 设备信息工具类
 */
public class DevicesUtil {

	public static final String SOFT_INPUT_HEIGHT = "soft_input_height";

	/**
	 * 尝试关闭软键盘
	 */
	public static void tryCloseKeyboard(final Activity activity) {
		View currentFocus = activity.getCurrentFocus();
		if (currentFocus != null) {
			hideKeyboard(currentFocus);
		}
	}

	public static void hideKeyboard(View view) {
		if (view != null) {
			InputMethodManager imm = (InputMethodManager) CoreZygote.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(view.getWindowToken(), 0); //强制隐藏键盘
		}
	}

	public static void showKeyboard(View view) {
		InputMethodManager imm = (InputMethodManager) CoreZygote.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		view.requestFocus();
		imm.showSoftInput(view, 0);
	}

	public static int getScreenWidth() {
		Context context = CoreZygote.getContext();
		return context.getResources().getDisplayMetrics().widthPixels;
	}

	public static int getScreenHeight() {
		Context context = CoreZygote.getContext();
		return context.getResources().getDisplayMetrics().heightPixels;
	}

	public static String getMNC(Context context) {
		String mnc = "**";
		try {
			final TelephonyManager telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			final String imsi = telManager.getSubscriberId();
			if (!TextUtils.isEmpty(imsi)) {
				mnc = imsi.substring(3, 5);
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return mnc;
	}

	/**
	 * 获取设备地址, 其实就是 wifi 地址
	 */
	public static String getDeviceAddress() {
		try {
			String interfaceName = "wlan0";
			List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
			for (NetworkInterface intf : interfaces) {
				if (!intf.getName().equalsIgnoreCase(interfaceName)) {
					continue;
				}
				byte[] mac = intf.getHardwareAddress();
				if (mac == null) {
					return "";
				}
				StringBuilder buf = new StringBuilder();
				for (byte aMac : mac) {
					buf.append(String.format("%02X:", aMac));
				}
				if (buf.length() > 0) {
					buf.deleteCharAt(buf.length() - 1);
				}
				return buf.toString();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	/**
	 * 获取状态栏高度
	 */
	public static int getStatusBarHeight(Context context) {
		int result = 0;
		int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			result = context.getResources().getDimensionPixelSize(resourceId);
		}
		return result;
	}

	/**
	 * 获取底部导航栏的高度(有些手机底部是虚拟按键，适配部分机型时需要减去这部分的高度)
	 */
	public static Point getNavigationBarSize(Context context) {
		Point appUsableSize = getAppUsableScreenSize(context);
		Point realScreenSize = getRealScreenSize(context);

		// navigation bar on the right
		if (appUsableSize.x < realScreenSize.x) {
			return new Point(realScreenSize.x - appUsableSize.x, appUsableSize.y);
		}

		// navigation bar at the bottom
		if (appUsableSize.y < realScreenSize.y) {
			return new Point(appUsableSize.x, realScreenSize.y - appUsableSize.y);
		}

		// navigation bar is not present
		return new Point();
	}

	/**
	 * 获取 APP 可用的屏幕尺寸大小，不包括底部导航栏
	 */
	public static Point getAppUsableScreenSize(Context context) {
		WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = windowManager.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		return size;
	}

	/**
	 * 获取 APP 整个屏幕的大小，包括底部导航栏
	 */
	public static Point getRealScreenSize(Context context) {
		WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = windowManager.getDefaultDisplay();
		Point size = new Point();

		if (Build.VERSION.SDK_INT >= 17) {
			display.getRealSize(size);
		}
		else if (Build.VERSION.SDK_INT >= 14) {
			try {
				size.x = (Integer) Display.class.getMethod("getRawWidth").invoke(display);
				size.y = (Integer) Display.class.getMethod("getRawHeight").invoke(display);
			} catch (Exception ignored) {

			}
		}

		return size;
	}

	public static String getDeviceToken() {
		final String uuidRaw = UUID.randomUUID().toString();
		return uuidRaw.replaceAll("-", "");
	}

	/**
	 * 拨打电话
	 */
	public static void DialTelephone(Context context, String photoNumber) {
		Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + photoNumber));
		if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
			context.startActivity(intent);
		}
	}

	/**
	 * 发短信
	 */
	public static void sendSms(Context context, String number) {
		Uri uri = Uri.parse("smsto:" + number);
		Intent sendIntent = new Intent(Intent.ACTION_VIEW, uri);
		context.startActivity(sendIntent);
	}

	/**
	 * 发邮件
	 */
	public static void sendEmail(Context context, String tos) {
		Intent data = new Intent(Intent.ACTION_SENDTO);
		data.setData(Uri.parse("mailto:" + tos));
		context.startActivity(data);
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

	public static boolean isCameraCanUsed(Context context) {
		boolean isCameraCanUsed = false;
		Camera mCamera = null;
		try {
			mCamera = Camera.open();
			isCameraCanUsed = true;
		} catch (Exception e) {
			isCameraCanUsed = false;
		}

		if (mCamera != null) {
			try {
				mCamera.release();
			} catch (Exception e) {
				isCameraCanUsed = true;
			}
		}
		return isCameraCanUsed;
	}

	private static int getStatusBarHeight() {
		int statusBarHeight = 0;
		try {
			Class<?> c = Class.forName("com.android.internal.R$dimen");
			Object obj = c.newInstance();
			Field field = c.getField("status_bar_height");
			int x = Integer.parseInt(field.get(obj).toString());
			statusBarHeight = CoreZygote.getContext().getResources().getDimensionPixelSize(x);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return statusBarHeight;
	}

	private static int getNavigationBarHeight() {
		Resources resources = CoreZygote.getContext().getResources();
		int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
		int height = resources.getDimensionPixelSize(resourceId);
		return height;
	}
	//监听键盘高度end

	/**
	 * 获取软键盘高度
	 */
	public static int getSupportSoftInputHeight(Activity activity) {
		Rect r = new Rect();
		/**
		 * decorView是window中的最顶层view，可以从window中通过getDecorView获取到decorView。
		 * 通过decorView获取到程序显示的区域，包括标题栏，但不包括状态栏。
		 */
		activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(r);
		//获取屏幕的高度
		int screenHeight = activity.getWindow().getDecorView().getRootView().getHeight();
		//计算软件盘的高度
		int softInputHeight = screenHeight - r.bottom;
		/**
		 * 某些Android版本下，没有显示软键盘时减出来的高度总是144，而不是零，
		 * 这是因为高度是包括了虚拟按键栏的(例如华为系列)，所以在API Level高于20时，
		 * 我们需要减去底部虚拟按键栏的高度（如果有的话）
		 */
		if (Build.VERSION.SDK_INT >= 20) {
			// When SDK Level >= 20 (Android L), the softInputHeight will contain the height of softButtonsBar (if has)
			softInputHeight = softInputHeight - getSoftButtonsBarHeight(activity);
		}
		if (softInputHeight < 0) {
			FELog.w("EmotionKeyboard--Warning: value of softInputHeight is below zero!");
		}
		//存一份到本地
		if (softInputHeight > 0) {
			SpUtil.put(DevicesUtil.SOFT_INPUT_HEIGHT, softInputHeight);
		}
		return softInputHeight;
	}

	/**
	 * 底部虚拟按键栏的高度
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	public static int getSoftButtonsBarHeight(Activity activity) {
		DisplayMetrics metrics = new DisplayMetrics();
		//这个方法获取可能不是真实屏幕的高度
		activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int usableHeight = metrics.heightPixels;
		//获取当前屏幕的真实高度
		activity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
		int realHeight = metrics.heightPixels;
		if (realHeight > usableHeight) {
			return realHeight - usableHeight;
		}
		else {
			return 0;
		}
	}

	/**
	 * 获取软键盘的高度
	 */
	public static int getKeyBoardHeight() {
		Point point = getRealScreenSize(CoreZygote.getContext());
		int defaultHeight = (int) (point.y * 0.4);
		int height = SpUtil.get(SOFT_INPUT_HEIGHT, defaultHeight);
		return height;
	}

	/**
	 * Return pseudo unique ID
	 * @return ID
	 */
//	public static String getDeviceUniqueId() {
//		String m_szDevIDShort = "35" + (Build.BOARD.length() % 10)
//				+ (Build.BRAND.length() % 10)
//				+ (Build.CPU_ABI.length() % 10)
//				+ (Build.DEVICE.length() % 10)
//				+ (Build.MANUFACTURER.length() % 10)
//				+ (Build.MODEL.length() % 10)
//				+ (Build.PRODUCT.length() % 10);
//
//		String serial;
//		try {
//			serial = android.os.Build.class.getField("SERIAL").get(null).toString();
//			String deviceId = new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
//			return deviceId.toUpperCase();
//		} catch (Exception exception) {
//			serial = "serial"; // some value
//		}
//		String deviceId = new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
//		return deviceId.toUpperCase();
//	}
	public static String getDeviceUniqueId() {
		String serial;
		String m_szDevIDShort = "35" +
				Build.BOARD.length() % 10 + Build.BRAND.length() % 10 +
				Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10 +
				Build.DISPLAY.length() % 10 + Build.HOST.length() % 10 +
				Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10 +
				Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10 +
				Build.TAGS.length() % 10 + Build.TYPE.length() % 10 +
				Build.USER.length() % 10; //13 位
		try {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				serial = android.os.Build.getSerial();
			} else {
				serial = Build.SERIAL;
			}
			String deviceId= new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
			return deviceId.toUpperCase();
		} catch (Exception exception) {
			//serial需要一个初始化
			serial = "serial"; // 随便一个初始化
		}
		//使用硬件信息拼凑出来的15位号码
		String deviceId= new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
		return deviceId.toUpperCase();
	}


	public static boolean isSpecialDevice() {
		if (VERSION.SDK_INT <= VERSION_CODES.KITKAT) {
			if (FEStatusBar.canModifyStatusBar(null)) {
				return false;
			}
			return true;
		}

		if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP && VERSION.SDK_INT < VERSION_CODES.M) {
			String devices = Build.DEVICE;
			if (TextUtils.equals(devices, "A3580")) {   // 狗日的联想
				return true;
			}
		}
		return false;
	}

}
