package com.haibuzou.datepicker.calendar.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Scroller;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.schedule.utils.ScheduleUtil;
import com.haibuzou.datepicker.calendar.bizs.calendars.DPCManager;
import com.haibuzou.datepicker.calendar.bizs.decors.DPDecor;
import com.haibuzou.datepicker.calendar.bizs.themes.DPTManager;
import com.haibuzou.datepicker.calendar.cons.DPMode;
import com.haibuzou.datepicker.calendar.entities.DPInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class WeekView extends View {

	private final Region[][] monthRegionsFour = new Region[4][7];
	private final Region[][] monthRegionsFive = new Region[5][7];
	private final Region[][] monthRegionsSix = new Region[6][7];

	private final DPInfo[][] infoFour = new DPInfo[4][7];
	private final DPInfo[][] infoFive;
	private final DPInfo[][] infoSix = new DPInfo[6][7];

	private final Map<String, List<Region>> regionSelected = new HashMap<>();

	private DPCManager mCManager = DPCManager.getInstance();
	private DPTManager mTManager = DPTManager.getInstance();

	protected Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG
			| Paint.DITHER_FLAG | Paint.LINEAR_TEXT_FLAG);
	protected Paint todayPaint = new Paint(Paint.ANTI_ALIAS_FLAG
			| Paint.DITHER_FLAG | Paint.LINEAR_TEXT_FLAG);
	private Scroller mScroller;
	private DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator();
	private AccelerateInterpolator accelerateInterpolator = new AccelerateInterpolator();

	private OnWeekViewChangeListener mWeekViewChangeListener;
	private MonthView.OnDatePickedListener mDatePickedListener;
	private OnWeekDateClick mWeekClickListener;
	private ScaleAnimationListener scaleAnimationListener;

	private DPMode mDPMode = DPMode.MULTIPLE;
	private SlideMode mSlideMode;
	private DPDecor mDPDecor;

	private int circleRadius;
	public int indexYear, indexMonth;
	public int centerYear, centerMonth;
	private int width, height;
	private int sizeDecor, sizeDecor2x, sizeDecor3x;
	private int lastPointX, lastPointY;
	private int lastMoveX, lastMoveY;
	private int animZoomOut1, animZoomIn1, animZoomOut2;

	private float sizeTextGregorian, sizeTextFestival;
	private float offsetYFestival1, offsetYFestival2;
	public int num = 5;
	private int count = 5;

	private boolean isNewEvent, isFestivalDisplay = true,
			isHolidayDisplay = true, isTodayDisplay = true,
			isDeferredDisplay = true;

	public Map<String, BGCircle> cirApr = new HashMap<>();
	public Map<String, BGCircle> cirDpr = new HashMap<>();

	private List<String> dateSelected = new ArrayList<>();

	private boolean isreset;
	private int year2;
	private int month2;
	private int day2;

	private boolean isDefineRegion;

	private Paint mRedPaint;

	/**
	 * 屏幕宽度
	 */
	private int screendWidth;

	/**
	 * 月视图移动
	 */
	public int move = 0;

	/**
	 * 周视图的长度
	 */
	private int zeroLineCount;

	public boolean isUseCache = false;

	private OnWeekViewSlide onWeekViewSlide;

	public interface OnWeekViewSlide {

		void onWeekViewSlide(int line);
	}

	private GetLine getLint;

	public interface GetLine {

		int getLine();
	}

	private GetLineCount getLineCount;

	public interface GetLineCount {

		int getLineCount();
	}

	private SetScrollLayoutLine setScrollLayoutLine;

	public interface SetScrollLayoutLine {

		void setScrollLayoutLine(int line);

		void reduceScrollLayoutLine(int num);
	}

	public void setScrollLayoutLineListener(SetScrollLayoutLine setScrollLayoutLine) {
		this.setScrollLayoutLine = setScrollLayoutLine;
	}

	/**
	 * 用于判断滑动下一月
	 */
	private boolean isChangeNextMonth = false;

	private boolean isMove = true;//是否允许滑动

	public void setMove(boolean isMove) {
		this.isMove = isMove;
	}

	private SetMonthViewIsUseCache setMonthViewIsUseCache;

	public interface SetMonthViewIsUseCache {

		void setMonthViewIsUseCache(boolean isUseCache);
	}

	public void setMonthViewIsUseCacheListener(SetMonthViewIsUseCache setMonthViewIsUseCache) {
		this.setMonthViewIsUseCache = setMonthViewIsUseCache;
	}


	private int tmpI, tmpJ;


	private OnWeekViewMoveToLastListener mWeekViewMoveToLastListener;

	// 用于解决 周视图移动，从只有5行的月份左移动到6行的月份（上个月），底部会有东西漏出来
	public interface OnWeekViewMoveToLastListener {

		void onWeekViewMoveToLast();
	}

	public void setWeekViewMoveToLastListener(OnWeekViewMoveToLastListener weekViewMoveToLastListener) {
		this.mWeekViewMoveToLastListener = weekViewMoveToLastListener;
	}

	public WeekView(Context context) {
		this(context, null);
	}

	public WeekView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public WeekView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			scaleAnimationListener = new ScaleAnimationListener();
		}
		mScroller = new Scroller(context);
		mPaint.setTextAlign(Paint.Align.CENTER);

		WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
		screendWidth = wm.getDefaultDisplay().getWidth();
		mRedPaint = new Paint();
		mRedPaint.setColor(Color.parseColor("#F25749"));
		mRedPaint.setAntiAlias(true);
		infoFive = new DPInfo[5][7];
	}

	public void setCirclePaintColor(int color) {
		if (mRedPaint != null) mRedPaint.setColor(color);
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		return super.onSaveInstanceState();
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		super.onRestoreInstanceState(state);
	}

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			invalidate();
		}
	}

	public void reset(int year2, int month2, int day2, int offsetX) {
		isreset = true;
		this.year2 = year2;
		this.month2 = month2;
		this.day2 = day2;
		setDate(year2, month2);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				mScroller.forceFinished(true);
				mSlideMode = null;
				isNewEvent = true;
				lastPointX = (int) event.getX();
				lastPointY = (int) event.getY();
				return true;
			case MotionEvent.ACTION_MOVE:
				isUseCache = true;
				if (setMonthViewIsUseCache != null) {
					setMonthViewIsUseCache.setMonthViewIsUseCache(true);
				}
				if (isNewEvent) {
					if (Math.abs(lastPointX - event.getX()) > 25) {
						mSlideMode = SlideMode.HOR;
						isNewEvent = false;
					}
				}
				if (mSlideMode == SlideMode.HOR) {
					int totalMoveX = (int) (lastPointX - event.getX()) + lastMoveX;
					// 第一周 不能像左滑动,最后一周不能像右滑动
					if (getLint != null && getLineCount != null) {
						int line = getLint.getLine();
//					if (line == 0 && (lastPointX - event.getX()) < 0 || line == getLineCount.getLineCount()-1 && (lastPointX - event.getX()) > 0) {
						isDefineRegion = false;
						if (isMove) smoothScrollTo(totalMoveX, indexYear * height);
					}
				}
				break;
			case MotionEvent.ACTION_UP:
				isChangeNextMonth = false;
				if (mSlideMode == SlideMode.VER) {
				}
				else if (mSlideMode == SlideMode.HOR) {
					if (Math.abs(lastPointX - event.getX()) > screendWidth / 7 && !isDefineRegion) {
						if (isMove == false) return true;
						if (lastPointX > event.getX()) {
							move++;
							if (null != mWeekViewChangeListener && num + move > getIndex(centerYear, centerMonth)) {
								mWeekViewChangeListener.onWeekViewChange(true);
								// add 2016-6-27 09:15:15
								indexMonth++;
								centerMonth = (centerMonth + 1) % 13;
								if (centerMonth == 0) {
									centerMonth = 1;
									centerYear++;
								}
								buildRegion();
								isChangeNextMonth = true;
							}
							else {
								isChangeNextMonth = false;
							}
							if (onWeekViewSlide != null) {
								onWeekViewSlide.onWeekViewSlide(1);
							}
							moveForwad2();
						}
						else {
							move--;
							if (null != mWeekViewChangeListener && num + move < 0 && getLint.getLine() == 0) {
								mWeekViewChangeListener.onWeekViewChange(false);
								// add 2016-6-25 16:20:39
								indexMonth--;
								centerMonth = (centerMonth - 1) % 12;
								if (centerMonth == 0) {
									centerMonth = 12;
									centerYear--;
								}
								buildRegion();
							}

							if (onWeekViewSlide != null) {
								onWeekViewSlide.onWeekViewSlide(-1);
							}
							moveBack2();
						}
						buildRegion();
						lastMoveX = width * indexMonth;
						checkLastDateAndNextDate((int) event.getX(), (int) event.getY(), false);
					}
					else {
						defineRegion((int) event.getX(), (int) event.getY());
						smoothScrollTo(width * indexMonth, indexYear * height);
					}
				}
				else {
					defineRegion((int) event.getX(), (int) event.getY());
					checkLastDateAndNextDate((int) event.getX(), (int) event.getY(), true);
				}
				break;
			default:
				defineRegion((int) event.getX(), (int) event.getY());
				smoothScrollTo(width * indexMonth, indexYear * height);
				break;
		}
		return super.onTouchEvent(event);
	}

	/**
	 * 用来检测当选中上个月日期或者下个月日期时，月视图要跳转
	 */
	private void checkLastDateAndNextDate(int x, int y, boolean isDefine) {
		DPInfo[][] aa = mCManager.obtainDPInfo(centerYear, centerMonth, year2, month2, day2, isUseCache);
		if (aa[tmpI][tmpJ].isNextDate) {
			if (mWeekViewChangeListener != null) {
				mWeekViewChangeListener.onWeekViewChange(true);
			}
			setScrollLayoutLine.setScrollLayoutLine(0);
			move = 0;
			num = 0;
			indexMonth++;
			centerMonth = (centerMonth + 1) % 13;
			if (centerMonth == 0) {
				centerMonth = 1;
				centerYear++;
			}
			lastMoveX = width * indexMonth;
			buildRegion();
			smoothScrollTo(width * indexMonth, indexYear * height);
			if (isDefine) {
				defineRegion(x, y);
			}
			return;
		}
		//

		DPInfo[][] bb = mCManager.obtainDPInfo(centerYear, centerMonth, year2, month2, day2, isUseCache);
		if (aa[tmpI][tmpJ].isLastDate) {
			if (mWeekViewChangeListener != null) {
				mWeekViewChangeListener.onWeekViewChange(false);
			}
			indexMonth--;
			centerMonth = (centerMonth - 1) % 12;
			if (centerMonth == 0) {
				centerMonth = 12;
				centerYear--;
			}
			setScrollLayoutLine.setScrollLayoutLine(getIndex(centerYear, centerMonth));
			move = 0;
			num = getIndex(centerYear, centerMonth);
			lastMoveX = width * indexMonth;
			buildRegion();
			smoothScrollTo(width * indexMonth, indexYear * height);
			if (isDefine) {
				defineRegion(x, y);
			}
			else {
				defineRegion((screendWidth / 7) / 2, getMeasuredHeight() / 2);
			}

		}

		if (mWeekViewMoveToLastListener != null && getIndex(centerYear, centerMonth) == 5) {
			mWeekViewMoveToLastListener.onWeekViewMoveToLast();
		}
	}

	// 得到最后一行的位置
	private int getIndex(int year, int month) {
		int index = 0;
		DPInfo[][] dpInfo = mCManager.obtainDPInfo(year, month, year2, month2, day2, isUseCache);
		if (dpInfo[5][0].isNextDate && dpInfo[4][6].isNextDate) {
			index = 4;
		}
		else if (dpInfo[5][0].isNextDate && !dpInfo[4][6].isNextDate) {
			index = 4;
		}
		else if (!dpInfo[5][0].isNextDate) {
			index = 5;
		}
		return index;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int measureWidth = MeasureSpec.getSize(widthMeasureSpec);
		setMeasuredDimension(measureWidth, (int) (measureWidth * 5F / 7F) / count);
	}

	public void moveForwad() {
		indexMonth++;
		centerMonth = (centerMonth + 1) % 13;
		if (centerMonth == 0) {
			centerMonth = 1;
			centerYear++;
		}
		buildRegion();
		smoothScrollTo(width * indexMonth, indexYear * height);
		lastMoveX = width * indexMonth;
		requestLayout();
	}

	/**
	 * add by yj  2016-6-18 13:37:13
	 */
	public void moveForwad2() {
		buildRegion();
		smoothScrollTo(width * indexMonth, indexYear * height);
		lastMoveX = width * indexMonth;
		requestLayout();
	}

	/**
	 * add by yj 2016-6-18 13:40:08
	 */
	//滑动back
	public void moveBack2() {
		buildRegion();
		smoothScrollTo(width * indexMonth, indexYear * height);
		lastMoveX = width * indexMonth;
		requestLayout();
	}

	//滑动back
	public void moveBack() {
		indexMonth--;
		centerMonth = (centerMonth - 1) % 12;
		if (centerMonth == 0) {
			centerMonth = 12;
			centerYear--;
		}
		buildRegion();
		smoothScrollTo(width * indexMonth, indexYear * height);
		lastMoveX = width * indexMonth;
		requestLayout();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldW, int oldH) {
		width = w;
		height = h;

		mRedPaint.setTextSize(width / 30F);

		int cellW = (int) (w / 7F);
		int cellH4 = (int) (h / 4F);
		int cellH5 = (int) (h / 5F);
		int cellH6 = (int) (h / 6F);

		circleRadius = (int) (cellW / 1.60F);

		animZoomOut1 = (int) (cellW * 1.2F);
		animZoomIn1 = (int) (cellW * 0.8F);
		animZoomOut2 = (int) (cellW * 1.1F);

		sizeDecor = (int) (cellW / 3F);
		sizeDecor2x = sizeDecor * 2;
		sizeDecor3x = sizeDecor * 3;

		sizeTextGregorian = width / 24F;
		mPaint.setTextSize(sizeTextGregorian);

		float heightGregorian = mPaint.getFontMetrics().bottom - mPaint.getFontMetrics().top;
		sizeTextFestival = width / 41F;
		mPaint.setTextSize(sizeTextFestival);

		float heightFestival = mPaint.getFontMetrics().bottom - mPaint.getFontMetrics().top;
		offsetYFestival1 = (((Math.abs(mPaint.ascent() + mPaint.descent())) / 2F) + heightFestival / 2F + heightGregorian / 2F) / 2F;
		offsetYFestival2 = offsetYFestival1 * 2F;

		for (int i = 0; i < monthRegionsFour.length; i++) {
			for (int j = 0; j < monthRegionsFour[i].length; j++) {
				Region region = new Region();
				region.set(j * cellW, i * cellH4, cellW + (j * cellW), cellW + (i * cellH4));
				monthRegionsFour[i][j] = region;
			}
		}
		for (int i = 0; i < monthRegionsFive.length; i++) {
			for (int j = 0; j < monthRegionsFive[i].length; j++) {
				Region region = new Region();
				region.set(j * cellW, i * cellH5, cellW + (j * cellW), cellW + (i * cellH5));
				monthRegionsFive[i][j] = region;
			}
		}
		for (int i = 0; i < monthRegionsSix.length; i++) {
			for (int j = 0; j < monthRegionsSix[i].length; j++) {
				Region region = new Region();
				region.set(j * cellW, i * cellH6, cellW + (j * cellW), cellW + (i * cellH6));
				monthRegionsSix[i][j] = region;
			}
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawColor(mTManager.colorBG());
		if (isreset) {  // 第一行
			drawBGCircle(canvas);
			draw(canvas, width * indexMonth, indexYear * height, centerYear, centerMonth, move);
		}
	}

	private void drawBGCircle(Canvas canvas) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			for (String s : cirDpr.keySet()) {
				BGCircle circle = cirDpr.get(s);
				drawBGCircle(canvas, circle);
			}
		}
		for (String s : cirApr.keySet()) {
			BGCircle circle = cirApr.get(s);
			drawBGCircle(canvas, circle);
		}
	}

	private void drawBGCircle(Canvas canvas, BGCircle circle) {
		canvas.save();
		canvas.translate(circle.getX() - circle.getRadius() / 2, circle.getY() - circle.getRadius() / 2);
		circle.getShape().getShape().resize(circle.getRadius(), circle.getRadius());
		circle.getShape().draw(canvas);
		canvas.restore();
	}

	private void draw(Canvas canvas, int x, int y, int year, int month) {
		canvas.save();
		canvas.translate(x, y);
		DPInfo[][] info = null;
		if (isreset) {
			info = mCManager.obtainDPInfo(year, month, year2, month2, day2, isUseCache);
		}
		DPInfo[][] result;
		Region[][] tmp;
		if (TextUtils.isEmpty(info[4][0].strG)) {
			tmp = monthRegionsFour;
			arrayClear(infoFour);
			result = arrayCopy(info, infoFour);
		}
		else if (TextUtils.isEmpty(info[5][0].strG)) {
			tmp = monthRegionsFive;
			arrayClear(infoFive);
			result = arrayCopy(info, infoFive);
		}
		else {
			tmp = monthRegionsSix;
			arrayClear(infoSix);
			result = arrayCopy(info, infoSix);
		}

		//test
		String a = "";
		for (int i = 0; i < result.length; i++) {
			for (int j = 0; j < result[i].length; j++) {
				a += result[i][j].strG + " ";
			}
			a += "\n";
		}

		// for (int i = 0; i < result.length; i++) {
		if (num >= result.length) {
			//5行与6行之间切换导致数组下标越界
			for (int j = 0; j < result[result.length - 1].length; j++) {
				draw(canvas, tmp[0][j].getBounds(), info[result.length - 1][j]);
			}
		}
		else {
			for (int j = 0; j < result[num].length; j++) {
				draw(canvas, tmp[0][j].getBounds(), info[num][j]);
			}
		}

		// }
		canvas.restore();
	}

	/**
	 * edit by yj
	 * @param d 画月
	 */
	private void draw(Canvas canvas, int x, int y, int year, int month, int d) {
		canvas.save();
		canvas.translate(x, y);
		DPInfo[][] info = null;
		if (isreset) {
			info = mCManager.obtainDPInfo(year, month, year2, month2, day2, isUseCache);
		}

		DPInfo[][] result;
		Region[][] tmp;
		if (TextUtils.isEmpty(info[4][0].strG) || info[4][0].isNextDate) {
			tmp = monthRegionsFour;
			arrayClear(infoFour);
			result = arrayCopy(info, infoFour);
		}
		else if (TextUtils.isEmpty(info[5][0].strG) || info[5][0].isNextDate) {
			tmp = monthRegionsFive;
			arrayClear(infoFive);
			result = arrayCopy(info, infoFive);
		}
		else {
			tmp = monthRegionsSix;
			arrayClear(infoSix);
			result = arrayCopy(info, infoSix);
		}

		zeroLineCount = 0;
		for (int i = 0; i < result[0].length; i++) {
			if (!"".equals(result[0][i].strG)) {
				zeroLineCount += 1;
			}
		}
		int index = num + d;
		DPInfo[][] dpInfos = mCManager.obtainDPInfo(year, month, year2, month2, day2, isUseCache);

		// 处于当月最低行右滑,要先获得当月的行数,即上个月
		int yearL = centerYear;
		int monthL = centerMonth;
		if (isChangeNextMonth) {
			if (centerMonth == 1) {
				monthL = 12;
				yearL = centerYear - 1;
			}
			else {
				monthL -= 1;
			}
		}
		if (num + d < 0) {
			DPInfo[][] dpInfo = mCManager.obtainDPInfo(centerYear, centerMonth, year2, month2, day2, isUseCache);
			if (dpInfo[5][0].isNextDate && dpInfo[4][6].isNextDate) {
				index = 3;
			}
			else if (dpInfo[5][0].isNextDate && !dpInfo[4][6].isNextDate) {
				index = 4;
			}
			else if (!dpInfo[5][0].isNextDate) {
				index = 4;
			}
			num = index;
			move = 0;
		}
		else if (num + d > getIndex(yearL, monthL)) {
			DPInfo[][] dpInfo = mCManager.obtainDPInfo(centerYear, centerMonth, year2, month2, day2, isUseCache);
			if (dpInfo[0][0].isLastDate) {
				num = 1;
			}
			else {
				num = 0;
			}
			move = 0;
			index = num;
		}

		for (int j = 0; j < result[num].length; j++) {
			draw(canvas, tmp[0][j].getBounds(), dpInfos[index][j]);
		}

		canvas.restore();
		// 画左边
		drawLeft(canvas, width * (indexMonth - 1), height * indexYear, year, month, index);
		drawRight(canvas, width * (indexMonth + 1), height * indexYear, year, month, index);
		// 画右边
	}

	private void drawLeft(Canvas canvas, int x, int y, int year, int month, int index) {
		canvas.save();
		canvas.translate(x, y);
		DPInfo[][] dpInfos = null;
		Region[][] tmp;
		int line = 0;
		if (index != 0) {
			dpInfos = mCManager.obtainDPInfo(year, month, year2, month2, day2, isUseCache);
			line = index - 1;
		}
		else {
			month -= 1;
			if (month == 0) {
				month = 12;
				year -= 1;
			}
			dpInfos = mCManager.obtainDPInfo(year, month, year2, month2, day2, isUseCache);
			if (dpInfos[5][0].isNextDate && dpInfos[4][6].isNextDate) {
				line = 3;
			}
			else if (dpInfos[5][0].isNextDate && !dpInfos[4][6].isNextDate) {
				line = 4;
			}
			else if (!dpInfos[5][0].isNextDate) {
				line = 4;
			}
		}
		if (dpInfos[4][0].isNextDate) {
			tmp = monthRegionsFour;
		}
		else if (dpInfos[5][0].isNextDate) {
			tmp = monthRegionsFive;
		}
		else {
			tmp = monthRegionsSix;
		}

		for (int j = 0; j < 7; j++) {
			draw(canvas, tmp[0][j].getBounds(), dpInfos[line][j]);
		}
		canvas.restore();
	}

	private void drawRight(Canvas canvas, int x, int y, int year, int month, int index) {
		canvas.save();
		canvas.translate(x, y);
		DPInfo[][] dpInfos = null;
		Region[][] tmp;
		int line = 0;

		// 最后一行位置
		int position = 0;
		dpInfos = mCManager.obtainDPInfo(year, month, year2, month2, day2, isUseCache);
		if (dpInfos[5][0].isNextDate && dpInfos[4][6].isNextDate) {
			position = 4;
		}
		else if (dpInfos[5][0].isNextDate && !dpInfos[4][6].isNextDate) {
			position = 5;
		}
		else if (!dpInfos[5][0].isNextDate) {
			position = 5;
		}

		if (position != index) {
			line = index + 1;
		}
		else {
			line = 1;
			month += 1;
			if (month == 13) {
				month = 1;
				year += 1;
			}
		}
		dpInfos = mCManager.obtainDPInfo(year, month, year2, month2, day2, isUseCache);

		if (dpInfos[4][0].isNextDate) {
			tmp = monthRegionsFour;
		}
		else if (dpInfos[5][0].isNextDate) {
			tmp = monthRegionsFive;
		}
		else {
			tmp = monthRegionsSix;
		}

		for (int j = 0; j < 7; j++) {
			draw(canvas, tmp[0][j].getBounds(), dpInfos[line][j]);
		}
		canvas.restore();
	}


	public void setLine(int num) {
		this.num = num;
	}

	public void setCount(int count) {
		this.count = count;
		requestLayout();
	}

	private void draw(Canvas canvas, Rect rect, DPInfo info) {
		drawBG(canvas, rect, info);
		drawGregorian(canvas, rect, info.strG, info.isWeekend, info.isToday, info.isLastDate, info.isNextDate, info.isChoosed);
		if (isFestivalDisplay) {
			drawFestival(canvas, rect, info.strF, info.isFestival, info.isToday, info.isLastDate, info.isNextDate, info.isChoosed,
					info.isWeekend);
		}
		drawDecor(canvas, rect, info);
	}

	private void drawBG(Canvas canvas, Rect rect, DPInfo info) {
		if (null != mDPDecor && info.isDecorBG) {
			mDPDecor.drawDecorBG(canvas, rect, mPaint, centerYear + "-" + centerMonth + "-" + info.strG);
		}
		if (info.isToday && isTodayDisplay && !info.isLastDate && !info.isNextDate) {
			drawBGToday(canvas, rect, info.isChoosed);
		}
	}

	// 因为今天的样式需要自定义所以 重新换了Paint
	private void drawBGToday(Canvas canvas, Rect rect, boolean isChoose) {
		if (isChoose) {
			todayPaint.setColor(mTManager.colorBGCircle());
		}
		else {
			if (!ScheduleUtil.isChoosed()) {
				todayPaint.setColor(mTManager.colorBGCircle());
			}
			else {
				todayPaint.setColor(mTManager.colorToday());
			}
		}
		todayPaint.setStyle(Paint.Style.FILL);
		canvas.drawCircle(rect.centerX(), rect.centerY(), circleRadius / 2F, todayPaint);
	}

	private void drawBGHoliday(Canvas canvas, Rect rect, boolean isHoliday) {
		mPaint.setColor(mTManager.colorHoliday());
		if (isHoliday) {
			canvas.drawCircle(rect.centerX(), rect.centerY(), circleRadius / 2F, mPaint);
		}
	}

	private void drawBGDeferred(Canvas canvas, Rect rect, boolean isDeferred) {
		mPaint.setColor(mTManager.colorDeferred());
		if (isDeferred) {
			canvas.drawCircle(rect.centerX(), rect.centerY(), circleRadius / 2F, mPaint);
		}
	}

	private void drawGregorian(Canvas canvas, Rect rect, String str,
			boolean isWeekend, boolean isToday, boolean isLastDate, boolean isNextDate, boolean isChoosed) {
		mPaint.setTextSize(sizeTextGregorian);

		if (isLastDate || isNextDate) {
			mPaint.setColor(Color.LTGRAY);
		}
		else if (isWeekend && !isToday && !isChoosed) {
			mPaint.setColor(mTManager.colorWeekend());
		}
		else if (isToday && isTodayDisplay || isChoosed) {
			mPaint.setColor(mTManager.colorTodayText());
		}
		else {
			mPaint.setColor(mTManager.colorG());
		}

		float y = rect.centerY();
		if (!isFestivalDisplay) {
			y = rect.centerY() + Math.abs(mPaint.ascent()) - (mPaint.descent() - mPaint.ascent()) / 2F;
		}
		canvas.drawText(str, rect.centerX(), y, mPaint);
	}

	private void drawFestival(Canvas canvas, Rect rect, String str,
			boolean isFestival, boolean isToday, boolean isLastDate, boolean isNextDate, boolean isChoosed, boolean isWeek) {
		mPaint.setTextSize(sizeTextFestival);
		if (isLastDate || isNextDate) {
			mPaint.setColor(Color.LTGRAY);
		}
		else if (isToday || isChoosed) {
			mPaint.setColor(Color.WHITE);
		}
		else if (isWeek) {
			mPaint.setColor(mTManager.colorWeekend());
		}
		else {
			mPaint.setColor(mTManager.colorL());
		}
//		if (isFestival) {
//			mPaint.setColor(mTManager.colorF());
//		} else {
//			mPaint.setColor(mTManager.colorL());
//		}
		if (str.contains("&")) {
			String[] s = str.split("&");
			String str1 = s[0];
			if (mPaint.measureText(str1) > rect.width()) {
				float ch = mPaint.measureText(str1, 0, 1);
				int length = (int) (rect.width() / ch);
				canvas.drawText(str1.substring(0, length), rect.centerX(), rect.centerY() + offsetYFestival1, mPaint);
				canvas.drawText(str1.substring(length), rect.centerX(), rect.centerY() + offsetYFestival2, mPaint);
			}
			else {
				canvas.drawText(str1, rect.centerX(), rect.centerY() + offsetYFestival1, mPaint);
				String str2 = s[1];
				if (mPaint.measureText(str2) < rect.width()) {
					canvas.drawText(str2, rect.centerX(), rect.centerY() + offsetYFestival2, mPaint);
				}
			}
		}
		else {
			if (mPaint.measureText(str) > rect.width()) {
				float ch = 0.0F;
				for (char c : str.toCharArray()) {
					float tmp = mPaint.measureText(String.valueOf(c));
					if (tmp > ch) {
						ch = tmp;
					}
				}
				int length = (int) (rect.width() / ch);
				canvas.drawText(str.substring(0, length), rect.centerX(), rect.centerY() + offsetYFestival1, mPaint);
				canvas.drawText(str.substring(length), rect.centerX(), rect.centerY() + offsetYFestival2, mPaint);
			}
			else {
				canvas.drawText(str, rect.centerX(), rect.centerY() + offsetYFestival1, mPaint);
			}
		}
	}

	private void drawDecor(Canvas canvas, Rect rect, DPInfo info) {
		if (!TextUtils.isEmpty(info.strG)) {
			String data = centerYear + "-" + centerMonth + "-" + info.strG;
			if (null != mDPDecor && info.isDecorTL) {
				canvas.save();
				canvas.clipRect(rect.left, rect.top, rect.left + sizeDecor, rect.top + sizeDecor);
				mDPDecor.drawDecorTL(canvas, canvas.getClipBounds(), mPaint, data);
				canvas.restore();
			}
			if (null != mDPDecor && info.isDecorT) {
				canvas.save();
				canvas.clipRect(rect.left + sizeDecor, rect.top, rect.left + sizeDecor2x, rect.top + sizeDecor);
				mDPDecor.drawDecorT(canvas, canvas.getClipBounds(), mPaint, data);
				canvas.restore();
			}
			if (info.isDecorTR) {
				canvas.save();
				canvas.clipRect(rect.left + sizeDecor2x, rect.top, rect.left + sizeDecor3x, rect.top + sizeDecor);
				float radius = width / 7F / 18F;
				FELog.i("-->>>>>TR-date:" + data);
				canvas.drawCircle(rect.left + sizeDecor3x - (4 * radius), rect.top + (4 * radius), radius, mRedPaint);
				canvas.restore();
			}
			if (null != mDPDecor && info.isDecorL) {
				canvas.save();
				canvas.clipRect(rect.left, rect.top + sizeDecor, rect.left + sizeDecor, rect.top + sizeDecor2x);
				mDPDecor.drawDecorL(canvas, canvas.getClipBounds(), mPaint, data);
				canvas.restore();
			}
			if (null != mDPDecor && info.isDecorR) {
				canvas.save();
				canvas.clipRect(rect.left + sizeDecor2x, rect.top + sizeDecor, rect.left + sizeDecor3x, rect.top + sizeDecor2x);
				mDPDecor.drawDecorR(canvas, canvas.getClipBounds(), mPaint, data);
				canvas.restore();
			}
		}
	}

	List<String> getDateSelected() {
		return dateSelected;
	}

	public void setWeekViewChangeListener(OnWeekViewChangeListener weekViewChangeListener) {
		this.mWeekViewChangeListener = weekViewChangeListener;
	}

	public void setOnWeekClickListener(OnWeekDateClick onWeekClick) {
		this.mWeekClickListener = onWeekClick;
	}


	public void setDatePickedListener(MonthView.OnDatePickedListener datePickedListener) {
		this.mDatePickedListener = datePickedListener;
	}

	public void setDPMode(DPMode mode) {
		this.mDPMode = mode;
	}

	public void setDPDecor(DPDecor decor) {
		this.mDPDecor = decor;
	}

	public DPMode getDPMode() {
		return mDPMode;
	}

	public void setDate(int year, int month) {
		centerYear = year;
		centerMonth = month;
		indexYear = 0;
		indexMonth = 0;
		buildRegion();
		requestLayout();
		invalidate();

		lastPointX = 0;
		lastMoveX = 0;
		lastMoveY = 0;
		lastPointY = 0;
//		scrollTo(0, 0);
		smoothScrollTo(width * indexMonth, indexYear * height);
	}

	public void setFestivalDisplay(boolean isFestivalDisplay) {
		this.isFestivalDisplay = isFestivalDisplay;
	}

	public void setTodayDisplay(boolean isTodayDisplay) {
		this.isTodayDisplay = isTodayDisplay;
	}

	public void setHolidayDisplay(boolean isHolidayDisplay) {
		this.isHolidayDisplay = isHolidayDisplay;
	}

	public void setDeferredDisplay(boolean isDeferredDisplay) {
		this.isDeferredDisplay = isDeferredDisplay;
	}

	private void smoothScrollTo(int fx, int fy) {
		int dx = fx - mScroller.getFinalX();
		int dy = fy - mScroller.getFinalY();
		smoothScrollBy(dx, dy);
	}

	private void smoothScrollBy(int dx, int dy) {
		mScroller.startScroll(mScroller.getFinalX(), mScroller.getFinalY(), dx, dy, 100);
		invalidate();
	}

	private BGCircle createCircle(float x, float y) {
		OvalShape circle = new OvalShape();
		circle.resize(0, 0);
		ShapeDrawable drawable = new ShapeDrawable(circle);
		BGCircle circle1 = new BGCircle(drawable);
		circle1.setX(x);
		circle1.setY(y);
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			circle1.setRadius(circleRadius);
		}
		drawable.getPaint().setColor(mTManager.colorBGCircle());
		return circle1;
	}

	private void buildRegion() {
		String key = indexYear + ":" + indexMonth;
		if (!regionSelected.containsKey(key)) {
			regionSelected.put(key, new ArrayList<>());
		}
	}

	private void arrayClear(DPInfo[][] info) {
		for (DPInfo[] anInfo : info) {
			Arrays.fill(anInfo, null);
		}
	}

	private DPInfo[][] arrayCopy(DPInfo[][] src, DPInfo[][] dst) {
		for (int i = 0; i < dst.length; i++) {
			System.arraycopy(src[i], 0, dst[i], 0, dst[i].length);
		}
		return dst;
	}

	public void defineRegion(final int x, final int y) {
		DPInfo[][] info = mCManager.obtainDPInfo(centerYear, centerMonth, year2, month2, day2, isUseCache);
		Region[][] tmp;
		if (TextUtils.isEmpty(info[4][0].strG)) {
			tmp = monthRegionsFour;
		}
		else if (TextUtils.isEmpty(info[5][0].strG)) {
			tmp = monthRegionsFive;
		}
		else {
			tmp = monthRegionsSix;
		}
		if (num >= tmp.length) {
			num = tmp.length - 1;
		}

		for (int j = 0; j < tmp[num].length; j++) {
			Region region = tmp[0][j];
			if (region.contains(x, y)) {
				List<Region> regions = regionSelected.get(indexYear + ":" + indexMonth);
				if (mDPMode == DPMode.SINGLE) {
					cirApr.clear();
					regions.add(region);
					// add by yj 2016-6-22 16:51:34
					int year = centerYear;
					int month = centerMonth;
					// 切换月了,上月
					int index = num + move;
					// 处于当月最低行右滑,要先获得当月的行数,即上个月
					int yearL = centerYear;
					int monthL = centerMonth;
					if (isChangeNextMonth) {
						if (centerMonth == 1) {
							monthL = 12;
							yearL = centerYear - 1;
						}
						else {
							monthL -= 1;
						}
					}

					if (index < 0 && getLint.getLine() == -1) {
						DPInfo[][] infos = mCManager.obtainDPInfo(year, month, year2, month2, day2, isUseCache);
						if (infos[5][0].isNextDate && infos[4][6].isNextDate) {
							index = 3;
							setScrollLayoutLine.setScrollLayoutLine(3);
						}
						else if (infos[5][0].isNextDate && !infos[4][6].isNextDate) {
							index = 4;
							setScrollLayoutLine.setScrollLayoutLine(4);
						}
						else if (!infos[5][0].isNextDate) {
							index = 4;
							setScrollLayoutLine.setScrollLayoutLine(4);
						}
					}
					else if (num + move > getIndex(yearL, monthL)) {
						DPInfo[][] infox = mCManager.obtainDPInfo(centerYear, centerMonth, year2, month2, day2, isUseCache);
						if (infox[0][0].isLastDate) {
							index = 1;
							setScrollLayoutLine.setScrollLayoutLine(1);
						}
						else {
							index = 0;
							setScrollLayoutLine.setScrollLayoutLine(0);
						}
					}
					// delete by yj 2016-6-28 19:42:18
					if (mCManager.obtainDPInfo(year, month, year2, month2, day2, isUseCache)[index][j].isLastDate) {
						month -= 1;
						if (month == 0) {
							month = 12;
							year -= 1;
						}
					}

					if (mCManager.obtainDPInfo(year, month, year2, month2, day2, isUseCache)[index][j].isNextDate) {
						month += 1;
						if (month == 13) {
							month = 1;
							year += 1;
						}
					}

					tmpI = index;
					tmpJ = j;

					// 清除上次选中的日期背景和自身颜色
					// 上次选择的i，j,year,month
					if (ScheduleUtil.isChoosed()) {
						mCManager.obtainDPInfo(ScheduleUtil.lastChoosedYear,
								ScheduleUtil.lastChoosedMonth, year2, month2, day2,
								isUseCache)[ScheduleUtil.lastChoosedI][ScheduleUtil.lastChoosedJ].isChoosed = false;
					}
					// 记录此次i,j,year,month
					ScheduleUtil.lastChoosedI = index;
					ScheduleUtil.lastChoosedJ = j;
					ScheduleUtil.lastChoosedYear = centerYear;
					ScheduleUtil.lastChoosedMonth = centerMonth;
					// 设置choosed
					mCManager.obtainDPInfo(centerYear,
							centerMonth, year2, month2, day2, isUseCache)[index][j].isChoosed = true;

					// edit by yj 2016-6-22 16:53:13
					final String date = year + "." + month + "." +
							mCManager.obtainDPInfo(centerYear, centerMonth, year2, month2, day2, isUseCache)[index][j].strG;
					BGCircle circle = createCircle(region.getBounds().centerX() + indexMonth * width,
							region.getBounds().centerY() + indexYear * height);
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
						ValueAnimator animScale1 = ObjectAnimator.ofInt(circle, "radius", 0, circleRadius);
						animScale1.setDuration(10);
						animScale1.setInterpolator(decelerateInterpolator);
						animScale1.addUpdateListener(scaleAnimationListener);
						AnimatorSet animSet = new AnimatorSet();
						animSet.playSequentially(animScale1);
						animSet.addListener(new AnimatorListenerAdapter() {
							@Override
							public void onAnimationEnd(Animator animation) {
								if (null != mDatePickedListener) {
									mDatePickedListener.onDatePicked(date);
								}
								if (null != mWeekClickListener) {
									mWeekClickListener.onWeekDateClick(x, y);
								}
							}
						});
						animSet.start();
					}
					cirApr.put(date, circle);
					if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
						invalidate();
						if (null != mDatePickedListener) {
							mDatePickedListener.onDatePicked(date);
						}
						if (null != mWeekClickListener) {
							mWeekClickListener.onWeekDateClick(x, y);
						}
					}
				}
			}
		}
	}

	public void changeChooseDate(int x, int y) {
		DPInfo[][] info = mCManager.obtainDPInfo(centerYear, centerMonth, year2, month2, day2, isUseCache);
		Region[][] tmp;
		if (TextUtils.isEmpty(info[4][0].strG)) {
			tmp = monthRegionsFour;
		}
		else if (TextUtils.isEmpty(info[5][0].strG)) {
			tmp = monthRegionsFive;
		}
		else {
			tmp = monthRegionsSix;
		}
//        for (int i = 0; i < tmp.length; i++) {
		for (int j = 0; j < tmp[num].length; j++) {
			Region region = tmp[0][j];
			if (TextUtils.isEmpty(mCManager.obtainDPInfo(centerYear, centerMonth, year2, month2, day2, isUseCache)[num][j].strG)) {
				continue;
			}
			if (region.contains(x, y)) {
				List<Region> regions = regionSelected.get(indexYear + ":" + indexMonth);
				if (mDPMode == DPMode.SINGLE) {
					cirApr.clear();
					regions.add(region);
					final String date = centerYear + "." + centerMonth + "." + mCManager
							.obtainDPInfo(centerYear, centerMonth, year2, month2, day2, isUseCache)[num][j].strG;
					BGCircle circle = createCircle(region.getBounds().centerX() + indexMonth * width,
							region.getBounds().centerY() + indexYear * height);
					WeekView.this.invalidate();
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
						ValueAnimator animScale1 = ObjectAnimator.ofInt(circle, "radius", 0, circleRadius);
						animScale1.setDuration(10);
						animScale1.setInterpolator(decelerateInterpolator);
						animScale1.addUpdateListener(scaleAnimationListener);
						animScale1.start();
					}
					cirApr.put(date, circle);
					if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
						invalidate();
					}
				}
			}
		}
	}

	public interface OnWeekViewChangeListener {

		void onWeekViewChange(boolean isForward);
	}

	public interface OnWeekDateClick {

		void onWeekDateClick(int x, int y);
	}

	private enum SlideMode {
		VER, HOR
	}

	public void setOnWeekViewSlideistener(
			OnWeekViewSlide onWeekViewSlide) {
		this.onWeekViewSlide = onWeekViewSlide;
	}

	public void setGetLineListener(GetLine getLint) {
		this.getLint = getLint;
	}

	public void setGetLineCountListener(GetLineCount getLineCount) {
		this.getLineCount = getLineCount;
	}

	private class BGCircle {

		private float x, y;
		private int radius;

		private ShapeDrawable shape;

		public BGCircle(ShapeDrawable shape) {
			this.shape = shape;
		}

		public float getX() {
			return x;
		}

		public void setX(float x) {
			this.x = x;
		}

		public float getY() {
			return y;
		}

		public void setY(float y) {
			this.y = y;
		}

		public int getRadius() {
			return radius;
		}

		public void setRadius(int radius) {
			this.radius = radius;
		}

		public ShapeDrawable getShape() {
			return shape;
		}

		public void setShape(ShapeDrawable shape) {
			this.shape = shape;
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private class ScaleAnimationListener implements ValueAnimator.AnimatorUpdateListener {

		@Override
		public void onAnimationUpdate(ValueAnimator animation) {
			WeekView.this.invalidate();
		}
	}

	/**
	 * 重置
	 */
	public void resetMove() {
		move = 0;
	}

	public int getZeroLineCount() {
		return zeroLineCount;
	}

}
