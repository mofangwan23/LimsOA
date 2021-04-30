package cn.flyrise.feep.location.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * 新建：陈冕;
 * 日期： 2018-5-15-15:50.
 * 签到月历列表中的蓝色圆点
 */

public class SignInCalendarCircleView extends View {

	private Paint mPaint;
	private float mRadius;

	public SignInCalendarCircleView(Context context) {
		this(context, null);
	}

	public SignInCalendarCircleView(Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SignInCalendarCircleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mPaint = new Paint();
	}


	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		mPaint.setAntiAlias(true);
		mPaint.setStyle(Style.FILL);
		mRadius = Math.min(getWidth(), getHeight()) / 2;
		mPaint.setColor(Color.parseColor("#8030B6FC"));
		canvas.drawCircle(getWidth() / 2, getHeight() / 2, mRadius, mPaint);
		mPaint.setColor(Color.parseColor("#30B6FC"));
		canvas.drawCircle(getWidth() / 2, getHeight() / 2, mRadius * 3 / 5, mPaint);
	}
}
