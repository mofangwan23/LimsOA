package cn.flyrise.feep.core.common.utils;

import android.support.annotation.LayoutRes;
import android.support.design.widget.TabLayout;
import android.support.v7.view.menu.MenuPopupHelper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.flyrise.feep.core.common.FELog;
import java.lang.reflect.Field;

/**
 * @author ZYP
 * @since 2018-05-17 19:00
 */
public class UIUtil {

	public static void updatePopupMenuItemLayout(@LayoutRes int layout) {
		try {
			Field field = MenuPopupHelper.class.getDeclaredField("ITEM_LAYOUT");
			field.setAccessible(true);
			field.set(null, layout);
		} catch (Exception e) {
			FELog.e("updatePopupMenuItemLayout");
		}
	}

	public static void fixTabLayoutIndicatorWidth(final TabLayout tabLayout) {
		fixTabLayoutIndicatorWidth(tabLayout, 0);
	}

	public static void fixTabLayoutIndicatorWidth(final TabLayout tabLayout, final int paddingDp) {
		tabLayout.post(() -> {
			try {
				//拿到tabLayout的mTabStrip属性
				LinearLayout mTabStrip = (LinearLayout) tabLayout.getChildAt(0);

				int dp10 = PixelUtil.dipToPx(24);
				if (mTabStrip.getChildCount() == 2) {
					dp10 = PixelUtil.dipToPx(paddingDp);
				}

				for (int i = 0; i < mTabStrip.getChildCount(); i++) {
					View tabView = mTabStrip.getChildAt(i);

					//拿到tabView的mTextView属性  tab的字数不固定一定用反射取mTextView
					Field mTextViewField = tabView.getClass().getDeclaredField("mTextView");
					mTextViewField.setAccessible(true);

					TextView mTextView = (TextView) mTextViewField.get(tabView);

					tabView.setPadding(0, 0, 0, 0);

					//因为我想要的效果是   字多宽线就多宽，所以测量mTextView的宽度
					int width = 0;
					width = mTextView.getWidth();
					if (width == 0) {
						mTextView.measure(0, 0);
						width = mTextView.getMeasuredWidth();
					}

					//设置tab左右间距为10dp  注意这里不能使用Padding 因为源码中线的宽度是根据 tabView的宽度来设置的
					LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tabView.getLayoutParams();
					params.width = width;
					params.leftMargin = dp10;
					params.rightMargin = dp10;
					tabView.setLayoutParams(params);

					tabView.invalidate();
				}

			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		});

	}


	public static void setMargins(View v, int l, int t, int r, int b) {
		if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
			ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
			p.setMargins(l, t, r, b);
			v.requestLayout();
		}
	}

}
