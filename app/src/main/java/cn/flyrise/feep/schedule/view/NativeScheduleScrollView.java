package cn.flyrise.feep.schedule.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowManager;
import android.widget.ScrollView;

import com.haibuzou.datepicker.calendar.views.MonthView;
import com.haibuzou.datepicker.calendar.views.WeekView;

import cn.flyrise.feep.R;

/**
 * @author ZYP
 * @since 2016-12-01 15:30
 */
public class NativeScheduleScrollView extends ScrollView {

    private int mMarginTop, mMaxHeight;
    private int mLine, mLineCount;

    private View mShadowView;
    private WeekView mWeekView;
    private MonthView mMonthView;

    public NativeScheduleScrollView(Context context) {
        this(context, null);
    }

    public NativeScheduleScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NativeScheduleScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        mMarginTop = mMonthView.getHeight() * mLine / mLineCount;
        mMaxHeight = mMonthView.getHeight() * (mLineCount - 1) / mLineCount;

        mWeekView.setVisibility((t != 0 && t >= mMarginTop) ? View.VISIBLE : View.INVISIBLE);
        mShadowView.setVisibility(t >= mMaxHeight ? View.VISIBLE : View.GONE);
    }

    @Override protected void onFinishInflate() {
        super.onFinishInflate();
        this.mMonthView = (MonthView) findViewById(R.id.monthView);
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
        mWeekView.setWeekViewChangeListener(new WeekView.OnWeekViewChangeListener() {
            @Override public void onWeekViewChange(boolean isForward) {
                if (isForward) mMonthView.moveForwad();
                else mMonthView.moveBack();
            }
        });

        mWeekView.setOnWeekClickListener(new WeekView.OnWeekDateClick() {
            @Override public void onWeekDateClick(int x, int y) {
                mMonthView.changeChooseDate(x, y + (mMonthView.getHeight() * (mLine) / mLineCount));
            }
        });

        mWeekView.setOnWeekViewSlideistener(new WeekView.OnWeekViewSlide() {
            @Override public void onWeekViewSlide(int move) {
                mLine = mLine + move;
                WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
                int screendWidth = wm.getDefaultDisplay().getWidth();
                int offset = 1;                                                         // 解决第一个格没有日期的情况
                if (mLine == 0) offset = (7 - ((mWeekView.getZeroLineCount()))) * 2;
                int x = offset * (screendWidth / 7) / 2;                                // 第一格中间的x坐标
                int y = mWeekView.getMeasuredHeight() / 2;                              // 第一格中间y坐标
                mWeekView.defineRegion(x, y);
            }
        });

        mWeekView.setScrollLayoutLineListener(new WeekView.SetScrollLayoutLine() {
            @Override public void setScrollLayoutLine(int line) {
                mLine = line;
            }

            @Override public void reduceScrollLayoutLine(int num) {
                mLine -= num;
            }
        });

        mWeekView.setMonthViewIsUseCacheListener(new WeekView.SetMonthViewIsUseCache() {
            @Override public void setMonthViewIsUseCache(boolean isUseCache) {
                mMonthView.isUseCache = isUseCache;
            }
        });

        mWeekView.setGetLineListener(() -> mLine);

        mWeekView.setFestivalDisplay(true);
        mMonthView.setFestivalDisplay(true);
    }

    private void setupScheduleListener() {
        mMonthView.setLinePickedListener(new MonthView.OnLineChooseListener() {
            @Override public void onLineChange(int line) {
                mLine = line;
                if (mWeekView != null) mWeekView.setLine(line);
            }
        });

        mMonthView.setLineCountChangeListener(new MonthView.OnLineCountChangeListener() {
            @Override public void onLineCountChange(int lineCount) {
                mLineCount = lineCount;
                if (mWeekView != null) mWeekView.setCount(lineCount);
            }
        });

        mMonthView.setOnMonthDateClickListener(new MonthView.OnMonthDateClick() {
            @Override public void onMonthDateClick(int x, int y) {
                if (mWeekView != null) {
                    mWeekView.changeChooseDate(x, y - (mMonthView.getHeight() * (mLine) / mLineCount));
                    mWeekView.resetMove();
                }
            }
        });

        mMonthView.setMonthViewChangeListener(new MonthView.OnMonthViewChangeListener() {
            @Override public void onMonthViewChange(boolean isforward) {
                if (mWeekView != null) {
                    if (isforward) mWeekView.moveForwad();
                    else mWeekView.moveBack();
                }
            }
        });

        mMonthView.setWeekViewIsUseCacheListener(new MonthView.SetWeekViewIsUseCache() {
            @Override public void setWeekViewIsUseCache(boolean isUseCache) {
                if (mWeekView != null) mWeekView.isUseCache = isUseCache;
            }
        });
    }

    public void scrollToTop() {
        scrollTo(0, 1);
        smoothScrollTo(0, mMaxHeight);
    }

    public int getMaxHeight() {
        return mMaxHeight;
    }

}
