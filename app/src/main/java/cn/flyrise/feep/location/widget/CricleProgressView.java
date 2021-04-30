package cn.flyrise.feep.location.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import cn.flyrise.feep.core.common.utils.PixelUtil;
import cn.flyrise.feep.location.util.LeaderDayCricleCororKt;

/**
 * 新建：陈冕;
 * 日期： 2018-5-15-19:13.
 */

public class CricleProgressView extends View {

	private final static int PROGRESS_MAX = 260;
	private final static int START_ANGEL = 140;//开始角度
	private Paint mPaint;
	private RectF mRectf;
	private float strokeWidth;
	private float progress = 0;

	public CricleProgressView(Context context) {
		this(context, null);
	}

	public CricleProgressView(Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CricleProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		strokeWidth = PixelUtil.dipToPx(12);
		mRectf = new RectF();

		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setStyle(Style.STROKE);
		mPaint.setStrokeWidth(strokeWidth);
		mPaint.setStrokeCap(Cap.ROUND);//笔触为圆点
	}

	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
		mRectf.left = strokeWidth / 2;
		mRectf.top = strokeWidth / 2;
		mRectf.right = getWidth() - strokeWidth / 2;
		mRectf.bottom = getHeight() - strokeWidth / 2;

		mPaint.setColor(Color.parseColor("#FFE4E6E7"));
		canvas.drawArc(mRectf, START_ANGEL, PROGRESS_MAX, false, mPaint);

		mPaint.setColor(Color.parseColor(LeaderDayCricleCororKt.resetColor(progress)));
		canvas.drawArc(mRectf, START_ANGEL, progress * PROGRESS_MAX, false, mPaint);
	}

	public void setProgress(float progress) {
		this.progress = progress;
		invalidate();
	}
}
