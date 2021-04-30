package cn.flyrise.feep.core.base.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * @author ZYP
 * @since 2017-09-05 15:24
 */
public class WaterMarkView extends View {

	private final static float DEFAULT_DEGREE = 342;

	private Paint mPaint;
	private int mWidth;
	private int mHeight;
	public String mWaterMark;

	private Rect mTextBounds;
	private boolean startDraw;

	public WaterMarkView(Context context) {
		this(context, null);
	}

	public WaterMarkView(Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public WaterMarkView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14, getResources().getDisplayMetrics()));
		mPaint.setColor(Color.parseColor("#16666666"));
		mTextBounds = new Rect();
	}

	public void setWaterMark(String text) {
		this.mWaterMark = text;
	}

	public void startDraw() {
		startDraw = true;
	}

	@Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);

		if (widthMode == MeasureSpec.AT_MOST) {
			widthSize = 480;
		}

		if (heightMode == MeasureSpec.AT_MOST) {
			heightSize = 800;
		}

		setMeasuredDimension(widthSize, heightSize);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		this.mWidth = w;
		this.mHeight = h;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (!startDraw) {
			return;
		}
		if (TextUtils.isEmpty(mWaterMark)) {
			return;
		}

		mPaint.getTextBounds(mWaterMark, 0, mWaterMark.length(), mTextBounds);

		int widthCount, heightCount;
		int singleWidth = (int) (mTextBounds.width() * 1.5);
		if (singleWidth > mWidth / 2) {//太宽的物理减半
			singleWidth = mWidth / 2;
			widthCount = 2;
		}
		else {
			widthCount = mWidth / singleWidth + 2;
		}

		int singleHeight = mTextBounds.height() * 6;
		if (singleHeight > mHeight) {
			singleHeight = mHeight;
			heightCount = 2;
		}
		else {
			heightCount = mHeight / singleHeight + 2;
			heightCount = heightCount < 0 ? 2 : heightCount;
		}

		int startX, startY;
		for (int i = 0; i < heightCount; i++) {            // 每一行
			for (int j = 0; j < widthCount; j++) {         // 每一列
				startX = i % 2 == 0 ? (j - 1) * singleWidth : (j - 1) * singleWidth + singleWidth / 2;
				startY = (i + 1) * singleHeight;
				canvas.save();
				canvas.rotate(DEFAULT_DEGREE, startX, startY);
				canvas.drawText(mWaterMark, startX, startY, mPaint);
				canvas.restore();
			}
		}
	}
}
