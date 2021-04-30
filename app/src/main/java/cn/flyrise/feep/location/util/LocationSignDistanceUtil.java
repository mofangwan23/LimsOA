package cn.flyrise.feep.location.util;

import android.content.Context;
import cn.flyrise.feep.R;
import java.math.BigDecimal;

/**
 * 新建：陈冕;
 * 日期： 2017-12-20-15:25.
 */

public class LocationSignDistanceUtil {

	public static String getExceedText(Context mContext, float mExceed) {
		StringBuilder sb = new StringBuilder();
		if (mExceed > 0 && mExceed < 1000) {
			if (mExceed > 1) sb.append((int) mExceed);
			else sb.append(mContext.getResources().getString(R.string.location_min_now));
			sb.append(mContext.getResources().getString(R.string.location_m));
		}
		else if (mExceed >= 1000) {
			float m = mExceed / 1000;
			BigDecimal bg = new BigDecimal(m);
			double exceed = bg.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
			sb.append(exceed);
			sb.append(mContext.getResources().getString(R.string.location_km));
		}
		return String.format(mContext.getResources().getString(R.string.location_no_sign_start), sb.toString());
	}
}
