package cn.flyrise.feep.core.common;

import android.app.Activity;
import android.os.Build;
import android.support.annotation.Keep;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author ZYP
 * @since 2016/8/10 13:41
 */
@Keep
public class FEStatusBar {

	public static boolean setLightStatusBar(Activity activity) {
		return setStatusBarMode(activity, false) != -1;
	}

	public static boolean setDarkStatusBar(Activity activity) {
		return setStatusBarMode(activity, true) != -1;
	}

	private static int setStatusBarMode(Activity activity, boolean isDark) {
		int result = -1;
		if (setMIUIStatusBarMode(activity.getWindow(), isDark)) {
			result = 1;
		}
		else if (setFlymeStatusBarMode(activity, isDark)) {
			result = 2;
		}
		return result;
	}

	/**
	 * 设置状态栏图标为深色和魅族特定的文字风格
	 * 可以用来判断是否为 fly-me 用户
	 */
	private static boolean setFlymeStatusBarMode(Activity activity, boolean dark) {
		FlyMeStatusBar.setStatusBarDarkIcon(activity, dark);
		return true;
	}

	public static boolean setMIUIStatusBarMode(Window window, boolean dark) {
		boolean result = false;
		if (window != null) {
			Class clazz = window.getClass();
			try {
				int darkModeFlag;
				Class layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
				Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
				darkModeFlag = field.getInt(layoutParams);
				Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
				extraFlagField.invoke(window, dark ? darkModeFlag : 0, darkModeFlag);
				result = true;
			} catch (Exception ignored) {

			}
		}
		return result;
	}

	public static boolean canModifyStatusBar(Window window) {
		return isXiaoMi() || isMeiZu();
	}


	public static boolean isXiaoMi() {
		try {
			Class layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
			Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
			field.getInt(layoutParams);
			return true;
		} catch (Exception ignored) {

		}
		return false;
	}

	private static boolean isMeiZu() {
		try {
			Field darkFlag = WindowManager.LayoutParams.class.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
			Field meizuFlags = WindowManager.LayoutParams.class.getDeclaredField("meizuFlags");
			darkFlag.setAccessible(true);
			meizuFlags.setAccessible(true);
			return true;
		} catch (Exception ignored) {

		}
		return false;
	}

	/**
	 * 沉侵入通知栏，并改变通知栏颜色
	 */
	public static void setupStatusBar(Window window, int color) {
		if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
			WindowManager.LayoutParams localLayoutParams = window.getAttributes();
			localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);
		}
		else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			if (FEStatusBar.canModifyStatusBar(window)) {
				WindowManager.LayoutParams localLayoutParams = window.getAttributes();
				localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);
			}
			else {
				window.setStatusBarColor(color);
				window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
			}
		}
	}

//	private static boolean isMiui9() {
//		boolean result = false;
//		try {
//			Properties prop = new Properties();
//			prop.load(new FileInputStream(new File(Environment.getRootDirectory(), "build.prop")));
//			String version = prop.getProperty("ro.miui.ui.version.name", "");
//			if (version.equals("V9")) {
//				result = true;
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		return result;
//	}
}
