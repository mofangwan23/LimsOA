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
 * 选择月份2018-05
 */

public class LocationMonthPickerUtil implements DateTimePickerDialog.ButtonCallBack {

	private DateTimePickerDialog mDatePickerDialog;
	private LocationMonthPickerListener mListener;
	private Calendar mCalendar;
	private Context mContext;

	public LocationMonthPickerUtil(Context context, Calendar calendar, LocationMonthPickerListener listener) {
		this.mContext = context;
		this.mCalendar = calendar;
		this.mListener = listener;
		initDatePickerDialog();
	}

	private void initDatePickerDialog() {//初始化月份选择框
		mDatePickerDialog = new DateTimePickerDialog();
		mDatePickerDialog.setButtonCallBack(this);
		mDatePickerDialog.setMaxCalendar((Calendar) mCalendar.clone());
		mDatePickerDialog.setTimeLevel(DateTimePickerDialog.TIME_LEVEL_MONTH);
	}


	@SuppressLint("SimpleDateFormat")
	public void showMonPicker(String yearMonth) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
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
		if (mListener != null) mListener.dateMonthPicker(calendar.get(Calendar.YEAR), (calendar.get(Calendar.MONTH) + 1)
				, getCalendarToYears(calendar));
		dateTimePickerDialog.dismiss();
	}

	public interface LocationMonthPickerListener {

		void dateMonthPicker(int year, int month, String years);
	}

	public String getCalendarToDate(String text) {//2018年08月转换成2018-08
		if (TextUtils.isEmpty(text) || !text.contains("年") || text.contains("-")) return text;
		if (text.length() != 8) return text;
		return text.substring(0, 4) + "-" + text.substring(5, text.length() - 1);
	}

	public String getCalendarToText(String text) {//2018-08转换成2018年08月
		if (TextUtils.isEmpty(text) || text.contains("年") || !text.contains("-")) return text;
		String[] texts = text.split("-");
		if (texts.length < 2) return text;
		return texts[0] + "年" + texts[1] + "月";
	}

	public String getCalendarToText(Calendar calendar) {//2018年07月
		return calendarPicker(calendar);
	}

	public String getCalendarToYears(Calendar calendar) {//2018-07
		return caleandarToPicker(calendar);
	}

	public String getDateToYears(String date) {//2018-07
		if (TextUtils.isEmpty(date) || date.length() != 6) return date;
		StringBuilder sb = new StringBuilder();
		sb.append(date.substring(0, 5));
		sb.append("0");
		sb.append(date.substring(5, date.length()));
		return sb.toString();
	}

	@SuppressLint("SimpleDateFormat")
	private String caleandarToPicker(Calendar calendar) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
		return sdf.format(calendar.getTime());
	}

	@SuppressLint("SimpleDateFormat")
	private String calendarPicker(Calendar calendar) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月");
		return sdf.format(calendar.getTime());
	}
}
