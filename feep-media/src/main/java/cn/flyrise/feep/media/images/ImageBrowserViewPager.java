package cn.flyrise.feep.media.images;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * @author ZYP
 * @since 2017-10-20 15:30
 */
public class ImageBrowserViewPager extends ViewPager {

	public ImageBrowserViewPager(Context context) {
		super(context);
	}

	public ImageBrowserViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		try {
			return super.onInterceptTouchEvent(ev);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return false;
		}
	}
}
