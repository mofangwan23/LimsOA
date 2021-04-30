package cn.flyrise.feep.media.common;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import cn.flyrise.feep.core.common.utils.PixelUtil;

/**
 * @author ZYP
 * @since 2017-11-07 16:56
 */
public class CircleProgressBar extends View {

	private Paint mPaint;
	private int mWidth;
	private int mHeight;
	private int mProgress = 0;
	private int mStrokeWidth;

	private static final int DEFAULT_COLOR = Color.parseColor("#EDEDED");

	public CircleProgressBar(Context context) {
		this(context, null);
	}

	public CircleProgressBar(Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CircleProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	}

	public void setProgress(int progress) {
		this.mProgress = progress;
		invalidate();
	}

	@Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);

		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		if (widthMode == MeasureSpec.AT_MOST) {
			widthSize = PixelUtil.dipToPx(36);
		}

		if (heightMode == MeasureSpec.AT_MOST) {
			heightSize = PixelUtil.dipToPx(36);
		}

		if (mWidth < mHeight) {
			mWidth = mHeight;
		}
		else {
			mHeight = mWidth;
		}

		setMeasuredDimension(widthSize, heightSize);
	}

	@Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		this.mWidth = w;
		this.mHeight = h;
		this.mStrokeWidth = mWidth / 10;
	}

	@Override protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		mPaint.setStrokeWidth(mStrokeWidth);
		mPaint.setColor(DEFAULT_COLOR);
		mPaint.setStyle(Style.STROKE);
		canvas.drawCircle(mWidth / 2, mHeight / 2, mWidth / 2 - mStrokeWidth, mPaint);

		// 画圆弧
		mPaint.setColor(Color.parseColor("#28B9FF"));
		RectF rectF = new RectF(mStrokeWidth, mStrokeWidth, mWidth - mStrokeWidth, mHeight - mStrokeWidth); // 除 100 乘 360
		canvas.drawArc(rectF, 0, mProgress * 360 / 100, false, mPaint);
	}
}