package cn.flyrise.feep.core.common.utils;

import android.content.Context;
import android.util.TypedValue;
import cn.flyrise.feep.core.CoreZygote;

/**
 * @author ZYP
 * @since 2017-02-06 11:16
 * 像素转换工具类
 */
public class PixelUtil {

	public static int dipToPx(float dpValue) {
		return dipToPx(CoreZygote.getContext(), dpValue);
	}

	public static int dipToPx(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	public static int pxToDip(float pxValue) {
		return pxToDip(CoreZygote.getContext(), pxValue);
	}

	private static int pxToDip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}
}
