package cn.flyrise.android.shared.utility.datepicker;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.kankan.wheel.widget.adapters.AbstractWheelTextAdapter;

import android.content.Context;

public class DayWeekWheelAdapter extends AbstractWheelTextAdapter {
    private Calendar calendar = null;
    private final int minValue;
    private final int maxValue;
    private final int currentYear;
    private final int currentMonth;
    private final DateFormat weekFormat;

    public DayWeekWheelAdapter (Context context, int year, int month) {
        super (context);
        this.currentYear = year;
        this.currentMonth = month;
        calendar = Calendar.getInstance ();
        final int day = calendar.get (Calendar.DATE);
        calendar.set (currentYear, currentMonth, day);
        this.minValue = 1;
        this.maxValue = calendar.getActualMaximum (Calendar.DAY_OF_MONTH);
        weekFormat = new SimpleDateFormat ("EEE");
    }

    @Override
    public int getItemsCount () {
        return maxValue - minValue + 1;
    }

    public int getMaxValue () {
        return maxValue;
    }

    public int getMinValue () {
        return minValue;
    }

    @Override
    protected CharSequence getItemText (int index) {

        if (index >= 0 && index < getItemsCount ()) {
            final int value = minValue + index;
            // Calendar newCalendar = (Calendar) calendar.clone();
            calendar.set (currentYear, currentMonth, value);
            // newCalendar.roll(Calendar.DAY_OF_YEAR,value-1);
            return String.format ("%02d", value) + weekFormat.format (calendar.getTime ());
        }
        return null;
    }

}
