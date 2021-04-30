package cn.flyrise.feep.location.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import com.borax12.materialdaterangepicker.DateTimePickerDialog;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * 新建：陈冕;
 * 日期： 2018-5-10-15:39.
 * 选择日期2018-05-03
 */

public class LocationDayPickerUtil implements DateTimePickerDialog.ButtonCallBack {

	private DateTimePickerDialog mDatePickerDialog;
	private LocationDayPickerListener mListener;
	private Calendar mCalendar;
	private Context mContext;

	public LocationDayPickerUtil(Context context, Calendar calendar, LocationDayPickerListener listener) {
		this.mContext = context;
		this.mCalendar = calendar;
		this.mListener = listener;
		initDatePickerDialog();
	}

	private void initDatePickerDialog() {//初始化月份选择框
		mDatePickerDialog = new DateTimePickerDialog();
		mDatePickerDialog.setButtonCallBack(this);
		mDatePickerDialog.setMaxCalendar((Calendar) mCalendar.clone());
		mDatePickerDialog.setTimeLevel(DateTimePickerDialog.TIME_LEVEL_DAY);
	}


	@SuppressLint("SimpleDateFormat")
	public void showMonPicker(String yearMonth) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			mCalendar.setTime(sdf.parse(getCalendarToDate(yearMonth)));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		mDatePickerDialog.setDateTime(mCalendar);
		mDatePickerDialog.show(((Activity) mContext).getFragmentManager(), "dateTimePickerDialog");
	}

	@Override
	public void onClearClick() {

	}

	@Override
	public void onOkClick(Calendar calendar, DateTimePickerDialog dateTimePickerDialog) {
		if (mListener != null) mListener.dateDayPicker(getCalendarToYears(calendar));
		dateTimePickerDialog.dismiss();
	}

	public interface LocationDayPickerListener {

		void dateDayPicker(String day);//2018-07-03
	}

	public String getCalendarToDate(String text) {//2018年08月03日转换成2018-08-03
		if (TextUtils.isEmpty(text) || !text.contains("年") || text.contains("-")) return text;
		if (text.length() != 11) return text;
		return text.substring(0, 4) + "-" + text.substring(5, 7) + "-" + text.substring(8, text.length() - 1);
	}

	public String getCalendarToTextDay(String text) {//2018-08-03转换成08月03日
		if (TextUtils.isEmpty(text) || text.contains("年") || !text.contains("-")) return text;
		String[] texts = text.split("-");
		if (texts.length < 3) return text;
		return texts[1] + "月" + texts[2] + "日";
	}

	public String getCalendarToText(String text) {//2018-08-03转换成2018年08月03日
		if (TextUtils.isEmpty(text) || text.contains("年") || !text.contains("-")) return text;
		String[] texts = text.split("-");
		if (texts.length < 3) return text;
		return texts[0] + "年" + texts[1] + "月" + texts[2] + "日";
	}

	public String getCalendarToText(Calendar calendar) {//2018年07月03日
		return calendarPicker(calendar);
	}

	public String getCalendarToTextDay(Calendar calendar) {//07月03日
		return calendarPickerDay(calendar);
	}

	public String getCalendarToYears(Calendar calendar) {//2018-07-03
		return caleandarToPicker(calendar);
	}

	@SuppressLint("SimpleDateFormat")
	private String caleandarToPicker(Calendar calendar) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(calendar.getTime());
	}

	@SuppressLint("SimpleDateFormat")
	private String calendarPicker(Calendar calendar) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
		return sdf.format(calendar.getTime());
	}

	@SuppressLint("SimpleDateFormat")
	private String calendarPickerDay(Calendar calendar) {
		SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日");
		return sdf.format(calendar.getTime());
	}
}
