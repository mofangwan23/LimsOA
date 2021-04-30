package cn.flyrise.feep.location.views;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import cn.flyrise.feep.core.common.FELog;

/**
 * 新建：陈冕;
 * 日期： 2018-5-26-13:35.
 */

public class SignInViewPager extends ViewPager {

	float leftX;
	float leftY;

	public SignInViewPager(Context context) {
		super(context);
	}

	public SignInViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (ev.getAction() == MotionEvent.ACTION_DOWN) {
			leftX = ev.getX();
			leftY = ev.getY();
		}
		else if (ev.getAction() == MotionEvent.ACTION_MOVE) {
			FELog.i("--->>>>>SignVIew:" + (Math.abs(getX() - leftX) > Math.abs(getY() - leftY)));
			return (Math.abs(getX() - leftX) > Math.abs(getY() - leftY));
		}
		else if (ev.getAction() == MotionEvent.ACTION_UP) {
			leftX = 0;
			leftY = 0;
		}
		return super.onInterceptTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {

		return super.onTouchEvent(ev);
	}
}
