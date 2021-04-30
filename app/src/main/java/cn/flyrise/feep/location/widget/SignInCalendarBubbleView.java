package cn.flyrise.feep.location.widget;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import cn.flyrise.feep.core.common.utils.PixelUtil;

/**
 * 新建：陈冕;
 * 日期： 2018-5-15-15:50.
 * 签到月历列表中的气泡
 */

public class SignInCalendarBubbleView extends Drawable {

	private Paint mPaint;

	private static final float mArrowWidth = 35;
	private static final float mAngle = 30;

	private float mArrowPosition;
	private float mArrowHeight;
	private RectF rect = new RectF();
	private Path path = new Path();

	public SignInCalendarBubbleView() {
		mPaint = new Paint();
		mArrowPosition = PixelUtil.dipToPx(10);
		mArrowHeight = PixelUtil.dipToPx(10);
	}

	@Override
	public void draw(@NonNull Canvas canvas) {
		mPaint.setAntiAlias(true);
		mPaint.setStyle(Style.FILL_AND_STROKE);
		mPaint.setStrokeWidth(2);
		mPaint.setColor(Color.parseColor("#F7F8F8"));
		Rect bounds = getBounds();
		rect.left = 0;
		rect.top = 0;
		rect.right = bounds.width();
		rect.bottom = bounds.height();
		canvas.drawPath(setUpLeftPath(), mPaint);
	}

	private Path setUpLeftPath() {
		path.moveTo(mArrowWidth + rect.left + mAngle, rect.top);
		path.lineTo(rect.width() - mAngle, rect.top);
		path.arcTo(new RectF(rect.right - mAngle, rect.top, rect.right, mAngle + rect.top), 270, 90);
		path.lineTo(rect.right, rect.bottom - mAngle);
		path.arcTo(new RectF(rect.right - mAngle, rect.bottom - mAngle, rect.right, rect.bottom), 0, 90);
		path.lineTo(rect.left + mArrowWidth + mAngle, rect.bottom);
		path.arcTo(new RectF(rect.left + mArrowWidth, rect.bottom - mAngle, mAngle + rect.left + mArrowWidth, rect.bottom), 90, 90);
		path.lineTo(rect.left + mArrowWidth, mArrowHeight + mArrowPosition);
		path.lineTo(rect.left, mArrowPosition + mArrowHeight / 2);
		path.lineTo(rect.left + mArrowWidth, mArrowPosition);
		path.lineTo(rect.left + mArrowWidth, rect.top + mAngle);
		path.arcTo(new RectF(rect.left + mArrowWidth, rect.top, mAngle + rect.left + mArrowWidth, mAngle + rect.top), 180, 90);
		path.close();
		return path;
	}

	@Override
	public void setAlpha(int i) {
		mPaint.setAlpha(i);
	}

	@Override
	public void setColorFilter(@Nullable ColorFilter colorFilter) {
		mPaint.setColorFilter(colorFilter);
	}

	@Override
	public int getOpacity() {
		return PixelFormat.TRANSLUCENT;
	}
}
