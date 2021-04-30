package cn.flyrise.feep.robot.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import cn.flyrise.feep.core.common.utils.PixelUtil;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 水波纹特效
 * Created by fbchen2 on 2016/5/25.
 */
public class WaveViews extends View {

	private float mInitialRadius = 70;   // 初始波纹半径
	private float mMaxRadius;   // 最大波纹半径
	private long mDuration = 5000; // 一个波纹从创建到消失的持续时间
	private int mSpeed = 500;   // 波纹的创建速度，每500ms创建一个
	private boolean mIsRunning;
	private long mLastCreateTime;
	private List<Circle> mCircleList = new ArrayList<>();

	private Runnable mCreateCircle = new Runnable() {
		@Override
		public void run() {
			if (mIsRunning) {
				newCircle();
				postDelayed(mCreateCircle, mSpeed);
			}
		}
	};

	private Interpolator mInterpolator = new LinearInterpolator();

	private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

	public WaveViews(Context context) {
		super(context);
	}

	public WaveViews(Context context, AttributeSet attrs) {
		super(context, attrs);
		mMaxRadius = PixelUtil.dipToPx(200);
		setColor(Color.parseColor("#12beff"));
		setStyle(Paint.Style.FILL);
		setInterpolator(new LinearOutSlowInInterpolator());
	}

	private void setInterpolator(Interpolator interpolator) {
		mInterpolator = interpolator;
		if (mInterpolator == null) mInterpolator = new LinearInterpolator();
	}

	private void setStyle(Paint.Style style) {
		mPaint.setStyle(style);
	}

	private void setColor(int color) {
		mPaint.setColor(color);
	}

	public void start() {//开始
		if (!mIsRunning) {
			mIsRunning = true;
			mCreateCircle.run();
		}
	}

	//缓慢停止
	public void stop() {
		mIsRunning = false;
	}

	protected void onDraw(Canvas canvas) {
		Iterator<Circle> iterator = mCircleList.iterator();
		int mHeight = getHeight() - PixelUtil.dipToPx(40 + 35);
		while (iterator.hasNext()) {
			Circle circle = iterator.next();
			float radius = circle.getCurrentRadius();
			if (System.currentTimeMillis() - circle.mCreateTime < mDuration) {
				mPaint.setAlpha(circle.getAlpha());
				canvas.drawCircle(getWidth() / 2, mHeight, radius, mPaint);
			}
			else {
				iterator.remove();
			}
		}
		if (mCircleList.size() > 0) postInvalidateDelayed(10);
	}

	public void setSpeed(int speed) {
		mSpeed = speed;
	}

	private void newCircle() {
		long currentTime = System.currentTimeMillis();
		if (currentTime - mLastCreateTime < mSpeed) return;
		Circle circle = new Circle();
		mCircleList.add(circle);
		invalidate();
		mLastCreateTime = currentTime;
	}

	private class Circle {

		private long mCreateTime;

		Circle() {
			mCreateTime = System.currentTimeMillis();
		}

		int getAlpha() {
			float percent = (getCurrentRadius() - mInitialRadius) / (mMaxRadius - mInitialRadius);
			return (int) (255 - mInterpolator.getInterpolation(percent) * 255);
		}

		float getCurrentRadius() {
			float percent = (System.currentTimeMillis() - mCreateTime) * 1.0f / mDuration;
			return mInitialRadius + mInterpolator.getInterpolation(percent) * (mMaxRadius - mInitialRadius);
		}
	}
}
