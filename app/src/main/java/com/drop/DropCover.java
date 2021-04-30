package com.drop;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class DropCover extends SurfaceView implements SurfaceHolder.Callback {

	private static final int EXPLOSION_SIZE = 200;
	private int mMaxDistance = 150;

	private ExplosionUpdateThread mThread;
	private Explosion mExplosion;

	private float mBaseX;
	private float mBaseY;

	private float mTargetX;
	private float mTargetY;

	private Bitmap mDest;
	private Paint mPaint = new Paint();

	private float mRadius = 0;
	private float mStrokeWidth = 20;
	private boolean isDraw = true;
	private float mStatusBarHeight = 0;
	private OnDragCompeteListener mOnDragCompeteListener;

	public interface OnDragCompeteListener {

		void onDrag();

		void onDownDrag(boolean isDownDrag);//解决swipeRefreshLayout冲突
	}

	@SuppressLint("NewApi")
	public DropCover(Context context) {
		super(context);
		this.setBackgroundColor(Color.TRANSPARENT);
		this.setZOrderOnTop(true);
		getHolder().setFormat(PixelFormat.TRANSPARENT);
		getHolder().addCallback(this);
		setFocusable(false);
		setClickable(false);
		mPaint.setAntiAlias(true);
		setLayerType(View.LAYER_TYPE_SOFTWARE, null);
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return false;
	}

	private void drawDrop() {
		Canvas canvas = getHolder().lockCanvas();
		if (canvas != null) {
			canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
			if (isDraw) {
				double distance = Math.sqrt(Math.pow(mBaseX - mTargetX, 2) + Math.pow(mBaseY - mTargetY, 2));
				mPaint.setColor(Color.parseColor("#FFF74D32"));
				if (distance < mMaxDistance) {
					mStrokeWidth = (float) ((1 - distance / mMaxDistance) * mRadius);
					mPaint.setStrokeWidth(mStrokeWidth);
					canvas.drawCircle(mBaseX, mBaseY, mStrokeWidth / 2, mPaint);
					drawBezier(canvas);
				}
				canvas.drawBitmap(mDest, mTargetX, mTargetY, mPaint);
			}
			getHolder().unlockCanvasAndPost(canvas);
		}
	}

	private void drawBezier(Canvas canvas) {
		mPaint.setStyle(Paint.Style.STROKE);

		Point[] points = calculate(new Point(mBaseX, mBaseY),
				new Point(mTargetX + mDest.getWidth() / 2f, mTargetY + mDest.getHeight() / 2f));

		Path path1 = new Path();
		path1.moveTo(points[0].x, points[0].y);
		path1.quadTo((points[2].x + points[3].x) / 2, (points[2].y + points[3].y) / 2, points[1].x, points[1].y);
		canvas.drawPath(path1, mPaint);

		Path path2 = new Path();
		path2.moveTo(points[2].x, points[2].y);
		path2.quadTo((points[0].x + points[1].x) / 2, (points[0].y + points[1].y) / 2, points[3].x, points[3].y);
		canvas.drawPath(path2, mPaint);
	}

	private Point[] calculate(Point start, Point end) {
		float a = end.x - start.x;
		float b = end.y - start.y;

		float x = (float) Math.sqrt(a * a / (a * a + b * b) * (mStrokeWidth / 2f) * (mStrokeWidth / 2f));
		float y = -b / a * x;

		System.out.println("x:" + x + " y:" + y);

		Point[] result = new Point[4];

		result[0] = new Point(start.x + x, start.y + y);
		result[1] = new Point(end.x + x, end.y + y);

		result[2] = new Point(start.x - x, start.y - y);
		result[3] = new Point(end.x - x, end.y - y);

		return result;
	}

	public void setTarget(Bitmap dest) {
		mDest = dest;
		mRadius = dest.getWidth() / 2;
		mStrokeWidth = mRadius;
	}

	public void init(float x, float y) {
		mBaseX = x + mDest.getWidth() / 2;
		mBaseY = y - mDest.getWidth() / 2;
		mTargetX = x;
		mTargetY = y - mStatusBarHeight;

		isDraw = true;
		drawDrop();
	}

	public void update(float x, float y) {

		mTargetX = x;
		mTargetY = y - mStatusBarHeight;
		drawDrop();
	}

	public void clearDatas() {
		mBaseX = -1;
		mBaseY = -1;
		mTargetX = -1;
		mTargetY = -1;
		mDest = null;
	}

	public void clearViews() {
		if (getParent() != null) {
			CoverManager.getInstance().getWindowManager().removeView(this);
		}
	}

	public void finish(View target, float x, float y) {
		double distance = Math.sqrt(Math.pow(mBaseX - mTargetX, 2) + Math.pow(mBaseY - mTargetY, 2));

		clearDatas();
		Canvas canvas = getHolder().lockCanvas();
		if (canvas != null) {
			canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
			getHolder().unlockCanvasAndPost(canvas);
		}
		if (distance > mMaxDistance) {
			if (mOnDragCompeteListener != null)
				mOnDragCompeteListener.onDrag();

			initExplosion(x, y);

			mThread = new ExplosionUpdateThread(getHolder(), this);
			mThread.setRunning(true);
			mThread.start();
		}
		else {
			clearViews();
			target.setVisibility(View.VISIBLE);
		}

		isDraw = false;
	}

	public void setStatusBarHeight(int statusBarHeight) {
		mStatusBarHeight = statusBarHeight;
	}

	public void setOnDragCompeteListener(OnDragCompeteListener onDragCompeteListener) {
		mOnDragCompeteListener = onDragCompeteListener;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		drawDrop();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if (mThread != null) {
			mThread.setRunning(false);
			mThread = null;
		}
	}

	public void initExplosion(float x, float y) {
		if (mExplosion == null || mExplosion.getState() == Explosion.STATE_DEAD) {
			mExplosion = new Explosion(EXPLOSION_SIZE, (int) x, (int) y);
		}
	}

	public boolean render(Canvas canvas) {
		boolean isAlive = false;
		canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
		canvas.drawColor(Color.argb(0, 0, 0, 0)); // To make canvas transparent
		if (mExplosion != null) {
			isAlive = mExplosion.draw(canvas);
		}
		return isAlive;
	}

	public void update() {
		if (mExplosion != null && mExplosion.isAlive()) {
			mExplosion.update(getHolder().getSurfaceFrame());
		}
	}

	class Point {

		float x, y;

		public Point(float x, float y) {
			this.x = x;
			this.y = y;
		}
	}
}
