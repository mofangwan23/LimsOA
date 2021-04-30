package cn.flyrise.feep.location.widget;

import android.content.Context;
import android.graphics.Rect;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowManager;
import cn.flyrise.feep.R;
import com.haibuzou.datepicker.calendar.views.MonthView;
import com.haibuzou.datepicker.calendar.views.WeekView;

/**
 * @author ZYP
 * @since 2018-12-01 15:30
 */
public class SignInCalendarScrollView extends NestedScrollView {

	private int mMaxHeight;
	private int mLine, mLineCount;

	private View mShadowView;
	private WeekView mWeekView;
	private MonthView mMonthView;

	public SignInCalendarScrollView(Context context) {
		this(context, null);
	}

	public SignInCalendarScrollView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SignInCalendarScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		int mMarginTop = mMonthView.getHeight() * mLine / mLineCount;
		mMaxHeight = mMonthView.getHeight() * (mLineCount - 1) / mLineCount;
		mWeekView.setVisibility((t != 0 && t >= mMarginTop) ? View.VISIBLE : View.INVISIBLE);
		mShadowView.setVisibility(t >= mMaxHeight ? View.VISIBLE : View.GONE);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		this.mMonthView = findViewById(R.id.mMonthView);
	}

	public void setShadowView(View view) {
		this.mShadowView = view;
	}

	public void setWeekView(WeekView weekView) {
		this.mWeekView = weekView;
		if (weekView == null) {
			throw new NullPointerException("Can not pass the null WeekView here~");
		}

		this.setupScheduleListener();
		mWeekView.setWeekViewChangeListener(isForward -> {
			if (isForward) mMonthView.moveForwad();
			else mMonthView.moveBack();
		});

		mWeekView.setOnWeekClickListener((x, y) ->
				mMonthView.changeChooseDate(x, y + (mMonthView.getHeight() * (mLine) / mLineCount)));

		mWeekView.setOnWeekViewSlideistener(move -> {
			mLine = mLine + move;
			WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
			assert wm != null;
			int screendWidth = wm.getDefaultDisplay().getWidth();
			int offset = 1;                                                         // 解决第一个格没有日期的情况
			if (mLine == 0) offset = (7 - ((mWeekView.getZeroLineCount()))) * 2;
			int x = offset * (screendWidth / 7) / 2;                                // 第一格中间的x坐标
			int y = mWeekView.getMeasuredHeight() / 2;                              // 第一格中间y坐标
			mWeekView.defineRegion(x, y);
		});

		mWeekView.setScrollLayoutLineListener(new WeekView.SetScrollLayoutLine() {
			@Override public void setScrollLayoutLine(int line) {
				mLine = line;
			}

			@Override public void reduceScrollLayoutLine(int num) {
				mLine -= num;
			}
		});

		mWeekView.setMonthViewIsUseCacheListener(isUseCache -> mMonthView.isUseCache = isUseCache);

		mWeekView.setGetLineListener(() -> mLine);

		mWeekView.setFestivalDisplay(true);
		mMonthView.setFestivalDisplay(true);
	}

	private void setupScheduleListener() {
		mMonthView.setLinePickedListener(line -> {
			mLine = line;
			if (mWeekView != null) mWeekView.setLine(line);
		});

		mMonthView.setLineCountChangeListener(lineCount -> {
			mLineCount = lineCount;
			if (mWeekView != null) mWeekView.setCount(lineCount);
		});

		mMonthView.setOnMonthDateClickListener((x, y) -> {
			if (mWeekView != null) {
				mWeekView.changeChooseDate(x, y - (mMonthView.getHeight() * (mLine) / mLineCount));
				mWeekView.resetMove();
			}
		});

		mMonthView.setMonthViewChangeListener(isforward -> {
			if (mWeekView != null) {
				if (isforward) mWeekView.moveForwad();
				else mWeekView.moveBack();
			}
		});

		mMonthView.setWeekViewIsUseCacheListener(isUseCache -> {
			if (mWeekView != null) mWeekView.isUseCache = isUseCache;
		});
	}

	public int getMaxHeight() {
		return mMaxHeight;
	}

	@Override
	protected int computeScrollDeltaToGetChildRectOnScreen(Rect rect) {
		return 0;
	}

}
