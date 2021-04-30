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

public class LocationDateTimePickerUtil implements DateTimePickerDialog.ButtonCallBack {

	private DateTimePickerDialog mDatePickerDialog;
	private LocationTimeListener mListener;
	private Calendar mCalendar;
	private Context mContext;

	public LocationDateTimePickerUtil(Context context, Calendar calendar, LocationTimeListener listener) {
		this.mContext = context;
		this.mCalendar = calendar;
		this.mListener = listener;
		initDatePickerDialog();
	}

	private void initDatePickerDialog() {//初始化月份选择框
		mDatePickerDialog = new DateTimePickerDialog();
		mDatePickerDialog.setButtonCallBack(this);
		mDatePickerDialog.setMaxCalendar((Calendar) mCalendar.clone());
		mDatePickerDialog.setOnlyTime(true);
	}


	@SuppressLint("SimpleDateFormat")
	public void showMonPicker(String dateTime) {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		try {
			mCalendar.setTime(sdf.parse(dateTime));
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
		if (mListener != null) mListener.dateTimePicker(getCalendarToYears(calendar));
		dateTimePickerDialog.dismiss();
	}

	public interface LocationTimeListener {

		void dateTimePicker(String day);//2018-07-03
	}


	public String getCalendarToYears(Calendar calendar) {//2018-07-03
		return caleandarToPicker(calendar);
	}

	@SuppressLint("SimpleDateFormat")
	private String caleandarToPicker(Calendar calendar) {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		return sdf.format(calendar.getTime());
	}
}
